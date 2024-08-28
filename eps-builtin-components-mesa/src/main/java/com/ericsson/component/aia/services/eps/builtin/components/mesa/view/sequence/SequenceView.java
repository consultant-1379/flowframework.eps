package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.common.resource.ResourceIdAware;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Wrapper around list of events of the same type for single resource ID.
 */
public interface SequenceView extends View, ResourceIdAware {

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    boolean isEmpty();

    /**
     * provide the size of SequenceView.
     *
     * @return the int
     */
    int size();

    /**
     * Gets the last Event.
     *
     * @return the last
     */
    Event getLast();

    @Override
    Event getFirst();

    /**
     * Data could be missing for that specific index.
     *
     * @param index
     *            the index
     * @return true, if successful
     */
    boolean has(int index);

    /**
     * Gets the Event related to the specified index.
     *
     * @param index
     *            the index
     * @return the event
     */
    Event get(int index);

    @Override
    List<Event> getAll();
}
