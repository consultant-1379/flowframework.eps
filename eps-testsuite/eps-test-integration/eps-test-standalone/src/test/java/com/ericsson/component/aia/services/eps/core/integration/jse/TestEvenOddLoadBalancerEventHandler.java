package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.util.Collection;

import com.ericsson.component.aia.itpf.common.event.handler.*;

public class TestEvenOddLoadBalancerEventHandler extends AbstractEventHandler implements EventInputHandler {

    private static Collection<EventSubscriber> subscribers;

    @Override
    public void destroy() {

    }

    @Override
    public void onEvent(final Object inputEvent) {
        final Integer num = (Integer) inputEvent;
        if (num.intValue() % 2 == 0) {
            findSubscriberByName("even").sendEvent(num.intValue() * 2);
        } else {
            findSubscriberByName("odd").sendEvent(num.intValue() * 1000);
        }
    }

    private EventSubscriber findSubscriberByName(final String name) {
        for (final EventSubscriber sub : getEventHandlerContext().getEventSubscribers()) {
            if (sub.getIdentifier().equals(name)) {
                return sub;
            }
        }
        return null;
    }

    @Override
    protected void doInit() {
        subscribers = getEventHandlerContext().getEventSubscribers();
    }

    public static Collection<EventSubscriber> getSubscribers() {
        return subscribers;
    }

    public static void clear() {
        subscribers = null;
    }

}
