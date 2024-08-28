package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.CollectionSplitterComponent;
import com.ericsson.component.aia.services.eps.core.parsing.EpsComponentConfiguration;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class CollectionSplitterComponentTest {

    @Test
    public void test_simple() {
        final CollectionSplitterComponent splitter = new CollectionSplitterComponent();
        final TestSubscriber sub = new TestSubscriber();
        splitter.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        splitter.onEvent("abc");
        Assert.assertEquals(1, sub.getReceivedEvents().size());
        Assert.assertTrue(sub.getReceivedEvents().contains("abc"));
        sub.clear();
        Assert.assertEquals(0, sub.getReceivedEvents().size());
    }

    @Test
    public void test_collections() {
        final CollectionSplitterComponent splitter = new CollectionSplitterComponent();
        final TestSubscriber sub = new TestSubscriber();
        splitter.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        final List<Integer> events = new LinkedList<>();
        final int totalEvents = 100;
        for (int i = 0; i < totalEvents; i++) {
            events.add(new Integer(i));
        }
        splitter.onEvent(events);
        Assert.assertEquals(totalEvents, sub.getReceivedEvents().size());
        for (int i = 0; i < totalEvents; i++) {
            Assert.assertTrue(sub.getReceivedEvents().contains(new Integer(i)));
        }
        sub.clear();
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        final String[] strings = { "1", "2", "3" };
        splitter.onEvent(strings);
        Assert.assertEquals(3, sub.getReceivedEvents().size());
        Assert.assertTrue(sub.getReceivedEvents().contains("1"));
        Assert.assertTrue(sub.getReceivedEvents().contains("2"));
        Assert.assertTrue(sub.getReceivedEvents().contains("3"));
    }

    private EventHandlerContext createCtx(final EventSubscriber sub) {
        return new EventHandlerContext() {

            @Override
            public void sendControlEvent(final ControlEvent controlEvent) {

            }

            @Override
            public Collection<EventSubscriber> getEventSubscribers() {
                final List<EventSubscriber> subs = new LinkedList<>();
                subs.add(sub);
                return subs;
            }

            @Override
            public Configuration getEventHandlerConfiguration() {
                return new EpsComponentConfiguration(new Properties());
            }

            @Override
            public Object getContextualData(final String name) {
                return null;
            }
        };
    }

    class TestSubscriber implements EventSubscriber {

        private final List<Object> receivedEvents = new LinkedList<>();

        public void clear() {
            receivedEvents.clear();
        }

        @Override
        public String getIdentifier() {
            return "test_id";
        }

        @Override
        public void sendEvent(final Object event) {
            receivedEvents.add(event);
        }

        public List<Object> getReceivedEvents() {
            return receivedEvents;
        }

    }
}
