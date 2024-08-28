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

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.FlowDeploymentFailurePolicy;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class JseScriptingFlowTest {

    private static final String FLOWS_TEST_JVM_SCRIPT_FLOW = "/flows/test_jvm_script_flow.xml";
    private static final String FLOWS_TEST_JVM_SCRIPT_FLOW_INVALID = "/flows/test_jvm_script_flow_invalid.xml";
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
    public void test_deploy_invalid_module() throws Exception {
        System.setProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY, FlowDeploymentFailurePolicy.CONTINUE.toString());
        epsTestUtil.deployModule(FLOWS_TEST_JVM_SCRIPT_FLOW_INVALID);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_deploy_tripling_jvm_scripting_module() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(3);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic-jython-in");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic-jython-out");

        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_TEST_JVM_SCRIPT_FLOW);
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());

        hazelcastSendTopic.publish("200");
        Assert.assertTrue(listener.cdLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener.getReceivedMessages().size());
        listener.clear(30);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        for (int i = 0; i < 10; i++) {
            hazelcastSendTopic.publish("300");
        }
        Assert.assertTrue(listener.cdLatch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(30, listener.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
    }

}