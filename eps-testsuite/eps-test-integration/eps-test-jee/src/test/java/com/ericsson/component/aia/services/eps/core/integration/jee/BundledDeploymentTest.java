/**
 *
 */
package com.ericsson.component.aia.services.eps.core.integration.jee;

import static java.nio.file.StandardCopyOption.*;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.*;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.cdi.JeeTestCdiEvent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 * @author esarlag
 * 
 */
@RunWith(Arquillian.class)
public class BundledDeploymentTest {

    private static final String EAR_FLOW_MULTI_OUTPUT_NO_ERROR = "flows/jee_flow_one_input_multi_outputs.xml";
    private static final String EAR_FLOW_MULTI_OUTPUT_WITH_ERROR = "flows/jee_flow_one_input_multi_outputs_throws_exception.xml";

    private static final Logger LOG = LoggerFactory.getLogger(BundledDeploymentTest.class);

    @Deployment
    public static Archive<?> createTestArchive() {

        final EnterpriseArchive ear = buildEarUsingEps("bundleddeploymenttest.ear", JeeTestEventDuplicatorComponent.class,
                "EpsConfiguration_multi_output.properties");

        return ear;
    }

    private static EnterpriseArchive buildEarUsingEps(final String archiveName, final Class<?> handlerClass, final String epsConfigFile) {

        final File archiveFile = Artifact.resolveArtifactWithoutDependencies(Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?");
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact " + Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR);
        }

        final WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class, archiveFile);
        war.addAsResource("EpsConfiguration.properties");

        final JavaArchive jarLib = ShrinkWrap.create(JavaArchive.class, "test.jar").addClass(BundledDeploymentTest.class).addClass(handlerClass);
        jarLib.addClass(Artifact.class);
        jarLib.addClass(HazelcastInputListener.class);
        jarLib.addClass(MessagingTestListener.class);
        jarLib.addClass(EPSConfigurationLoader.class);
        jarLib.addClass(EpsUtil.class);
        jarLib.addAsResource(EAR_FLOW_MULTI_OUTPUT_NO_ERROR);
        jarLib.addAsResource(EAR_FLOW_MULTI_OUTPUT_WITH_ERROR);
        jarLib.addAsResource(epsConfigFile, "EpsConfiguration.properties");
        jarLib.addAsManifestResource("META-INF/beans.xml", "beans.xml");

        final JavaArchive jarEjb = ShrinkWrap.create(JavaArchive.class, "test-ejb.jar");
        jarEjb.addClass(JeeTestCdiEvent.class);

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, archiveName);

        ear.addAsLibraries(Maven.resolver().loadPomFromFile("pom4test.xml").importRuntimeDependencies().resolve().withTransitivity().asFile())
                .addAsLibraries(jarLib);

        ear.addAsManifestResource("MANIFEST3x.MF", "MANIFEST.MF");
        ear.addAsManifestResource("META-INF/beans.xml", "beans.xml");
        ear.addAsManifestResource("jboss-deployment-structure_bundledDeployment.xml", "jboss-deployment-structure.xml");
        ear.addAsModules(jarEjb);
        ear.addAsModules(war);

        return ear;
    }

    public boolean copyFlowFile(final String testFile, final InputStream inputStream, final String folderName) throws IOException {
        final int indexOfSlash = testFile.lastIndexOf("/");
        String simpleFileName = testFile;
        if (indexOfSlash != -1) {
            simpleFileName = testFile.substring(indexOfSlash + 1, testFile.length());
            LOG.debug("Simple file name is {}", simpleFileName);
        }
        simpleFileName = simpleFileName.replace(".xml", "");

        if (inputStream == null) {
            throw new IllegalArgumentException("Was not able to find " + testFile + " in classpath!");
        }
        final File watchedFolder = new File(folderName);
        if (!watchedFolder.exists() || !watchedFolder.isDirectory()) {
            throw new IllegalStateException(folderName + " is not valid folder!");
        }
        final Random random = new Random();
        final String finalFileNameRoot = simpleFileName + "_eps_module_" + random.nextInt(1000) + "_" + System.currentTimeMillis();
        final String tmpfileName = finalFileNameRoot + ".tmp";
        final String xmlFileName = finalFileNameRoot + ".xml";
        LOG.debug("Trying to create file {} in folder {}", tmpfileName, folderName);
        final File tmpDestinationFile = new File(watchedFolder, tmpfileName);
        final File xmlDestinationFile = new File(watchedFolder, xmlFileName);
        final Path tmpDestinationPath = tmpDestinationFile.toPath();
        final Path xmlDestinationPath = xmlDestinationFile.toPath();
        final long bytesWritten = Files.copy(inputStream, tmpDestinationPath, StandardCopyOption.REPLACE_EXISTING);
        LOG.debug("Successfully wrote {} bytes into temporary file. Now renaming to {}", bytesWritten, xmlFileName);
        Files.move(tmpDestinationPath, xmlDestinationPath, REPLACE_EXISTING, ATOMIC_MOVE);
        LOG.debug("Moved file to {}", xmlFileName);
        inputStream.close();
        return bytesWritten > 1;
    }

    public void deployFlowFile(final String flowFile, final String configurationFileName) throws IOException {
        final Properties properties = new Properties();
        InputStream propertiesIs = null;
        InputStream flowIs = null;
        boolean copied = false;
        try {
            LOG.debug("Loading properties from {}", configurationFileName);
            propertiesIs = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath", configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }
            final String folderName = properties.getProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);
            flowIs = Thread.currentThread().getContextClassLoader().getResourceAsStream(flowFile);
            copied = this.copyFlowFile(flowFile, flowIs, folderName);
        } catch (final IOException ie) {
            LOG.error("loadProperties IOexception on loading file {} from classpath", ie);
            throw ie;
        } finally {
            Assert.assertNotNull("Configuration file stream is null ", propertiesIs);
            flowIs.close();
            propertiesIs.close();
            Assert.assertTrue("File has not been copied ", copied);
        }
    }

    @Test
    @InSequence(1)
    public void test_deploy_invalid_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        deployFlowFile(EAR_FLOW_MULTI_OUTPUT_WITH_ERROR, "EpsConfiguration.properties");

        final boolean waited = Artifact.wait(5 * 1000);
        Assert.assertTrue("Wait interrupted ", waited);
    }

    @Test
    @InSequence(2)
    public void test_deploy_module() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        deployFlowFile(EAR_FLOW_MULTI_OUTPUT_NO_ERROR, "EpsConfiguration.properties");

        final boolean waited = Artifact.wait(5 * 1000);
        Assert.assertTrue("Wait interrupted ", waited);
    }

    @Test
    @InSequence(3)
    public void test_use_module() throws InterruptedException {

        final HazelcastInputListener listener1 = new HazelcastInputListener(5);
        final HazelcastInputListener listener2 = new HazelcastInputListener(5);
        final HazelcastInstance hazelcastInstance = Artifact.createHazelcastInstance();
        final ITopic<Object> hazelcastSendTopic = hazelcastInstance.getTopic("eps-topic-in");
        final ITopic<Object> hazelcastReceiveTopic1 = hazelcastInstance.getTopic("eps-topic1-out");
        final ITopic<Object> hazelcastReceiveTopic2 = hazelcastInstance.getTopic("eps-topic2-out");
        hazelcastReceiveTopic1.addMessageListener(listener1);
        hazelcastReceiveTopic2.addMessageListener(listener2);

        hazelcastSendTopic.publish("1");
        Assert.assertTrue(listener1.latch.await(5, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener1.getReceivedMessages().size());
        Assert.assertEquals(5, listener2.getReceivedMessages().size());

        hazelcastInstance.getLifecycleService().shutdown();

        Artifact.cleanupConfiguredXMLFolder();
    }

}
