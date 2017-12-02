package com.walmart.TicketService.timing;

import org.junit.Test;

public class SystemClockTest {
    @Test
    public void testSystemClock() throws InterruptedException {
        HoldClock clock = new SystemClock();
        long t1 = clock.getCurTime();
        Thread.sleep(1200);
        long t2 = clock.getCurTime();
        assert(t2 > t1);
    }
}
