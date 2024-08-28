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
package com.ericsson.component.aia.services.eps.io.adapter.jms;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.*;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.threading.EpsThreadFactory;
import com.ericsson.component.aia.services.eps.io.adapter.util.IOAdapterUtil;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

/**
 * The JmsOutputAdapter.
 *
 * @see AbstractEventHandler
 * @see OutputAdapter
 */
public class JmsOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    /**
     * Inner class to wrap event processing logic to be submitted to a multi threaded scheduler.
     *
     * @author esarlag
     *
     */
    class JmsEventProcessor implements Runnable {

        Object inputEvent;

        /**
         * Constructor for processor takes input event
         *
         * @param inputEvent
         *            the event that shall be processed
         */
        public JmsEventProcessor(final Object inputEvent) {
            this.inputEvent = inputEvent;
        }

        @Override
        public void run() {
            ObjectMessage msg = null;
            if (!JmsConnectorWrapper.isInitialized()) {
                JmsConnectorWrapper.set(new JmsConnector());
                JmsConnectorWrapper.initialize(configuration);
            }
            try {
                msg = JmsConnectorWrapper.get().createObjectMessage();
                msg.setObject((Serializable) inputEvent);
                JmsConnectorWrapper.getProducer().send(msg);
            } catch (final JMSException e) {
                throw new JmsAdapterException("Error sending JMS Message" + msg + " with producer " + JmsConnectorWrapper.getProducer().toString(),
                        e);
            }
            updateStatisticsWithEventsSent(inputEvent);
        }

    }

    static final String JMS_ADAPTER_THREAD_POOL_SIZE = "jmsAdapterThreadpoolSize";
    static final String JMS_ADAPTER_THREADS_PRIORITY = "jmsAdapterThreadsPriority";
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;

    private static final int DEFAULT_THREAD_PRIORITY = 5;

    private static final String URI = "jms:/";
    private Configuration configuration;

    private ExecutorService executor;

    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;
    private Counter totalEventsSent;

    @Override
    public void destroy() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e1) {
            throw new JmsAdapterException("Could not stop thread pool executor", e1);
        }
        JmsConnectorWrapper.cleanup();
        super.destroy();
    }

    @Override
    public void onEvent(final Object inputEvent) {

        if (!(inputEvent instanceof Serializable)) {

            throw new IllegalArgumentException("Received events must implement Serializable.");
        }

        final Runnable runnable = new JmsEventProcessor(inputEvent);
        executor.submit(runnable);

    }

    @Override
    public boolean understandsURI(final String uri) {
        if (uri == null) {
            return false;
        }
        return uri.equals(URI);
    }

    @Override
    protected void doInit() {

        final String poolSizeString = getConfiguration().getStringProperty(JMS_ADAPTER_THREAD_POOL_SIZE);
        final int poolSize = ((poolSizeString == null) || poolSizeString.isEmpty()) ? DEFAULT_THREAD_POOL_SIZE : Integer.parseInt(poolSizeString);
        final String threadPriorityString = getConfiguration().getStringProperty(JMS_ADAPTER_THREADS_PRIORITY);
        final int threadPriority = ((threadPriorityString == null) || threadPriorityString.isEmpty()) ? DEFAULT_THREAD_PRIORITY : Integer
                .parseInt(threadPriorityString);
        final String threadPoolName = "jms-input-adapter-" + UUID.randomUUID().toString();
        executor = Executors.newFixedThreadPool(poolSize, new EpsThreadFactory(threadPoolName, EpsThreadFactory.DEFAULT_LOCAL_STACK_SIZE,
                threadPriority));
        configuration = getConfiguration();

        log.debug("Sucessfully created new JmsOutputAdapter instance.");

        initialiseStatistics();
    }

    /**
     * Initialize statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null, event statistics will not be measured");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                final String destinationName = configuration.getStringProperty("jmsDestinationName");
                eventMeter = statisticsRegister.createMeter(destinationName + ".batchesSent", this);
                totalEventsSent = statisticsRegister.createCounter(destinationName + ".totalEventsSent", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
            updateTotalEventsWritten(inputEvent);
        }
    }

    private void updateTotalEventsWritten(final Object inputEvent) {
        final int totalEvents = IOAdapterUtil.getTotalEventsWritten(inputEvent);
        totalEventsSent.inc(totalEvents);
        log.trace("Batch size is {}", totalEvents);
    }
}
