package com.ericsson.component.aia.services.eps.core.integration.jse;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class TestPassThroughEventHandler extends AbstractEventHandler implements EventInputHandler {

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