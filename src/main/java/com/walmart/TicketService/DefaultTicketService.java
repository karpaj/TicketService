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
        nextHoldId = 0;
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

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        // cannot make a reservation for a number of seats less than one
        if(numSeats <= 0)
            return null;

        PriorityQueue<Integer> seatsToBeGivenOut = new PriorityQueue<>(numSeats);
        // if we need to take from global pool of available seats
        if(holds.size() == 0 || !holds.peekFirstHold().holdExpired(clock.getCurTime(), maxHoldTime)) {
            addFromGlobal(seatsToBeGivenOut, numSeats);
        } else { // start stealing from other people's holds if possible
            SeatHold nextHold = holds.pollFirstHold();
            PriorityQueue<Integer> nextSeats = nextHold.getSeats();
            while(numSeats > 0) {
                if(nextSeats.size() != 0) {
                    seatsToBeGivenOut.add(nextSeats.poll());
                    numSeats--;
                } else if(holds.size() != 0 && holds.peekFirstHold().holdExpired(clock.getCurTime(), maxHoldTime)) {
                    nextHold = holds.pollFirstHold();
                    nextSeats = nextHold.getSeats();
                } else { // we have exhausted the hold list stealing, fill the remaining from the global
                    addFromGlobal(seatsToBeGivenOut, numSeats);
                    numSeats = 0;
                }
            }
            // drop the remaining seats from the currently polled hold if any remain
            if(nextSeats.size() != 0) {
                globalSeats.addAll(nextSeats);
            }
        }

        SeatHold newHold = new SeatHold(nextHoldId, seatsToBeGivenOut, clock.getCurTime(), customerEmail);
        holds.put(nextHoldId, newHold);
        nextHoldId++;
        return newHold;
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        SeatHold hold = holds.get(seatHoldId);
        StringBuilder response = new StringBuilder();
        if(hold == null) {
            response.append("Hold #");
            response.append(seatHoldId);
            response.append(" for user: ");
            response.append(customerEmail);
            response.append(" cannot be completed as the hold has expired and some seats are no longer available.");
        } else {
            holds.remove(seatHoldId);
            response.append("User: ");
            response.append(customerEmail);
            response.append(" has reserved the following seats: ");
            Integer seatNum;
            while((seatNum = hold.getSeats().poll()) != null) {
                response.append(seatNum);
                if(hold.getSeats().peek() != null)
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
