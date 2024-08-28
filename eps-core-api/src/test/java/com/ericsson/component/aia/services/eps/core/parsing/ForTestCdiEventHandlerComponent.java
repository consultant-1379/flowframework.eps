package com.ericsson.component.aia.services.eps.core.parsing;

import javax.inject.Named;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

@Named("cdiEventHandlerComponent")
public class ForTestCdiEventHandlerComponent extends AbstractEventHandler implements EventInputHandler {

    @Override
    public void onEvent(final Object inputEvent) {

    }

    @Override
    protected void doInit() {

    }

}