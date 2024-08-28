/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jee.flow.components;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.oss.itpf.common.event.handler.*;

/**
 * Simulates a long taking computation.
 * 
 * @author esarlag
 */
public class JeeTestLongProcessingHandler extends AbstractEventHandler implements EventInputHandler {

    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Override
    public void onEvent(final Object inputEvent) {
        log.debug("Received event {}", inputEvent);
        if (!(inputEvent instanceof String)) {
            log.error("Wrong message type");
            return;
        }
        int number = Integer.parseInt(((String) inputEvent).split("test")[1]);
        while (!isPrime(number) && number > 0) {
            number--;
        }
        for (final EventSubscriber eih : this.getEventHandlerContext().getEventSubscribers()) {
            eih.sendEvent(inputEvent);
            updateStatisticsWithEventsSent(inputEvent);
            log.debug("Prime number {} - {} ", inputEvent, number);
        }
    }

    @Override
    protected void doInit() {
        initialiseStatistics();
    }

    /**
     * checks whether an int is prime or not.
     * 
     * @param n
     *            the n
     * @return true, if is prime
     */
    boolean isPrime(final int n) {
        if (n <= 0)
            return false;
        if (n == 1 || n == 2)
            return true;
        for (int i = 3; 2 * i < n; i += 2) {
            if (n % i == 0)
                return false;
        }
        return true;
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
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}
