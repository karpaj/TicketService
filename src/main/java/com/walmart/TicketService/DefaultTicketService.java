package com.walmart.TicketService;

import com.walmart.TicketService.timing.HoldClock;
import com.walmart.TicketService.timing.SystemClock;

import java.util.*;

public class DefaultTicketService implements TicketService {
    private long maxHoldTime;
    private int nextHoldId;
    private PriorityQueue<Integer> globalSeats;
    private Holds holds;
    private HoldClock clock;


    public DefaultTicketService(int venueSize, long maxHoldTime) {
        this(venueSize, maxHoldTime, new SystemClock());
    }

    public DefaultTicketService(int venueSize, long maxHoldTime, HoldClock clock) {
        this.clock = clock;
        this.maxHoldTime = maxHoldTime;
        nextHoldId = 1;
        globalSeats = new PriorityQueue<>(venueSize);
        for(int seat = 1; seat <= venueSize; seat++)
            globalSeats.add(seat);
        holds = new Holds();
    }

    @Override
    public int numSeatsAvailable() {
        boolean done = false;
        SeatHold curHead;
        while(!done) {
            curHead = holds.peekFirstHold();
            if(curHead == null || !curHead.holdExpired(clock.getCurTime(), maxHoldTime))
                done = true;
            else {
                globalSeats.addAll(holds.pollFirstHold().getSeats());
            }
        }
        return globalSeats.size();
    }

    /**
     * Reserve the best seats possible.
     * @param numSeats the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return SeatHold object representing the hold, null if an invalid number of seats or there are not enough
     * seats to fulfil the hold in its entirety
     */
    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        // cannot make a reservation for a number of seats less than one or for more seats then are remaining
        if(numSeats <= 0 || numSeats > numSeatsAvailable())
            return null;

        PriorityQueue<Integer> seatsToBeGivenOut = new PriorityQueue<>(numSeats);
        // if we need to take from global pool of available seats
        if(holds.isEmpty() || !holds.peekFirstHold().holdExpired(clock.getCurTime(), maxHoldTime)) {
            addFromGlobal(seatsToBeGivenOut, numSeats);
        } else { // start taking from holds that have expired
            SeatHold nextHold = holds.pollFirstHold();
            PriorityQueue<Integer> nextSeats = nextHold.getSeats();
            while(numSeats > 0) {
                if(!nextSeats.isEmpty()) {
                    seatsToBeGivenOut.add(nextSeats.poll());
                    numSeats--;
                } else if(!holds.isEmpty() && holds.peekFirstHold().holdExpired(clock.getCurTime(), maxHoldTime)) {
                    nextHold = holds.pollFirstHold();
                    nextSeats = nextHold.getSeats();
                } else {
                    addFromGlobal(seatsToBeGivenOut, numSeats);
                    numSeats = 0;
                }
            }
            // drop the remaining seats from the currently polled hold if any remain
            if(!nextSeats.isEmpty()) {
                globalSeats.addAll(nextSeats);
            }
        }

        SeatHold newHold = new SeatHold(nextHoldId, seatsToBeGivenOut, clock.getCurTime(), customerEmail);
        holds.put(nextHoldId, newHold);
        nextHoldId++;
        return newHold;
    }

    /**
     * Reserve the seats
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold
     *                      is assigned
     * @return
     */
    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        SeatHold hold = holds.get(seatHoldId);
        StringBuilder response = new StringBuilder();
        if(hold == null) {
            response.append("Hold #");
            response.append(seatHoldId);
            response.append(" for customer: ");
            response.append(customerEmail);
            response.append(" cannot be completed as the hold has expired and some seats are no longer available.");
        } else if(!hold.getCustomerEmail().equals(customerEmail)) {
            response.append("Customer: ");
            response.append(customerEmail);
            response.append(" does not have a hold with id: ");
            response.append(seatHoldId);
            response.append(" on their account.");
        } else {
            holds.remove(seatHoldId);
            response.append("Customer: ");
            response.append(customerEmail);
            response.append(" has reserved the following seats: ");
            Integer seatNum;
            PriorityQueue<Integer> curSeats = hold.getSeats();
            while((seatNum = curSeats.poll()) != null) {
                response.append(seatNum);
                if(curSeats.peek() != null)
                    response.append(",");
            }
            response.append(".");
        }
        return response.toString();
    }

    private void addFromGlobal(PriorityQueue<Integer> queue, int numSeats) {
        while(numSeats > 0) {
            queue.add(globalSeats.poll());
            numSeats--;
        }
    }

}
