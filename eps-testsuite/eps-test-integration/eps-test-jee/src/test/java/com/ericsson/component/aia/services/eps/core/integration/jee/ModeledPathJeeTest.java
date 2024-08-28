/*------------------------------------------------------------------------------
 *******************************************************************************
 * © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jee;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent.*;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;

@RunWith(Arquillian.class)
public class ModeledPathJeeTest {

    private static final String MODULE_TO_TEST = "flows/jee_flow_modeled_event.xml";
    private static final String MODULE_TO_TEST_NEGATIVE = "flows/jee_flow_modeled_event_no_subclass.xml";

    @Inject
    private TestModeledEventGenerator generator;

    private TestModeledEventReceiver receiver;

    @Deployment(name = "ModelPathJeeTestWar")
    public static Archive<?> createTestArchive() {
        final WebArchive war = Artifact.getEpsArchive().addClass(ModeledPathJeeTest.class).addClass(TestModeledEventSwapperComponent.class)
                .addClass(TestInputModeledEvent.class).addClass(TestInputModeledEventSubclass.class).addClass(TestOutputModeledEvent.class)
                .addClass(TestModeledEventGenerator.class).addClass(TestModeledEventReceiver.class).addAsResource(MODULE_TO_TEST)
                .addAsResource(MODULE_TO_TEST_NEGATIVE);
        war.addAsResource("EpsConfiguration_statsAsCsv.properties", "EpsConfiguration.properties");
        war.addAsWebInfResource("WEB-INF/channel-resources-jms.xml");
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
    public void test_subclasses_accepted() throws Exception {

        generator = new TestModeledEventGenerator();
        receiver = new TestModeledEventReceiver();
        receiver.subscribeForEvents();

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());

        final String msg = "MY MODELED MESSAGE SUBCLASS";
        generator.sendModeledMessage(msg, TestInputModeledEventSubclass.class);

        Artifact.wait(3 * 1000);

        receiver.unsubscribe();

        Assert.assertEquals(1, receiver.getReceived().size());
        Assert.assertEquals(msg, receiver.getReceived().iterator().next());

    }

    @Test
    @Ignore
    @InSequence(4)
    public void test_subclasses_notAccepted() throws Exception {

        Artifact.cleanupConfiguredXMLFolder();

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();
        Assert.assertEquals(0, modulesManager.getDeployedModulesCount());

        final boolean moduleCopied = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST_NEGATIVE);
        Assert.assertTrue(moduleCopied);
        final boolean waited = Artifact.wait(10 * 1000);
        Assert.assertTrue(waited);

        generator = new TestModeledEventGenerator();
        receiver = new TestModeledEventReceiver();
        receiver.subscribeForEvents();

        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());

        final String msg = "MY MODELED MESSAGE SUBCLASS NOT ACCEPTED";
        generator.sendModeledMessage(msg, TestInputModeledEventSubclass.class);

        Artifact.wait(3 * 1000);

        receiver.unsubscribe();

        Assert.assertEquals(0, receiver.getReceived().size());
    }

    @Test
    @InSequence(5)
    public void test_undeploy_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();
        Assert.assertEquals(0, modulesManager.getDeployedModulesCount());
    }

    @Test
    @InSequence(2)
    public void test_use_module() throws Exception {

        generator = new TestModeledEventGenerator();
        receiver = new TestModeledEventReceiver();
        receiver.subscribeForEvents();

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());

        final String msg = "MY MODELED MESSAGE";
        generator.sendModeledMessage(msg);

        Artifact.wait(3 * 1000);

        receiver.unsubscribe();

        Assert.assertEquals(1, receiver.getReceived().size());
        Assert.assertEquals(msg, receiver.getReceived().iterator().next());

    }

}