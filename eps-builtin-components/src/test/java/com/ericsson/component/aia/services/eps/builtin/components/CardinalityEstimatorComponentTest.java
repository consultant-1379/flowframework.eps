package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.CardinalityEstimatorComponent;
import com.ericsson.component.aia.services.eps.core.parsing.EpsComponentConfiguration;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class CardinalityEstimatorComponentTest {

    @Test
    public void test_simple() {
        final CardinalityEstimatorComponent hll = new CardinalityEstimatorComponent();
        final TestSubscriber sub = new TestSubscriber();
        hll.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 0; i < 10000000; i++) {
            final Long val = i;
            hll.onEvent(val);
        }

        final ControlEvent ctrlEv = new ControlEvent(CardinalityEstimatorComponent.CONTROL_EVENT_GET_CARDINALITY);
        hll.react(ctrlEv);

        Assert.assertEquals(1, sub.getReceivedEvents().size());
        Assert.assertTrue(sub.getReceivedEvents().contains(new Long(9842214)));
        sub.clear();
        Assert.assertEquals(0, sub.getReceivedEvents().size());
    }

    @Test
    public void test_merge() {
        final CardinalityEstimatorComponent hll3 = new CardinalityEstimatorComponent();
        final TestSubscriber sub3 = new TestSubscriber();
        hll3.init(createCtx(sub3));
        Assert.assertEquals(0, sub3.getReceivedEvents().size());

        final TestSubscriber sub = new TestSubscriber();
        final CardinalityEstimatorComponent hll1 = new CardinalityEstimatorComponent();
        hll1.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 0; i < 5000001; i++) {
            final Long val = i;
            hll1.onEvent(val);
        }

        final CardinalityEstimatorComponent hll2 = new CardinalityEstimatorComponent();
        hll2.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 5000001; i < 10000000; i++) {
            final Long val = i;
            hll2.onEvent(val);
        }

        final ControlEvent ctrlEv = new ControlEvent(CardinalityEstimatorComponent.CONTROL_EVENT_GET_BUCKETS);
        hll1.react(ctrlEv);
        hll2.react(ctrlEv);
        hll3.onEvent(sub.getReceivedEvents());

        final ControlEvent ctrlEv2 = new ControlEvent(CardinalityEstimatorComponent.CONTROL_EVENT_GET_CARDINALITY);
        hll3.react(ctrlEv2);

        Assert.assertTrue(sub3.getReceivedEvents().contains(new Long(9842214)));
        sub.clear();
        sub3.clear();
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        Assert.assertEquals(0, sub3.getReceivedEvents().size());
    }

    @Test
    public void test_merge2() {

        final CardinalityEstimatorComponent hll3 = new CardinalityEstimatorComponent();
        final TestSubscriber sub3 = new TestSubscriber();
        hll3.init(createCtx(sub3));
        Assert.assertEquals(0, sub3.getReceivedEvents().size());

        final TestSubscriber sub = new TestSubscriber();
        final CardinalityEstimatorComponent hll1 = new CardinalityEstimatorComponent();
        hll1.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 0; i < 2500001; i++) {
            final Long val = i;
            hll1.onEvent(val);
        }

        final CardinalityEstimatorComponent hll2 = new CardinalityEstimatorComponent();
        hll2.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 2500001; i < 5000000; i++) {
            final Long val = i;
            hll2.onEvent(val);
        }

        final CardinalityEstimatorComponent hll5 = new CardinalityEstimatorComponent();
        hll5.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 5000000; i < 7500001; i++) {
            final Long val = i;
            hll5.onEvent(val);
        }

        final CardinalityEstimatorComponent hll4 = new CardinalityEstimatorComponent();
        hll4.init(createCtx(sub));
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        for (long i = 7500001; i < 10000000; i++) {
            final Long val = i;
            hll4.onEvent(val);
        }

        final ControlEvent ctrlEv = new ControlEvent(CardinalityEstimatorComponent.CONTROL_EVENT_GET_BUCKETS);
        hll1.react(ctrlEv);
        hll2.react(ctrlEv);
        hll4.react(ctrlEv);
        hll5.react(ctrlEv);
        hll3.onEvent(sub.getReceivedEvents());

        final ControlEvent ce2 = new ControlEvent(CardinalityEstimatorComponent.CONTROL_EVENT_GET_CARDINALITY);
        hll3.react(ce2);

        Assert.assertTrue(sub3.getReceivedEvents().contains(new Long(9842214)));
        sub.clear();
        sub3.clear();
        Assert.assertEquals(0, sub.getReceivedEvents().size());
        Assert.assertEquals(0, sub3.getReceivedEvents().size());
    }

    private EventHandlerContext createCtx(final EventSubscriber sub) {
        return new EventHandlerContext() {

            @Override
            public void sendControlEvent(final ControlEvent controlEvent) {

            }

            @Override
            public Object getContextualData(final String str) {
                return null;
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
