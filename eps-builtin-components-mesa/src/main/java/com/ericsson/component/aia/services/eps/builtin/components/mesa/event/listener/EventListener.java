package com.ericsson.component.aia.services.eps.builtin.components.mesa.event.listener;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Listener;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * The listener interface for receiving events. The class that is interested in processing a event implements this interface, and the object created
 * with that class is registered with a component using the component's
 * <code>addEventListener<code> method. When the event occurs, that object's appropriate method is invoked.
 *
 * @see Event
 */
public interface EventListener extends Listener<Event> {
}
