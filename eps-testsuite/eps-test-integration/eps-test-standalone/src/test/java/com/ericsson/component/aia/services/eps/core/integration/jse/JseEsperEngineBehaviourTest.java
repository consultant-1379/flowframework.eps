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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngine;
import com.ericsson.component.aia.services.eps.pe.core.AbstractProcessingEngine;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class JseEsperEngineBehaviourTest {

    private static final String FLOWS_ESPER_ENGINE_FLOW = "/flows/esper_engine_flow.xml";
    private static final String FLOWS_ESPER_ENGINE_FLOW_FS_RESOURCE = "/flows/esper_engine_flow_fs_resource.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();
    private EpsProvider epsProvider;

    @Before
    public void setUp() throws InterruptedException, ExecutionException {
        epsProvider = EpsProvider.getInstance();
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    @Test
    public void testModuleDeployment() throws InterruptedException {

        final String epsInstanceId = "esperEngine8";

        final HazelcastInputListener listener = new HazelcastInputListener(3);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-input-topic");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-output-topic");
        hazelcastReceiveTopic.addMessageListener(listener);

        final String uniqueModuleIdentifier = epsTestUtil.deployModule(FLOWS_ESPER_ENGINE_FLOW);
        assertEquals("com.ericsson.test_esper_engine_flow_1.0.0", uniqueModuleIdentifier);
        final ModuleManager moduleManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        assertEquals(1, moduleManager.getDeployedModulesCount());

        final ProcessingEngine processingEngine = epsProvider.getCreatedProcessingEngine(epsInstanceId);
        assertNotNull(processingEngine);
        assertEquals(epsInstanceId, processingEngine.getInstanceId());

        for (final String deployedModule : processingEngine.getDeployedModules()) {
            if (uniqueModuleIdentifier.equals(deployedModule)) {
                assertEquals(1, processingEngine.getDeployedModules().size());
                assertTrue(processingEngine.getDeployedModules().contains(uniqueModuleIdentifier));
                break;
            }
        }
        final String odd = "odd";
        final String even = "even";
        for (int i = 0; i < 6; i++) {
            String strToSend = odd;
            if (i % 2 == 0) {
                strToSend = even;
            }
            final Map<String, String> map = new HashMap<String, String>();
            map.put("str", strToSend);
            hazelcastSendTopic.publish(map);
        }

        Assert.assertTrue(listener.cdLatch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener.getReceivedMessages().size());

        hzI.getLifecycleService().shutdown();
        final boolean undeployed = processingEngine.undeployModule(uniqueModuleIdentifier);
        assertTrue(undeployed);
        assertEquals(0, processingEngine.getDeployedModules().size());

        processingEngine.destroy();
        // test double invocation
        processingEngine.destroy();

        final String state = Whitebox.getInternalState((AbstractProcessingEngine) processingEngine, "state").toString();
        assertEquals("STOPPED", state);
        moduleManager.undeployAllModules();
        assertEquals(0, moduleManager.getDeployedModulesCount());
    }

    @Test
    public void testModuleDeploymentMixedClasspathFileSystemResources() throws Exception {
        final String epsInstanceId = "esperEngine9";

        final HazelcastInputListener listener = new HazelcastInputListener(8);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-input-topic-mixed");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-output-topic-mixed");
        hazelcastReceiveTopic.addMessageListener(listener);

        final String uniqueModuleIdentifier = epsTestUtil.deployModule(FLOWS_ESPER_ENGINE_FLOW_FS_RESOURCE);
        assertEquals("com.ericsson.test_esper_engine_flow_fs_resource_1.0.0", uniqueModuleIdentifier);
        final ModuleManager moduleManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        assertEquals(1, moduleManager.getDeployedModulesCount());

        final ProcessingEngine processingEngine = epsProvider.getCreatedProcessingEngine(epsInstanceId);
        assertNotNull(processingEngine);
        assertEquals(epsInstanceId, processingEngine.getInstanceId());

        for (final String deployedModule : processingEngine.getDeployedModules()) {
            if (uniqueModuleIdentifier.equals(deployedModule)) {
                assertEquals(1, processingEngine.getDeployedModules().size());
                assertTrue(processingEngine.getDeployedModules().contains(uniqueModuleIdentifier));
                break;
            }
        }
        final String odd = "odd";
        final String even = "even";
        for (int i = 0; i < 10; i++) {
            String strToSend = odd;
            if (i < 8) {
                strToSend = even;
            }
            final Map<String, String> map = new HashMap<String, String>();
            map.put("str", strToSend);
            hazelcastSendTopic.publish(map);
        }

        Assert.assertTrue(listener.cdLatch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(8, listener.getReceivedMessages().size());

        hzI.getLifecycleService().shutdown();
        final boolean undeployed = processingEngine.undeployModule(uniqueModuleIdentifier);
        assertTrue(undeployed);
        assertEquals(0, processingEngine.getDeployedModules().size());

        processingEngine.destroy();
        // test double invocation
        processingEngine.destroy();

        final String state = Whitebox.getInternalState((AbstractProcessingEngine) processingEngine, "state").toString();
        assertEquals("STOPPED", state);
        moduleManager.undeployAllModules();
        assertEquals(0, moduleManager.getDeployedModulesCount());
    }

    @Test
    public void testGetEngineApi() {
        try {
            epsProvider.getCreatedProcessingEngine(null);
            fail();
        } catch (final IllegalArgumentException iae) {
            Assert.assertTrue(true);
        }
    }

}