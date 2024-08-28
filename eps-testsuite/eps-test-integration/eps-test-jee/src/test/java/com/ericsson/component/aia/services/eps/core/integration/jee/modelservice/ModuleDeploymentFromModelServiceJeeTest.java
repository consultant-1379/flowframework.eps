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
package com.ericsson.component.aia.services.eps.core.integration.jee.modelservice;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;

@RunWith(Arquillian.class)
public class ModuleDeploymentFromModelServiceJeeTest {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleDeploymentFromModelServiceJeeTest.class);
    private static final String MODULE_TO_TEST_DUMMY_ONE = "flows/dummy_module_one.xml";
    private static final String MODULE_TO_TEST_DUMMY_TWO = "flows/dummy_module_two.xml";

    private static final String MODULE_TO_TEST_OTHER_NAMESPC = "flows/jee_flow_one_input_multi_outputs.xml";

    @Deployment(name = "ModuleDeploymentFromModelServiceJeeTestWar")
    public static WebArchive createDeployment() throws IOException {
        LOG.trace("START @Deployment createDeployment");
        final WebArchive war = Artifact.getEpsModelServiceArchive("com.test.cdi").addClass(ModuleDeploymentFromModelServiceJeeTest.class)
                .addClass(JeeTestEventDuplicatorComponent.class).addAsResource(MODULE_TO_TEST_DUMMY_ONE)
                .addClass(JeeTestPassThroughEventHandler.class).addAsResource(MODULE_TO_TEST_DUMMY_TWO).addAsResource(MODULE_TO_TEST_OTHER_NAMESPC);
        LOG.trace("END @Deployment createDeployment");
        return war;
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void test_deploy_modules() throws Exception {
        LOG.trace("START test_deploy_modules");

        final boolean moduleCopied1 = Artifact.runMdtAndDeployTestModels("/" + MODULE_TO_TEST_DUMMY_ONE);
        final boolean moduleCopied2 = Artifact.runMdtAndDeployTestModels("/" + MODULE_TO_TEST_DUMMY_TWO);
        final boolean moduleCopied3 = Artifact.runMdtAndDeployTestModels("/" + MODULE_TO_TEST_OTHER_NAMESPC);

        assertTrue(moduleCopied1);
        assertTrue(moduleCopied2);
        assertTrue(moduleCopied3);

        LOG.trace("END test_deploy_modules");
    }

    @Test
    @InSequence(4)
    public void test_deploy_modules_check() throws Exception {
        LOG.trace("START test_deploy_modules_check");

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        final boolean deployed = Artifact.wait4deploy("/" + MODULE_TO_TEST_OTHER_NAMESPC, 3);

        assertFalse(deployed); // only two modules should be deployed
        assertEquals(2, modulesManager.getDeployedModulesCount());

        LOG.trace("END test_deploy_modules_check");
    }

    @Test
    @InSequence(1)
    public void test_retrieve_modules() throws Exception {
        LOG.trace("START test_retrieve_modules");
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        assertEquals(1, modulesManager.getDeployedModulesCount());
        LOG.trace("END test_retrieve_modules");
    }

    @Test
    @InSequence(2)
    public void test_undeploy_modules1() throws Exception {
        LOG.trace("START test_undeploy_modules1");
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();
        assertEquals(0, modulesManager.getDeployedModulesCount());
        LOG.trace("END test_undeploy_modules1");
    }

    @Test
    @InSequence(5)
    public void test_undeploy_modules2() throws Exception {
        LOG.trace("START test_undeploy_modules2");

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        modulesManager.undeployAllModules();

        assertEquals(0, modulesManager.getDeployedModulesCount());

        LOG.trace("END test_undeploy_modules2");
    }

}
