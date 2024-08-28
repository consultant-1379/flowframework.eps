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

import static com.ericsson.component.aia.services.eps.core.util.EpsUtil.isDigit;

import java.util.UUID;
import java.util.concurrent.*;

import com.codahale.metrics.Counter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.threading.EpsThreadFactory;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.*;

/**
 * Built-in component used for parallelizing work using multiple threads. All
 * {@link EventHandler} instances downstream from this component in the flow
 * will receive events from multiple threads.
 *
 * @author eborziv
 *
 */
public class ThreadManagementComponent extends AbstractEventHandler implements
        EventInputHandler {

    protected static final String REJECTED_EXECUTION_POLICY = "rejectedExecutionPolicy";

    protected static final String QUEUE_SIZE_PROPERTY_NAME = "queueSize";

    protected static final String THREAD_POOL_SIZE_PROPERTY_NAME = "threadPoolSize";

    protected static final String THREAD_PRIORITY_PROPERTY_NAME = "threadPriority";

    protected static final String REJECTED_EVENTS_COUNTER_NAME = "rejectedEventsCounterName";

    protected static final String REJECTED_EVENTS_COUNTER_NAME_PREFIX = "rejectedEventsCounterNamePrefix";

    protected static final String DEFAULT_REJECTED_EVENTS_COUNTER_NAME = "rejectedEvents";

    protected static final int DEFAULT_THREAD_POOL_SIZE = 10;

    protected static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

    private static final int REJECTED_DROPPED_RECORD_THRESHOLD = 1000;

    private static boolean statisticsOn;

    private EpsStatisticsRegister statisticsRegister;

    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

    private int threadPriority = DEFAULT_THREAD_PRIORITY;

    private int dropCount;

    private int rejectedCount;

    private Integer runnableQueueSize;

    private ThreadPoolExecutor forwarderThreadPool;

    private RejectedExecutionHandler rejectedExecutionHandler;

    private String threadPoolName;

    @Override
    public void destroy() {
        if (forwarderThreadPool != null) {
            log.debug("Shutting down thread pool {}", threadPoolName);
            forwarderThreadPool.shutdownNow();
            log.debug("Successfully shut down thread pool {}", threadPoolName);
        }
    }

    @Override
    public void onEvent(final Object inputEvent) {
        try {
            forwarderThreadPool
                    .execute(new EventSenderWorker(this, inputEvent));
        } catch (final Exception exc) {
            log.error("Failed to add new task to thread pool. Details {}",
                    exc.getMessage());
        }
    }

    /**
     * Sends the event to subscribers
     *
     * @param event
     *            the event to send
     */
    void sendEvent(final Object event) {
        sendToAllSubscribers(event);
    }

    @Override
    protected void doInit() {
        initialiseStatistics();

        threadPoolSize = getThreadPoolSizeConfigParam();
        threadPriority = getThreadPriorityConfigParam();
        runnableQueueSize = getRunnableQueueSizeConfigParam();
        final LinkedBlockingQueue<Runnable> workQueue = getBlockingQueue();
        log.info(
                "Creating thread pool with size={}, priority={}, queueSize={}",
                threadPoolSize, threadPriority, workQueue.remainingCapacity());
        final String identity = UUID.randomUUID().toString();
        threadPoolName = "threading-component-" + identity;
        rejectedExecutionHandler = getRejectedExecutionHandlerConfigParam();
        log.info("Initializing ThreadPoolExecutor with {} policy",
                rejectedExecutionHandler.getClass().getCanonicalName());
        forwarderThreadPool = new ThreadPoolExecutor(threadPoolSize,
                threadPoolSize, 0, TimeUnit.SECONDS, workQueue,
                new EpsThreadFactory(threadPoolName, 512, threadPriority),
                rejectedExecutionHandler);
        log.debug("Created thread pool {}", threadPoolName);
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                        EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            statisticsOn = statisticsRegister.isStatisticsOn();
        }
    }

    private LoggedLinkedBlockingQueue<Runnable> getBlockingQueue() {
        return runnableQueueSize == null ? new LoggedLinkedBlockingQueue<Runnable>()
                : new LoggedLinkedBlockingQueue<Runnable>(
                        runnableQueueSize.intValue());
    }

    /*
     * Only Used in Test
     */
    protected ThreadPoolExecutor getForwarderThreadPool() {
        return forwarderThreadPool;
    }

    /*
     * Only Used in Test
     */
    protected RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    /**
     * @return get Thread Priority
     */
    private int getThreadPriorityConfigParam() {
        final String threadPriority = getEventHandlerContext()
                .getEventHandlerConfiguration().getStringProperty(
                        THREAD_PRIORITY_PROPERTY_NAME);

        if ((threadPriority == null) || threadPriority.isEmpty()
                || !isDigit(threadPriority)) {
            logInvalidConfigParam(THREAD_PRIORITY_PROPERTY_NAME, threadPriority
                    + "", DEFAULT_THREAD_PRIORITY + "");
            return DEFAULT_THREAD_PRIORITY;
        }

        final int threadPriorityIntVal = Integer.parseInt(threadPriority);
        if (isValidPriority(threadPriorityIntVal)) {
            return threadPriorityIntVal;
        }

        log.warn(
                "Unable to set thread priority value. Invalid value {}, Should be (1 or 5 or 10) using default value {}",
                threadPriorityIntVal, DEFAULT_THREAD_PRIORITY);
        return DEFAULT_THREAD_PRIORITY;
    }

    private boolean isValidPriority(final int threadPriorityIntVal) {
        return (threadPriorityIntVal == Thread.NORM_PRIORITY)
                || (threadPriorityIntVal == Thread.MIN_PRIORITY)
                || (threadPriorityIntVal == Thread.MAX_PRIORITY);
    }

    /**
     *
     * @return thread pool size
     */
    private int getThreadPoolSizeConfigParam() {
        final String poolSize = getEventHandlerContext()
                .getEventHandlerConfiguration().getStringProperty(
                        THREAD_POOL_SIZE_PROPERTY_NAME);

        if ((poolSize == null) || poolSize.isEmpty() || !isDigit(poolSize)) {
            logInvalidConfigParam(THREAD_POOL_SIZE_PROPERTY_NAME,
                    poolSize + "", DEFAULT_THREAD_POOL_SIZE + "");
            return DEFAULT_THREAD_POOL_SIZE;
        }

        final int poolSizeInt = Integer.parseInt(poolSize);

        if (poolSizeInt <= 0) {
            log.warn(
                    "Configuration property {} must be greater than zero. Setting it to default value",
                    THREAD_POOL_SIZE_PROPERTY_NAME);
            return DEFAULT_THREAD_POOL_SIZE;
        }
        return poolSizeInt;
    }

    /**
     *
     * @return thread pool size
     */
    private Integer getRunnableQueueSizeConfigParam() {
        final String queueSize = getEventHandlerContext()
                .getEventHandlerConfiguration().getStringProperty(
                        QUEUE_SIZE_PROPERTY_NAME);

        if ((queueSize == null) || queueSize.isEmpty() || !isDigit(queueSize)) {
            log.warn(
                    "The value of {} property: {}, is not set or is an invalid value. will initialize without any limit",
                    QUEUE_SIZE_PROPERTY_NAME, queueSize);
            return null;
        }

        final int queueSizeIntVal = Integer.parseInt(queueSize);

        if (queueSizeIntVal <= 0) {
            log.warn(
                    "Configuration property {} must be greater than zero. Setting it to default value(no limit)",
                    THREAD_POOL_SIZE_PROPERTY_NAME);
            return null;
        }
        return queueSizeIntVal;
    }

    private RejectedExecutionHandler getRejectedExecutionHandlerConfigParam() {
        final String rejectionPolicy = getEventHandlerContext()
                .getEventHandlerConfiguration().getStringProperty(
                        REJECTED_EXECUTION_POLICY);
        if ((rejectionPolicy == null) || rejectionPolicy.isEmpty()) {
            log.warn(
                    "The value of {} property: {}, is not set or is an invalid value. will initialize default CallerRunsPolicy",
                    REJECTED_EXECUTION_POLICY, rejectionPolicy);
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }

        return getPolicy(rejectionPolicy);
    }

    private RejectedExecutionHandler getPolicy(final String rejectionPolicy) {
        switch (rejectionPolicy.trim()) {
            case "AbortPolicy":
                return new ThreadPoolExecutor.AbortPolicy();
            case "DiscardPolicy":
                return new ThreadPoolExecutor.DiscardPolicy();
            case "DiscardOldestPolicy":
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case "LoggedDiscardPolicy":
                return new LoggedDiscardPolicy(this);
            default:
                log.warn(
                        "No Policy defined will initialize default CallerRunsPolicy",
                        REJECTED_EXECUTION_POLICY, rejectionPolicy);
                return new ThreadPoolExecutor.CallerRunsPolicy();

        }
    }

    /**
     * @return the threadPoolSize
     */
    protected int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * @return the threadPriority
     */
    protected int getThreadPriority() {
        return threadPriority;
    }

    /**
     * @return the runnableQueueSize
     */
    protected Integer getRunnableQueueSize() {
        return runnableQueueSize;
    }

    private void logInvalidConfigParam(final String paramName,
            final String value, final String defaultValue) {
        log.warn(
                "The value of {} property: {}, is not set or is an invalid value. Using the default value of {} for this property instead.",
                paramName, value, defaultValue);
    }

    /**
     * @return the dropCount
     */
    protected int getDropCount() {
        return dropCount;
    }

    private class LoggedLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

        private static final long serialVersionUID = 1L;

        /**
         * Default Constructor
         */
        public LoggedLinkedBlockingQueue() {
            super();
        }

        public LoggedLinkedBlockingQueue(final int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(final E record) {
            final boolean offer = super.offer(record);
            if (!offer) {
                logDroppedRecord();
            }
            return offer;
        }

        private void logDroppedRecord() {
            dropCount++;
            if (dropCount >= REJECTED_DROPPED_RECORD_THRESHOLD) {
                log.info(
                        "Unable to add {} records to Queue, Dropped {} records .. ",
                        dropCount, dropCount);
                dropCount = 0;
            }
        }
    }

    /**
     * A handler for rejected tasks that discards the rejected tasks. The
     * rejected tasks are counted and reported with the EPS statistics.
     *
     */
    // Protected access for unit test
    protected class LoggedDiscardPolicy implements RejectedExecutionHandler {

        private Counter rejectedEvents;

        /**
         * Initialise the LoggedDiscardPolicy
         *
         * Create counter for rejected events if statistics is enabled.
         *
         * @param eventHandler
         *            the EventHandler using the LoggedDiscardPolicy
         */
        public LoggedDiscardPolicy(AbstractEventHandler eventHandler) {
            if (statisticsOn) {
                rejectedEvents = statisticsRegister.createCounter(
                        getRejectedEventsCounterNamePrefixConfigParam()
                        + "." + getRejectedEventsCounterNameConfigParam(), eventHandler);
            }
        }

        /**
         * Does nothing, which has the effect of discarding task r.
         *
         * @param runnable
         *            the runnable task requested to be executed
         * @param threadPoolExecutor
         *            the executor attempting to execute this task
         */
        @Override
        public void rejectedExecution(final Runnable runnable,
                final ThreadPoolExecutor threadPoolExecutor) {
            log.debug("{} runnable Rejected ... ", runnable);
            rejectedCount++;
            if (rejectedCount >= REJECTED_DROPPED_RECORD_THRESHOLD) {
                log.error("{} runnables rejected ... ", rejectedCount);
                rejectedCount = 0;
            }

            incrRejectedEventsCounterIfStatisticsOn(1);
        }

        /**
         *
         * @return the configured name for the rejected events counter
         *         if available, otherwise will return the default name
         *         for rejected events
         */
        // Access set for unit test
        final String getRejectedEventsCounterNameConfigParam() {
            final String rejectedEventsCounterName = getEventHandlerContext()
                    .getEventHandlerConfiguration().getStringProperty(
                            REJECTED_EVENTS_COUNTER_NAME);

            if ((rejectedEventsCounterName == null)
                    || rejectedEventsCounterName.isEmpty()) {
                log.warn(
                        "The property {}, is not set. Will use the default value of \"{}\"",
                        REJECTED_EVENTS_COUNTER_NAME,
                        DEFAULT_REJECTED_EVENTS_COUNTER_NAME);
                return DEFAULT_REJECTED_EVENTS_COUNTER_NAME;
            }

            return rejectedEventsCounterName;
        }

        /**
         *
         * @return the configured name prefix for the rejected events counter
         *         if available, otherwise will return the default name
         *         prefix for rejected events
         */
        // Access set for unit test
        final String getRejectedEventsCounterNamePrefixConfigParam() {
            final String rejectedEventsCounterNamePrefix = getEventHandlerContext()
                    .getEventHandlerConfiguration().getStringProperty(
                            REJECTED_EVENTS_COUNTER_NAME_PREFIX);

            if ((rejectedEventsCounterNamePrefix == null)
                    || rejectedEventsCounterNamePrefix.isEmpty()) {
                final String defaultPrefix = LoggedDiscardPolicy.class
                        .getName();
                log.warn(
                        "The property {}, is not set. Will use the default value of \"{}\"",
                        REJECTED_EVENTS_COUNTER_NAME_PREFIX, defaultPrefix);
                return defaultPrefix;
            }

            return rejectedEventsCounterNamePrefix;
        }

        private void incrRejectedEventsCounterIfStatisticsOn(final int count) {
            if (statisticsOn) {
                rejectedEvents.inc(count);
                if (rejectedEvents.getCount() < 0) {
                    // Reset the counter as it has wrapped around
                    rejectedEvents.dec(rejectedEvents.getCount());
                    rejectedEvents.inc(count);
                }
            }
        }

        /**
         *
         * @return the configured name for the rejected events counter
         *         if available, otherwise will return the default name
         *         for rejected events
         */
        Counter getRejectedEventsCounter() {
            return rejectedEvents;
        }

    }

}
