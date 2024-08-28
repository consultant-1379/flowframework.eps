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
package com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample;

public enum KpiInterval {

    MINUTES_1(1), MINUTES_2(2), MINUTES_5(5), MINUTES_15(15), MINUTES_30(30), MINUTES_60(60);

    private final int val;

    private KpiInterval(final int v) {
        val = v;
    }

    public int getValue() {
        return val;
    }

}
