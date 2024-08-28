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
package com.ericsson.component.aia.services.eps.core.integration.jse.hornetq;

import java.util.*;
import java.util.concurrent.*;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.*;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.*;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.junit.*;

import com.ericsson.component.aia.services.eps.io.adapter.hornetq.*;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class HornetQAdaptersTest {

    private static final String QUEUE_NAME = "eps_test_channel";
    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_PORT = "5445";

    private HornetQServer server;
    private ClientSessionFactory factory;
    private ClientSession session;
    private ServerLocator serverLocator;

    private EpsTestEventSubscriber subscriber;

    @Test
    public void test_simple_sending() throws Exception {
        subscriber.clear(1);
        Assert.assertTrue(subscriber.getReceivedMessages().isEmpty());
        final HornetQInputAdapter inputAdapter = new HornetQInputAdapter();
        inputAdapter.init(createHornetQContext(subscriber));
        final HornetQOutputAdapter outputAdapter = new HornetQOutputAdapter();
        outputAdapter.init(createHornetQContext(null));
        outputAdapter.onEvent("abc");
        Assert.assertTrue(subscriber.latch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(1, subscriber.getReceivedMessages().size());
        final Object message = subscriber.getReceivedMessages().get(0);
        Assert.assertEquals("abc", message);
        inputAdapter.destroy();
        outputAdapter.destroy();
    }

    @Test
    public void test_simple_sending_big_messages() throws Exception {
        final int messageNumber = 5000;
        subscriber.clear(messageNumber);
        Assert.assertTrue(subscriber.getReceivedMessages().isEmpty());
        final HornetQInputAdapter inputAdapter = new HornetQInputAdapter();
        inputAdapter.init(createHornetQContext(subscriber));
        final HornetQOutputAdapter outputAdapter = new HornetQOutputAdapter();
        outputAdapter.init(createHornetQContext(null));
        for (int i = 0; i < messageNumber; i++) {
            outputAdapter.onEvent(createBigMessage());
        }
        Assert.assertTrue(subscriber.latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(messageNumber, subscriber.getReceivedMessages().size());
        inputAdapter.destroy();
        outputAdapter.destroy();
    }

    @Test
    public void test_complex_sending() throws Exception {
        // send multiple simple messages
        subscriber.clear(5);
        Assert.assertTrue(subscriber.getReceivedMessages().isEmpty());
        final HornetQInputAdapter inputAdapter = new HornetQInputAdapter();
        inputAdapter.init(createHornetQContext(subscriber));
        final HornetQOutputAdapter outputAdapter = new HornetQOutputAdapter();
        outputAdapter.init(createHornetQContext(null));
        for (int i = 0; i < 5; i++) {
            outputAdapter.onEvent("abc_" + i);
        }
        Assert.assertTrue(subscriber.latch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(5, subscriber.getReceivedMessages().size());
        for (int i = 0; i < 5; i++) {
            final String msg = "abc_" + i;
            Assert.assertTrue(subscriber.getReceivedMessages().contains(msg));
        }
        // try sending collections of pojos
        subscriber.clear(1);
        final List<Integer> bigMessage = new LinkedList<>();
        final Random random = new Random();
        for (int i = 0; i < 15; i++) {
            bigMessage.add(Integer.valueOf(random.nextInt()));
        }
        outputAdapter.onEvent(bigMessage);
        Assert.assertTrue(subscriber.latch.await(2, TimeUnit.SECONDS));
        Assert.assertEquals(1, subscriber.getReceivedMessages().size());
        final List<Integer> receivedMessage = (List<Integer>) subscriber.getReceivedMessages().get(0);
        for (final Integer sendInt : bigMessage) {
            Assert.assertTrue(receivedMessage.contains(sendInt));
        }
        // now test destroy and see that message is not received
        subscriber.clear(1);
        inputAdapter.destroy();
        outputAdapter.onEvent("never_to_be_received");
        Assert.assertFalse(subscriber.latch.await(2, TimeUnit.SECONDS));
        inputAdapter.destroy();
        outputAdapter.destroy();
    }

    @Test
    @Ignore("not implemented")
    public void test_multi_threaded_sending() throws Exception {
        // send multiple simple messages
        final int msgNum = 500;
        subscriber.clear(msgNum);
        final int threadNum = 5;
        final int msgPerThread = 100;
        final ExecutorService execService = Executors.newFixedThreadPool(threadNum);
        Assert.assertTrue(subscriber.getReceivedMessages().isEmpty());
        final HornetQInputAdapter inputAdapter = new HornetQInputAdapter();
        inputAdapter.init(createHornetQContext(subscriber));
        final HornetQOutputAdapter outputAdapter = new HornetQOutputAdapter();
        outputAdapter.init(createHornetQContext(null));
        for (int i = 0; i < threadNum; i++) {
            final int lowerEventLimit = i * msgPerThread;
            execService.submit(new Runnable() {

                @Override
                public void run() {
                    for (int i = lowerEventLimit; i < lowerEventLimit + msgPerThread; i++) {
                        outputAdapter.onEvent("evt_" + i);
                    }
                };
            });
        }
        Assert.assertTrue(subscriber.latch.await(10, TimeUnit.SECONDS));
        Assert.assertEquals(msgNum, subscriber.getReceivedMessages().size());
        for (int i = 0; i < msgNum; i++) {
            final String msg = "evt_" + i;
            Assert.assertTrue(subscriber.getReceivedMessages().contains(msg));
        }
        execService.shutdown();
        inputAdapter.destroy();
        outputAdapter.destroy();
    }

    @Test
    @Ignore("not implemented")
    public void test_multi_threaded_sending_big_messages() throws Exception {
        // send multiple simple messages
        final int msgNum = 10000;
        subscriber.clear(msgNum);
        final int threadNum = 10;
        final int msgPerThread = 1000;
        final ExecutorService execService = Executors.newFixedThreadPool(threadNum);
        Assert.assertTrue(subscriber.getReceivedMessages().isEmpty());
        final HornetQInputAdapter inputAdapter = new HornetQInputAdapter();
        inputAdapter.init(createHornetQContext(subscriber));
        final HornetQOutputAdapter outputAdapter = new HornetQOutputAdapter();
        outputAdapter.init(createHornetQContext(null));
        for (int i = 0; i < threadNum; i++) {
            final int lowerEventLimit = i * msgPerThread;
            execService.submit(new Runnable() {

                @Override
                public void run() {
                    for (int i = lowerEventLimit; i < lowerEventLimit + msgPerThread; i++) {
                        outputAdapter.onEvent(createBigMessage());
                    }
                };
            });
        }
        Assert.assertTrue(subscriber.latch.await(30, TimeUnit.SECONDS));
        Assert.assertEquals(msgNum, subscriber.getReceivedMessages().size());
        execService.shutdown();
        inputAdapter.destroy();
        outputAdapter.destroy();
    }

    private String createBigMessage() {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            strBuilder.append(UUID.randomUUID().toString() + "_" + i);
        }
        return strBuilder.toString();
    }

    private Map<String, String> createConfigMap() {
        final Map<String, String> cfg = new HashMap<>();
        cfg.put(AbstractHornetQAdapter.HORNETQ_CHANNEL_NAME, QUEUE_NAME);
        cfg.put(AbstractHornetQAdapter.HORNETQ_SERVER_ADDRESS_PROP_NAME, SERVER_HOST);
        cfg.put(AbstractHornetQAdapter.HORNETQ_SERVER_PORT_PROP_NAME, SERVER_PORT);
        return cfg;
    }

    private EventHandlerContext createHornetQContext(final EventSubscriber subscriber) {
        final Map<String, String> configParams = createConfigMap();
        final EventHandlerContext ctx = new EventHandlerContext() {

            @Override
            public void sendControlEvent(final ControlEvent evt) {

            }

            @Override
            public Collection<EventSubscriber> getEventSubscribers() {
                final Collection<EventSubscriber> subscribers = new LinkedList<>();
                if (subscriber != null) {
                    subscribers.add(subscriber);
                }
                return subscribers;
            }

            @Override
            public Object getContextualData(final String name) {
                return null;
            }

            @Override
            public com.ericsson.component.aia.itpf.common.config.Configuration getEventHandlerConfiguration() {
                return new com.ericsson.component.aia.itpf.common.config.Configuration() {

                    @Override
                    public String getStringProperty(final String arg0) {
                        return configParams.get(arg0);
                    }

                    @Override
                    public Integer getIntProperty(final String arg0) {
                        return null;
                    }

                    @Override
                    public Boolean getBooleanProperty(final String arg0) {
                        return null;
                    }

                    @Override
                    public Map<String, Object> getAllProperties() {
                        return null;
                    }
                };
            }
        };
        return ctx;
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        initHornetQServer();
        subscriber = new EpsTestEventSubscriber();
    }

    /**
     * @throws Exception
     */
    private void initHornetQServer() throws Exception {
        // Step 1. Create the Configuration, and set the properties accordingly
        final Configuration configuration = new ConfigurationImpl();
        // we only need this for the server lock file
        configuration.setJournalDirectory("target/data/journal");
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);

        final TransportConfiguration transpConf = new TransportConfiguration(NettyAcceptorFactory.class.getName());

        final HashSet<TransportConfiguration> setTransp = new HashSet<TransportConfiguration>();
        setTransp.add(transpConf);

        configuration.setAcceptorConfigurations(setTransp);

        // Step 2. Create and start the server
        server = HornetQServers.newHornetQServer(configuration);
        server.start();
        final HashMap<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(TransportConstants.HOST_PROP_NAME, SERVER_HOST);
        connectionParams.put(TransportConstants.PORT_PROP_NAME, SERVER_PORT);
        serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams));
        factory = serverLocator.createSessionFactory();
        session = factory.createSession(false, true, true);
        session.createQueue(QUEUE_NAME, QUEUE_NAME);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        session.close();
        factory.close();
        server.stop();
        if (serverLocator != null) {
            serverLocator.close();
        }
    }

}
