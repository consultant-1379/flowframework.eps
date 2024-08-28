/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.io.adapter.jms;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * The Class JmsThreadLocal.
 *
 * @author esarlag
 */
class JmsConnectorWrapper {

    private static final Logger logger = LoggerFactory.getLogger(JmsConnectorWrapper.class);

    private static ConcurrentMap<String, Session> sessionList = new ConcurrentHashMap<>();

    private static final ThreadLocal<JmsConnector> userThreadLocal = new ThreadLocal<>();

    /**
     * Close all sessions.
     *
     */
    static void cleanup() {
        for (final Session session : sessionList.values()) {
            try {
                session.close();
            } catch (final JMSException e) {
                logger.info("Could not properly shut down JMS session {}", session.toString());
            }
        }
    }

    /**
     * Gets the thread local JmsConnector.
     *
     * @return thread local JmsConnector
     */
    static JmsConnector get() {
        return userThreadLocal.get();
    }

    /**
     * Gets the producer.
     *
     * @return thread local message producer
     */
    static MessageProducer getProducer() {
        return get().getProducer();
    }

    /**
     * Initialize thread local JMS connector.
     *
     * @param configuration
     *            the configuration
     */
    static void initialize(final Configuration configuration) {
        get().init(configuration);
        sessionList.put(Thread.currentThread().getName(), get().getSession());
        get().createProducer();

    }

    /**
     * Checks if JmsConnector is initialized.
     *
     * @return true, if is initialized
     */
    static boolean isInitialized() {
        return get() != null;
    }

    /**
     * Sets the thread local JMS connector.
     *
     * @param connector
     *            JMS connector
     */
    static void set(final JmsConnector connector) {
        userThreadLocal.set(connector);
    }

    /**
     * Unset thread local JMS connector.
     */
    static void unset() {
        userThreadLocal.remove();
    }
}
