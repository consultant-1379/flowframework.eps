package com.ericsson.component.aia.services.eps.builtin.components.mesa.common.rop;

import java.util.Comparator;

import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * The Class RopAwareEventComparator.
 */
public final class RopAwareEventComparator implements Comparator<Event> {

    private final boolean firstEventLatest;

    /**
     * Instantiates a new rop aware event comparator.
     *
     * @param firstEventLatest
     *            true = sort the list so the first element is the latest one false = sort the list so the first element is the oldest one
     */
    public RopAwareEventComparator(final boolean firstEventLatest) {
        this.firstEventLatest = firstEventLatest;
    }

    @Override
    public int compare(final Event left, final Event right) {
        if (left.getRopId() < right.getRopId()) {
            if (firstEventLatest) {
                return 1;
            } else {
                return -1;
            }
        } else if (left.getRopId() > right.getRopId()) {
            if (firstEventLatest) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

}
