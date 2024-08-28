/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 *
 * @author eborziv
 *
 */
public class LoadBalancerComponentTest {

    private static final String FLOW_LB_RANDOM_FLOW = "/flows/load_balancing_random_flow.xml";
    private static final String FLOW_LB_ROUND_FLOW = "/flows/load_balancing_round_flow.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testRandomRobinLB() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(1);
        Assert.assertEquals(0, listener.getReceivedMessages().size());

        final HazelcastInputListener listener2 = new HazelcastInputListener(1);
        Assert.assertEquals(0, listener2.getReceivedMessages().size());

        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topicLBRand1");
        final ITopic hazelcastReceiveTopic1 = hzI.getTopic("eps-topicOutLBRand1");
        final ITopic hazelcastReceiveTopic2 = hzI.getTopic("eps-topicOutLBRand2");
        hazelcastReceiveTopic1.addMessageListener(listener);
        hazelcastReceiveTopic2.addMessageListener(listener2);

        epsTestUtil.deployModule(FLOW_LB_RANDOM_FLOW);
        final ModuleManager moduleManager = epsTestUtil.getEpsInstance().getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        final int messageCount = 15;
        for (int i = 0; i < messageCount; i++) {
            hazelcastSendTopic.publish("testMessage_" + i);
        }
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.cdLatch.await(1, TimeUnit.SECONDS));

        int totalReceived = 0;
        int attempts = 0;
        while (totalReceived < messageCount) {
            final int numFirstReceived = listener.getReceivedMessages().size();
            final int numSecondReceived = listener2.getReceivedMessages().size();
            totalReceived = numFirstReceived + numSecondReceived;
            attempts++;
            if (attempts > 5) {
                throw new IllegalStateException("Did not receive all messages within given time");
            }
            Thread.sleep(300);
        }
        Assert.assertEquals(messageCount, totalReceived);
        hzI.getLifecycleService().shutdown();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testRoundRobinLB() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(8);
        Assert.assertEquals(0, listener.getReceivedMessages().size());

        final HazelcastInputListener listener2 = new HazelcastInputListener(8);
        Assert.assertEquals(0, listener2.getReceivedMessages().size());

        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topicLBRound1");
        final ITopic hazelcastReceiveTopic1 = hzI.getTopic("eps-topicOutLBRound1");
        final ITopic hazelcastReceiveTopic2 = hzI.getTopic("eps-topicOutLBRound2");
        hazelcastReceiveTopic1.addMessageListener(listener);
        hazelcastReceiveTopic2.addMessageListener(listener2);

        epsTestUtil.deployModule(FLOW_LB_ROUND_FLOW);
        final ModuleManager moduleManager = epsTestUtil.getEpsInstance().getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        final int messageCount = 16;
        for (int i = 0; i < messageCount; i++) {
            hazelcastSendTopic.publish("testMessage_" + i);
        }
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.cdLatch.await(1, TimeUnit.SECONDS));

        final int numFirstReceived = listener.getReceivedMessages().size();
        final int numSecondReceived = listener2.getReceivedMessages().size();

        Assert.assertEquals(8, numFirstReceived);
        Assert.assertEquals(8, numSecondReceived);
        hzI.getLifecycleService().shutdown();
    }

}