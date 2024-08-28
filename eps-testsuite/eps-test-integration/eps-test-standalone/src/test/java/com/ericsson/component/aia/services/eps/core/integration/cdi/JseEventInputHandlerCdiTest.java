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
package com.ericsson.component.aia.services.eps.core.integration.cdi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.*;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.jse.TestUndeploymentComponent;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.pe.java.util.EpsCdiInstanceManager;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

public class JseEventInputHandlerCdiTest {

    private static final String FLOWS_SIMPLE_CDI_NONCDI_FLOW = "/flows/simple_cdi_noncdi_flow.xml";
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
    public void test_install_cdi_javacomponent() throws Exception {
        TestUndeploymentComponent.clear();

        final WeldContainer weldContainer = new Weld().initialize();
        final BeanManager beanManager = weldContainer.getBeanManager();
        final EpsCdiInstanceManager cdiInstanceManager = EpsCdiInstanceManager.getInstance();
        cdiInstanceManager.setBeanManager(beanManager);

        final HazelcastInputListener listener = new HazelcastInputListener(5);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        final HazelcastInstance hazelcastInstance = epsTestUtil.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hazelcastInstance.getTopic("eps-topic1");
        final ITopic hazelcastReceiveTopic = hazelcastInstance.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);
        epsTestUtil.deployModule(FLOWS_SIMPLE_CDI_NONCDI_FLOW);

        final ModuleManager moduleManager = epsTestUtil.getEpsInstance().getModuleManager();
        Assert.assertEquals(1, moduleManager.getDeployedModulesCount());

        hazelcastSendTopic.publish("test1");
        Assert.assertTrue(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());

        // now send control event and see that nothing passes through
        listener.clear(1);
        final ControlEvent controlEvent = new ControlEvent(TestCdiControlEventSenderComponent.CONTROL_EVENT_PAUSE_TYPE);
        final int modulesReceived = moduleManager.sendControlEventToAllModules(controlEvent);
        Assert.assertEquals(1, modulesReceived);

        // now everything should be paused... try to send few events
        hazelcastSendTopic.publish("test1");
        hazelcastSendTopic.publish("test2");
        hazelcastSendTopic.publish("test3");

        // checking count for CDI events
        Assert.assertEquals(5, TestCdiControlEventSenderComponent.cdiEventCount);

        Assert.assertFalse(listener.cdLatch.await(1, TimeUnit.SECONDS));
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        hazelcastInstance.getLifecycleService().shutdown();
    }
}
