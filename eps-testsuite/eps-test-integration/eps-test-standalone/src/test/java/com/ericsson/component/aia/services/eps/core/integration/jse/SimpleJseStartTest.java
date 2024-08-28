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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

@RunWith(Parameterized.class)
public class SimpleJseStartTest {

    private static final String FLOWS_SIMPLE_INPUT_OUTPUT = "/flows/simple_input_output_flow.xml";
    private static final String FLOWS_SIMPLE_INPUT_OUTPUT_2 = "/flows/simple_input_output_flow_2.xml";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public final String nameSpace;

    private final EpsTestUtil epsTestUtil = new EpsTestUtil();
    private EpsInstanceManager epsInstanceManager;

    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] { { "" }, { "com.standalone.simpleio" } });
    }

    public SimpleJseStartTest(final String nameSpace) {
        this.nameSpace = nameSpace;
    }

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsTestUtil.setEpsRepositoryType(this.nameSpace);
        epsInstanceManager = epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
        epsInstanceManager = null;
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
    public void test_deploy_simple_module() throws Exception {
        TestUndeploymentComponent.clear();
        final HazelcastInputListener listener = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        Assert.assertFalse(TestUndeploymentComponent.isDestroyed());
        Assert.assertFalse(TestUndeploymentComponent.isInitialized());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);
        log.info("START DEPLOYMODULE");
        final String identifier = epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT);
        log.info("END DEPLOYMODULE");
        Assert.assertNotNull(identifier);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        Assert.assertFalse(TestUndeploymentComponent.isDestroyed());
        Assert.assertTrue(TestUndeploymentComponent.isInitialized());
        // double deploy - still only one module deployed
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("test1");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        hazelcastReceiveTopic.removeMessageListener(listener);
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
        Assert.assertTrue(TestUndeploymentComponent.isDestroyed());
        Assert.assertTrue(TestUndeploymentComponent.isInitialized());
        hzI.getLifecycleService().shutdown();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_deploy_two_modules() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(5);
        final HazelcastInputListener listener2 = new HazelcastInputListener(10);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        Assert.assertEquals(0, listener2.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic2");

        final ITopic hazelcastSendTopic2 = hzI.getTopic("newTestTopic1");
        final ITopic hazelcastReceiveTopic2 = hzI.getTopic("newTestTopic2");

        hazelcastReceiveTopic.addMessageListener(listener);
        hazelcastReceiveTopic2.addMessageListener(listener2);
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        final String flowId2 = epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT_2);
        Assert.assertEquals(2, moduleManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("test2");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        Assert.assertEquals(0, listener2.getReceivedMessages().size());
        hazelcastSendTopic2.publish("test3");
        Assert.assertTrue(listener2.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, listener2.getReceivedMessages().size());

        // now remove one module and assert
        moduleManager.undeployModule(flowId2);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        // listener2 should not receive any messages
        listener2.clear(1);
        listener.clear(5);
        hazelcastSendTopic.publish("test4");
        hazelcastSendTopic2.publish("test5");
        Assert.assertTrue(listener.cdLatch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        Assert.assertFalse(listener2.cdLatch.await(5, TimeUnit.SECONDS));
        hazelcastReceiveTopic.removeMessageListener(listener);
        hazelcastReceiveTopic2.removeMessageListener(listener2);
        hzI.getLifecycleService().shutdown();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_control_event() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        // double deploy - still only one module deployed
        epsTestUtil.deployModule(FLOWS_SIMPLE_INPUT_OUTPUT);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("test1");
        log.debug("Sent event");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        log.debug("Verified that all events were received");
        // now send control event and see that nothing passes through
        listener.clear(1);
        Assert.assertTrue(listener.getReceivedMessages().isEmpty());
        Assert.assertFalse(listener.cdLatch.await(1, TimeUnit.SECONDS));
        log.debug("Verified that everything is clear - sending control event");
        hazelcastSendTopic.publish(TestControlEventSenderComponent.SEND_CONTROL_EVENT_TRIGGER);
        log.debug("Sent control event - now sending regular events which should not be received");
        Thread.sleep(2000);
        // now everything should be paused... try to send few events
        hazelcastSendTopic.publish("test_no_receive1");
        hazelcastSendTopic.publish("test_no_receive2");
        hazelcastSendTopic.publish("test_no_receive3");
        Assert.assertFalse(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
    }

}