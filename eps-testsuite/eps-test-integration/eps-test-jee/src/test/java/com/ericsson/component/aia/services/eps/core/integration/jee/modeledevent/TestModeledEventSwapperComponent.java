/*------------------------------------------------------------------------------
 *******************************************************************************
 * © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent.TestInputModeledEvent;
import com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent.TestOutputModeledEvent;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.oss.itpf.common.event.handler.*;

public class TestModeledEventSwapperComponent extends AbstractEventHandler implements EventInputHandler {

    private final String STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME = "eps_statistics_register";
    private boolean destroyed = false;
    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    public void onEvent(final Object inputEvent) {
        Object outputEvent = null;
        if (destroyed) {
            throw new IllegalStateException("Component was already destroyed - should not be invoked again");
        }
        log.debug("Received event {}", inputEvent);
        if (TestInputModeledEvent.class.isAssignableFrom(inputEvent.getClass())) {
            final TestOutputModeledEvent output = new TestOutputModeledEvent();
            output.setMyTestValue(((TestInputModeledEvent) inputEvent).getMyTestValue());
            outputEvent = output;
        } else if (TestOutputModeledEvent.class.isAssignableFrom(inputEvent.getClass())) {
            final TestInputModeledEvent output = new TestInputModeledEvent();
            output.setMyTestValue(((TestOutputModeledEvent) inputEvent).getMyTestValue());
            outputEvent = output;
        }

        for (final EventSubscriber eih : this.getEventHandlerContext().getEventSubscribers()) {
            eih.sendEvent(outputEvent);
            log.debug("Output event {}", outputEvent);
        }
        updateStatisticsWithEventsSent(inputEvent);
    }

    @Override
    protected void doInit() {
        initialiseStatistics();
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
