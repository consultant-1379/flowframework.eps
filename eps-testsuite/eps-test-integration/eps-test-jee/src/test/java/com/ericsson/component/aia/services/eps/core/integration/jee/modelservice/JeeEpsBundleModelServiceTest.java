package com.ericsson.component.aia.services.eps.core.integration.jee.modelservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.bundle.EpsBundleServlet;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;

@RunWith(Arquillian.class)
public class JeeEpsBundleModelServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(JeeEpsBundleModelServiceTest.class);

    private static final String MODULE_TO_TEST_DUMMY_THREE = "flows/dummy_module_three.xml";

    @Deployment(name = "JeeEpsBundleModelServiceWar")
    public static Archive<?> createTestArchive() throws IOException {

        LOG.info("START @Deployment createTestArchive");

        final File archiveFile = Artifact.resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?");
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact " + Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR);
        }

        final WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class, archiveFile);
        war.setWebXML("WEB-INF/web.xml");
        war.addAsWebInfResource("META-INF/beans.xml");

        war.addAsLibraries(Artifact.resolveArtifactWithDependencies(Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_DIST_JAR));
        war.addAsResource(MODULE_TO_TEST_DUMMY_THREE);
        war.addClass(JeeTestEventDuplicatorComponent.class);
        war.addClass(JeeEpsBundleModelServiceTest.class);

        war.addClass(EpsBundleServlet.class);
        war.addClass(Artifact.class);
        war.addClass(HazelcastInputListener.class);
        war.addClass(MessagingTestListener.class);
        war.addClass(JeeTestPassThroughEventHandler.class);
        war.addAsResource("modelsDeploymentEvent_transport.xml", "modelsDeploymentEvent_transport.xml");

        war.addAsResource(new StringAsset(EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN + "=//" + "com.test.odd" + "/*/*"),
                "EpsConfiguration.properties");

        LOG.info("END @Deployment createTestArchive");

        return war;
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void test_deploy_module() throws Exception {
        LOG.info("START test_deploy_module");

        final boolean moduleCopied = Artifact.runMdtAndDeployTestModels("/" + MODULE_TO_TEST_DUMMY_THREE);
        assertTrue(moduleCopied);

        LOG.info("END test_deploy_module");
    }

    @Test
    @InSequence(2)
    public void test_deploy_module_check() throws Exception {
        LOG.info("START test_deploy_module_check");

        final boolean deployed = Artifact.wait4deploy("/" + MODULE_TO_TEST_DUMMY_THREE, 1);
        assertTrue(deployed);

        LOG.info("END test_deploy_module_check");
    }

}
