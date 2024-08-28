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

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
public class MultiplePathsJeeTest {

    private static final String MODULE_TO_TEST_PATHS_IN_PARALLEL = "flows/jee_flow_multi_paths_in_parallel.xml";
    private static final String MODULE_TO_TEST_PATHS_IN_SEQUENCE = "flows/jee_flow_multi_paths_in_sequence.xml";

    @Deployment(name = "MultiplePathsJeeTestWar")
    public static WebArchive createDeployment() {
        final WebArchive archive = Artifact.getEpsArchive().addClass(MultiplePathsJeeTest.class).addClass(JeeTestEventDuplicatorComponent.class)
                .addAsResource(MODULE_TO_TEST_PATHS_IN_PARALLEL).addAsResource(MODULE_TO_TEST_PATHS_IN_SEQUENCE);
        archive.addAsResource("EpsConfiguration.properties");
        return archive;
    }

    @Test
    @InSequence(1)
    public void test_deploy_modules() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final boolean moduleParallelCopied = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST_PATHS_IN_PARALLEL);
        final boolean moduleSequenceCopied = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST_PATHS_IN_SEQUENCE);
        assertTrue(moduleParallelCopied);
        assertTrue(moduleSequenceCopied);
        final boolean waited = Artifact.wait(15 * 1000);
        assertTrue(waited);
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        assertEquals(2, modulesManager.getDeployedModulesCount());
    }

    @Test
    @InSequence(4)
    public void test_undeploy_modules() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();
        assertEquals(0, modulesManager.getDeployedModulesCount());
    }

    @Test
    @InSequence(2)
    public void test_use_module_paths_in_parallel() throws Exception {
        final HazelcastInputListener listener1 = new HazelcastInputListener(3);
        final HazelcastInputListener listener2 = new HazelcastInputListener(5);
        final HazelcastInstance hazelcastInstance = Artifact.createHazelcastInstance();
        final ITopic<Object> topic1In = hazelcastInstance.getTopic("eps-topic1-in");
        final ITopic<Object> topic1Out = hazelcastInstance.getTopic("eps-topic1-out");
        final ITopic<Object> topic2In = hazelcastInstance.getTopic("eps-topic2-in");
        final ITopic<Object> topic2Out = hazelcastInstance.getTopic("eps-topic2-out");
        topic1Out.addMessageListener(listener1);
        topic2Out.addMessageListener(listener2);

        topic1In.publish("1");
        topic2In.publish("1");
        assertTrue(listener1.latch.await(5, TimeUnit.SECONDS));
        assertTrue(listener2.latch.await(5, TimeUnit.SECONDS));
        assertEquals(3, listener1.getReceivedMessages().size());
        assertEquals(5, listener2.getReceivedMessages().size());

        hazelcastInstance.getLifecycleService().shutdown();
    }

    @Test
    @InSequence(3)
    public void test_use_module_paths_in_sequence() throws Exception {
        final HazelcastInputListener listener = new HazelcastInputListener(6);
        final HazelcastInstance hazelcastInstance = Artifact.createHazelcastInstance();
        final ITopic<Object> topicIn = hazelcastInstance.getTopic("eps-topic-in");
        final ITopic<Object> topicOut = hazelcastInstance.getTopic("eps-topic-out");
        topicOut.addMessageListener(listener);

        topicIn.publish("1");
        assertTrue(listener.latch.await(10, TimeUnit.SECONDS));
        assertEquals(6, listener.getReceivedMessages().size());

        hazelcastInstance.getLifecycleService().shutdown();
    }

}
