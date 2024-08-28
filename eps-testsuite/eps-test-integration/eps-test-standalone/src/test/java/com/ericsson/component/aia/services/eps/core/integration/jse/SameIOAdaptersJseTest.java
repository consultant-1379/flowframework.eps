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

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class SameIOAdaptersJseTest {

    private static final String FLOWS_SAME_NAME_INPUT_1 = "/flows/same_name_input_1.xml";
    private static final String FLOWS_SAME_NAME_INPUT_1_1 = "/flows/same_name_input_1_1.xml";
    private static final String FLOWS_SAME_NAME_INPUT_2 = "/flows/same_name_input_2.xml";
    private EpsInstanceManager epsInstanceManager;
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsInstanceManager = epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
        epsInstanceManager = null;
    }

    /**
     * Two modules with same name for input and output adapters, same Hazelcast topic as input, and different output
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void test_deploy_two_modules() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(3);
        final HazelcastInputListener listener2 = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        Assert.assertEquals(0, listener2.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic-in");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic-out-1");
        final ITopic hazelcastReceiveTopic2 = hzI.getTopic("eps-topic-out-2");

        hazelcastReceiveTopic.addMessageListener(listener);
        hazelcastReceiveTopic2.addMessageListener(listener2);
        epsTestUtil.deployModule(FLOWS_SAME_NAME_INPUT_1);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        epsTestUtil.deployModule(FLOWS_SAME_NAME_INPUT_2);
        Assert.assertEquals(2, moduleManager.getDeployedModulesCount());

        hazelcastSendTopic.publish("test1");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener.getReceivedMessages().size());
        Assert.assertEquals(5, listener2.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
    }

    /**
     * module upgrade maintaining the same name for input and output adapters, different Hazelcast topic as input, different processing and same topic
     * as output
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void test_upgrade_module() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(3);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic-in");
        final ITopic hazelcastSendTopic1_1 = hzI.getTopic("eps-topic-in-1-1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic-out-1");

        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_SAME_NAME_INPUT_1);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        epsTestUtil.deployModule(FLOWS_SAME_NAME_INPUT_1_1);
        Assert.assertEquals(2, moduleManager.getDeployedModulesCount());

        hazelcastSendTopic.publish("test2");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener.getReceivedMessages().size());

        listener.clear(4);
        hazelcastSendTopic1_1.publish("test3");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(4, listener.getReceivedMessages().size());

        hzI.getLifecycleService().shutdown();
    }
}