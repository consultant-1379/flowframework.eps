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

public class SimpleEsperFlowTest {

    private static final String FLOWS_ESPER_WITH_CONFIGURATION = "/flows/esper_flow_with_configuration.xml";
    private static final String FLOWS_SIMPLE_ESPER = "/flows/simple_esper_flow.xml";
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
    public void test_deploy_simple_module() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(10);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-input-topic");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-output-topic");
        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_SIMPLE_ESPER);
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
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, listener.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_deploy_module_with_esper_configuration() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(10);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-input-topic");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-output-topic");
        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_ESPER_WITH_CONFIGURATION);
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
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, listener.getReceivedMessages().size());
        hzI.getLifecycleService().shutdown();
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
    }

}
