package com.ericsson.component.aia.services.eps.core.integration.jee.bundle;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;

@RunWith(Arquillian.class)
public class JeeEpsBundleTest {

    private static final String FLOWS_ESPER_S1_CORRELATION_FLOW_XML = "flows/esper_s1_correlation_flow.xml";

    @Deployment(name = "JeeEpsBundleWar")
    public static Archive<?> createTestArchive() {

        final File archiveFile = Artifact.resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?");
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact " + Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR);
        }

        final WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class, archiveFile);
        war.setWebXML("WEB-INF/web.xml");
        war.addAsWebInfResource("META-INF/beans.xml");
        war.addAsLibraries(Artifact.resolveArtifactWithDependencies(Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_DIST_JAR));
        war.addAsResource("EpsConfiguration.properties");
        war.addAsResource(FLOWS_ESPER_S1_CORRELATION_FLOW_XML);
        war.addClass(JeeEpsBundleTest.class);
        war.addClass(EpsBundleServlet.class);
        war.addClass(Artifact.class);
        war.addClass(HazelcastInputListener.class);
        war.addClass(MessagingTestListener.class);

        return war;
    }

    @Test
    @InSequence(1)
    public void test_deploy_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final boolean moduleCopied = Artifact.copyXmlContentToConfiguredFolder("/" + FLOWS_ESPER_S1_CORRELATION_FLOW_XML);
        Assert.assertTrue(moduleCopied);
        final boolean waited = Artifact.wait(30000);
        Assert.assertTrue(waited);
        Artifact.cleanupConfiguredXMLFolder();
    }
}
