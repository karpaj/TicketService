package com.walmart.TicketService;

import java.util.*;

public class DefaultTicketService implements TicketService {
    private long maxHoldTime;
    private int nextHoldId;
    PriorityQueue<Integer> globalSeats;
    Holds holds;


    public DefaultTicketService(int venueSize, long maxHoldTime) {
        this.maxHoldTime = maxHoldTime;
        nextHoldId = 0;
        globalSeats = new PriorityQueue<>(venueSize);
        for(int seat = 1; seat <= venueSize; seat++)
            globalSeats.add(seat);
        holds = new Holds();
    }

    @Override
    public int numSeatsAvailable() {
        return globalSeats.size();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        // cannot make a reservation for a number of seats less than one
        if(numSeats <= 0)
            return null;

        PriorityQueue<Integer> seatsToBeGivenOut = new PriorityQueue<>(numSeats);
        // if we need to take from global pool of available seats
        if(holds.size() == 0 || !holds.peekFirstHold().holdExpired(getCurTime(), maxHoldTime)) {
            addFromGlobal(seatsToBeGivenOut, numSeats);
        } else { // start stealing from other people's holds if possible
            SeatHold nextHold = holds.peekFirstHold();
            while(numSeats > 0) {
                if(nextHold != null && nextHold.holdExpired(getCurTime(), maxHoldTime)) {
                    if (nextHold.getSeats().size() > 0) {
                        seatsToBeGivenOut.add(nextHold.getSeats().poll());
                        numSeats--;
                    } else {
                        nextHold = holds.pollFirstHold();
                    }
                } else { // we have exhausted the hold list stealing, fill the remaining from the global
                    addFromGlobal(seatsToBeGivenOut, numSeats);
                }
            }
            // drop the remaining seats from the currently polled hold if any remain
            if(nextHold.getSeats().size() != 0) {
                globalSeats.addAll(nextHold.getSeats());
            }

            // drop the hold from the list because it has given up some of its seats
            holds.pollFirstHold();
        }

        SeatHold newHold = new SeatHold(nextHoldId, seatsToBeGivenOut, getCurTime(), customerEmail);
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

    private long getCurTime() {
        return System.currentTimeMillis() / 1000L;
    }

}
