package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton;

import java.io.Serializable;
import java.util.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.AbstractView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * provide the simple management for {@link SingletonView}.
 */
public final class SimpleSingletonView extends AbstractView implements SingletonView, Serializable {

    private static final long serialVersionUID = 4087848637273532663L;

    private final Event event;

    /**
     * Instantiates a new simple singleton view.
     *
     * @param policyCoreId
     *            the policy core id
     * @param ruleGroupId
     *            the rule group id
     * @param event
     *            the event
     */
    public SimpleSingletonView(final Id policyCoreId, final Id ruleGroupId, final Event event) {
        super(policyCoreId, ruleGroupId);
        this.event = event;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.SINGLETON;
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public Iterator<Event> iterator() {
        return Arrays.asList(event).iterator();
    }

    @Override
    public Event getFirst() {
        return event;
    }

    @Override
    public List<Event> getAll() {
        final List<Event> eventList = new ArrayList<Event>();
        eventList.add(event);
        return eventList;
    }

    @Override
    public String toString() {
        return "SingletonView[" + event + "]";
    }
}
