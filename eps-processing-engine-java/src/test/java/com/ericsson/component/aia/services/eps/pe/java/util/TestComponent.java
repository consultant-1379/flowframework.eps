package com.ericsson.component.aia.services.eps.pe.java.util;

import javax.inject.Named;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

@Named("testComponent")
public class TestComponent extends AbstractEventHandler implements EventInputHandler {

    @Override
    public void destroy() {

    }

    @Override
    public void onEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
    }

    @Override
    protected void doInit() {

    }

}