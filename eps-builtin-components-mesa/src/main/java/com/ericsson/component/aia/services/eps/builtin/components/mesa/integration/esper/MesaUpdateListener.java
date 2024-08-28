package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * Marker interface for all mesa related {@link UpdateListener}s.
 */
public abstract class MesaUpdateListener implements UpdateListener {

    private final ViewListener sink;

    /**
     * Instantiates a new mesa update listener.
     *
     * @param sink
     *            the sink
     */
    public MesaUpdateListener(final ViewListener sink) {
        this.sink = sink;
    }

    protected final ViewListener getSink() {
        return sink;
    }

    @Override
    public final void update(final EventBean[] newEvents, final EventBean[] oldEvents) {
        if ((newEvents != null) && (newEvents.length > 0)) {
            updateNew(newEvents);
        }
        if ((oldEvents != null) && (oldEvents.length > 0)) {
            updateOld(oldEvents);
        }
    }

    /**
     * Update new events.
     *
     * @param events
     *            the events
     */
    protected abstract void updateNew(EventBean[] events);

    /**
     * Update old events.
     *
     * @param events
     *            the events
     */
    protected abstract void updateOld(EventBean[] events);
}
