/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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

public class EpsTestOutputAdapter implements com.ericsson.component.aia.services.eps.adapter.OutputAdapter {
    private static final String URL = "epsApiOutput:";

    private boolean onEventCalled = false;
    private boolean initCalled = false;
    private boolean destroyCalled = false;

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(URL);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        onEventCalled = true;
    }

    @Override
    public void init(final EventHandlerContext eventHandlerContext) {
        initCalled = true;
    }

    @Override
    public void destroy() {
        destroyCalled = true;
    }
    
    public boolean isOnEventCalled(){
        return this.onEventCalled;
    }
    
    public boolean isInitCalled(){
        return this.initCalled;
    }
    
    public boolean isDestroyCalled(){
        return this.destroyCalled;
    }

}
