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
package com.ericsson.component.aia.services.eps.io.adapter.hornetq;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.io.adapter.util.*;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

/**
 *
 * @author eborziv
 *
 */
public class HornetQOutputAdapter extends AbstractHornetQAdapter implements Adapter, OutputAdapter {

    private EpsStatisticsRegister statisticsRegister;

    private final ObjectSerializer serializer = new DefaultJavaSerializer();

    private Meter eventMeter;

    private boolean destroyed;

    private Counter totalEventsSent;

    private ClientSession session;
    private ClientProducer producer;

    @Override
    public void onEvent(final Object evt) {
        if (destroyed) {
            throw new IllegalStateException("Already destroyed! Can not receive events. Received event is " + evt);
        }
        final Message message = session.createMessage(false);
        try {

            final byte[] objectBytes = (evt instanceof byte[]) ? (byte[]) evt : serializer.objectToBytes(evt);
            if (objectBytes != null) {
                message.getBodyBuffer().writeBytes(objectBytes);
                final long start = System.currentTimeMillis();
                producer.send(message);
                final long total = System.currentTimeMillis() - start;
                if (total >= IOAdapterUtil.PUBLISHING_THRESHOLD_MILLIS) {
                    final int totalEventsPublished = IOAdapterUtil.getTotalEventsWritten(evt);
                    log.warn("It took {}ms to publish collection of batched {} events", total, totalEventsPublished);
                }
                if (log.isTraceEnabled()) {
                    log.trace("Publishing event to hornetq took in total {}ms", total);
                }
                log.trace("Sent message {}", evt);
                updateStatisticsWithEventsSent(evt);
            } else {
                log.warn("Will not send {} because it was converted to null byte array", evt);
            }
        } catch (final Exception exc) {
            log.error("Was not able to send message. Details {}", exc.getMessage());
            exc.printStackTrace();
        }
    }

    private void updateTotalEventsWritten(final Object inputEvent) {
        final int totalEvents = IOAdapterUtil.getTotalEventsWritten(inputEvent);
        totalEventsSent.inc(totalEvents);
        log.trace("Batch size is {}", totalEvents);
    }

    @Override
    public void destroy() {
        closeResources();
        freeConnectionFactory();
        destroyed = true;
        super.destroy();
    }

    private void closeResources() {
        try {
            if ((producer != null) && !producer.isClosed()) {
                producer.close();
            }
        } catch (final HornetQException ignored) {
            ignored.printStackTrace();
        }
        try {
            if ((session != null) && !session.isClosed()) {
                session.close();
            }
        } catch (final HornetQException ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    protected void doInit() {
        initSharedResources(getEventHandlerContext());
        try {
            session = getSessionFactory().createSession(false, true, true, true);
            log.info("Created hornetq session");
            producer = session.createProducer(channelName);
        } catch (final Exception exc) {
            log.error("Exception while creating hornetq resources. Details {}", exc.getMessage());
            throw new HornetQAdapterException(exc);
        }
        initialiseStatistics();
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
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(channelName + ".batchesSent", this);
                totalEventsSent = statisticsRegister.createCounter(channelName + ".totalEventsSent", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
            this.updateTotalEventsWritten(inputEvent);
        }
    }
}
