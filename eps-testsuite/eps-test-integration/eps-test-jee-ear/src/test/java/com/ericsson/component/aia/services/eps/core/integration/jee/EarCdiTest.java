package com.ericsson.component.aia.services.eps.core.integration.jee;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.cdi.*;
import com.ericsson.component.aia.services.eps.core.integration.jee.ear.*;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.hazelcast.config.Config;
import com.hazelcast.core.*;

/**
 * Test scenario using CDI handlers.
 *
 * Test scenario using CDI handlers in the same way (so have H1, CDIH1, H2 and CDIH2) Test scenario where H1 uses some stateless session bean defined
 * in EAR1/ejb1.jar, CDIH1 injecting some EJB from EAR/ejb.jar and using that during flow execution.
 *
 * @author esarlag
 *
 */
@RunWith(Arquillian.class)
public class EarCdiTest {

    private static final Logger LOG = LoggerFactory.getLogger(EarCdiTest.class);

    @Deployment(name = "EarCdiTest1", order = 1)
    public static Archive<?> createTestArchiveEarCdiTest1() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarCdiTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarCdiTest.class.getSimpleName() + "/properties/firstEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarCdiTest.class.getSimpleName() + "/flows/firstEar/flow.xml";
        final String jbossDeploymentFileName = EarCdiTest.class.getSimpleName() + "/application/firstEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.asList(MessagingTestListener.class, HazelcastInputListener.class,
                JeeTestEventGeneratorComponent.class, JeeTestPassThroughEventHandler.class, JeeTestCdiEventHandler.class, JeeTestCdiEvent.class);

        final List<Class<?>> ejbJarClasses = Arrays.asList(SomeStatelessSessionBean.class, SomeCDIBean.class, EventProcessingLocal.class,
                EventProcessingBean.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarCdiTest.class.getSimpleName() + "1.ear", warCoordinates, flowFileName, sfWkConfigFileName, epsConfigFileName,
                "MANIFEST3x.MF", jbossDeploymentFileName, EarCdiTest.class, handlerClasses, ejbJarClasses);

        Artifact.createModuleOutputLocation(epsConfigFileName);

        ear.setApplicationXML(EarCdiTest.class.getSimpleName() + "/application/firstEar/application.xml");

        return ear;

    }

    @Deployment(name = "EarCdiTest2", order = 2)
    public static Archive<?> createTestArchiveEarCdiTest2() {

        final String warCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?";
        final String epsConfigFileName = EarCdiTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.CONFIGURATION_FILE_NAME;
        final String sfWkConfigFileName = EarCdiTest.class.getSimpleName() + "/properties/secondEar/" + Artifact.SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME;
        final String flowFileName = EarCdiTest.class.getSimpleName() + "/flows/secondEar/flow.xml";

        final String jbossDeploymentFileName = EarCdiTest.class.getSimpleName() + "/application/secondEar/jboss-deployment-structure.xml";

        final List<Class<?>> handlerClasses = Arrays.asList(H1EventHandler.class, HazelcastInputListener.class, MessagingTestListener.class,
                JeeTestEventGeneratorComponent.class, JeeTestPassThroughEventHandler.class, JeeTestCdiEventHandler.class, JeeTestCdiEvent.class);

        final List<Class<?>> ejbJarClasses = Arrays.asList(SomeStatelessSessionBean.class, SomeCDIBean.class, EventProcessingLocal.class,
                EventProcessingBean.class);

        final EnterpriseArchive ear = Artifact.buildEar(EarCdiTest.class.getSimpleName() + "2.ear", warCoordinates, flowFileName, sfWkConfigFileName, epsConfigFileName,
                "MANIFEST3x.MF", jbossDeploymentFileName, EarCdiTest.class, handlerClasses, ejbJarClasses);

        Artifact.createModuleOutputLocation(epsConfigFileName);

        ear.setApplicationXML(EarCdiTest.class.getSimpleName() + "/application/secondEar/application.xml");

        return ear;

    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("EarCdiTest1")
    public void test_cdi_handlers1() throws Exception {

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers1 STARTED\n*\n*");

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);

        final HazelcastInputListener listener = new HazelcastInputListener(5);
        final Config cfg = new Config();
        final HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(cfg);
        final ITopic<String> hazelcastSendTopic = hzInstance.getTopic("eps-cdi-topic1");
        final ITopic<String> hazelcastReceiveTopic = hzInstance.getTopic("eps-cdi-topic2");
        hazelcastReceiveTopic.addMessageListener(listener);

        Artifact.waitForModuleDeploy(1, 3);

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers1 publish cdi_event_ STARTED\n*\n*");

        hazelcastSendTopic.publish("cdi_event_" + UUID.randomUUID().toString());

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers1 publish cdi_event_ FINISHED\n*\n*");

        Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        Assert.assertEquals(2, JeeTestCdiEventHandler.cdiEventCount);

        hzInstance.getLifecycleService().shutdown();

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers1 FINISHED\n*\n*");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("EarCdiTest2")
    public void test_cdi_handlers2() throws Exception {

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers2 STARTED\n*\n*");

        Artifact.deployFlowFile("flow.xml", Artifact.CONFIGURATION_FILE_NAME);

        final HazelcastInputListener listener = new HazelcastInputListener(5);
        final Config cfg = new Config();
        final HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(cfg);
        final ITopic<String> hazelcastSendTopic = hzInstance.getTopic("eps-cdi-topic3");
        final ITopic<String> hazelcastReceiveTopic = hzInstance.getTopic("eps-cdi-topic4");
        hazelcastReceiveTopic.addMessageListener(listener);

        Artifact.waitForModuleDeploy(1, 3);

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers2 publish cdi_event_ STARTED\n*\n*");

        hazelcastSendTopic.publish("cdi_event_" + UUID.randomUUID().toString());

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers2 publish cdi_event_ FINISHED\n*\n*");

        Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(5, listener.getReceivedMessages().size());
        Assert.assertEquals(2, JeeTestCdiEventHandler.cdiEventCount);

        hzInstance.getLifecycleService().shutdown();

        LOG.info("\n*\n*\n* EarCdiTest::test_cdi_handlers2 FINISHED\n*\n*");
    }

}
