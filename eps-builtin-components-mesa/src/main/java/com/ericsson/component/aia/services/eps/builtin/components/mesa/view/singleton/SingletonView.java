package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Wrapper around single event.
 */
public interface SingletonView extends View {

    /**
     * Gets the event.
     *
     * @return the event
     */
    Event getEvent();
}
