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
package com.ericsson.component.aia.services.eps.io.adapter.hc;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.threading.EpsThreadFactory;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 *
 * @author eborziv
 *
 */
public class HazelcastInputAdapter extends AbstractEventHandler implements InputAdapter {

    private static final int DEFAULT_THREAD_POOL_SIZE = 5;
    private static final int DEFAULT_THREAD_PRIORITY = 8;

    ExecutorService executorService;

    private EpsStatisticsRegister statisticsRegister;

    private Meter eventMeter;

    private HazelcastEventListener hazelcastEventListener;
    private String channelName;
    private ITopic topic;

    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private int threadPriority = DEFAULT_THREAD_PRIORITY;

    @Override
    public void onEvent(final Object inputEvent) {
        this.sendToAllSubscribers(inputEvent);
    }

    @Override
    public void destroy() {
        if (hazelcastEventListener != null) {
            hazelcastEventListener.destroyListener();
        }
        try {
            if ((hazelcastEventListener != null) && (topic != null)) {
                topic.removeMessageListener(hazelcastEventListener.toString());
            }
            log.info("Destroyed hazelcast listener and removed it from topic {}", channelName);
            HazelcastAdapterUtil.shutdownHazelcastInstance(this.getConfiguration());
        } catch (final Exception exc) {
            log.warn("Exception while shutting down Hazelcast components. Details: {}", exc.getMessage());
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        super.destroy();
    }

    @Override
    public boolean understandsURI(final String uri) {
        return (uri != null) && uri.startsWith(HazelcastAdapterUtil.HAZELCAST_URI_SCHEME);
    }

    /**
     * Send the specified event to all the subscribers and update the statistic if enabled.
     *
     * @param event
     *            the event
     */
    void sendEvent(final Object event) {
        this.sendToAllSubscribers(event);
        updateStatisticsWithEventReceived();
    }

    @Override
    protected void doInit() {
        channelName = this.getConfiguration().getStringProperty(HazelcastAdapterUtil.CHANNEL_NAME_PROP);
        if ((channelName == null) || channelName.isEmpty()) {
            throw new IllegalStateException("channelName must not be null or empty");
        }

        final String threadPoolSizeString = this.getConfiguration().getStringProperty(HazelcastAdapterUtil.HAZELCAST_LISTENER_THREAD_POOL_SIZE);

        if ((threadPoolSizeString != null) && !threadPoolSizeString.trim().isEmpty()) {
            try {
                threadPoolSize = Integer.parseInt(threadPoolSizeString);
                log.info("Thread pool size is {}", threadPoolSize);
            } catch (final NumberFormatException e) {
                log.warn("Could not parse thread pool size property. Will use default value of {}", threadPoolSize);
            }
        }

        setThreadPriority(this.getConfiguration().getStringProperty(HazelcastAdapterUtil.HAZELCAST_LISTENER_THREAD_POOL_THREAD_PRIORITY));

        final String threadPoolName = "hazelcast-input-adapter-" + UUID.randomUUID().toString();
        executorService = Executors.newFixedThreadPool(threadPoolSize, new EpsThreadFactory(threadPoolName,
                EpsThreadFactory.DEFAULT_LOCAL_STACK_SIZE, threadPriority));

        log.info("Will have input adapter on HC channel [{}]. Number of threads {}, thread priority {}", channelName, threadPoolSize,
                threadPriority);
        final HazelcastInstance hazelcastInstance = HazelcastAdapterUtil.getOrCreateHazelcastInstance(this.getConfiguration());
        topic = hazelcastInstance.getTopic(channelName);
        log.debug("Create new hazelcast input adapter - listening for new events on {}...", channelName);
        if (topic != null) {
            hazelcastEventListener = new HazelcastEventListener(this);
            //eventListenerRegistrationId = topic.addMessageListener(hazelcastEventListener);
            topic.addMessageListener(hazelcastEventListener);
            log.info("Attached listener to {}", channelName);
        }
        initialiseStatistics();
    }

    private void setThreadPriority(final String threadPriorityString) {
        if ((threadPriorityString != null) && !threadPriorityString.trim().isEmpty()) {
            try {
                this.threadPriority = Integer.parseInt(threadPriorityString);
                log.info("Thread priority is {}", threadPriority);
            } catch (final NumberFormatException nfe) {
                log.warn("Could not parse thread priority property. Will use default value of {}", threadPriority);
            }
        }
    }

    @Override
    public String toString() {
        return "HazelcastInputAdapter [channelName=" + channelName + ", threadPoolSize=" + threadPoolSize
                + ", threadPriority=" + threadPriority + "]";
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(channelName + ".eventsReceived", this);
            }
        }
    }

    private void updateStatisticsWithEventReceived() {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}
