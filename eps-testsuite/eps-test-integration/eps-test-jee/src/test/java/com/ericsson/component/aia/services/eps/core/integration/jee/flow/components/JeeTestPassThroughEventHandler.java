package com.ericsson.component.aia.services.eps.core.integration.jee.flow.components;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.oss.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;

public class JeeTestPassThroughEventHandler extends AbstractEventHandler implements EventInputHandler {

    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Override
    protected void doInit() {
        initialiseStatistics();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
        updateStatisticsWithEventsSent(inputEvent);
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
                eventMeter = statisticsRegister.createMeter("passThroughCount", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if (!(statisticsRegister == null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}