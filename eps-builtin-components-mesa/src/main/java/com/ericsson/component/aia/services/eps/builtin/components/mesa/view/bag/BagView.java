package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.bag;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Single resource ID, bag of events of arbitrary type, sorted in increasing time order.
 */
public interface BagView extends View {

    /**
     * The Enum TraceType.
     *
     * Posible vlues are: SINGLE_IMSI, MULTI_IMSI
     */
    public static enum TraceType {
        SINGLE_IMSI, MULTI_IMSI,
    }

    /**
     * Gets the trace type.
     *
     * @return the trace type
     */
    TraceType getTraceType();

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    boolean isEmpty();

    /**
     * Size.
     *
     * @return the int
     */
    int size();

    /**
     * Returns events in increasing time order.
     *
     * @return the all events
     */
    @Override
    List<Event> getAll();
}
