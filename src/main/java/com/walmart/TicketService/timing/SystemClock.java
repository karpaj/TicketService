package com.walmart.TicketService.timing;

public class SystemClock implements HoldClock {
    @Override
    public long getCurTime() {
        return System.currentTimeMillis() / 1000L;
    }
}
