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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;

@RunWith(Arquillian.class)
public class ModuleDeploymentJeeTest {

    private static final String MODULE_TO_TEST_PATHS_IN_PARALLEL = "flows/dummy_module_one.xml";
    private static final String MODULE_TO_TEST_PATHS_IN_SEQUENCE = "flows/dummy_module_two.xml";

    @Deployment(name = "ModuleDeploymentJeeTestWar")
    public static WebArchive createDeployment() {
        final WebArchive archive = Artifact.getEpsArchive().addClass(ModuleDeploymentJeeTest.class).addClass(JeeTestEventDuplicatorComponent.class)
                .addAsResource(MODULE_TO_TEST_PATHS_IN_PARALLEL).addClass(JeeTestPassThroughEventHandler.class)
                .addAsResource(MODULE_TO_TEST_PATHS_IN_SEQUENCE);
        archive.addAsResource("EpsConfiguration.properties");
        return archive;
    }

    @Test
    @InSequence(1)
    public void test_deploy_modules() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final boolean moduleCopied1 = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST_PATHS_IN_PARALLEL);
        final boolean moduleCopied2 = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST_PATHS_IN_SEQUENCE);
        assertTrue(moduleCopied1);
        assertTrue(moduleCopied2);
        final boolean waited = Artifact.wait(10 * 1000);
        assertTrue(waited);
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        assertEquals(2, modulesManager.getDeployedModulesCount());
    }

    @Test
    @InSequence(2)
    public void test_undeploy_modules() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();
        assertEquals(0, modulesManager.getDeployedModulesCount());
    }
}
