package com.walmart.TicketService;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.PriorityQueue;

public class HoldsTest {
    Holds holds;

    @Before
    public void setup() {
        holds = new Holds();
        PriorityQueue<Integer> seatsOne = new PriorityQueue<>(Arrays.asList(1, 2, 3, 4));
        SeatHold holdOne = new SeatHold(5, seatsOne, 1000, "user@sample.com");
        holds.put(5,holdOne);

        PriorityQueue<Integer> seatsTwo = new PriorityQueue<>(Arrays.asList(5, 6));
        SeatHold holdTwo = new SeatHold(1, seatsTwo, 1001, "user2@sample.com");
        holds.put(1, holdTwo);
    }

    @Test
    public void testHoldsHaveBeenAddedCorrectly() {
        assert(holds.size() == 2);
        assert(holds.peekFirstId() == 5);
        assert(holds.peekFirstHold().getSeats().containsAll(Arrays.asList(1, 2, 3, 4)));
        assert(holds.peekFirstHold().getId() == 5);
        assert(holds.get(1).getId() == 1);
        assert(holds.get(1).getSeats().containsAll(Arrays.asList(5,6)));

    }

    @Test
    public void testRemoveHolds() {
        assert(holds.size() == 2);
        assert(holds.peekFirstId() == 5);
        
        SeatHold insideHolds = holds.peekFirstHold();
        SeatHold outsideHolds = holds.pollFirstHold();
        assert(holds.size() == 1);
        assert(holds.peekFirstId() == 1);
        // ensure that both references are pointing to the same SeatHold
        assert(outsideHolds == insideHolds);

        holds.pollFirstHold();
        assert(holds.size() == 0);
    }
}
