package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;
import com.espertech.esper.client.EventBean;

/**
 * TODO FIXME add more strict semantic checks + thread safety
 */
public final class SmartSingletonUpdateListener extends SmartMesaUpdateListener {

    /**
     * Instantiates a new smart singleton update listener.
     *
     * @param sink
     *            the sink
     * @param coreId
     *            the core id
     * @param ruleId
     *            the rule id
     */
    public SmartSingletonUpdateListener(final ViewListener sink, final Id coreId, final Id ruleId) {
        super(sink, coreId, ruleId);
    }

    @Override
    protected void updateNew(final EventBean[] events) {
        for (int i = 0; i < events.length; i++) {
            final Event event = (Event) events[i].getUnderlying();
            if (isNew(event)) {
                getSink().on(new SimpleSingletonView(getCoreId(), getRuleId(), event));
            }
        }
    }

    @Override
    protected void updateOld(final EventBean[] events) {
        // noop
    }
}
