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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ericsson.component.aia.services.eps.builtin.components.batch.nonblocking.EventBatchingWorker;
import com.ericsson.component.aia.services.eps.builtin.components.batch.nonblocking.EventHolder;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * Builtin component used for batching events into collection before sending
 * them downstream. This component uses Disruptor for non-blocking batching and
 * sending of batches downstream.
 *
 * @author eborziv
 *
 */
public class NonBlockingBatchComponent extends AbstractEventHandler implements
        EventInputHandler {

    /**
     * Used to notify all workers to flush their batched events
     */
    public static final Object FLUSH_BATCHED_EVENTS_SIGNAL = new Object();

    static final String MAX_BATCH_SIZE_CONFIG_PARAM_NAME = "maxBatchSize";

    static final String NUM_BATCH_WORKERS_CONFIG_PARAM_NAME = "numberOfBatchWorkers";

    static final String FLUSH_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME = "flushBatchPeriodMillis";

    static final String WAIT_STRATEGY_CONFIG_PARAM_NAME = "waitStrategy";

    static final String RING_SIZE_CONFIG_PARAM_NAME = "ringSize";

    static final String FLUSH_ON_END_OF_BATCH_CONFIG_PARAM_NAME = "flushOnEndOfBatch";

    private static final int DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS = 10 * 1000;

    private static final String DEFAULT_WAIT_STRATEGY = "Blocking";

    /*
     * must be power of two
     */
    private static final int DEFAULT_RING_SIZE = 131072;
    private static final int DEFAULT_NUMBER_OF_WORKERS = 3;
    private static final int DEFAULT_MAX_BATCH_SIZE = 10000;
    private static final boolean DEFAULT_FLUSH_ON_END_OF_BATCH = false;

    volatile boolean isDestroyed;

    long lastTimeEventReceived;

    private int numOfWorkers = DEFAULT_NUMBER_OF_WORKERS;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private int flushBatchPeriodMillis = DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS;
    private int ringSize = DEFAULT_RING_SIZE;
    private boolean flushOnEndOfBatch = DEFAULT_FLUSH_ON_END_OF_BATCH;
    private String waitStrategy = DEFAULT_WAIT_STRATEGY;

    private final ExecutorService periodicFlushExecutorService = Executors
            .newSingleThreadExecutor();
    private ExecutorService batchWorkerExecutorService;
    private Disruptor<EventHolder> disruptor;
    private RingBuffer<EventHolder> ringBuffer;

    @Override
    public void destroy() {
        log.debug("Shutting down batching component");
        isDestroyed = true;
        disruptor.shutdown();
        disruptor.halt();
        periodicFlushExecutorService.shutdownNow();
        batchWorkerExecutorService.shutdownNow();
        log.debug("Successfully shut down batching component");
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (isDestroyed) {
            log.error("Component destroyed but received event {}", inputEvent);
            throw new IllegalStateException(
                    "Component already destroyed - unable to receive any new events!");
        }
        sendEvent(inputEvent);
        /*
         * whenever we receive event we remember timestamp - needed for periodic
         * flush of batched events.
         */
        lastTimeEventReceived = System.currentTimeMillis();
    }

    /**
     *
     * @param inputEvent
     *            the Event to send downstream
     */
    void sendEvent(final Object inputEvent) {
        final long sequence = ringBuffer.next();
        final EventHolder eventHolder = ringBuffer.get(sequence);
        eventHolder.setEvent(inputEvent);
        ringBuffer.publish(sequence);
    }

    @Override
    protected void doInit() {
        maxBatchSize = getConfigurationParameterValue(
                MAX_BATCH_SIZE_CONFIG_PARAM_NAME, DEFAULT_MAX_BATCH_SIZE);
        numOfWorkers = getConfigurationParameterValue(
                NUM_BATCH_WORKERS_CONFIG_PARAM_NAME, DEFAULT_NUMBER_OF_WORKERS);
        flushBatchPeriodMillis = getConfigurationParameterValue(
                FLUSH_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME,
                DEFAULT_PURGE_BATCH_WORKER_TIME_PERIOD_MILLIS);
        ringSize = getConfigurationParameterValue(RING_SIZE_CONFIG_PARAM_NAME,
                DEFAULT_RING_SIZE);

        batchWorkerExecutorService = Executors.newFixedThreadPool(numOfWorkers);

        final String waitStrategyConfigValue = getConfiguration()
                .getStringProperty(WAIT_STRATEGY_CONFIG_PARAM_NAME);
        if ((waitStrategyConfigValue != null)
                && !waitStrategyConfigValue.trim().isEmpty()) {
            waitStrategy = waitStrategyConfigValue;
        }
        log.info("Wait strategy is {}", waitStrategy);

        disruptor = new Disruptor<EventHolder>(EventHolder.FACTORY,
                batchWorkerExecutorService, new MultiThreadedClaimStrategy(
                        ringSize), determineWaitStrategy());
        flushOnEndOfBatch = "true".equalsIgnoreCase(getConfiguration()
                .getStringProperty(FLUSH_ON_END_OF_BATCH_CONFIG_PARAM_NAME));

        ringBuffer = disruptor.getRingBuffer();
        final EventBatchingWorker[] eventWorkers = new EventBatchingWorker[numOfWorkers];
        for (int i = 0; i < numOfWorkers; i++) {
            final EventBatchingWorker worker = new EventBatchingWorker(i,
                    numOfWorkers, maxBatchSize, this, flushOnEndOfBatch);
            eventWorkers[i] = worker;
            log.debug("Created batch worker #{}", i);
        }
        disruptor.handleEventsWith(eventWorkers);
        disruptor.start();
        log.debug("Started event batch workers and disruptor");
        final PeriodicFlushWorker periodicFlush = new PeriodicFlushWorker(this,
                flushBatchPeriodMillis);
        periodicFlushExecutorService.submit(periodicFlush);
        log.debug("Started periodic flush worker to run every {}ms",
                flushBatchPeriodMillis);
    }

    private WaitStrategy determineWaitStrategy() {
        if ("blocking".equalsIgnoreCase(waitStrategy)) {
            return new BlockingWaitStrategy();
        }
        if ("yielding".equalsIgnoreCase(waitStrategy)) {
            return new YieldingWaitStrategy();
        }
        if ("busyspin".equalsIgnoreCase(waitStrategy)) {
            return new BusySpinWaitStrategy();
        }
        if ("sleeping".equalsIgnoreCase(waitStrategy)) {
            return new SleepingWaitStrategy();
        }
        log.warn("Invalid wait strategy value {}. Will default to {}",
                waitStrategy, DEFAULT_WAIT_STRATEGY);
        waitStrategy = DEFAULT_WAIT_STRATEGY;
        return determineWaitStrategy();
    }

    private int getConfigurationParameterValue(final String configParamName,
            final int defaultValue) {
        final String configValueString = getEventHandlerContext()
                .getEventHandlerConfiguration().getStringProperty(
                        configParamName);
        int val = defaultValue;
        if ((configValueString != null) && !configValueString.isEmpty()) {
            log.info("Found {}={}. Will try to parse it to integer value",
                    configParamName, configValueString);
            try {
                val = Integer.valueOf(configValueString);
                log.debug("Successfully parsed {} to integer",
                        configValueString);
            } catch (final NumberFormatException nfe) {
                log.warn(
                        "Was not able to parse {} to integer. Will use default value {}",
                        configValueString, defaultValue);
                val = defaultValue;
            }
        }
        if (val <= 0) {
            throw new IllegalStateException(configParamName
                    + " must be greater than zero");
        }
        return val;
    }

    /**
     *
     * @param events
     *            a {@link Collection} of events to send downstream
     * @return the number of events in the Collection
     * @throws IllegalArgumentException
     *             if events is null or empty
     */
    public int sendEventsDownstream(final Collection<Object> events) {
        if ((events == null) || events.isEmpty()) {
            throw new IllegalArgumentException(
                    "Event collection must not be null or empty");
        }
        sendToAllSubscribers(events);
        return events.size();
    }

}
