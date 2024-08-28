/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.coordination.utils;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

public class TestingOutputAdapter implements OutputAdapter {
    private static final String ADAPTIVE_STREAMING_URL = "zookeeper-test-output";

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(ADAPTIVE_STREAMING_URL);
    }

    @Override
    public void onEvent(final Object inputEvent) {
    }

    @Override
    public void init(final EventHandlerContext eventHandlerContext) {
    }

    @Override
    public void destroy() {
    }

}
