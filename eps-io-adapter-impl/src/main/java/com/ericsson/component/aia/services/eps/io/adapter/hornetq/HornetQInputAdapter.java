package com.ericsson.component.aia.services.eps.io.adapter.hornetq;

import java.util.LinkedList;
import java.util.List;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.*;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;

/**
 *
 * @author eborziv
 *
 */
public class HornetQInputAdapter extends AbstractHornetQAdapter implements Adapter, InputAdapter {

    private static final String MAX_RATE_MSG_PER_SEC_PROP_NAME = "consumerMaxRateMsgPerSec";

    private static final int DEFAULT_MAX_RATE_MSG_PER_SEC = -1; // no max rate

    private static final int DEFAULT_NUMBER_OF_CONSUMER_SESSIONS = 1;

    private static final String NUMBER_OF_CONSUMER_SESSIONS_PROP_NAME = "consumerSessionsNumber";

    private EpsStatisticsRegister statisticsRegister;

    private Meter eventMeter;

    private int numberOfConsumerSessions = DEFAULT_NUMBER_OF_CONSUMER_SESSIONS;

    private int maxRateMsgPerSec = DEFAULT_MAX_RATE_MSG_PER_SEC;

    private final List<ClientConsumer> clientConsumers = new LinkedList<>();

    private final List<ClientSession> clientSessions = new LinkedList<>();

    @Override
    public void onEvent(final Object evt) {
        throw new UnsupportedOperationException("operation not supported!");
    }

    /**
     * Send event to all the Subscribers and update the statistics if enabled.
     *
     * @param event
     *            The event to be sent out
     */
    void sendEvent(final Object event) {
        sendToAllSubscribers(event);
        updateStatisticsWithEventReceived();
    }

    @Override
    public void destroy() {
        for (int i = 0; i < numberOfConsumerSessions; i++) {
            try {
                final ClientConsumer clientConsumer = clientConsumers.get(i);
                final ClientSession clientSession = clientSessions.get(i);
                if ((clientConsumer != null) && !clientConsumer.isClosed()) {
                    clientConsumer.close();
                    log.debug("Closed message consumer!");
                }
                if (!clientSession.isClosed()) {
                    clientSession.close();
                    log.debug("Stopped client session");
                }
            } catch (final HornetQException e) {
                log.error("Exception while closing hornetq resources");
                e.printStackTrace();
            }
        }

        freeConnectionFactory();
        super.destroy();

    }

    @Override
    protected void doInit() {

        initSharedResources(getEventHandlerContext());
        final ClientSessionFactory sessionFactory = getSessionFactory();
        numberOfConsumerSessions = getConfigurationParamIfExistsOrDefault(NUMBER_OF_CONSUMER_SESSIONS_PROP_NAME,
                DEFAULT_NUMBER_OF_CONSUMER_SESSIONS, true);
        maxRateMsgPerSec = getConfigurationParamIfExistsOrDefault(MAX_RATE_MSG_PER_SEC_PROP_NAME, DEFAULT_MAX_RATE_MSG_PER_SEC, false);
        if (maxRateMsgPerSec != -1) {
            log.warn("You set {} to {}. Consumer receiving rate will be limited to this number messages per second",
                    MAX_RATE_MSG_PER_SEC_PROP_NAME, maxRateMsgPerSec);
        }
        try {
            for (int i = 0; i < numberOfConsumerSessions; i++) {
                final ClientSession clientSession = sessionFactory.createSession(false, true, true, true);
                log.debug("Created session");
                final boolean browseOnly = false;
                final int maxRate = maxRateMsgPerSec;
                final int windowSize = 1024 * 1024 * connectionInfo.getWindowSizeMb();
                final String filter = null;
                final ClientConsumer clientConsumer = clientSession.createConsumer(channelName, filter, windowSize, maxRate, browseOnly);
                final MessageHandler messageHandler = new HornetQMessageHandlerImpl(this);
                clientConsumer.setMessageHandler(messageHandler);
                clientSession.start();
                clientSessions.add(clientSession);
                clientConsumers.add(clientConsumer);
                log.debug("Created message consumer #{} and attached message handler!", i);
            }
        } catch (final HornetQException e) {
            log.error("Exception while creating message consumer. Details {}", e.getMessage());
            throw new HornetQAdapterException(e);
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
                eventMeter = statisticsRegister.createMeter(channelName + ".eventsReceived", this);
            }
        }
    }

    private boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }

    private void updateStatisticsWithEventReceived() {
        if (isStatisticsOn()) {
            eventMeter.mark();
        }
    }

}
