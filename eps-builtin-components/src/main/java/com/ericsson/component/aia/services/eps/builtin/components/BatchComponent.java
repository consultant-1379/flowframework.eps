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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.threading.EpsThreadFactory;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Builtin component used for batching events into collection before sending them downstream. Events are flushed downstream in two cases:
 * <ul>
 * <li>there is not enough space in batching buffer (queue), this is configurable</li>
 * <li>background flushing thread is woken up and flushes all events currently found in buffer (queue). Period is configurable</li>
 * </ul>
 *
 * @author eborziv
 *
 */
public class BatchComponent extends AbstractEventHandler implements EventInputHandler {

    static final String PURGE_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME = "flushBatchPeriodMillis";

    static final String MAX_BATCH_SIZE_CONFIG_PARAM_NAME = "maxBatchSize";

    private static final AtomicInteger INSTANCE_ID_COUNTER = new AtomicInteger(0);

    /**
     * Default value, how many events can be stored in batch at any point in time, before being flushed downstream
     */
    private static final int DEFAULT_MAX_BATCH_SIZE = 10000;

    /**
     * When adding event to batch, how long to block before considering it a failure
     */
    private static final int DEFAULT_ADD_EVENT_BLOCKING_TIME_MILLIS = 100;

    /**
     * How often do we want to purge batch
     */
    private static final int DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS = 10000;

    private BlockingQueue<Object> elementQueue;

    /**
     * How many events can be batched before being flushed downstream.
     */
    private int batchSize;

    private int uniqueBatchComponentIdentifier;

    /**
     * How often should background thread wake up to flush all events in buffer downstream.
     */
    private int purgeBatchPeriodMillis;

    private ScheduledExecutorService purgeBatchExecutor;

    private volatile boolean isDestroyed;

    private void initializePurgeBatchWorker() {
        if (purgeBatchPeriodMillis > 0) {
            log.debug("Trying to initialize flush batch worker. Period is {}", purgeBatchPeriodMillis);
            final PurgeBatchWorker worker = new PurgeBatchWorker(this);
            final String threadPoolName = "eps-batch-component-" + uniqueBatchComponentIdentifier + "-";
            final EpsThreadFactory etf = new EpsThreadFactory(threadPoolName, 512, Thread.NORM_PRIORITY);
            purgeBatchExecutor = Executors.newSingleThreadScheduledExecutor(etf);
            purgeBatchExecutor.scheduleAtFixedRate(worker, 0, purgeBatchPeriodMillis, TimeUnit.MILLISECONDS);
            log.debug("Successfully created worker to periodically flushing batched events");
        } else {
            log.info("Will not initialize flush batch worker because purge batch period is set to be <= 0");
        }
    }

    @Override
    public void destroy() {
        isDestroyed = true;
        if (purgeBatchExecutor != null) {
            purgeBatchExecutor.shutdownNow();
        }
        log.debug("Successfully shut down flush batch workers");
    }

    @Override
    public void onEvent(final Object evt) {
        if (evt != null) {
            log.trace("Received event {}", evt);
            addEventToBatch(evt);
        }
    }

    private void addEventToBatch(final Object evt) {
        boolean added = false;
        while (!added) {
            try {
                added = elementQueue.offer(evt, DEFAULT_ADD_EVENT_BLOCKING_TIME_MILLIS, TimeUnit.MILLISECONDS);
                if (added) {
                    log.trace("Successfully added event {} to batch", evt);
                    break;
                } else {
                    log.debug("Was not able to add event even after being blocked. Trying to send batched events downstream...");
                    sendAllBatchedEventsDownStream();
                }
            } catch (final InterruptedException ie) {
                log.warn("InterruptedException", ie);
            } catch (final Throwable throwable) {
                log.error("Exception occurred while adding {} ", evt.getClass(), throwable);
            }
        }
    }

    private int sendAllBatchedEventsDownStream() {
        log.debug("Sending all events downstream");
        final Collection<Object> events = new LinkedList<>();
        final int drainedEvents = elementQueue.drainTo(events);
        if (drainedEvents > 0) {
            log.debug("Drained {} events from batch", drainedEvents);
            sendToAllSubscribers(events);
            if (log.isDebugEnabled()) {
                final int numberOfEventsInBatch = elementQueue.size();
                log.debug("Sent {} events downstream. Number of batched events is {}", drainedEvents, numberOfEventsInBatch);
            }
            log.trace("Successfully sent {} to all subscribers", events);
        } else {
            log.debug("Drained 0 events. Will not send anything downstream");
        }
        return drainedEvents;
    }

    /**
     * Thread that periodically purges batched events.
     *
     * @author eborziv
     *
     */
    private static class PurgeBatchWorker implements Runnable {

        private final Logger log = LoggerFactory.getLogger(getClass());

        private final BatchComponent batchComponent;

        public PurgeBatchWorker(final BatchComponent batchComp) {
            if (batchComp == null) {
                throw new IllegalArgumentException("Batch component must not be null");
            }
            batchComponent = batchComp;
        }

        @Override
        public void run() {
            if (!batchComponent.isDestroyed) {
                try {
                    log.trace("Periodically sending all batched events downstream");
                    batchComponent.sendAllBatchedEventsDownStream();
                } catch (final Throwable throwable) {
                    log.error("Exception occurred while flushing", throwable);
                }
            }
        }
    }

    @Override
    protected void doInit() {
        // find batch size
        final String batchSizeString = getConfiguration().getStringProperty(MAX_BATCH_SIZE_CONFIG_PARAM_NAME);
        batchSize = DEFAULT_MAX_BATCH_SIZE;
        if ((batchSizeString != null) && !batchSizeString.isEmpty()) {
            log.info("Found maxBatchSize={}. Will try to parse it to integer value", batchSizeString);
            try {
                batchSize = Integer.valueOf(batchSizeString);
                log.debug("Successfully parsed {} to integer", batchSizeString);
            } catch (final NumberFormatException nfe) {
                log.warn("Was not able to parse {} to integer. Will use default value {}", batchSizeString, DEFAULT_MAX_BATCH_SIZE);
                batchSize = DEFAULT_MAX_BATCH_SIZE;
            }
        }
        log.info("Batch size is {}", batchSize);
        if (batchSize <= 0) {
            log.info("Batch size set to {} - will use batch queue with unlimited size!", batchSize);
            elementQueue = new LinkedBlockingQueue<>();
        } else {
            log.info("Batch size set to {} - will use batch queue with limited size", batchSize);
            elementQueue = new ArrayBlockingQueue<>(batchSize);
        }

        // find purge period
        final String purgeBatchPeriodStr = getConfiguration().getStringProperty(PURGE_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME);
        purgeBatchPeriodMillis = DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS;
        if ((purgeBatchPeriodStr != null) && !purgeBatchPeriodStr.isEmpty()) {
            log.info("Found {}={}. Will try to parse it to integer value", PURGE_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME, purgeBatchPeriodStr);
            try {
                purgeBatchPeriodMillis = Integer.valueOf(purgeBatchPeriodStr);
                log.debug("Successfully parsed {} to integer", purgeBatchPeriodStr);
            } catch (final NumberFormatException nfe) {
                log.warn("Was not able to parse {} to integer. Will use default value {}", purgeBatchPeriodStr,
                        DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS);
                purgeBatchPeriodMillis = DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS;
            }
        }
        log.info("Flush batch period is {} milliseconds", purgeBatchPeriodMillis);
        if ((batchSize <= 0) && (purgeBatchPeriodMillis <= 0)) {
            throw new IllegalArgumentException("Not both " + MAX_BATCH_SIZE_CONFIG_PARAM_NAME + " and "
                    + PURGE_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME + " can be set to non-positive integer values!");
        }
        uniqueBatchComponentIdentifier = INSTANCE_ID_COUNTER.incrementAndGet();
        log.info("Assigned unique identifier {}", uniqueBatchComponentIdentifier);
        initializePurgeBatchWorker();
    }
}
