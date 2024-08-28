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
package com.ericsson.component.aia.services.eps.builtin.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eborziv
 *
 */
class EventSenderWorker implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(EventSenderWorker.class);

    private final ThreadManagementComponent threadManagementComponent;
    private Object event;

    /**
     * @param threadManagementComponent
     *            the {@link ThreadManagementComponent} which will send the
     *            event
     * @param evt
     *            the event to send
     */
    public EventSenderWorker(
            final ThreadManagementComponent threadManagementComponent,
            final Object evt) {
        this.threadManagementComponent = threadManagementComponent;
        event = evt;
    }

    @Override
    public void run() {
        try {
            threadManagementComponent.sendEvent(event);
        } catch (final Exception exc) {
            LOG.error("Exception while sending event downstream.", exc);
        }
        event = null;
    }
}