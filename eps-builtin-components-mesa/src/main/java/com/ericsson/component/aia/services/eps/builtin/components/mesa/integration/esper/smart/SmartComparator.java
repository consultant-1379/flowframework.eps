package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart;

import java.util.Comparator;

import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Sorts in increasing resource ID order, followed by sort in decreasing ROP order.
 */
public final class SmartComparator implements Comparator<Event> {

    @Override
    public int compare(final Event left, final Event right) {
        if (left.getResourceId() < right.getResourceId()) {
            return -1;
        }
        if (left.getResourceId() > right.getResourceId()) {
            return 1;
        }
        if (left.getRopId() < right.getRopId()) {
            return 1;
        }
        if (left.getRopId() > right.getRopId()) {
            return -1;
        }
        return 0;
    }

}
