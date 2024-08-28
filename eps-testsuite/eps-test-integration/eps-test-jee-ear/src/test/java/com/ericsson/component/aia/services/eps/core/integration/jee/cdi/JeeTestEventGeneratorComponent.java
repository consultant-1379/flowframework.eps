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
package com.ericsson.component.aia.services.eps.core.integration.jee.cdi;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

@Named("cdiEventGenerator")
public class JeeTestEventGeneratorComponent extends AbstractEventHandler implements EventInputHandler {

    private final String STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME = "eps_statistics_register";
    private boolean destroyed = false;
    private int count;
    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Inject
    private Event<JeeTestCdiEvent> cdiEvents;

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    protected void doInit() {
        count = getConfiguration().getIntProperty("count");
        initialiseStatistics();
        log.debug("cdiEventGenerator init: count = " + count);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (destroyed) {
            throw new IllegalStateException("Component was already destroyed - should not be invoked again. Received event is " + inputEvent);
        }

        log.debug("Sending event {} to observers {} times.", inputEvent, count);
        for (int i = 0; i < count; i++) {
            cdiEvents.fire(new JeeTestCdiEvent(inputEvent));
        }

        log.debug("Sending events to all subscribers");
        sendToAllSubscribers(inputEvent);
        updateStatisticsWithEventsSent(inputEvent);
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                this.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter("sampleMeterForduplicator", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if (!(statisticsRegister == null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}
