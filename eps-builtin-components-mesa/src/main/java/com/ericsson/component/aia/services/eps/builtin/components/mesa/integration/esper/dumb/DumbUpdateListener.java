package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.dumb;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.MesaUpdateListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.espertech.esper.client.EventBean;

/**
 * Marker interface for update listeners which assume and understand old data naming conventions.
 */
public final class DumbUpdateListener extends MesaUpdateListener {

    /**
     * Instantiates a new dumb update listener.
     *
     * @param sink
     *            {@link ViewListener}
     */
    public DumbUpdateListener(final ViewListener sink) {
        super(sink);
    }

    @Override
    protected void updateNew(final EventBean[] events) {
    }

    @Override
    protected void updateOld(final EventBean[] events) {
    }

}
