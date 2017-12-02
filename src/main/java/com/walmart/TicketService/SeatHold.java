package com.walmart.TicketService;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class SeatHold {
    private int id;
    private long timestamp;
    private String customerEmail;
    private PriorityQueue<Integer> seats;

    public SeatHold(int id, PriorityQueue<Integer> seats, long timestamp, String customerEmail) {
        this.id = id;
        this.seats = seats;
        this.timestamp = timestamp;
        this.customerEmail = customerEmail;
    }

    public PriorityQueue<Integer> getSeats() {
        PriorityQueue<Integer> results = new PriorityQueue<>(seats.size());
        results.addAll(seats);
        return results;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public boolean holdExpired(long curTime, long maxReservationTime) {
        return timestamp + maxReservationTime < curTime;
    }
}
