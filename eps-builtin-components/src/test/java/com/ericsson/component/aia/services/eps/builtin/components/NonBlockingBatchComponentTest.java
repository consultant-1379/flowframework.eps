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
package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.*;
import java.util.concurrent.*;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.NonBlockingBatchComponent;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventSubscriberImpl;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.*;

public class NonBlockingBatchComponentTest {

    @Test(expected = IllegalStateException.class)
    public void test_error() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(1, 0, 1000, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        nbc.destroy();
    }

    @Test(expected = IllegalStateException.class)
    public void test_error2() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(0, 1, 1000, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
    }

    @Test(expected = IllegalStateException.class)
    public void test_error3() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(1, 1, 0, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
    }

    @Test
    public void test_simple() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(1, 1, 10000, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        Assert.assertTrue(receiver.receivedEvents.isEmpty());
        nbc.onEvent("1a2b");
        Assert.assertTrue(receiver.cdLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(1, receiver.receivedEvents.size());
        nbc.destroy();
    }

    @Test
    public void test_multiple_events() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(100, 2, 1000, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        final int eventsToBeSent = 400;
        receiver.clear(eventsToBeSent);
        Assert.assertTrue(receiver.receivedEvents.isEmpty());
        for (int i = 0; i < eventsToBeSent; i++) {
            nbc.onEvent("evt_" + i);
        }
        Assert.assertTrue(receiver.cdLatch.await(4, TimeUnit.SECONDS));
        Assert.assertEquals(eventsToBeSent, receiver.receivedEvents.size());
        nbc.destroy();
    }

    @Test
    public void test_multiple_events_not_even() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(100, 3, 300, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        final int eventsToBeSent = 4739;
        receiver.clear(eventsToBeSent);
        Assert.assertTrue(receiver.receivedEvents.isEmpty());
        for (int i = 0; i < eventsToBeSent; i++) {
            nbc.onEvent("evt_" + i);
        }
        Assert.assertTrue(receiver.cdLatch.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(eventsToBeSent, receiver.receivedEvents.size());
        nbc.destroy();
    }

    @Test
    public void test_test_multiple_events_not_even() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(117, 13, 300, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        final int eventsToBeSent = 27;
        receiver.clear(eventsToBeSent);
        Assert.assertTrue(receiver.receivedEvents.isEmpty());
        for (int i = 0; i < eventsToBeSent; i++) {
            nbc.onEvent("evt_" + i);
        }
        Assert.assertTrue(receiver.cdLatch.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(eventsToBeSent, receiver.receivedEvents.size());
        nbc.destroy();
    }

    @Test
    public void test_accuracy() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(119, 7, 300, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        final int eventsToBeSent = 7317;
        receiver.clear(eventsToBeSent);
        Assert.assertTrue(receiver.receivedEvents.isEmpty());
        for (int i = 0; i < eventsToBeSent; i++) {
            nbc.onEvent("" + i);
        }
        Assert.assertTrue(receiver.cdLatch.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(eventsToBeSent, receiver.receivedEvents.size());
        for (int i = 0; i < eventsToBeSent; i++) {
            final String event = "" + i;
            Assert.assertTrue("Should contain " + event, receiver.receivedEvents.contains(event));
        }
        nbc.destroy();
    }

    @Test
    public void test_accuracy_multithreaded() throws Exception {
        final TestEventReceiver receiver = new TestEventReceiver();
        final EventHandlerContext ctx = createContext(17, 7, 700, receiver);
        final NonBlockingBatchComponent nbc = new NonBlockingBatchComponent();
        nbc.init(ctx);
        final int eventsToBeSent = 13177;
        final int totalEventsToBeReceived = 3 * eventsToBeSent;
        receiver.clear(totalEventsToBeReceived);
        Assert.assertTrue(receiver.receivedEvents.isEmpty());
        final ExecutorService execService = Executors.newCachedThreadPool();
        final Future<Integer> futureA = execService.submit(new Callable<Integer>() {

            @Override
            public Integer call() {
                for (int i = 0; i < eventsToBeSent; i++) {
                    nbc.onEvent("a" + i);
                }
                return eventsToBeSent;
            }
        });
        final Future<Integer> futureB = execService.submit(new Callable<Integer>() {

            @Override
            public Integer call() {
                for (int i = 0; i < eventsToBeSent; i++) {
                    nbc.onEvent("b" + i);
                }
                return eventsToBeSent;
            }
        });
        for (int i = 0; i < eventsToBeSent; i++) {
            nbc.onEvent("x" + i);
        }
        // make sure threads do finish their work
        final Integer sentA = futureA.get();
        Assert.assertEquals(eventsToBeSent, sentA.intValue());
        final Integer sentB = futureB.get();
        Assert.assertEquals(eventsToBeSent, sentB.intValue());
        Assert.assertTrue(receiver.cdLatch.await(4, TimeUnit.SECONDS));
        Assert.assertEquals(totalEventsToBeReceived, receiver.receivedEvents.size());
        for (int i = 0; i < eventsToBeSent; i++) {
            final String eventX = "x" + i;
            final String eventA = "a" + i;
            final String eventB = "b" + i;
            Assert.assertTrue(receiver.receivedEvents.contains(eventA));
            Assert.assertTrue(receiver.receivedEvents.contains(eventB));
            Assert.assertTrue(receiver.receivedEvents.contains(eventX));
        }
        nbc.destroy();
        execService.shutdownNow();
    }

    private EventHandlerContext createContext(final int batchSize, final int numOfWorkers, final int flushPeriodMillis,
                                              final TestEventReceiver receiver) {
        final EventHandlerContext ctx = new EventHandlerContext() {

            @Override
            public void sendControlEvent(final ControlEvent ctrl) {

            }

            @Override
            public Collection<EventSubscriber> getEventSubscribers() {
                final Collection<EventSubscriber> receivers = new LinkedList<>();
                final EpsEventSubscriberImpl subscriber = new EpsEventSubscriberImpl("test_receiver", receiver);
                receivers.add(subscriber);
                return receivers;
            }

            @Override
            public Configuration getEventHandlerConfiguration() {
                final Configuration config = new Configuration() {

                    @Override
                    public String getStringProperty(final String paramName) {
                        if (paramName.equals(NonBlockingBatchComponent.MAX_BATCH_SIZE_CONFIG_PARAM_NAME)) {
                            return "" + batchSize;
                        }
                        if (paramName.equals(NonBlockingBatchComponent.NUM_BATCH_WORKERS_CONFIG_PARAM_NAME)) {
                            return "" + numOfWorkers;
                        }
                        if (paramName.equals(NonBlockingBatchComponent.FLUSH_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME)) {
                            return "" + flushPeriodMillis;
                        }
                        return null;
                    }

                    @Override
                    public Integer getIntProperty(final String arg0) {
                        return null;
                    }

                    @Override
                    public Boolean getBooleanProperty(final String arg0) {
                        return null;
                    }

                    @Override
                    public Map<String, Object> getAllProperties() {
                        return null;
                    }
                };
                return config;
            }

            @Override
            public Object getContextualData(final String name) {
                return null;
            }
        };
        return ctx;
    }

    private static class TestEventReceiver implements EventInputHandler {

        private final Logger log = LoggerFactory.getLogger(getClass());

        private final List<Object> receivedEvents = new LinkedList<>();
        CountDownLatch cdLatch = new CountDownLatch(1);

        @Override
        public void init(final EventHandlerContext ctx) {

        }

        @Override
        public void destroy() {

        }

        @Override
        public synchronized void onEvent(final Object evt) {
            final int eventsBefore = receivedEvents.size();
            log.debug("Received {}. In total have {} events so far", evt, receivedEvents.size());
            final long countBefore = cdLatch.getCount();
            final int received = unwrap(evt);
            final int eventsAfter = receivedEvents.size();
            final long countAfter = cdLatch.getCount();
            if (countAfter + received != countBefore) {
                throw new IllegalStateException("Counts do not match! before=" + countBefore + ", after=" + countAfter + ", receivedEvents="
                        + received);
            }
            if (eventsBefore + received != eventsAfter) {
                throw new IllegalStateException("Event count does not match, before=" + eventsBefore + ", after=" + eventsAfter + ", receivedEvents="
                        + received);
            }
        }

        private synchronized int unwrap(final Object evt) {
            if (evt instanceof Collection) {
                final Collection events = (Collection) evt;
                for (final Object obj : events) {
                    receivedEvents.add(obj);
                    cdLatch.countDown();
                }
                return events.size();
            } else {
                receivedEvents.add(evt);
                cdLatch.countDown();
                return 1;
            }
        }

        public synchronized void clear(final int count) {
            receivedEvents.clear();
            cdLatch = new CountDownLatch(count);
        }

    }

}
