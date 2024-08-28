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

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.threading.EpsThreadFactory;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;

/**
 * The Class JmsInputAdapter that extends {@link AbstractEventHandler}.
 *
 * @see MessageListener
 * @see InputAdapter
 */
public class JmsInputAdapter extends AbstractEventHandler implements MessageListener, InputAdapter {

    static final String JMS_ADAPTER_THREAD_POOL_SIZE = "jmsAdapterThreadpoolSize";
    static final String JMS_ADAPTER_THREADS_PRIORITY = "jmsAdapterThreadsPriority";

    private static final int DEFAULT_THREAD_POOL_SIZE = 1;

    private static final int DEFAULT_THREAD_PRIORITY = 5;

    private static final String URI = "jms:/";
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final JmsConnector connector;

    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    private ExecutorService executor;

    /**
     * Instantiates a new jms input adapter.
     */
    public JmsInputAdapter() {
        connector = new JmsConnector();
    }

    /**
     * Enable unit test for adapter, allowing JmsConnector mocked instances to be passed here.
     *
     * @param connector
     *            The JmsConnector
     */
    JmsInputAdapter(final JmsConnector connector) {
        this.connector = connector;
    }

    @Override
    public void destroy() {
        connector.closeSession();
        executor.shutdownNow();
        super.destroy();
    }

    @Override
    public void onEvent(final Object event) {
        throw new UnsupportedOperationException("Operation not supported. JMS input adapter is always entry points on event chain!");
    }

    @Override
    public void onMessage(final Message message) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (message instanceof ObjectMessage) {
                        JmsInputAdapter.this.sendToAllSubscribers(((ObjectMessage) message).getObject());
                    } else if (message instanceof TextMessage) {
                        JmsInputAdapter.this.sendToAllSubscribers(((TextMessage) message).getText());
                    } else {
                        JmsInputAdapter.this.sendToAllSubscribers(message);
                    }
                    log.debug("Forwarded message to all subscribers. Message: {}", message);

                    updateStatisticsWithEventReceived();

                } catch (final Exception e) {
                    throw new JmsAdapterException("Error pushing JMS Message to the flow", e);
                }
            }
        };
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
        connector.init(getConfiguration());
        final MessageConsumer consumer = connector.createConsumer();
        try {
            consumer.setMessageListener(this);
            final String poolSizeString = getConfiguration().getStringProperty(JMS_ADAPTER_THREAD_POOL_SIZE);
            final int poolSize = ((poolSizeString == null) || "".equals(poolSizeString)) ? DEFAULT_THREAD_POOL_SIZE : Integer
                    .parseInt(poolSizeString);
            final String threadPriorityString = getConfiguration().getStringProperty(JMS_ADAPTER_THREADS_PRIORITY);
            final int threadPriority = ((threadPriorityString == null) || "".equals(threadPriorityString)) ? DEFAULT_THREAD_PRIORITY : Integer
                    .parseInt(threadPriorityString);
            final String threadPoolName = "jms-input-adapter-" + UUID.randomUUID().toString();
            executor = Executors.newFixedThreadPool(poolSize, new EpsThreadFactory(threadPoolName, EpsThreadFactory.DEFAULT_LOCAL_STACK_SIZE,
                    threadPriority));
            log.debug("Successfully created JmsInputAdapter instance listening to destination: {}", consumer.getMessageSelector());
        } catch (final JMSException e) {
            throw new JmsAdapterException("Could not register adapter as a JMS listener.", e);
        } catch (final NumberFormatException e) {
            connector.closeSession();
            throw new JmsAdapterException("Invalid pool size value", e);
        }
        initialiseStatistics();
    }

    /**
     * Initialize statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                        EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null, event statistics will not be measured");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(connector.getDestinationName() + ".eventsReceived", this);
            }
        }
    }

    private void updateStatisticsWithEventReceived() {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}
