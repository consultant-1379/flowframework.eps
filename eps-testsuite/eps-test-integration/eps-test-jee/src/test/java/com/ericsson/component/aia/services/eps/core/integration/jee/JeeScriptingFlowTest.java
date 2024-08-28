/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
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
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

@RunWith(Arquillian.class)
public class JeeScriptingFlowTest {

    private static final String FLOW_TO_TEST = "flows/test_jvm_script_flow.xml";
    private static final String SCRIPT_TO_TEST = "scripts/triple_messages.py";

    @Deployment(name = "JeeScriptingFlowTest")
    public static Archive<?> createTestArchive() {
        final WebArchive war = Artifact.getEpsArchive();
        war.addClass(JeeScriptingFlowTest.class);
        war.addAsResource("EpsConfiguration.properties");
        war.addAsResource(FLOW_TO_TEST);
        war.addAsResource(SCRIPT_TO_TEST);
        return war;
    }

    @Test
    public void test_deploy_tripling_jvm_scripting_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();

        final boolean flowCopied = Artifact.copyXmlContentToConfiguredFolder("/" + FLOW_TO_TEST);
        Assert.assertTrue(flowCopied);
        final boolean waited = Artifact.wait(30000);
        Assert.assertTrue(waited);

        final HazelcastInputListener listener = new HazelcastInputListener(3);
        Assert.assertEquals(0, listener.getReceivedMessages().size());

        final HazelcastInstance hazelcastInstance = Artifact.createHazelcastInstance();
        final ITopic<Object> hazelcastSendTopic = hazelcastInstance.getTopic("eps-topic-jython-in");
        final ITopic<Object> hazelcastReceiveTopic = hazelcastInstance.getTopic("eps-topic-jython-out");
        hazelcastReceiveTopic.addMessageListener(listener);

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());

        hazelcastSendTopic.publish("200");
        Assert.assertTrue(listener.latch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener.getReceivedMessages().size());
        listener.clear(30);
        Assert.assertEquals(0, listener.getReceivedMessages().size());
        for (int i = 0; i < 10; i++) {
            hazelcastSendTopic.publish("300");
        }
        Assert.assertTrue(listener.latch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(30, listener.getReceivedMessages().size());
        hazelcastInstance.getLifecycleService().shutdown();
        Artifact.cleanupConfiguredXMLFolder();

    }

}