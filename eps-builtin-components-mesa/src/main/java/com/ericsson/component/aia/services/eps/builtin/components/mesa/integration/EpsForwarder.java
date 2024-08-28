package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.x733.StandardEvent;
import com.ericsson.component.aia.services.eps.mesa.event.Event;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * The Class EpsForwarder implements {@link Forwarder}.
 */
public final class EpsForwarder implements Forwarder {

    private final Logger log = LoggerFactory.getLogger(EpsForwarder.class);

    private final List<EventSubscriber> subscribers;
    private Context context;

    /**
     * Instantiates a new eps forwarder.
     *
     * @param subscribers
     *            the subscribers
     */
    public EpsForwarder(final List<EventSubscriber> subscribers) {
        super();
        this.subscribers = subscribers;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void on(final Event event) {
        if (log.isTraceEnabled()) {
            log.trace("Forwarding event {}", event.toString());
        }
        for (final EventSubscriber subscriber : subscribers) {
            if (event instanceof StandardEvent) {
                subscriber.sendEvent(event);
                context.stats().meter(event.getClass().getSimpleName() + ".forwarded");
            } else {
                log.warn("Expected StandardEvent but received '" + event.getClass() + "' instead. Silently dropping it.");
            }
        }
    }

    @Override
    public void inject(final Context context) {
        this.context = context;
    }
}
