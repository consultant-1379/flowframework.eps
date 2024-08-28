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

import com.ericsson.component.aia.itpf.common.io.InputAdapter;

/**
 * Needed for SPI
 *
 * @author eborziv
 *
 */
public class InVmInputAdapterImpl extends InVmAbstractAdapterImpl implements InputAdapter {

    @Override
    public String toString() {
        return "InVmInputAdapterImpl [channelId=" + channelId + "]";
    }

}
