package com.ericsson.component.aia.services.eps.mesa.contextualizer;

import java.util.ServiceLoader;

import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Implementations of contextualizer should be loaded via {@link ServiceLoader} API.
 */
public interface Contextualizer {

    /**
     * Contextualize the {@link Event}.
     *
     * @param event
     *            the event
     * @return the event
     */
    Event contextualize(Event event);
}
