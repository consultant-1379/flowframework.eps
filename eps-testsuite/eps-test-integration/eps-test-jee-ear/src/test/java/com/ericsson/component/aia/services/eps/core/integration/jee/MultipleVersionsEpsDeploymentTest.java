package com.ericsson.component.aia.services.eps.core.integration.jee;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 * Test scenario where 3.x and 2.x EARs bundling 2.x and 3.x eps-jee-war works in the same deployment scenario.
 * 
 * @author esarlag
 * 
 */
@RunWith(Arquillian.class)
@Ignore
public class MultipleVersionsEpsDeploymentTest {

    static final Logger LOG = LoggerFactory.getLogger(MultipleVersionsEpsDeploymentTest.class);

    @Deployment(name = "MultipleVersionsEpsDeploymentTest1", order = 1)
    public static Archive<?> createTestArchiveMultipleVersionsEpsDeploymentTest1() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_SERVICES_EPS_WAR_OLD + ":" + Artifact.readOldProjectVersion();
        final String epsConfigFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/properties/firstEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/flows/firstEar/flow.xml";
        final String jbossDeploymentFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName()
                + "/application/firstEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.asList(JeeTestEventDuplicatorComponent.class, HazelcastInputListener.class,
                MessagingTestListener.class);
        final EnterpriseArchive ear = Artifact.buildEar(MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "1.ear", warCoordinates,
                flowFileName, sfWkConfigFileName, epsConfigFileName, "MANIFEST.MF", jbossDeploymentFileName, MultipleVersionsEpsDeploymentTest.class, handlerClasses,
                null);

        Artifact.createModuleOutputLocation(epsConfigFileName);

        ear.setApplicationXML(MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/application/firstEar/application.xml");
        return ear;

    }

    @Deployment(name = "MultipleVersionsEpsDeploymentTest2", order = 2)
    public static Archive<?> createTestArchiveMultipleVersionsEpsDeploymentTest2() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String flowFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/flows/secondEar/flow.xml";
        final String epsConfigFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/properties/secondEar/"
                + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String jbossDeploymentFileName = MultipleVersionsEpsDeploymentTest.class.getSimpleName()
                + "/application/secondEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.asList(JeeTestEventDuplicatorComponent.class, HazelcastInputListener.class,
                MessagingTestListener.class);

        final EnterpriseArchive ear = Artifact.buildEar(MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "2.ear", warCoordinates,
                flowFileName, sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, MultipleVersionsEpsDeploymentTest.class, handlerClasses,
                null);

        Artifact.createModuleOutputLocation(epsConfigFileName);

        ear.setApplicationXML(MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/application/secondEar/application.xml");

        return ear;

    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("MultipleVersionsEpsDeploymentTest1")
    public void test_deploy_module1() throws Exception {

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);
        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::test_deploy_module1 Deployed module: "
                + MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/flows/firstEar/flow.xml" + "\n*\n*");
        final boolean waited = oldWaitForModuleDeploy(1, 3);
        Assert.assertTrue(waited);
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("MultipleVersionsEpsDeploymentTest2")
    public void test_deploy_module2() throws Exception {

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);
        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::test_deploy_module2 Deployed module: "
                + MultipleVersionsEpsDeploymentTest.class.getSimpleName() + "/flows/secondEar/flow.xml" + "\n*\n*");
        final boolean waited = Artifact.waitForModuleDeploy(1, 3);
        Assert.assertTrue(waited);
    }

    @Test
    @InSequence(3)
    @OperateOnDeployment("MultipleVersionsEpsDeploymentTest1")
    public void test_use_module1() throws Exception {
        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::test_use_module1 STARTED\n*\n*");
        final HazelcastInputListener listener1 = new HazelcastInputListener(5);
        final HazelcastInputListener listener2 = new HazelcastInputListener(5);
        final HazelcastInstance hzInstance = Artifact.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzInstance.getTopic("eps-topic-in-old");
        final ITopic hazelcastReceiveTopic1 = hzInstance.getTopic("eps-topic1-out-old");
        final ITopic hazelcastReceiveTopic2 = hzInstance.getTopic("eps-topic2-out-old");
        hazelcastReceiveTopic1.addMessageListener(listener1);
        hazelcastReceiveTopic2.addMessageListener(listener2);

        hazelcastSendTopic.publish("1");
        Assert.assertTrue(listener1.latch.await(5, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener1.getReceivedMessages().size());
        Assert.assertEquals(5, listener2.getReceivedMessages().size());

        hzInstance.getLifecycleService().shutdown();

        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::test_use_module1 FINISHED\n*\n*");
    }

    @Test
    @InSequence(4)
    @OperateOnDeployment("MultipleVersionsEpsDeploymentTest2")
    public void test_use_module_paths_in_parallel() throws Exception {
        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::test_use_module_paths_in_parallel\n*\n*");
        final HazelcastInputListener listener1 = new HazelcastInputListener(3);
        final HazelcastInputListener listener2 = new HazelcastInputListener(5);
        final HazelcastInstance hzInstance = Artifact.createHazelcastInstance();
        final ITopic<String> topic1In = hzInstance.getTopic("eps-topic1-in-new");
        final ITopic<String> topic1Out = hzInstance.getTopic("eps-topic1-out-new");
        final ITopic<String> topic2In = hzInstance.getTopic("eps-topic2-in-new");
        final ITopic<String> topic2Out = hzInstance.getTopic("eps-topic2-out-new");
        topic1Out.addMessageListener(listener1);
        topic2Out.addMessageListener(listener2);

        topic1In.publish("1");
        topic2In.publish("1");
        Assert.assertTrue(listener1.latch.await(5, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(3, listener1.getReceivedMessages().size());
        Assert.assertEquals(5, listener2.getReceivedMessages().size());

        hzInstance.getLifecycleService().shutdown();

        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::test_use_module_paths_in_parallel FINISHED\n*\n*");

    }

    private static boolean oldWaitForModuleDeploy(final int moduleCount, final int retry) throws InterruptedException {
        LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::oldWaitForModuleDeploy: ModuleCount " + moduleCount + " Retry: " + retry + "\n*\n*");

        com.ericsson.component.aia.services.eps.core.modules.ModuleManager modulesManager = null;
        try {
            modulesManager = com.ericsson.component.aia.services.eps.core.util.LoadingUtil
                    .loadSingletonInstance(com.ericsson.component.aia.services.eps.core.modules.ModuleManager.class);
        } catch (final Exception ex) {
            LOG.error("Cannot load ModuleManager: Cause: " + ex.getMessage(), ex);
            return false;
        }

        boolean isModuleDeployed = false;
        int index = 0;
        while (!isModuleDeployed && index < retry) {

            Thread.sleep(5000);

            final int count = modulesManager.getDeployedModulesCount();

            LOG.info("\n*\n*\n* MultipleVersionsEpsDeploymentTest::ModuleManager: " + modulesManager + " Count: " + count + "\n*\n*");

            index++;

            if (count == 1) {
                isModuleDeployed = true;
                break;
            }
        }

        return isModuleDeployed;
    }

}
