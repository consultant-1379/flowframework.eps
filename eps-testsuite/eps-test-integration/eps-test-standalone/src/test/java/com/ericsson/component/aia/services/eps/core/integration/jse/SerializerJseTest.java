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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.io.adapter.util.DefaultJavaSerializer;
import com.ericsson.component.aia.services.eps.io.adapter.util.ObjectSerializer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class SerializerJseTest {

    private static final String FLOWS_KRYO_SERIALIZER_DESERIALIZER = "/flows/kryo_serializer_deserializer_flow.xml";
    private static final String FLOWS_DEFAULT_SERIALIZER = "/flows/default_serializer_flow.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();
    private EpsInstanceManager epsInstanceManager;
    private final ObjectSerializer serializer = new DefaultJavaSerializer();

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        epsInstanceManager = epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
        epsInstanceManager = null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_send_receive_event_as_bytes_simple_event() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(1);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);
        final String identifier = epsTestUtil.deployModule(FLOWS_DEFAULT_SERIALIZER);
        Assert.assertNotNull(identifier);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        final String event = "test1";
        final byte[] bytePresentation = serializer.objectToBytes(event);
        hazelcastSendTopic.publish(event);
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(1, listener.getReceivedMessages().size());
        final byte[] receivedBytes = (byte[]) listener.getReceivedMessages().get(0);
        Assert.assertTrue(Arrays.equals(bytePresentation, receivedBytes));
        hazelcastReceiveTopic.removeMessageListener(listener);
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
        hzI.getLifecycleService().shutdown();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void test_send_receive_event_serialize_deserialize() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(1);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("eps-topic11");
        final ITopic hazelcastReceiveTopic = hzI.getTopic("eps-topic22");
        hazelcastReceiveTopic.addMessageListener(listener);
        final String identifier = epsTestUtil.deployModule(FLOWS_KRYO_SERIALIZER_DESERIALIZER);
        Assert.assertNotNull(identifier);
        final ModuleManager moduleManager = epsInstanceManager.getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        final String event = "test1";
        hazelcastSendTopic.publish(event);
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(1, listener.getReceivedMessages().size());
        final Object received = listener.getReceivedMessages().get(0);
        Assert.assertEquals(event, received);
        hazelcastReceiveTopic.removeMessageListener(listener);
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
        hzI.getLifecycleService().shutdown();
    }
}