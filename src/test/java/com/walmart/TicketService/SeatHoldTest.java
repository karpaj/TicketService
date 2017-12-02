package com.walmart.TicketService;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.PriorityQueue;

public class SeatHoldTest {
    SeatHold hold;
    @Before
    public void setup() {
        PriorityQueue<Integer> seats = new PriorityQueue<>(Arrays.asList(1, 2, 3, 4));
        hold = new SeatHold(1, seats, 1000, "user@sample.com");
    }

    @Test
    public void testAccessors() {
        assert(hold.getId() == 1);
        assert(hold.getSeats().containsAll(Arrays.asList(1, 2, 3, 4)));
        assert(hold.getTimestamp() == 1000);
        assert(hold.getCustomerEmail().equals("user@sample.com"));
    }

    @Test
    public void testExpired() {
        assert(hold.holdExpired(10000, 5));
    }
}
