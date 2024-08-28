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
package com.ericsson.component.aia.services.eps.builtin.components.batch.nonblocking;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.NonBlockingBatchComponent;
import com.lmax.disruptor.EventHandler;

/**
 *
 * @author eborziv
 *
 */
public class EventBatchingWorker implements EventHandler<EventHolder> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final int ordinalNumber;
    private final int numberOfEventWorkers;
    private final int maxEventsInBatch;
    private final NonBlockingBatchComponent eventHandler;

    private final List<Object> batchedEvents = new LinkedList<>();

    private final boolean flushOnEndOfBatch;

    /**
     * @param ordinalNumber
     *            the ordinalNumber of this EventBatchingWorker
     * @param numberOfEventWorkers
     *            the number of EventBatchingWorker which the
     *            eventHandler uses
     * @param maxEventsInBatch
     *            the max number of events to include in a batch
     * @param eventHandler
     *            the {@link NonBlockingBatchComponent} handler which will send
     *            the events
     * @param flushOnEndOfBatch
     *            if the events should be flushed on end of batch
     */
    public EventBatchingWorker(final int ordinalNumber,
            final int numberOfEventWorkers, final int maxEventsInBatch,
            final NonBlockingBatchComponent eventHandler,
            final boolean flushOnEndOfBatch) {
        if (ordinalNumber < 0) {
            throw new IllegalArgumentException("Ordinal must be >= 0");
        }
        this.ordinalNumber = ordinalNumber;
        if (numberOfEventWorkers <= 0) {
            throw new IllegalArgumentException(
                    "Number of event workers must be > 0");
        }
        this.numberOfEventWorkers = numberOfEventWorkers;
        if (maxEventsInBatch <= 0) {
            throw new IllegalArgumentException(
                    "Number of events in batch must be > 0");
        }
        this.maxEventsInBatch = maxEventsInBatch;
        if (eventHandler == null) {
            throw new IllegalArgumentException("Event handler must not be null");
        }
        this.eventHandler = eventHandler;
        this.flushOnEndOfBatch = flushOnEndOfBatch;
    }

    @Override
    public void onEvent(final EventHolder event, final long sequence,
            final boolean endOfBatch) {
        final Object realEvent = event.getEvent();
        // forced flush happens whenever we receive special event
        final boolean isForcedFlush = (realEvent == NonBlockingBatchComponent.FLUSH_BATCHED_EVENTS_SIGNAL);
        if (isForcedFlush) {
            log.trace("Forced flush of all batched events");
            sendAllBatchedEvents();
            return;
        }
        /*
         * if not forced flush then we check if this worker should process this
         * particular event
         */

        // multiple consumers - but only one should batch every event
        // we do not want duplicates
        // https://code.google.com/p/disruptor/wiki/FrequentlyAskedQuestions#How_do_you_arrange_a_Disruptor_with_multiple_consumers_so_that_e
        final boolean shouldProcess = ((sequence % numberOfEventWorkers) == ordinalNumber);
        if (!shouldProcess) {
            return;
        }
        log.trace("Batching event {}", realEvent);
        batchedEvents.add(realEvent);
        final int totalEventCount = batchedEvents.size();
        if (totalEventCount >= maxEventsInBatch) {
            log.debug(
                    "Batched {} events, batch is full - sending batch downstream",
                    totalEventCount);
            sendAllBatchedEvents();
        } else if (endOfBatch && flushOnEndOfBatch) {
            log.debug(
                    "End of batch, total events in batch {} - sending batch downstream",
                    totalEventCount);
            sendAllBatchedEvents();
        }
    }

    private void sendAllBatchedEvents() {
        if (!batchedEvents.isEmpty()) {
            final List<Object> eventsToBeSent = new LinkedList<>();
            eventsToBeSent.addAll(batchedEvents);
            final int sentEvents = eventHandler
                    .sendEventsDownstream(eventsToBeSent);
            batchedEvents.clear();
            log.debug("Sent {} batched events downstream", sentEvents);
        } else {
            log.debug("No batched events present. Nothing to send downstream!");
        }
    }
}
