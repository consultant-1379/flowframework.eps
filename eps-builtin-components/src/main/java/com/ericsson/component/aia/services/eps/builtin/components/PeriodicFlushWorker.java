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
 * Thread responsible to periodically check if there are any unflushed
 * events being batched for too long (in case when we do not receive events
 * for some time).
 */
class PeriodicFlushWorker implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NonBlockingBatchComponent batchComponent;

    private final int sleepTime;

    /**
     * @param nbbc
     *            the {@link NonBlockingBatchComponent} monitor
     * @param sleepTime
     *            the length of time to sleep in milliseconds
     */
    public PeriodicFlushWorker(final NonBlockingBatchComponent nbbc,
            final int sleepTime) {
        if (nbbc == null) {
            throw new IllegalArgumentException(
                    "Batch component must not be null");
        }
        if (sleepTime <= 0) {
            throw new IllegalArgumentException(
                    "Sleep time must be greater than zero");
        }
        batchComponent = nbbc;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        try {
            while (!batchComponent.isDestroyed) {
                final long now = System.currentTimeMillis();
                final long millisSinceLastReceivedEvent = now
                        - batchComponent.lastTimeEventReceived;
                if (millisSinceLastReceivedEvent > sleepTime) {
                    log.debug(
                            "Did not receive any events in {}ms. Forcing flushing of all remaining batched events!",
                            millisSinceLastReceivedEvent);
                    batchComponent
                            .sendEvent(NonBlockingBatchComponent.FLUSH_BATCHED_EVENTS_SIGNAL);
                }
                log.trace("Sleeping for {}ms", sleepTime);
                // wait for some time
                Thread.sleep(sleepTime);
            }
        } catch (final Exception e) {
            log.warn("Caught exception while forcing flush. Details {}",
                    e.getMessage());
        }
    }
}