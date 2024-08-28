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
package com.ericsson.component.aia.services.eps.io.adapter.core;

import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

/**
 * Needed for SPI
 *
 * @author eborziv
 *
 */
public class InVmOutputAdapterImpl extends InVmAbstractAdapterImpl implements OutputAdapter {

    @Override
    public String toString() {
        return "InVmOutputAdapterImpl [channelId=" + channelId + "]";
    }

}
