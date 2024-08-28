/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.component.config;

import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * The Class EpsEventSubscriberImpl.
 *
 * @author eborziv
 * @see EventSubscriber
 * @see Comparable
 */
public class EpsEventSubscriberImpl implements EventSubscriber, Comparable<EventSubscriber> {

    private final String identifier;

    private final EventInputHandler eventInputHandler;

    /**
     * Instantiates a new eps event subscriber impl.
     *
     * @param identifier
     *            the identifier
     * @param eventInputHandler
     *            the event input handler
     */
    public EpsEventSubscriberImpl(final String identifier, final EventInputHandler eventInputHandler) {
        if ((identifier == null) || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier must not be null or empty");
        }
        this.identifier = identifier;
        if (eventInputHandler == null) {
            throw new IllegalArgumentException("event input handler must not be null or empty");
        }
        this.eventInputHandler = eventInputHandler;
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Send event.
     *
     * @param event
     *            the event
     */
    @Override
    public void sendEvent(final Object event) {
        eventInputHandler.onEvent(event);
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "EpsEventSubscriberImpl [identifier=" + identifier + ", eventInputHandler=" + eventInputHandler + "]";
    }

    /**
     * Compare to.
     *
     * @param eventSubscriber
     *            the event subscriber
     * @return the int
     */
    @Override
    public int compareTo(final EventSubscriber eventSubscriber) {
        if (eventSubscriber == this) {
            return 0;
        }
        return getIdentifier().compareTo(eventSubscriber.getIdentifier());
    }
}
