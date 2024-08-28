package com.ericsson.component.aia.services.eps.builtin.components.mesa.model;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View.ViewType;

/**
 * The Class Subscription.
 */
public final class Subscription {

    private final ViewType viewType;
    private final Set<EventRef> set;

    /**
     * Instantiates a new subscription.
     *
     * @param viewType
     *            the view type
     */
    public Subscription(final ViewType viewType) {
        super();
        this.viewType = viewType;
        set = new HashSet<EventRef>();
    }

    /**
     * Append an {@link EventRef}.
     *
     * @param eventRef
     *            the event ref
     */
    public void append(final EventRef eventRef) {
        set.add(eventRef);
    }

    /**
     * Gets the set of {@link EventRef}.
     *
     * @return the sets
     */
    public Set<EventRef> get() {
        return set;
    }

    /**
     * Gets the view type.
     *
     * @return the view type
     */
    public ViewType getViewType() {
        return viewType;
    }
}
