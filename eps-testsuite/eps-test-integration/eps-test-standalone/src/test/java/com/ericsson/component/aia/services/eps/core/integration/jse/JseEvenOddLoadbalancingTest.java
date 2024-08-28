package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class JseEvenOddLoadbalancingTest {

    private static final String FLOWS_TEST_MULTIPLE_SUBSCRIBERS_FLOW = "/flows/test_multiple_subscribers_flow.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();
    private EpsInstanceManager epsInstanceManager;

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsInstanceManager = epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
        epsInstanceManager = null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test() throws Exception {
        final HazelcastInputListener oddListener = new HazelcastInputListener(3 * 5);
        final HazelcastInputListener evenListener = new HazelcastInputListener(2 * 5);
        Assert.assertEquals(0, oddListener.getReceivedMessages().size());
        Assert.assertEquals(0, evenListener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic-in");
        final ITopic hazelcastReceiveTopicEven = hzI.getTopic("eps-topic-even");
        final ITopic hazelcastReceiveTopicOdd = hzI.getTopic("eps-topic-odd");
        TestEvenOddLoadBalancerEventHandler.clear();
        Assert.assertNull(TestEvenOddLoadBalancerEventHandler.getSubscribers());
        hazelcastReceiveTopicEven.addMessageListener(evenListener);
        hazelcastReceiveTopicOdd.addMessageListener(oddListener);
        final String identifier = epsTestUtil.deployModule(FLOWS_TEST_MULTIPLE_SUBSCRIBERS_FLOW);
        Assert.assertNotNull(identifier);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        for (int i = 0; i < 10; i++) {
            final Integer num = new Integer(i);
            hazelcastSendTopic.publish(num);
        }
        Assert.assertTrue(oddListener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(evenListener.cdLatch.await(1, TimeUnit.SECONDS));
        for (int i = 0; i < 10; i++) {
            final Integer evenExpected = new Integer(i * 2);
            final Integer oddExpected = new Integer(i * 1000);
            if (i % 2 == 0) {
                Assert.assertTrue(evenListener.getReceivedMessages().contains(evenExpected));
            } else {
                Assert.assertTrue(oddListener.getReceivedMessages().contains(oddExpected));
            }
        }
        Assert.assertEquals(15, oddListener.getReceivedMessages().size());
        Assert.assertEquals(10, evenListener.getReceivedMessages().size());
        Assert.assertNotNull(TestEvenOddLoadBalancerEventHandler.getSubscribers());
        Assert.assertEquals(2, TestEvenOddLoadBalancerEventHandler.getSubscribers().size());
        // now test ordering of subscribers
        final Iterator<EventSubscriber> subscribersIterator = TestEvenOddLoadBalancerEventHandler.getSubscribers().iterator();
        Assert.assertEquals("even", subscribersIterator.next().getIdentifier());
        Assert.assertEquals("odd", subscribersIterator.next().getIdentifier());
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
        hzI.getLifecycleService().shutdown();
    }

}
