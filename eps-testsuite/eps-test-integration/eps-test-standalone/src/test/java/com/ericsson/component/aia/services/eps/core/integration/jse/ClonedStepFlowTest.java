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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class ClonedStepFlowTest {

    private static final String FLOWS_CLONED_STEP_FLOW = "/flows/cloned_step_flow.xml";
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
    public void test_flow_with_cloned_steps() throws Exception {
        final HazelcastInputListener listenerA = new HazelcastInputListener(10);
        final HazelcastInputListener listenerB = new HazelcastInputListener(10);
        final HazelcastInputListener listenerC = new HazelcastInputListener(10);
        Assert.assertEquals(0, listenerA.getReceivedMessages().size());
        Assert.assertEquals(0, listenerB.getReceivedMessages().size());
        Assert.assertEquals(0, listenerC.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-input-topic");
        final ITopic hazelcastReceiveTopicA = hzI.getTopic("eps-output-topic-A");
        final ITopic hazelcastReceiveTopicB = hzI.getTopic("eps-output-topic-B");
        final ITopic hazelcastReceiveTopicC = hzI.getTopic("eps-output-topic-C");
        hazelcastReceiveTopicA.addMessageListener(listenerA);
        hazelcastReceiveTopicB.addMessageListener(listenerB);
        hazelcastReceiveTopicC.addMessageListener(listenerC);

        epsTestUtil.deployModule(FLOWS_CLONED_STEP_FLOW);
        final ModuleManager moduleManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        for (int i = 0; i < 10; i++) {
            final Map<String, String> evenMap = new HashMap<String, String>();
            evenMap.put("str", "even");
            hazelcastSendTopic.publish(evenMap);
            final Map<String, String> oddMap = new HashMap<String, String>();
            oddMap.put("str", "odd");
            hazelcastSendTopic.publish(oddMap);
        }
        Assert.assertTrue(listenerA.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, listenerA.getReceivedMessages().size());
        Assert.assertTrue(listenerB.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, listenerB.getReceivedMessages().size());
        Assert.assertTrue(listenerC.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, listenerC.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
    }
}
