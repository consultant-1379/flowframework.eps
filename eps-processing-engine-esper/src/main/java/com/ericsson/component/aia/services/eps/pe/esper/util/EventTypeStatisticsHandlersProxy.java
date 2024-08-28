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
package com.ericsson.component.aia.services.eps.pe.esper.util;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl;

/**
 * The Class EventTypeStatisticsHandlersProxy.
 */
public class EventTypeStatisticsHandlersProxy extends EpsStatisticsRegisterImpl {

    private static final Map<String, Meter> knownEventTypes = new HashMap<String, Meter>();

    /**
    * @param eventTypeName    eventTypeName
    * @return  Meter
    */
    public Meter createMeter(final String eventTypeName) {
        Meter meter = null;
        if (!knownEventTypes.containsKey(eventTypeName)) {
            meter = createMeter(eventTypeName + ".eventsReceived");
            knownEventTypes.put(eventTypeName, meter);
        } else {
            meter = knownEventTypes.get(eventTypeName);
        }
        return meter;
    }

}
