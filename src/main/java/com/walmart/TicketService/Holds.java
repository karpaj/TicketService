package com.walmart.TicketService;

import java.util.*;

public class Holds extends LinkedHashMap<Integer, SeatHold> {

    public Holds() {
        super();
    }

    public Integer peekFirstId() {
        try {
            return getEntryIterator().next().getKey();
        } catch(NoSuchElementException nsee) {
            return null;
        }
    }

    public SeatHold peekFirstHold() {
        try {
            return getEntryIterator().next().getValue();
        } catch(NoSuchElementException nsee) {
            return null;
        }
    }

    public SeatHold pollFirstHold() {
        try {
            SeatHold first = getEntryIterator().next().getValue();
            this.remove(first.getId());
            return first;
        } catch(NoSuchElementException nsee) {
            return null;
        }
    }

    private Iterator<Map.Entry<Integer, SeatHold>> getEntryIterator() {
        return this.entrySet().iterator();
    }
}
