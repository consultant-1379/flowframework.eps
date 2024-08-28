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
package com.ericsson.component.aia.services.eps.core.integration.jse;

import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class TestControlEventSenderComponent extends AbstractEventHandler implements EventInputHandler {

    public static final int CONTROL_EVENT_PAUSE_TYPE = 42;
    public static final String SEND_CONTROL_EVENT_TRIGGER = "send_control";

    private boolean destroyed = false;

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    protected void doInit() {

    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (destroyed) {
            throw new IllegalStateException("Component was already destroyed - should not be invoked again. Received event is " + inputEvent);
        }
        log.debug("Received event {}", inputEvent);
        if (SEND_CONTROL_EVENT_TRIGGER.equals(inputEvent)) {
            final ControlEvent ctrlEv = new ControlEvent(CONTROL_EVENT_PAUSE_TYPE);
            getEventHandlerContext().sendControlEvent(ctrlEv);
            log.debug("Sent control event");
        }
        sendToAllSubscribers(inputEvent);
    }

}
