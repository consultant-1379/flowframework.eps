package com.ericsson.component.aia.services.eps.core.integration.jee;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestEventDuplicatorComponent;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.ioadapters.SimpleFileInputAdapter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 *
 * Test scenario where 2 different EARs use IO adapters. The second EAR use injected IO adapter providing its implementation into services folder.
 *
 * @author esarlag
 *
 */

@RunWith(Arquillian.class)
public class MultipleEarIOAdapterTest {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleEarIOAdapterTest.class);

    @Deployment(name = "MultipleEarIOAdapterTest1", order = 1)
    public static Archive<?> createTestArchiveMultipleEarIOAdapterTest1() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = MultipleEarIOAdapterTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = MultipleEarIOAdapterTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = MultipleEarIOAdapterTest.class.getSimpleName() + "/flows/firstEar/flow.xml";
        final String jbossDeploymentFileName = MultipleEarIOAdapterTest.class.getSimpleName()
                + "/application/firstEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.asList(JeeTestEventDuplicatorComponent.class, HazelcastInputListener.class,
                MessagingTestListener.class);

        final EnterpriseArchive ear = Artifact.buildEar(MultipleEarIOAdapterTest.class.getSimpleName() + "1.ear", warCoordinates, flowFileName,
        		sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, MultipleEarIOAdapterTest.class, handlerClasses, null);

        Artifact.createModuleOutputLocation(epsConfigFileName);

        ear.setApplicationXML(MultipleEarIOAdapterTest.class.getSimpleName() + "/application/firstEar/application.xml");

        return ear;

    }

    @Deployment(name = "MultipleEarIOAdapterTest2", order = 2)
    public static Archive<?> createTestArchiveMultipleEarIOAdapterTest2() throws IOException {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = MultipleEarIOAdapterTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = MultipleEarIOAdapterTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = MultipleEarIOAdapterTest.class.getSimpleName() + "/flows/secondEar/flow.xml";
        final String jbossDeploymentFileName = MultipleEarIOAdapterTest.class.getSimpleName()
                + "/application/secondEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.asList(JeeTestPassThroughEventHandler.class, HazelcastInputListener.class,
                MessagingTestListener.class);

        final EnterpriseArchive ear = Artifact.buildEar(MultipleEarIOAdapterTest.class.getSimpleName() + "2.ear", warCoordinates, flowFileName,
        		sfWkConfigFileName, epsConfigFileName, "MANIFEST3x.MF", jbossDeploymentFileName, MultipleEarIOAdapterTest.class, handlerClasses, null);

        Artifact.createModuleOutputLocation(epsConfigFileName);

        ear.setApplicationXML(MultipleEarIOAdapterTest.class.getSimpleName() + "/application/secondEar/application.xml");

        final JavaArchive adapterJar = ShrinkWrap.create(JavaArchive.class, "adapter.jar").addClass(SimpleFileInputAdapter.class);
        adapterJar.addAsManifestResource("services/com.ericsson.component.aia.services.eps.adapter.InputAdapter");

        adapterJar.addAsResource("files/adapterTestFile.data");

        ear.addAsLibraries(adapterJar);

        return ear;
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("MultipleEarIOAdapterTest1")
    public void testUseModule() throws IOException, InterruptedException {

        LOG.info("\n*\n*\n* MultipleEarIOAdapterTest::testUseModule STARTED\n*\n*");

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);

        final HazelcastInputListener listener1 = new HazelcastInputListener(5);
        final HazelcastInputListener listener2 = new HazelcastInputListener(5);
        final HazelcastInstance hzInstance = Artifact.createHazelcastInstance();
        final ITopic hazelcastSendTopic = hzInstance.getTopic("eps-topic-in");
        final ITopic hazelcastReceiveTopic1 = hzInstance.getTopic("eps-topic1-out");
        final ITopic hazelcastReceiveTopic2 = hzInstance.getTopic("eps-topic2-out");
        hazelcastReceiveTopic1.addMessageListener(listener1);
        hazelcastReceiveTopic2.addMessageListener(listener2);

        Artifact.waitForModuleDeploy(1, 3);

        hazelcastSendTopic.publish("1");
        Assert.assertTrue(listener1.latch.await(5, TimeUnit.SECONDS));
        Assert.assertTrue(listener2.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener1.getReceivedMessages().size());
        Assert.assertEquals(5, listener2.getReceivedMessages().size());

        hzInstance.getLifecycleService().shutdown();

        LOG.info("\n*\n*\n* MultipleEarIOAdapterTest::testUseModule FINISHED\n*\n*");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("MultipleEarIOAdapterTest2")
    public void testUseAdapter() throws IOException, InterruptedException {

        LOG.info("\n*\n*\n* MultipleEarIOAdapterTest::testUseAdapter STARTED\n*\n*");

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);

        final HazelcastInputListener listener1 = new HazelcastInputListener(1);
        final HazelcastInstance hzInstance = Artifact.createHazelcastInstance();
        final ITopic hazelcastReceiveTopic1 = hzInstance.getTopic("eps-topic3-out");
        hazelcastReceiveTopic1.addMessageListener(listener1);

        Artifact.waitForModuleDeploy(1, 3);

        Assert.assertTrue(listener1.latch.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(1, listener1.getReceivedMessages().size());

        hzInstance.getLifecycleService().shutdown();

        LOG.info("\n*\n*\n* MultipleEarIOAdapterTest::testUseAdapter FINISHED\n*\n*");

    }
}
