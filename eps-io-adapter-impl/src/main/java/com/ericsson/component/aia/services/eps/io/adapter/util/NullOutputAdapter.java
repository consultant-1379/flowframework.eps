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

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

/**
 * Output adapter that behaves like /dev/null
 *
 * @author eborziv
 *
 */
public class NullOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    private static final String NULL_URI = "null";

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(NULL_URI);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        // do nothing
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    protected void doInit() {
        // do nothing
    }

}
