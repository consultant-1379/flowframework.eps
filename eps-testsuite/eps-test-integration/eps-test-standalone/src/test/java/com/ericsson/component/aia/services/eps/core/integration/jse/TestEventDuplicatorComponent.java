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

import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.*;

public class TestEventDuplicatorComponent extends AbstractEventHandler implements EventInputHandler, Controllable {

    private boolean destroyed = false;
    private int duplicationCount;
    private boolean paused = false;

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    protected void doInit() {
        duplicationCount = getConfiguration().getIntProperty("duplicationCount");
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (paused) {
            return;
        }
        if (destroyed) {
            throw new IllegalStateException("Component was already destroyed - should not be invoked again. Received event is " + inputEvent);
        }
        log.debug("Received event {}", inputEvent);
        for (final EventSubscriber eih : getEventHandlerContext().getEventSubscribers()) {
            for (int i = 0; i < duplicationCount; i++) {
                eih.sendEvent(inputEvent);
            }
            log.debug("Duplicated event {} - {} times", inputEvent, duplicationCount);
        }
    }

    @Override
    public void react(final ControlEvent controlEvent) {
        if (controlEvent.getType() == TestControlEventSenderComponent.CONTROL_EVENT_PAUSE_TYPE) {
            log.debug("Asked to pause - will do that");
            paused = true;
        }
    }

}
