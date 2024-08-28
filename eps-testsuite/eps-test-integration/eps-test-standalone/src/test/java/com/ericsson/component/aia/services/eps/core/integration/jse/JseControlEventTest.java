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
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 * 
 * @author eborziv
 * 
 */
public class JseControlEventTest {

    private static final String FLOWS_SIMPLE_INPUT_OUTPUT_FLOW = "/flows/simple_input_output_flow.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_deploy_null_module() {
        if (epsTestUtil.isModelService()) {
            epsTestUtil.deployModuleFromModel(null);
        } else {
            epsTestUtil.deployModuleFromFile(null);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_control_event_to_all_modules() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT_FLOW);
        final ModuleManager moduleManager = epsTestUtil.getEpsInstance().getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        // double deploy - still only one module deployed
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT_FLOW);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("test1");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        // now send control event and see that nothing passes through
        listener.clear(1);
        final ControlEvent ctrlEv = new ControlEvent(TestControlEventSenderComponent.CONTROL_EVENT_PAUSE_TYPE);
        final int modulesReceived = moduleManager.sendControlEventToAllModules(ctrlEv);
        Assert.assertEquals(1, modulesReceived);
        // now everything should be paused... try to send few events
        hazelcastSendTopic.publish("test1");
        hazelcastSendTopic.publish("test2");
        hazelcastSendTopic.publish("test3");
        Assert.assertFalse(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void test_control_event_to_specific_module() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);
        final String moduleId = epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT_FLOW);
        final ModuleManager moduleManager = epsTestUtil.getEpsInstance().getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        // double deploy - still only one module deployed
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT_FLOW);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("test1");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        // now send control event and see that nothing passes through
        listener.clear(1);
        final ControlEvent ctrlEv = new ControlEvent(TestControlEventSenderComponent.CONTROL_EVENT_PAUSE_TYPE);
        final boolean sent = moduleManager.sendControlEventToModuleById(moduleId, ctrlEv);
        Assert.assertTrue(sent);
        Assert.assertFalse(moduleManager.sendControlEventToModuleById("module_that_does_not_exist", ctrlEv));
        // now everything should be paused... try to send few events
        hazelcastSendTopic.publish("test1");
        hazelcastSendTopic.publish("test2");
        hazelcastSendTopic.publish("test3");
        Assert.assertFalse(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
    }

}