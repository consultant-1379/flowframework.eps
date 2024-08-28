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

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;

@RunWith(Arquillian.class)
public class OutOfNamespaceDeploymentJeeTest {

    private static final Logger LOG = LoggerFactory.getLogger(OutOfNamespaceDeploymentJeeTest.class);

    private static final String MODULE_TO_TEST_DUMMY_FOUR = "flows/dummy_module_four.xml";

    @Deployment(name = "OutOfNamespaceForModuleDeploymentJeeTest")
    public static WebArchive createDeployment() throws IOException {
        LOG.trace("START @Deployment createDeployment");

        final WebArchive war = Artifact.getEpsModelServiceArchive("com.test.invalid").addClass(OutOfNamespaceDeploymentJeeTest.class)
                .addClass(JeeTestPassThroughEventHandler.class).addAsResource(MODULE_TO_TEST_DUMMY_FOUR);
        war.addAsResource("EpsConfiguration.properties");
        LOG.trace("END @Deployment createDeployment");

        return war;
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void test_deploy_modules() throws Exception {
        LOG.trace("START test_deploy_modules");

        final boolean moduleCopied1 = Artifact.runMdtAndDeployTestModels("/" + MODULE_TO_TEST_DUMMY_FOUR);
        assertTrue(moduleCopied1);

        LOG.trace("END test_deploy_modules");
    }

    @Test
    @InSequence(2)
    public void test_deploy_modules_check() throws Exception {

        LOG.trace("START test_deploy_modules_check");

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        final boolean deployed = Artifact.wait4deploy("/" + MODULE_TO_TEST_DUMMY_FOUR, 1);
        assertFalse(deployed); // should not deploy any modules for "com.invalid" namespace
        assertEquals(0, modulesManager.getDeployedModulesCount());

        LOG.trace("END test_deploy_modules_check");
    }
}
