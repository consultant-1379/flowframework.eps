package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.listener.EventListener;
import com.ericsson.component.aia.services.eps.mesa.common.Lifecycle;

/**
 * Used by internal components to emit events to outside world, such as JMS forwarder.
 */
public interface Forwarder extends Lifecycle, EventListener {

    /**
     * Inject.
     *
     * @param context
     *            the context
     */
    void inject(Context context);
}
