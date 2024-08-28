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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;

@RunWith(Arquillian.class)
public class InvalidDirectoryForModuleDeploymentJeeTest {

    private static final String MODULE_INVALID_ONE = "flows/dummy_module_one.xml";

    @Deployment(name = "InvalidDirectoryForModuleDeploymentJeeTest")
    public static WebArchive createDeployment() {

        final WebArchive archive = Artifact.getEpsArchive().addClass(InvalidDirectoryForModuleDeploymentJeeTest.class)
                .addClass(JeeTestEventDuplicatorComponent.class).addAsResource(MODULE_INVALID_ONE).addClass(JeeTestPassThroughEventHandler.class);
        // configure with invalid directory
        archive.addAsResource("EpsConfiguration_invalidDeployDir.properties", "EpsConfiguration.properties");
        return archive;
    }

    @Test(expected = IllegalStateException.class)
    @InSequence(1)
    public void test_deploy_modules() throws Exception {
        Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_INVALID_ONE);
    }
}
