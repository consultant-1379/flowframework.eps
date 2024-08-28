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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.io.adapter.util.IOAdapterUtil;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 *
 * @author eborziv
 *
 */
public class HazelcastOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    private EpsStatisticsRegister statisticsRegister;
    private Meter batchesSentMeter;

    /*
     * Whether this component should process received events. If destroy process was started we
     * should immediately stop processing events, even before destroy process was not finalized.
     */
    private volatile boolean shouldProcessEvents = true;

    /*
     * Was this component fully destroyed
     */
    private volatile boolean destroyed;

    private String channelName;
    private ITopic topic;

    private Counter totalEventsSent;

    @Override
    public boolean understandsURI(final String uri) {
        return (uri != null) && uri.startsWith(HazelcastAdapterUtil.HAZELCAST_URI_SCHEME);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (!shouldProcessEvents) {
            // if destroy process started (hazelcast shutdown started) no point in sending any events to it
            log.warn("Destroy process already started! Will not process any more events!");
            return;
        }
        if (destroyed) {
            throw new IllegalStateException("Already destroyed! Can not receive events. Received event is " + inputEvent);
        }
        log.trace("Publishing event {} to destination {}", inputEvent, channelName);
        final long start = System.currentTimeMillis();
        topic.publish(inputEvent);
        final long total = System.currentTimeMillis() - start;
        this.checkWritingTooSlow(inputEvent, total);
        if (log.isDebugEnabled()) {
            log.debug("Publishing event to hazelcast took in total {}ms", total);
        }
        updateStatisticsWithEventsSent(inputEvent);
    }

    /**
     * Checks if writing to bus is too slow. If so writes warning message.
     *
     * @param inputEvent
     * @param total
     */
    private void checkWritingTooSlow(final Object inputEvent, final long total) {
        if (total >= IOAdapterUtil.PUBLISHING_THRESHOLD_MILLIS) {
            final int totalEventsPublished = IOAdapterUtil.getTotalEventsWritten(inputEvent);
            final boolean isByteArrayEvent = (inputEvent instanceof byte[]);
            if (isByteArrayEvent) {
                final byte[] bytes = (byte[]) inputEvent;
                final int messageLength = bytes.length;
                log.warn("It took {}ms to publish byte array of size {}", total, messageLength);
            } else {
                log.warn("It took {}ms to publish collection of batched {} events", total, totalEventsPublished);
            }
        }
    }

    private void updateTotalEventsWritten(final Object inputEvent) {
        final int totalEvents = IOAdapterUtil.getTotalEventsWritten(inputEvent);
        totalEventsSent.inc(totalEvents);
        log.debug("Batch size is {}", totalEvents);
    }

    @Override
    public void destroy() {
        log.info("Destroying component...");
        shouldProcessEvents = false;
        log.info("Will not process events - starting shutdown procedure of all resources...");
        HazelcastAdapterUtil.shutdownHazelcastInstance(this.getConfiguration());
        log.info("Successfully destroyed all resources.");
        destroyed = true;
        super.destroy();
    }

    @Override
    protected void doInit() {
        channelName = this.getConfiguration().getStringProperty(HazelcastAdapterUtil.CHANNEL_NAME_PROP);
        if ((channelName == null) || channelName.isEmpty()) {
            throw new IllegalStateException("channelName must not be null or empty");
        }
        log.info("Will have output adapter on hazelcast channel [{}]", channelName);
        final HazelcastInstance hazelcastInstance = HazelcastAdapterUtil.getOrCreateHazelcastInstance(this.getConfiguration());
        topic = hazelcastInstance.getTopic(channelName);
        log.info("Found topic {} by name {}", topic, channelName);
        initialiseStatistics();
    }

    @Override
    public String toString() {
        return "HazelcastOutputAdapter [shouldProcessEvents=" + shouldProcessEvents + ", destroyed=" + destroyed + ", channelName=" + channelName
                + ", totalEventsSent=" + (totalEventsSent == null ? 0 : totalEventsSent.getCount()) + "]";
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(
                        EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                batchesSentMeter = statisticsRegister.createMeter(channelName + ".eventsSent", this);
                totalEventsSent = statisticsRegister.createCounter(channelName + ".totalEventsSent", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if ((statisticsRegister != null)
                && (statisticsRegister.isStatisticsOn())) {
            batchesSentMeter.mark();
            updateTotalEventsWritten(inputEvent);
        }
    }

}
