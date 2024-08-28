package com.ericsson.component.aia.services.eps.builtin.components.mesa.view;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Listener;

/**
 * The listener interface for receiving view events. The class that is interested in processing a view event implements this interface, and the object
 * created with that class is registered with a component using the component's <code>addViewListener<code> method. When
 * the view event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ViewEvent
 */
public interface ViewListener extends Listener<View> {
}
