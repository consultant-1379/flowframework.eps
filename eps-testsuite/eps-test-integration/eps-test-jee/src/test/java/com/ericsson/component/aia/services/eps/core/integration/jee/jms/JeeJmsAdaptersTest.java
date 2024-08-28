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
package com.ericsson.component.aia.services.eps.core.integration.jee.jms;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.integration.commons.util.JmsMessageListener;
import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestLongProcessingHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;

@RunWith(Arquillian.class)
public class JeeJmsAdaptersTest {

    private static final String FLOWS_JMS_FLOW_XML = "flows/jms_flow.xml";
    private static final Logger LOG = LoggerFactory.getLogger(JeeJmsAdaptersTest.class);

    private static final int NUMBER_OF_MESSAGES = 100;
    private Connection conn;
    private Session sessionIn;
    private Session sessionOut;

    @Deployment(name = "JeeJmsAdaptersTestWar")
    public static Archive<?> createTestArchive() {
        final WebArchive war = Artifact.getEpsArchive();
        war.addAsResource(FLOWS_JMS_FLOW_XML);
        war.addClass(JeeJmsAdaptersTest.class);
        war.addClass(JmsMessageListener.class);
        war.addClass(JeeTestLongProcessingHandler.class);
        war.addAsResource("EpsConfiguration.properties");
        return war;
    }

    @Before
    public void setup() throws JMSException, NamingException {
        connectToJmsServer();
    }

    @After
    public void tearDown() throws Exception {
        sessionIn.close();
        sessionOut.close();
        conn.close();
    }

    @Test
    @InSequence(1)
    public void test_jee_epsJmsAdapters() throws Exception {
        Artifact.cleanupConfiguredXMLFolder();
        final boolean moduleCopied = Artifact.copyXmlContentToConfiguredFolder("/" + FLOWS_JMS_FLOW_XML);
        Assert.assertTrue(moduleCopied);
        final boolean waited = Artifact.wait(3000);
        Assert.assertTrue(waited);
        final JmsMessageListener listener = new JmsMessageListener(NUMBER_OF_MESSAGES);
        registerJmsConsumer(listener);
        final long initialTime = System.currentTimeMillis();
        sendJmsMessages(NUMBER_OF_MESSAGES);
        listener.latch.await(50, TimeUnit.SECONDS);
        assertEquals(listener.latch.getCount(), 0);
        assertEquals(NUMBER_OF_MESSAGES, listener.getReceivedMessages().size());
        LOG.info("Time taken for {} messages: {}", NUMBER_OF_MESSAGES, System.currentTimeMillis() - initialTime);
        final Object message = listener.getReceivedMessages().get(0);
        assertTrue(message instanceof ObjectMessage);
        final Serializable object = ((ObjectMessage) message).getObject();
        assertTrue(((String) object).startsWith("test"));
        Artifact.cleanupConfiguredXMLFolder();
    }

    private void connectToJmsServer() throws JMSException, NamingException {
        final InitialContext jndiCtxt = new InitialContext();
        final ConnectionFactory connFactory = (ConnectionFactory) jndiCtxt.lookup("/EpsTestConnectionFactory");
        conn = connFactory.createConnection();
        conn.start();
        sessionIn = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        sessionOut = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private void registerJmsConsumer(final JmsMessageListener listener) throws JMSException {
        final Destination destination = sessionOut.createQueue("OutTestQueue");
        final MessageConsumer consumer = sessionOut.createConsumer(destination);
        consumer.setMessageListener(listener);
    }

    private void sendJmsMessages(final int numberOfMessages) throws JMSException {
        final Destination destination = sessionIn.createQueue("InTestQueue");
        final MessageProducer producer = sessionIn.createProducer(destination);
        for (int i = 0; i < numberOfMessages; i++) {
            final TextMessage msg = sessionIn.createTextMessage();
            msg.setText("test" + i);
            producer.send(msg);
            LOG.info("Sent message" + msg.getText());
        }
    }
}
