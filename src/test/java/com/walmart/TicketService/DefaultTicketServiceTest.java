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
    private static final String USER3 = "user3@sample.com";

    TicketService ts;
    CustomizableClock testingClock;
    @Before
    public void setup() {
        testingClock = new CustomizableClock(0);
        ts = new DefaultTicketService(20, 4, testingClock);
    }

    @Test
    public void testNumSeatsAvailable() {
        assert(ts.numSeatsAvailable() == 20);
        ts.findAndHoldSeats(3, USER1);
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(4000);
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(8000);
        assert(ts.numSeatsAvailable() == 20);
    }

    @Test
    public void testBasicSingleUser() {
        SeatHold firstHold = ts.findAndHoldSeats(5, USER1);
        assert(ts.numSeatsAvailable() == 15);
        assert(firstHold.getSeats().containsAll(Arrays.asList(1,2,3,4,5)));
        ts.reserveSeats(firstHold.getId(), USER1);
        assert(ts.numSeatsAvailable() == 15);
    }

    @Test
    public void testBasicMultipleUsers() {
        SeatHold firstHold = ts.findAndHoldSeats(5, USER1);
        SeatHold secondHold = ts.findAndHoldSeats(6, USER2);
        SeatHold thirdHold = ts.findAndHoldSeats(8, USER3);
        assert(ts.numSeatsAvailable() == 1);
        assert(firstHold.getSeats().containsAll(Arrays.asList(1,2,3,4,5)));
        assert(secondHold.getSeats().containsAll(Arrays.asList(6,7,8,9,10,11)));
        assert(thirdHold.getSeats().containsAll(Arrays.asList(12,13,14,15,16,17,18,19)));
    }

    @Test
    public void testSecondUserUsurpsExpiredSeats() {
        SeatHold firstHold = ts.findAndHoldSeats(3, USER1);
        assert(firstHold.getSeats().size() == 3);
        assert(firstHold.getSeats().containsAll(Arrays.asList(1,2,3)));
        assert(ts.numSeatsAvailable() == 17);
        testingClock.setCurTimeInMillis(8000);
        SeatHold secondHold = ts.findAndHoldSeats(4, USER2);
        assert(secondHold.getSeats().size() == 4);
        assert(secondHold.getSeats().containsAll(Arrays.asList(1,2,3,4)));
        assert(ts.numSeatsAvailable() == 16);

        // ensure USER1 held seats cannot be reserved
        String user1Expected = String.format("Hold #%d for customer: %s cannot be completed as the hold has expired " +
                "and some seats are no longer available.", firstHold.getId(), USER1);
        assert(ts.reserveSeats(firstHold.getId(), USER1).equals(user1Expected));

        // ensure USER2 can reserve seats successfully
        String user2Expected = String.format("Customer: %s has reserved the following seats: 1,2,3,4.", USER2);
        assert(ts.reserveSeats(secondHold.getId(), USER2).equals(user2Expected));

        assert(ts.numSeatsAvailable() == 16);
    }

    @Test
    public void testThirdUserOnlyTakesFromFirst() {
        SeatHold firstHold = ts.findAndHoldSeats(5, USER1);
        testingClock.setCurTimeInMillis(1000);
        SeatHold secondHold = ts.findAndHoldSeats(6, USER2);
        assert(ts.numSeatsAvailable() == 9);
        assert(firstHold.getSeats().containsAll(Arrays.asList(1,2,3,4,5)));
        assert(secondHold.getSeats().containsAll(Arrays.asList(6,7,8,9,10,11)));
        testingClock.setCurTimeInMillis(5000);
        SeatHold thirdHold = ts.findAndHoldSeats(14, USER3);
        assert(ts.numSeatsAvailable() == 0);
        assert(thirdHold.getSeats().containsAll(Arrays.asList(1,2,3,4,5,12,13,14,15,16,17,18,19,20)));
        assert(secondHold.getSeats().containsAll(Arrays.asList(6,7,8,9,10,11)));
    }

    @Test
    public void invalidHoldId() {
        SeatHold firstHold = ts.findAndHoldSeats(5, USER1);
        String expected = String.format("Customer: %s does not have a hold with id: %d on their account.", USER2, firstHold.getId());
        assert(ts.reserveSeats(firstHold.getId(), USER2).equals(expected));
    }
}
