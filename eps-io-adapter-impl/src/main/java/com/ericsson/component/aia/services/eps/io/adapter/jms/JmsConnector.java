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
package com.ericsson.component.aia.services.eps.io.adapter.jms;

import java.util.Properties;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * The Class JmsConnector manages all the configuration parameters requested by Jms and the relative lyfecycle.
 */
class JmsConnector {

    private static final String JMS_DESTINATION_NAME = "jmsDestinationName";
    private static final String JMS_DESTINATION_TYPE_NAME = "jmsDestinationType";
    private static final String JNDI_JMS_FACTORY_NAME = "jndiJmsConnectionFactory";
    private static final String JNDI_PROPERTIES_PREFIX = "java.naming";

    private static final String TYPE_QUEUE = "Queue";
    private static final String TYPE_TOPIC = "Topic";

    private Configuration configuration;

    private Connection connection;
    private String destinationName;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private MessageProducer producer;
    private Session session;

    /**
     * Close session.
     */
    void closeSession() {
        try {
            if (producer != null) {
                producer.close();
            }
            session.close();
            connection.close();
        } catch (final JMSException e) {
            throw new JmsAdapterException("Could not close JMS session.", e);
        }
    }

    /**
     * Creates the consumer for specific destination.
     *
     * @return the message consumer
     */
    MessageConsumer createConsumer() {
        try {
            final Destination destination = createDestination(session);
            final MessageConsumer consumer = session.createConsumer(destination);
            log.debug("Successfully created consumer for destination {}.", destinationName);
            return consumer;
        } catch (final JMSException e) {
            throw new JmsAdapterException("Could not create JMS Consumer.", e);
        }
    }

    /**
     * Creates the object message.
     *
     * @return the object message
     */
    ObjectMessage createObjectMessage() {
        try {
            return session.createObjectMessage();
        } catch (final JMSException e) {
            throw new JmsAdapterException("Could not create ObjectMessage.", e);
        }
    }

    /**
     * Creates the producer for specific destination.
     *
     * @return the message producer
     */
    MessageProducer createProducer() {
        try {
            final Destination destination = createDestination(session);
            producer = session.createProducer(destination);
            log.debug("Successfully created producer for destination {}.", destinationName);
            return producer;
        } catch (final JMSException e) {
            throw new JmsAdapterException("Could not create JMS Producer.", e);
        }
    }

    String getDestinationName() {
        return destinationName;
    }

    MessageProducer getProducer() {
        return producer;
    }

    /**
     * @return the session
     */
    Session getSession() {
        return session;
    }

    /**
     * Inits the JmsConnector from {@link Configuration}, it read and validate configuration and properties, create context and connection, start the
     * connection and create the session.
     *
     * @param configuration
     *            the configuration
     */
    void init(final Configuration configuration) {
        this.configuration = configuration;
        final String jndiName = this.configuration.getStringProperty(JNDI_JMS_FACTORY_NAME);
        if ((jndiName == null) || "".equals(jndiName)) {
            throw new IllegalArgumentException("ConnectionFactory jndi name must not be empty.");
        }
        try {
            final Properties props = parseJNDIProperties();
            final InitialContext context = new InitialContext(props);
            log.debug("Looking up [{}] from initial context", jndiName);
            log.trace("JNDI properties are {}", props);
            final ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(jndiName);
            connection = connectionFactory.createConnection();
            log.debug("Successfully created connection. Will start it now...");
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (final NamingException e) {
            throw new JmsAdapterException("Could not find JMS connection by name [" + jndiName + "]", e);
        } catch (final JMSException e) {
            throw new JmsAdapterException("Exception while configuring JMS connection.", e);
        }
    }

    /**
     * Check if initialization method has already been invoked on instance
     *
     * @return true if configuration is not null
     */
    boolean isInitialized() {
        return configuration != null;
    }

    /**
     * @param session
     *            the session to set
     */
    void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Creates the jms destination (queue or topic) from {@link Session}.
     *
     * @param session
     * @return the destination
     */
    private Destination createDestination(final Session session) {
        final String destinationType = configuration.getStringProperty(JMS_DESTINATION_TYPE_NAME);
        if ((destinationType == null) || "".equals(destinationType)) {
            throw new IllegalArgumentException("Destination type must not be empty.");
        }
        destinationName = configuration.getStringProperty(JMS_DESTINATION_NAME);
        if ((destinationName == null) || "".equals(destinationName)) {
            throw new IllegalArgumentException("Destination name must not be empty.");
        }
        try {
            Destination destination = null;
            switch (destinationType) {
                case TYPE_QUEUE:
                    destination = session.createQueue(destinationName);
                    break;
                case TYPE_TOPIC:
                    destination = session.createTopic(destinationName);
                    break;
                default:
                    throw new IllegalArgumentException("Informed destination type not supported.");
            }
            return destination;
        } catch (final JMSException e) {
            log.error("Exception while creating JMS destination [" + destinationName + "]", e);
            throw new JmsAdapterException("Could not create JMS Destination [" + destinationName + "]", e);
        }
    }

    /**
     * Parses the jndi properties.
     *
     * @return the properties
     */
    private Properties parseJNDIProperties() {
        final Properties properties = new Properties();
        for (final String propertyName : configuration.getAllProperties().keySet()) {
            if (propertyName.startsWith(JNDI_PROPERTIES_PREFIX)) {
                final String stringPropertyVal = configuration.getStringProperty(propertyName);
                properties.put(propertyName, stringPropertyVal);
                log.debug("Found JNDI configuration: {}, value: {}", propertyName, stringPropertyVal);
            }
        }
        return properties;
    }

}
