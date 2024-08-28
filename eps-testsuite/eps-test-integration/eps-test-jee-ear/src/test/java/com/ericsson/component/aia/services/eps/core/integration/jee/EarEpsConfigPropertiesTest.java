package com.ericsson.component.aia.services.eps.core.integration.jee;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;

/**
 * Test scenario where two EPS instances load configuration properties from files, OS or JVM.
 * 
 * @author esarlag
 * 
 */
@RunWith(Arquillian.class)
public class EarEpsConfigPropertiesTest {

    private static final Logger LOG = LoggerFactory.getLogger(EarEpsConfigPropertiesTest.class);

    // each eps instance with stats enabled creates a flowsDecployed counter
    private static final int NUM_EPS_INSTANCES_PER_FOLDER = 1;

    // EPS will create a default meter "<componentname>.eventsReceived" per component
    private static final int NUM_COMPONENTS_EAR_1 = 3;

    // EPS will create a default meter "<componentname>.eventsReceived" per component
    private static final int NUM_COMPONENTS_EAR_2 = 3;

    // the hz output adapter has a counter and a meter, the pass through component has 1 meter
    private static final int NUM_COMPONENT_SPECIFIC_METRICS_EAR_1 = 3;

    // the hz output adapter has a counter and a meter, the pass through component has 1 meter
    private static final int NUM_COMPONENT_SPECIFIC_METRICS_EAR_2 = 3;

    // for this test class ears are configured to output to the same directory
    // note: these must change if flow.xml used changes
    private static final int EXPECTED_NUMBER_OF_CSV_FILES = NUM_EPS_INSTANCES_PER_FOLDER + NUM_COMPONENTS_EAR_1 + NUM_COMPONENT_SPECIFIC_METRICS_EAR_1
            + NUM_COMPONENTS_EAR_2 + NUM_COMPONENT_SPECIFIC_METRICS_EAR_2;

    @Deployment(name = "EarEpsConfigPropertiesTest1", order = 1)
    public static Archive<?> createTestArchiveEarEpsConfigPropertiesTest1() throws IOException {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarEpsConfigPropertiesTest.class.getSimpleName() + "/properties/firstEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarEpsConfigPropertiesTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarEpsConfigPropertiesTest.class.getSimpleName() + "/flows/firstEar/flow.xml";
        final String jbossDeploymentFileName = EarEpsConfigPropertiesTest.class.getSimpleName()
                + "/application/firstEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.<Class<?>> asList(JeeTestPassThroughEventHandler.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarEpsConfigPropertiesTest.class.getSimpleName() + "1.ear", warCoordinates, flowFileName,
        		sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, EarEpsConfigPropertiesTest.class, handlerClasses, null);

        createOutputDirectories();

        ear.setApplicationXML(EarEpsConfigPropertiesTest.class.getSimpleName() + "/application/firstEar/application.xml");

        return ear;

    }

    private static void createOutputDirectories() throws IOException {

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest createOutputDirectories\n*\n*");

        final String epsConfigFileName1 = EarEpsConfigPropertiesTest.class.getSimpleName() + "/properties/firstEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String epsConfigFileName2 = EarEpsConfigPropertiesTest.class.getSimpleName() + "/properties/secondEar/"
                + Artifact.CONFIGURATION_FILE_NAME;

        Artifact.createCsvOutputLocation(epsConfigFileName1);
        Artifact.createModuleOutputLocation(epsConfigFileName1);
        Artifact.createCsvOutputLocation(epsConfigFileName2);
        Artifact.createModuleOutputLocation(epsConfigFileName2);

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest createOutputDirectories\n*\n*");
    }

    @Deployment(name = "EarEpsConfigPropertiesTest2", order = 2)
    public static Archive<?> createTestArchiveEarEpsConfigPropertiesTest2() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarEpsConfigPropertiesTest.class.getSimpleName() + "/properties/secondEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarEpsConfigPropertiesTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarEpsConfigPropertiesTest.class.getSimpleName() + "/flows/secondEar/flow.xml";
        final String jbossDeploymentFileName = EarEpsConfigPropertiesTest.class.getSimpleName()
                + "/application/secondEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.<Class<?>> asList(JeeTestPassThroughEventHandler.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarEpsConfigPropertiesTest.class.getSimpleName() + "2.ear", warCoordinates, flowFileName,
        		sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, EarEpsConfigPropertiesTest.class, handlerClasses, null);

        ear.setApplicationXML(EarEpsConfigPropertiesTest.class.getSimpleName() + "/application/secondEar/application.xml");

        return ear;

    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("EarEpsConfigPropertiesTest1")
    public void testDeployEarEpsConfigPropertiesTest1() throws IOException, InterruptedException {
        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest::testDeployEarEpsConfigPropertiesTest1 STARTED\n*\n*");

        final boolean waited = Artifact.waitForModuleDeploy(1, 5);

        Assert.assertTrue(waited);

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest::testDeployEarEpsConfigPropertiesTest1 FINISHED\n*\n*");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("EarEpsConfigPropertiesTest2")
    public void testDeployEarEpsConfigPropertiesTest2() throws IOException, InterruptedException {
        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest::testDeployEarEpsConfigPropertiesTest2 STARTED\n*\n*");

        final boolean waited = Artifact.waitForModuleDeploy(1, 5);

        Assert.assertTrue(waited);

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest::testDeployEarEpsConfigPropertiesTest2 FINISHED\n*\n*");
    }

    @Test
    @InSequence(3)
    @OperateOnDeployment("EarEpsConfigPropertiesTest2")
    public void testStatisticsCsvOutput() throws IOException, InterruptedException {
        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest::testStatisticsCsvOutput STARTED\n*\n*");

        final List<File> csvFiles = Artifact.getCsvFiles();

        Assert.assertEquals("csv files should have been created", EXPECTED_NUMBER_OF_CSV_FILES, csvFiles.size());

        LOG.info("\n*\n*\n* EarEpsConfigPropertiesTest::testStatisticsCsvOutput FINISHED\n*\n*");
    }

}
