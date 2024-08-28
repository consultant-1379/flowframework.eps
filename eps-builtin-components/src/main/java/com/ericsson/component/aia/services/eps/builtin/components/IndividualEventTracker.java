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
package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Tracks and exposes received events - so that we can see total count for every
 * distinct event type received. Should be used for debugging purposes only.
 *
 * @author eborziv
 *
 */
public class IndividualEventTracker extends AbstractEventHandler implements
        EventInputHandler {

    private EpsStatisticsRegister statisticsRegister;
    private boolean isStatisticsOn;

    /*
     * per event meters
     */
    private final Map<String, Meter> eventMeters = new HashMap<String, Meter>();

    @Override
    public void destroy() {
        eventMeters.clear();
    }

    @Override
    public void onEvent(final Object evt) {
        updateStatistics(evt);
        if (evt != null) {
            sendToAllSubscribers(evt);
        }
    }

    /**
     * @param evt
     */
    private void updateStatistics(final Object evt) {
        if (isStatisticsOn) {
            String className = "NULL";
            if (evt != null) {
                className = evt.getClass().getSimpleName();
            }
            // not thread-safe but close enough
            Meter meter = eventMeters.get(className);
            if (meter == null) {
                meter = statisticsRegister.createMeter("EVT-" + className + ".eventsReceived", this);
                eventMeters.put(className, meter);
            }
            meter.mark();
        }
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
                        EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            isStatisticsOn = statisticsRegister.isStatisticsOn();
        }
    }
}
