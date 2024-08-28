package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.MesaUpdateListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Marker interface for update listeners which assume and understand new data naming conventions.
 */
public abstract class SmartMesaUpdateListener extends MesaUpdateListener {

    // key is resource ID, value is ROP ID...
    private final Map<Long, Long> maxRopIdPerResourceId = new HashMap<Long, Long>();
    private final Id coreId;
    private final Id ruleId;

    /**
     * Instantiates a new smart mesa update listener.
     *
     * @param sink
     *            the sink
     * @param coreId
     *            the core id
     * @param ruleId
     *            the rule id
     */
    public SmartMesaUpdateListener(final ViewListener sink, final Id coreId, final Id ruleId) {
        super(sink);
        this.coreId = coreId;
        this.ruleId = ruleId;
    }

    /**
     * Returns true if given event is new. Event is new if its ROP is higher than any previous ROP observed for given resource ID.
     *
     * @param event
     *            the event
     * @return true, if is new
     */
    protected final boolean isNew(final Event event) {
        final Long maxRopId = maxRopIdPerResourceId.get(event.getResourceId());
        if ((maxRopId == null) || (event.getRopId() > maxRopId)) {
            maxRopIdPerResourceId.put(event.getResourceId(), event.getRopId());
            return true;
        }
        return false;
    }

    public Id getCoreId() {
        return coreId;
    }

    public Id getRuleId() {
        return ruleId;
    }

}
