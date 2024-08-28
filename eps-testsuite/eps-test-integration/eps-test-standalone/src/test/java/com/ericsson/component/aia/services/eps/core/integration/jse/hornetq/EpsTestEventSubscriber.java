package com.ericsson.component.aia.services.eps.core.integration.jse.hornetq;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class EpsTestEventSubscriber implements EventSubscriber {

    private final List<Object> objects = new LinkedList<>();
    public CountDownLatch latch;

    public void clear(final int latchCount) {
        latch = new CountDownLatch(latchCount);
        objects.clear();
    }

    @Override
    public String getIdentifier() {
        return "eps_test_subscriber";
    }

    @Override
    public void sendEvent(final Object evt) {
        objects.add(evt);
        latch.countDown();
    }

    public List<Object> getReceivedMessages() {
        return objects;
    }

}
