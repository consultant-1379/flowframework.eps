package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class EsperInOutPortFlowTest {

    private static final String FLOWS_INOUT_PORT_FLOW = "/flows/esper_inout_port_flow.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    @Before
    public void setup() throws Exception {
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    @Test
    public void test() throws Exception {
        final HazelcastInputListener oddListener = new HazelcastInputListener(10);
        Assert.assertEquals(0, oddListener.getReceivedMessages().size());

        final HazelcastInputListener oddListener1 = new HazelcastInputListener(10);
        Assert.assertEquals(0, oddListener1.getReceivedMessages().size());

        final HazelcastInputListener evenListener = new HazelcastInputListener(5);
        Assert.assertEquals(0, evenListener.getReceivedMessages().size());

        final HazelcastInputListener evenListener1 = new HazelcastInputListener(5);
        Assert.assertEquals(0, evenListener1.getReceivedMessages().size());

        final HazelcastInputListener evenListener2 = new HazelcastInputListener(5);
        Assert.assertEquals(0, evenListener2.getReceivedMessages().size());

        final HazelcastInstance hzI = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzI.getTopic("numbersIn");
        final ITopic hazelcastReceiveOddTopic = hzI.getTopic("oddOut");
        final ITopic hazelcastReceiveOddTopic1 = hzI.getTopic("oddOut1");
        hazelcastReceiveOddTopic.addMessageListener(oddListener);
        hazelcastReceiveOddTopic1.addMessageListener(oddListener1);

        final ITopic hazelcastReceiveEvenTopic = hzI.getTopic("evenOut");
        hazelcastReceiveEvenTopic.addMessageListener(evenListener);

        final ITopic hazelcastReceiveEvenTopic1 = hzI.getTopic("evenOut1");
        hazelcastReceiveEvenTopic1.addMessageListener(evenListener1);

        final ITopic hazelcastReceiveEvenTopic2 = hzI.getTopic("evenOut2");
        hazelcastReceiveEvenTopic2.addMessageListener(evenListener2);

        epsTestUtil.deployModule(FLOWS_INOUT_PORT_FLOW);
        final ModuleManager moduleManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                final Map<String, String> evenMap = new HashMap<String, String>();
                evenMap.put("str", "even");
                hazelcastSendTopic.publish(evenMap);
            }
            final Map<String, String> oddMap = new HashMap<String, String>();
            oddMap.put("str", "odd");
            hazelcastSendTopic.publish(oddMap);
        }
        Assert.assertTrue(oddListener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, oddListener.getReceivedMessages().size());

        Assert.assertTrue(oddListener1.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(10, oddListener1.getReceivedMessages().size());

        Assert.assertTrue(evenListener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, evenListener.getReceivedMessages().size());

        Assert.assertTrue(evenListener1.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, evenListener1.getReceivedMessages().size());

        Assert.assertTrue(evenListener2.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, evenListener2.getReceivedMessages().size());

        hzI.getLifecycleService().shutdown();
        moduleManager.undeployAllModules();
        Assert.assertEquals(0, moduleManager.getDeployedModulesCount());
    }

}
