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
package com.ericsson.component.aia.services.eps.io.adapter.util;

import java.util.Collection;

/**
 *
 * @author eborziv
 *
 */
public abstract class IOAdapterUtil {

    /**
     * Threshold, how many millis it takes to publish something onto eventbus so that we report it as a warning message.
     */
    public static final int PUBLISHING_THRESHOLD_MILLIS = 2000;

    /**
     * Gets the num of total events written.
     *
     * @param inputEvent
     *            the input event
     * @return the total events written
     */
    public static int getTotalEventsWritten(final Object inputEvent) {
        int totalEvents = 0;
        if (inputEvent instanceof Collection) {
            totalEvents = ((Collection) inputEvent).size();
        } else if (inputEvent instanceof Object[]) {
            totalEvents = ((Object[]) inputEvent).length;
        } else {
            totalEvents = 1;
        }
        return totalEvents;
    }

}
