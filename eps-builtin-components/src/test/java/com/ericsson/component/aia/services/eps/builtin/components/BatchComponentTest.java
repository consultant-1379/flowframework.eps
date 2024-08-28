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

import org.junit.*;

import com.ericsson.component.aia.services.eps.builtin.components.BatchComponent;
import com.ericsson.component.aia.services.eps.builtin.components.utils.StubbedConfiguration;
import com.ericsson.component.aia.services.eps.builtin.components.utils.StubbedEventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * 
 * @author eborziv
 * 
 */
public class BatchComponentTest {
    private static final String EVENT_RECEIVER_IDENTIFIER = "test_batch_subscriber";

    private final TestEventReceiver receiver = new TestEventReceiver();

    @Before
    public void setup() {
        receiver.clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_invalid_batch_size_purge_combination() {
        final EventHandlerContext ctx = getEventHandlerContext(-1, 0);
        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_invalid_batch_size_purge_combination1() {
        final EventHandlerContext ctx = getEventHandlerContext(0, 0);

        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_invalid_batch_size_purge_combination2() {
        final EventHandlerContext ctx = getEventHandlerContext(0, -1);
        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
    }

    @Test
    public void test_unlimited_batch_size() throws Exception {
        final EventHandlerContext ctx = getEventHandlerContext(-1, 100);
        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("abc");
        // sleep and let batch events to be purged downstream
        Thread.sleep(500);
        final List<Object> receivedEvents = receiver.getReceivedEvents();
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(1, receivedEvents.size());
    }

    @Test
    public void test_no_purging() {
        final EventHandlerContext ctx = getEventHandlerContext(2, 0);

        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("abc");
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("def");
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("ghi");
        final List<Object> receivedEvents = receiver.getReceivedEvents();
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(1, receivedEvents.size());
        Assert.assertTrue(receivedEvents.get(0) instanceof Collection);
        final Collection coll = (Collection) receivedEvents.get(0);
        Assert.assertEquals(2, coll.size());
        receiver.clear();
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("jkl");
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("mno");
        final List<Object> receivedEvents1 = receiver.getReceivedEvents();
        Assert.assertNotNull(receivedEvents1);
        Assert.assertEquals(1, receivedEvents1.size());
        Assert.assertTrue(receivedEvents1.get(0) instanceof Collection);
        final Collection coll1 = (Collection) receivedEvents1.get(0);
        Assert.assertEquals(2, coll1.size());
        batchComp.destroy();
    }

    @Test
    public void test_purging() throws Exception {
        final EventHandlerContext ctx = getEventHandlerContext(200, 100);

        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("abc");
        // sleep and let batch events to be purged downstream
        Thread.sleep(800);
        final List<Object> receivedEvents = receiver.getReceivedEvents();
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(1, receivedEvents.size());
        Assert.assertTrue(receivedEvents.get(0) instanceof Collection);
        final Collection coll = (Collection) receivedEvents.get(0);
        Assert.assertEquals(1, coll.size());
        receiver.clear();
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        Thread.sleep(500);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.destroy();
    }

    @Test
    public void test_multiple_purging() throws Exception {
        final EventHandlerContext ctx = getEventHandlerContext(200, 100);

        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.onEvent("abc");
        // sleep and let batch events to be purged downstream
        Thread.sleep(500);
        batchComp.onEvent("lmn");
        Thread.sleep(500);
        final List<Object> receivedEvents = receiver.getReceivedEvents();
        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(2, receivedEvents.size());
        Assert.assertTrue(receivedEvents.get(0) instanceof Collection);
        final Collection coll0 = (Collection) receivedEvents.get(0);
        Assert.assertEquals(1, coll0.size());
        Assert.assertTrue(receivedEvents.get(1) instanceof Collection);
        final Collection coll1 = (Collection) receivedEvents.get(1);
        Assert.assertEquals(1, coll1.size());
        receiver.clear();
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        Thread.sleep(500);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        batchComp.destroy();
    }

    @Test
    public void test_purging_multiple_threads() throws Exception {
        final EventHandlerContext ctx = getEventHandlerContext(200, 30);

        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
        Assert.assertEquals(0, receiver.getReceivedEvents().size());
        new Thread(new EventSenderWorker("a", batchComp, 1000)).start();
        new Thread(new EventSenderWorker("b", batchComp, 1000)).start();
        new Thread(new EventSenderWorker("c", batchComp, 1000)).start();
        // sleep and let batch events to be purged downstream
        Thread.sleep(2000);
        final List<Object> receivedEvents = receiver.getReceivedEvents();
        int countA = 0;
        int countB = 0;
        int countC = 0;
        // count that we received all events
        for (final Object obj : receivedEvents) {
            if (obj instanceof Collection) {
                final Collection coll = (Collection) obj;
                for (final Object singleElement : coll) {
                    if ("a".equals(singleElement)) {
                        countA++;
                    } else if ("b".equals(singleElement)) {
                        countB++;
                    } else if ("c".equals(singleElement)) {
                        countC++;
                    }
                }
            }
        }
        Assert.assertEquals(1000, countA);
        Assert.assertEquals(1000, countB);
        Assert.assertEquals(1000, countC);
        batchComp.destroy();
    }
    
    @Test
    public void test_SchedulingShouldNotDieIfExceptioIsThrown() throws Exception {
        final StubbedConfiguration configuration = new StubbedConfiguration();
        final StubbedEventReceiverThrowsException eventReceiver = new StubbedEventReceiverThrowsException();
        final EventHandlerContext ctx = new StubbedEventHandlerContext(configuration, EVENT_RECEIVER_IDENTIFIER, eventReceiver);
        configuration.addProperty(BatchComponent.MAX_BATCH_SIZE_CONFIG_PARAM_NAME, 200 + "");
        configuration.addProperty(BatchComponent.PURGE_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME, 30 + "");
        
        eventReceiver.setThrowExpection(true);
        
        final BatchComponent batchComp = new BatchComponent();
        batchComp.init(ctx);
        batchComp.onEvent("throwExpection");
        Thread.sleep(50);
        eventReceiver.setThrowExpection(false);
        Assert.assertEquals(0, eventReceiver.getReceivedEvents().size());
        new Thread(new EventSenderWorker("a", batchComp, 1000)).start();
        new Thread(new EventSenderWorker("b", batchComp, 1000)).start();
        new Thread(new EventSenderWorker("c", batchComp, 1000)).start();
        // sleep and let batch events to be purged downstream
        Thread.sleep(2000);
        final List<Object> receivedEvents = eventReceiver.getReceivedEvents();
        int countA = 0;
        int countB = 0;
        int countC = 0;
        // count that we received all events
        for (final Object obj : receivedEvents) {
            if (obj instanceof Collection) {
                final Collection coll = (Collection) obj;
                for (final Object singleElement : coll) {
                    if ("a".equals(singleElement)) {
                        countA++;
                    } else if ("b".equals(singleElement)) {
                        countB++;
                    } else if ("c".equals(singleElement)) {
                        countC++;
                    }
                }
            }
        }
        Assert.assertEquals(1000, countA);
        Assert.assertEquals(1000, countB);
        Assert.assertEquals(1000, countC);
        eventReceiver.clear();
        batchComp.destroy();
    }

    private EventHandlerContext getEventHandlerContext(final int batchSize, final int purgeBatchPeriod) {
        final StubbedConfiguration configuration = new StubbedConfiguration();
        final EventHandlerContext ctx = new StubbedEventHandlerContext(configuration, EVENT_RECEIVER_IDENTIFIER,
                receiver);
        configuration.addProperty(BatchComponent.MAX_BATCH_SIZE_CONFIG_PARAM_NAME, batchSize + "");
        configuration.addProperty(BatchComponent.PURGE_BATCH_PERIOD_MILLIS_CONFIG_PARAM_NAME, purgeBatchPeriod + "");
        return ctx;
    }

    private static class EventSenderWorker implements Runnable {

        private final BatchComponent batchComponent;

        private final Object event;

        int repeatCount;

        public EventSenderWorker(final Object event, final BatchComponent batchComp, final int repeat) {
            if (event == null) {
                throw new IllegalArgumentException("Event must not be null");
            }
            if (batchComp == null) {
                throw new IllegalArgumentException("Batch component must not be null");
            }
            this.event = event;
            batchComponent = batchComp;
            repeatCount = repeat;
        }

        @Override
        public void run() {
            for (int i = 0; i < repeatCount; i++) {
                if ((i % 200) == 0) {
                    try {
                        Thread.sleep(50);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                batchComponent.onEvent(event);
            }
        }

    }
    
    private static class StubbedEventReceiverThrowsException implements EventInputHandler {

        private final List<Object> receivedEvents = new LinkedList<>();
        
        private boolean throwExpectionFlag = false;

        @Override
        public void init(final EventHandlerContext arg0) {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void onEvent(final Object evt) {
            if(throwExpectionFlag){
                throw new RuntimeException();
            }
            receivedEvents.add(evt);
        }

        public void clear() {
            receivedEvents.clear();
        }

        public List<Object> getReceivedEvents() {
            return receivedEvents;
        }
        
        public void setThrowExpection(final boolean throwExpectionFlag){
            this.throwExpectionFlag = throwExpectionFlag;
        }
    }

    private static class TestEventReceiver implements EventInputHandler {

        private final List<Object> receivedEvents = new LinkedList<>();

        @Override
        public void init(final EventHandlerContext arg0) {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void onEvent(final Object evt) {
            receivedEvents.add(evt);
        }

        public void clear() {
            receivedEvents.clear();
        }

        public List<Object> getReceivedEvents() {
            return receivedEvents;
        }

    }

}
