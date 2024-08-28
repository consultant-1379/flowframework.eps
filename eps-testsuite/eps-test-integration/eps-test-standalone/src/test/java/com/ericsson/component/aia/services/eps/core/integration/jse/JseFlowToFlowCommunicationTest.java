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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class JseFlowToFlowCommunicationTest {

    private static final String FLOWS_FLOW_COMMUNICATION_OUTPUT2 = "/flows/flow_communication_output2.xml";
    private static final String FLOWS_FLOW_COMMUNICATION_OUTPUT = "/flows/flow_communication_output.xml";
    private static final String FLOWS_FLOW_COMMUNICATION_INPUT = "/flows/flow_communication_input.xml";
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
    public void test() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(3);
        final HazelcastInputListener listener1 = new HazelcastInputListener(1);
        final HazelcastInputListener listener2 = new HazelcastInputListener(1);
        final HazelcastInputListener listener3 = new HazelcastInputListener(1);
        final HazelcastInputListener listener4 = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        Assert.assertEquals(0, listener1.getReceivedMessages().size());
        Assert.assertEquals(0, listener2.getReceivedMessages().size());
        Assert.assertEquals(0, listener3.getReceivedMessages().size());
        Assert.assertEquals(0, listener4.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-input-flow");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-output-flow");
        final ITopic hazelcastReceiveTopic1 = hzI.getTopic("eps-output-flow1");
        final ITopic hazelcastReceiveTopic2 = hzI.getTopic("eps-output-flow2");
        final ITopic hazelcastReceiveTopic3 = hzI.getTopic("eps-output-flow3");
        final ITopic hazelcastReceiveTopic4 = hzI.getTopic("eps-output-flow4");
        hazelcastReceiveTopic.addMessageListener(listener);
        hazelcastReceiveTopic1.addMessageListener(listener1);
        hazelcastReceiveTopic2.addMessageListener(listener2);
        hazelcastReceiveTopic3.addMessageListener(listener3);
        hazelcastReceiveTopic4.addMessageListener(listener4);
        epsTestUtil.deployModule(FLOWS_FLOW_COMMUNICATION_INPUT);
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("flow2flow");
        Assert.assertFalse(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        epsTestUtil.deployModule(FLOWS_FLOW_COMMUNICATION_OUTPUT);
        Assert.assertEquals(2, modulesManager.getDeployedModulesCount());
        epsTestUtil.deployModule(FLOWS_FLOW_COMMUNICATION_OUTPUT2);
        Assert.assertEquals(3, modulesManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("flow2flow should be sent");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener1.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener3.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener4.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener.getReceivedMessages().size());
        Assert.assertEquals(1, listener1.getReceivedMessages().size());
        Assert.assertEquals(1, listener2.getReceivedMessages().size());
        Assert.assertEquals(1, listener3.getReceivedMessages().size());
        Assert.assertEquals(5, listener4.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
    }

}
