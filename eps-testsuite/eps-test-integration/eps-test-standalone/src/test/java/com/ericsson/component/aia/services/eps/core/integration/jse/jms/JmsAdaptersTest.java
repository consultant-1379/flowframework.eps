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
package com.ericsson.component.aia.services.eps.core.integration.jse.jms;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.*;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.jms.server.impl.StandaloneNamingServer;
import org.junit.*;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventHandlerContext;
import com.ericsson.component.aia.services.eps.core.parsing.EpsComponentConfiguration;
import com.ericsson.component.aia.services.eps.io.adapter.jms.JmsInputAdapter;
import com.ericsson.component.aia.services.eps.io.adapter.jms.JmsOutputAdapter;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class JmsAdaptersTest {

    private static final String FACTORY_NAME = "EpsIntegration";
    private static final String QUEUE_NAME = "eps_test_channel";
    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_PORT = "5445";

    private JMSServerManagerImpl jmsServer;
    private StandaloneNamingServer jndiServer;
    private HornetQServer server;

    @Before
    public void setup() throws Exception {
        initHornetQServer();
    }

    @After
    public void tearDown() throws Exception {
        this.jmsServer.stop();
        this.jndiServer.stop();
    }

    @Test
    public void test_sending_10_000_simple_objectMessage() throws Exception {
        final CountDownLatch countDown = new CountDownLatch(10_000);
        final List<String> receivedEvents = new ArrayList<>();
        final EventHandlerContext context = this.createConfiguredContext(countDown, receivedEvents);
        final JmsInputAdapter inputAdapter = new JmsInputAdapter();
        inputAdapter.init(context);
        final JmsOutputAdapter outputAdapter = new JmsOutputAdapter();
        outputAdapter.init(context);
        for (int i = 0; i < 10_000; i++) {
            outputAdapter.onEvent("test message");
        }
        countDown.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDown.getCount());
        assertTrue(receivedEvents.contains("test message"));
        assertEquals(receivedEvents.size(), 10_000);
        inputAdapter.destroy();
        outputAdapter.destroy();
    }

    @Test
    public void test_sending_simple_objectMessage() throws Exception {
        final CountDownLatch countDown = new CountDownLatch(1);
        final List<String> receivedEvents = new ArrayList<>();
        final EventHandlerContext context = this.createConfiguredContext(countDown, receivedEvents);
        final JmsInputAdapter inputAdapter = new JmsInputAdapter();
        inputAdapter.init(context);
        final JmsOutputAdapter outputAdapter = new JmsOutputAdapter();
        outputAdapter.init(context);
        outputAdapter.onEvent("test message");
        countDown.await(1, TimeUnit.SECONDS);
        assertEquals(0, countDown.getCount());
        assertTrue(receivedEvents.contains("test message"));
        inputAdapter.destroy();
        outputAdapter.destroy();

    }

    private EpsEventHandlerContext createConfiguredContext(final CountDownLatch countDown, final List<String> receivedEvents) {
        final Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        props.put("java.naming.provider.url", "jnp://localhost:1099");
        props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        props.put("jndiJmsConnectionFactory", "/" + FACTORY_NAME);
        props.put("jmsDestinationType", "Queue");
        props.put("jmsDestinationName", QUEUE_NAME);

        final EpsComponentConfiguration cfg = new EpsComponentConfiguration(props);

        final EpsModule epsModule = new EpsModule();
        epsModule.setName("flow34");
        epsModule.setNamespace("com.test");
        epsModule.setVersion("1.2.3");
        final EpsEventHandlerContext context = new EpsEventHandlerContext(cfg, epsModule, "componentIdForJmsAdaptersTest");

        context.addEventSubscriber(new EventInputHandler() {

            @Override
            public void destroy() {
            }

            @Override
            public void init(final EventHandlerContext ctx) {
            }

            @Override
            public void onEvent(final Object inputEvent) {
                receivedEvents.add((String) inputEvent);
                countDown.countDown();
            }
        }, "test-handler");

        return context;
    }

    private void initHornetQServer() throws Exception {
        // Step 1. Create the Configuration, and set the properties accordingly
        final Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);

        final HashMap<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(TransportConstants.HOST_PROP_NAME, SERVER_HOST);
        connectionParams.put(TransportConstants.PORT_PROP_NAME, SERVER_PORT);

        final TransportConfiguration nettyAcceptor = new TransportConfiguration(NettyAcceptorFactory.class.getName());
        final TransportConfiguration nettyConector = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams, "netty");

        final HashSet<TransportConfiguration> setAcceptors = new HashSet<TransportConfiguration>();
        setAcceptors.add(nettyAcceptor);
        final HashMap<String, TransportConfiguration> connectors = new HashMap<>();
        connectors.put("netty", nettyConector);

        configuration.setAcceptorConfigurations(setAcceptors);
        configuration.setConnectorConfigurations(connectors);
        System.getProperties().put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        System.getProperties().put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");

        // Step 2. Create and start the server
        server = HornetQServers.newHornetQServer(configuration);

        jndiServer = new StandaloneNamingServer(server);
        jndiServer.setPort(1099);
        jndiServer.setBindAddress("localhost");
        jndiServer.setRmiPort(1098);
        jndiServer.setRmiBindAddress("localhost");
        jndiServer.start();

        jmsServer = new JMSServerManagerImpl(server);
        final List<String> connectorsName = new ArrayList<>();
        connectorsName.add("netty");
        final ConnectionFactoryConfigurationImpl connectionFactoryConfig = new ConnectionFactoryConfigurationImpl(FACTORY_NAME, false,
                connectorsName, "/" + FACTORY_NAME);
        jmsServer.start();
        jmsServer.createConnectionFactory(false, connectionFactoryConfig, "/" + FACTORY_NAME);
        final boolean queueCreated = jmsServer.createQueue(false, QUEUE_NAME, "", false, "/queue/" + QUEUE_NAME);

        if (!queueCreated) {
            throw new RuntimeException("Did not create the Queue properly!");
        }

    }

}
