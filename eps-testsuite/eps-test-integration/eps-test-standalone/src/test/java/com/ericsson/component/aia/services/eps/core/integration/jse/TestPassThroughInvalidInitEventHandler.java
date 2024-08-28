package com.ericsson.component.aia.services.eps.core.integration.jse;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class TestPassThroughInvalidInitEventHandler extends AbstractEventHandler implements EventInputHandler {

    static boolean isDestroyInvoked = false;

    @Override
    public void destroy() {
        isDestroyInvoked = true;
    }

    static void clear() {
        isDestroyInvoked = false;
    }

    @Override
    public void onEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
    }

    @Override
    protected void doInit() {
        throw new IllegalStateException("I can not be initialized");
    }

}