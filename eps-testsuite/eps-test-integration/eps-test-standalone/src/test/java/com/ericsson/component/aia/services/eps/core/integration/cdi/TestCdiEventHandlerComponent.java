package com.ericsson.component.aia.services.eps.core.integration.cdi;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

@Named("cdiEventHandler")
public class TestCdiEventHandlerComponent extends AbstractEventHandler implements EventInputHandler {

    @Inject
    private Event<TestCdiEvent> cdiEvents;

    @Override
    protected void doInit() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onEvent(final Object inputEvent) {
        log.debug("Sending event {} to observers.", inputEvent);
        cdiEvents.fire(new TestCdiEvent(inputEvent));

        log.debug("send events to all subscribers {}", inputEvent);
        sendToAllSubscribers(inputEvent);
    }

}