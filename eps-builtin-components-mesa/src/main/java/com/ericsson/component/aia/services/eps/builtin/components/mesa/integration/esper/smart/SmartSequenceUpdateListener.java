package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SimpleSequenceView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;
import com.espertech.esper.client.EventBean;

/**
 * TODO FIXME add more strict semantic checks + thread safety
 *
 * looked at the case of an unordered event (rop Id from the past) arriving Esper servers up the data in the correct format, however as the RopId is
 * lower then the stored max we ignore the Rop for that Id. Thus currently strict data order/delivery must be maintained. As we will be looking at
 * processing aggs sufficient time should be available for the event to travel through the system. could be more of an issue in a replay scenario.
 */
public final class SmartSequenceUpdateListener extends SmartMesaUpdateListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Instantiates a new smart sequence update listener.
     *
     * @param sink
     *            the sink
     * @param coreId
     *            the core id
     * @param ruleId
     *            the rule id
     */
    public SmartSequenceUpdateListener(final ViewListener sink, final Id coreId, final Id ruleId) {
        super(sink, coreId, ruleId);
    }

    // TODO performance
    @Override
    protected void updateNew(final EventBean[] events) {
        final Event[] underlyings = new Event[events.length];
        for (int i = 0; i < events.length; i++) {
            underlyings[i] = (Event) events[i].getUnderlying();
        }
        Arrays.sort(underlyings, new SmartComparator());

        final List<Event> changedSet = findChangedResourceId(underlyings);
        final SequenceView view = new SimpleSequenceView(getCoreId(), getRuleId(), changedSet);
        if (changedSet.isEmpty()) {
            log.warn("Event Listener triggered, but no new events were found. Assumming out of sequence data no view will be created");
        } else {
            getSink().on(view);
        }

    }

    private List<Event> findChangedResourceId(final Event[] events) {
        int index = -1;
        for (int i = 0; i < events.length; i++) {
            if (isNew(events[i])) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return Collections.emptyList();
        }

        int first = index;
        while (true) {
            if (first == 0) {
                break;
            }
            final Event previous = events[first - 1];
            final Event current = events[first];
            if (previous.getResourceId() != current.getResourceId()) {
                break;
            }
            --first;
        }

        int second = index;
        while (true) {
            if ((second + 1) > (events.length - 1)) {
                break;
            }
            final Event next = events[second + 1];
            final Event current = events[second];
            if (next.getResourceId() != current.getResourceId()) {
                break;
            }
            ++second;
        }
        final List<Event> result = new ArrayList<Event>();
        for (int i = first; i <= second; i++) {
            result.add(events[i]);
        }
        return result;
    }

    @Override
    protected void updateOld(final EventBean[] events) {
    }

}
