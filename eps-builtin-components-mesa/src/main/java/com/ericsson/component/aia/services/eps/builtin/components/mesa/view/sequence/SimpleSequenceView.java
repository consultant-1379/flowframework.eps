package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence;

import java.io.Serializable;
import java.util.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.AbstractView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Provide the management of a simple sequencer view.
 */
public final class SimpleSequenceView extends AbstractView implements SequenceView, Serializable {

    private static final long serialVersionUID = -4491097786825063769L;

    private final List<Event> events;

    /**
     * Instantiates a new simple sequence view.
     *
     * @param policyCoreId
     *            the policy core id
     * @param ruleGroupId
     *            the rule group id
     */
    public SimpleSequenceView(final Id policyCoreId, final Id ruleGroupId) {
        this(policyCoreId, ruleGroupId, new ArrayList<Event>());
    }

    /**
     * Instantiates a new simple sequence view.
     *
     * @param policyCoreId
     *            the policy core id
     * @param ruleGroupId
     *            the rule group id
     * @param events
     *            the events
     */
    public SimpleSequenceView(final Id policyCoreId, final Id ruleGroupId, final List<Event> events) {
        super(policyCoreId, ruleGroupId);
        this.events = events;
    }

    /**
     * Append an event.
     *
     * @param event
     *            the event
     */
    public void append(final Event event) {
        events.add(event);
    }

    @Override
    public boolean has(final int index) {
        return (index < size()) && (events.get(index) != null);
    }

    @Override
    public Event get(final int index) {
        return events.get(index);
    }

    @Override
    public boolean isEmpty() {
        return events.isEmpty();
    }

    @Override
    public ViewType getViewType() {
        return ViewType.SEQUENCE;
    }

    @Override
    public boolean hasResourceId() {
        return getLast().hasResourceId();
    }

    @Override
    public long getResourceId() {
        return getLast().getResourceId();
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public List<Event> getAll() {
        return events;
    }

    @Override
    public Event getLast() {
        return events.get(events.size() - 1);
    }

    @Override
    public Event getFirst() {
        return events.get(0);
    }

    @Override
    public Iterator<Event> iterator() {
        return events.iterator();
    }

    @Override
    public String toString() {
        return "SequenceView[" + events + "]";
    }
}
