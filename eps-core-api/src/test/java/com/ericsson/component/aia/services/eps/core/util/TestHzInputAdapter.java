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
package com.ericsson.component.aia.services.eps.core.util;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;

public class TestHzInputAdapter implements InputAdapter {

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.endsWith("hzTest:");
    }

    @Override
    public void onEvent(final Object arg0) {

    }

    @Override
    public void init(final EventHandlerContext arg0) {

    }

    @Override
    public void destroy() {

    }

}
