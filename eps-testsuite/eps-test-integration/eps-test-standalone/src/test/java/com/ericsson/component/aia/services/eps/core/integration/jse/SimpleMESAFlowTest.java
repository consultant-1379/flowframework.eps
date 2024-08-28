package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class SimpleMESAFlowTest {

    private static final String SRC_TEST_RESOURCES_FLOWS_SIMPLE_MESA = "/flows/simple_mesa_flow.xml";
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
    public void test_deploy_module_with_esper_configuration() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(10);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final Config cfg = new XmlConfigBuilder("src/test/resources/config/hazelcast.xml").build();
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance(cfg);
        final ITopic hazelcastSendTopic = hzI.getTopic("mesa-input-topic");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("mesa-incident_topic");
        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(SRC_TEST_RESOURCES_FLOWS_SIMPLE_MESA);
        final ModuleManager moduleManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());

        final int resourceCount = 10;
        final int resourceId[] = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
        final int ropId[] = { 10, 15, 20, 25, 30, 35, 40, 45, 50, 55 };

        Thread.sleep(5000);
        for (int j = 0; j < resourceCount; j++) {
            for (int i = 0; i < resourceCount; i++) {
                final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
                event.setResourceId(resourceId[j]);
                event.setRopId(ropId[i]);
                event.setCONSUMED_CREDITS_UL(111);
                event.setCONSUMED_CREDITS_DL(111);
                hazelcastSendTopic.publish(event);

                final INTERNAL_SYSTEM_UTILIZATION eventOther = new INTERNAL_SYSTEM_UTILIZATION();
                eventOther.setResourceId(resourceId[j]);
                eventOther.setRopId(ropId[i]);
                eventOther.setCONSUMED_CREDITS_UL(111);
                eventOther.setCONSUMED_CREDITS_DL(111);
                hazelcastSendTopic.publish(eventOther);

            }
        }
        // TODO: do we really need to wait for 10sec here? Latch below should take care of that?
        // Thread.sleep(10000);
        Assert.assertTrue(listener.cdLatch.await(10, TimeUnit.SECONDS));

        hzI.getLifecycleService().shutdown();
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
    }
}
