package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

import java.util.Comparator;

import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * The Class TimestampEventComparator compare the timestamp of 2 events.
 */
public final class TimestampEventComparator implements Comparator<Event> {

    @Override
    public int compare(final Event event1, final Event event2) {
        throw new NotYetImplementedException();
    }
}
