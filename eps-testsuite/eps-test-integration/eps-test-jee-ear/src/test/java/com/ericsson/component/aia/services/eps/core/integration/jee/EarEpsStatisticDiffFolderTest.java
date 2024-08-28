package com.ericsson.component.aia.services.eps.core.integration.jee;

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

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;

/**
 * Test scenario where 2 different EARs reports statistics into different folders.
 * 
 * @author eaudluc
 * 
 */
@RunWith(Arquillian.class)
public class EarEpsStatisticDiffFolderTest {

    private static final Logger LOG = LoggerFactory.getLogger(EarEpsStatisticDiffFolderTest.class);

    // each eps instance with stats enabled creates a flowsDeployed counter
    // each outputs to a different directory
    // EarEpsStatisticDiffFolderTest3 has stats disabled
    private static final int NUM_EPS_INSTANCES_STATS_ENABLED = 1;

    // EPS will create a default meter "<componentname>.eventsReceived" per component
    private static final int NUM_COMPONENTS_EAR_1 = 3;

    // the hz output adapter has a counter and a meter, the pass through component has 1 meter
    private static final int NUM_COMPONENT_SPECIFIC_METRICS_EAR_1 = 3;

    // for this test class ears are configured to output to different directories
    // note, these must change if flow.xml used changes
    private static final int EXPECTED_NUMBER_CSV_FILES_EAR_1 = NUM_EPS_INSTANCES_STATS_ENABLED + NUM_COMPONENTS_EAR_1
            + NUM_COMPONENT_SPECIFIC_METRICS_EAR_1;

    private static final int EXPECTED_NUMBER_CSV_FILES_EAR_2 = NUM_EPS_INSTANCES_STATS_ENABLED;

    @Deployment(name = "EarEpsStatisticDiffFolderTest1", order = 1)
    public static Archive<?> createTestArchiveEarEpsStatisticDiffFolderTest1() throws IOException {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/firstEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/flows/firstEar/flow.xml";
        final String jbossDeploymentFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName()
                + "/application/firstEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.<Class<?>> asList(JeeTestPassThroughEventHandler.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarEpsStatisticDiffFolderTest.class.getSimpleName() + "1.ear", warCoordinates,
                flowFileName, sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, EarEpsStatisticDiffFolderTest.class, handlerClasses,
                null);

        createOutputDirectories();

        ear.setApplicationXML(EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/application/firstEar/application.xml");

        return ear;
    }

    private static void createOutputDirectories() throws IOException {

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest BEFORE CLASS STARTED\n*\n*");

        final String epsConfigFileName1 = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/firstEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String epsConfigFileName2 = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/secondEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String epsConfigFileName3 = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/thirdEar/"
                + Artifact.CONFIGURATION_FILE_NAME;

        Artifact.createCsvOutputLocation(epsConfigFileName1);
        Artifact.createModuleOutputLocation(epsConfigFileName1);
        Artifact.createCsvOutputLocation(epsConfigFileName2);
        Artifact.createModuleOutputLocation(epsConfigFileName2);
        Artifact.createCsvOutputLocation(epsConfigFileName3);
        Artifact.createModuleOutputLocation(epsConfigFileName3);

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest BEFORE CLASS FINISHED\n*\n*");
    }

    @Deployment(name = "EarEpsStatisticDiffFolderTest2", order = 2)
    public static Archive<?> createTestArchiveEarEpsStatisticDiffFolderTest2() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/secondEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/flows/secondEar/flow.xml";
        final String jbossDeploymentFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName()
                + "/application/secondEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.<Class<?>> asList(JeeTestPassThroughEventHandler.class, JeeTestEventDuplicatorComponent.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarEpsStatisticDiffFolderTest.class.getSimpleName() + "2.ear", warCoordinates,
                flowFileName, sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, EarEpsStatisticDiffFolderTest.class, handlerClasses,
                null);

        ear.setApplicationXML(EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/application/secondEar/application.xml");

        return ear;

    }

    @Deployment(name = "EarEpsStatisticDiffFolderTest3", order = 3)
    public static Archive<?> createTestArchiveEarEpsStatisticDiffFolderTest3() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/thirdEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/properties/thirdEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/flows/thirdEar/flow.xml";
        final String jbossDeploymentFileName = EarEpsStatisticDiffFolderTest.class.getSimpleName()
                + "/application/thirdEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.<Class<?>> asList(JeeTestPassThroughEventHandler.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarEpsStatisticDiffFolderTest.class.getSimpleName() + "3.ear", warCoordinates,
                flowFileName, sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, EarEpsStatisticDiffFolderTest.class, handlerClasses,
                null);

        ear.setApplicationXML(EarEpsStatisticDiffFolderTest.class.getSimpleName() + "/application/thirdEar/application.xml");

        return ear;
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("EarEpsStatisticDiffFolderTest1")
    public void testStatisticsCsvOutput1() throws IOException, InterruptedException {

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);
        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest::testStatisticsCsvOutput1 STARTED\n*\n*");

        final boolean waited = Artifact.waitForModuleDeploy(1, 3);

        Assert.assertTrue(waited);

        Assert.assertEquals("csv files should have been created", EXPECTED_NUMBER_CSV_FILES_EAR_1, Artifact.getCsvFiles().size());

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest::testStatisticsCsvOutput1 FINISHED\n*\n*");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("EarEpsStatisticDiffFolderTest2")
    public void testStatisticsCsvOutput2() throws IOException, InterruptedException {

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest::testStatisticsCsvOutput2 STARTED\n*\n*");

        Assert.assertEquals("csv files should have been created", EXPECTED_NUMBER_CSV_FILES_EAR_2, Artifact.getCsvFiles().size());

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest::testStatisticsCsvOutput2 FINISHED\n*\n*");
    }

    @Test
    @InSequence(3)
    @OperateOnDeployment("EarEpsStatisticDiffFolderTest3")
    public void testStatisticsCsvOutput3() throws IOException, InterruptedException {

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest::testStatisticsCsvOutput3 STARTED\n*\n*");

        Assert.assertEquals("csv files should have not been created", 0, Artifact.getCsvFiles().size());

        LOG.info("\n*\n*\n* EarEpsStatisticDiffFolderTest::testStatisticsCsvOutput3 FINISHED\n*\n*");

    }
}
