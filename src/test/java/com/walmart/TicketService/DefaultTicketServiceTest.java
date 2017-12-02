package com.walmart.TicketService;

import com.walmart.TicketService.timing.HoldClock;
import org.junit.Before;
import org.junit.Test;

public class DefaultTicketServiceTest {
    class CustomizableClock implements HoldClock {
        private long curTime;
        public CustomizableClock(long curTime) {
            this.curTime = curTime;
        }
        @Override
        public long getCurTime() {
            return curTime / 1000L;
        }
        public void setCurTimeInMillis(long curTime) {
            this.curTime = curTime;
        }
    }

    TicketService ts;
    CustomizableClock testingClock = new CustomizableClock(0);
    @Before
    public void setup() {
        ts = new DefaultTicketService(20, 1, testingClock);
    }

    @Test
    public void testNumSeatsAvailable() {
        assert(ts.numSeatsAvailable() == 20);
        ts.findAndHoldSeats(3, "user1@sample.com");
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(1000);
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(2000);
        assert(ts.numSeatsAvailable() == 20);
    }
}
