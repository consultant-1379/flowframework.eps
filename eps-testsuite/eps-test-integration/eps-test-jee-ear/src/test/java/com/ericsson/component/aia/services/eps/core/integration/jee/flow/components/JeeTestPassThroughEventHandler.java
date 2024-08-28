package com.ericsson.component.aia.services.eps.core.integration.jee.flow.components;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;


@Named("testPassThroughComponent")
public class JeeTestPassThroughEventHandler extends AbstractEventHandler implements EventInputHandler {

    private final String STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME = "eps_statistics_register";
    private static final Logger LOG = LoggerFactory.getLogger(JeeTestPassThroughEventHandler.class);
    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Override
    protected void doInit() {
        LOG.info("JeeTestPassThroughEventHandler::doInit");
        initialiseStatistics();
    }

    @Override
    public void destroy() {
        LOG.info("JeeTestPassThroughEventHandler::destroy");
    }

    @Override
    public void onEvent(final Object inputEvent) {
        LOG.info("JeeTestPassThroughEventHandler::onEvent: " + inputEvent);
        sendToAllSubscribers(inputEvent);
        updateStatisticsWithEventsSent(inputEvent);
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter("sampleMeterForPassThrough", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}