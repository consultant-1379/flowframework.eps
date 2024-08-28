package com.ericsson.component.aia.services.eps.core.integration.jee.cdi;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

@RunWith(Arquillian.class)
public class JeeEpsCdiTest {

    private static final String MODULE_TO_TEST = "flows/cdi_flow.xml";

    @Deployment(name = "EpsJeeCdiWar")
    public static Archive<?> createTestArchive() {
        final WebArchive war = Artifact.getEpsArchive();
        war.addClass(JeeEpsCdiTest.class);
        war.addClass(JeeTestEventGeneratorComponent.class);
        war.addClass(JeeTestPassThroughEventHandler.class);
        war.addClass(JeeTestCdiEventHandler.class);
        war.addClass(JeeTestCdiEvent.class);
        war.addClass(EventProcessingLocal.class);
        war.addClass(EventProcessingBean.class);
        war.addAsResource(MODULE_TO_TEST);
        war.addAsResource("EpsConfiguration.properties");
        return war;
    }

    @Test
    @InSequence(1)
    public void test_cdi_handlers() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final boolean moduleCopied = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST);
        Assert.assertTrue(moduleCopied);
        final boolean waited = Artifact.wait(5 * 1000);
        Assert.assertTrue(waited);

        final HazelcastInputListener listener = new HazelcastInputListener(5);
        final HazelcastInstance hazelcastInstance = Artifact.createHazelcastInstance();
        final ITopic<Object> hazelcastSendTopic = hazelcastInstance.getTopic("eps-cdi-topic1");
        final ITopic<Object> hazelcastReceiveTopic = hazelcastInstance.getTopic("eps-cdi-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("cdi_event_" + UUID.randomUUID().toString());
        Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        Assert.assertEquals(2, JeeTestCdiEventHandler.cdiEventCount);

        hazelcastInstance.getLifecycleService().shutdown();
        Artifact.cleanupConfiguredXMLFolder();
    }

}
