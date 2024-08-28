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
package com.ericsson.component.aia.services.eps.core.integration.jee;

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
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

@RunWith(Arquillian.class)
public class SinglePathJeeTest {

    private static final String MODULE_TO_TEST = "flows/jee_flow_single_path.xml";

    @Deployment(name = "SinglePathJeeTestWar")
    public static Archive<?> createTestArchive() {
        final WebArchive war = Artifact.getEpsArchive().addClass(SinglePathJeeTest.class).addClass(JeeTestEventDuplicatorComponent.class)
                .addAsResource(MODULE_TO_TEST);
        war.addAsResource("EpsConfiguration.properties");
        return war;
    }

    @Test
    @InSequence(1)
    public void test_deploy_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final boolean moduleCopied = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST);
        Assert.assertTrue(moduleCopied);
        final boolean waited = Artifact.wait(10 * 1000);
        Assert.assertTrue(waited);
    }

    @Test
    @InSequence(3)
    public void test_undeploy_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();
        Assert.assertEquals(0, modulesManager.getDeployedModulesCount());
    }

    @Test
    @InSequence(2)
    public void test_use_module() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(5);
        final HazelcastInstance hazelcastInstance = Artifact.createHazelcastInstance();
        final ITopic<Object> hazelcastSendTopic = hazelcastInstance.getTopic("eps-topic1");
        final ITopic<Object> hazelcastReceiveTopic = hazelcastInstance.getTopic("eps-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());
        hazelcastSendTopic.publish("1");
        Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());

        hazelcastInstance.getLifecycleService().shutdown();
    }

}