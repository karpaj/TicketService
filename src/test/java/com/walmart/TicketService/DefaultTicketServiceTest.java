package com.walmart.TicketService;

import com.walmart.TicketService.timing.HoldClock;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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

    private static final String USER1 = "user1@sample.com";
    private static final String USER2 = "user2@sample.com";

    TicketService ts;
    CustomizableClock testingClock;
    @Before
    public void setup() {
        testingClock = new CustomizableClock(0);
        ts = new DefaultTicketService(20, 1, testingClock);
    }

    @Test
    public void testNumSeatsAvailable() {
        assert(ts.numSeatsAvailable() == 20);
        ts.findAndHoldSeats(3, USER1);
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(1000);
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(2000);
        assert(ts.numSeatsAvailable() == 20);
    }

    @Test
    public void testSecondUserUsurpsExpiredSeats() {
        SeatHold firstHold = ts.findAndHoldSeats(3, USER1);
        assert(firstHold.getSeats().size() == 3);
        assert(firstHold.getSeats().containsAll(Arrays.asList(1,2,3)));
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(2000);
        SeatHold secondHold = ts.findAndHoldSeats(4, USER2);
        assert(secondHold.getSeats().size() == 4);
        assert(secondHold.getSeats().containsAll(Arrays.asList(1,2,3,4)));
        assert(ts.numSeatsAvailable() == 16);

        // ensure USER1 held seats cannot be reserved
        String user1Expected = String.format("Hold #%d for user: %s cannot be completed as the hold has expired " +
                "and some seats are no longer available.", firstHold.getId(), USER1);
        assert(ts.reserveSeats(firstHold.getId(), USER1).equals(user1Expected));

        // ensure USER2 can reserve seats successfully
        String user2Expected = String.format("User: %s has reserved the following seats: 1,2,3,4.", USER2);
        assert(ts.reserveSeats(secondHold.getId(), USER2).equals(user2Expected));
    }
}
