package com.ericsson.component.aia.services.eps.core.integration.jse;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class TestUndeploymentComponent extends AbstractEventHandler implements EventInputHandler {

    private static boolean initialized = false;
    private static boolean destroyed = false;

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    public void onEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
    }

    @Override
    protected void doInit() {
        initialized = true;
    }

    public static void clear() {
        initialized = false;
        destroyed = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isDestroyed() {
        return destroyed;
    }

}
