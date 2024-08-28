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
package com.ericsson.component.aia.services.eps.io.adapter.hornetq.connection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.*;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.io.adapter.hornetq.HornetQAdapterException;

/**
 *
 * @author ealbeor Singleton which manages connections to HornetQ servers inside an EPS instance Care should be taken with this inside JEE
 */
public enum HornetQConnections {

    INSTANCE;

    private final ConcurrentMap<HornetQConnectionInfo, HornetQSharedSessionFactory> sessionFactories = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(HornetQConnections.class);

    /**
     * Gets the session factory from {@link HornetQConnectionInfo} if already available, otherwise return new one, .
     *
     * @param connectionInfo
     *            the connection info
     * @return the session factory
     */
    public synchronized ClientSessionFactory getSessionFactory(final HornetQConnectionInfo connectionInfo) {

        HornetQSharedSessionFactory sharedSessionFactory = sessionFactories.get(connectionInfo);
        if (sharedSessionFactory == null) {
            sharedSessionFactory = newSharedSessionFactory(connectionInfo);
            sessionFactories.put(connectionInfo, sharedSessionFactory);
        }
        return sharedSessionFactory.getSessionFactory();
    }

    /**
     * Destroy the sharedSessionFactory if available.
     *
     * @param connectionInfo
     *            the connection info
     */
    public synchronized void destroy(final HornetQConnectionInfo connectionInfo) {

        final HornetQSharedSessionFactory sharedSessionFactory = sessionFactories.get(connectionInfo);
        if (sharedSessionFactory != null) {
            if (sharedSessionFactory.destroy()) {
                log.debug("Destroyed session factory for: {}", connectionInfo);
                sessionFactories.remove(connectionInfo);
            } else {
                log.debug("Pending adapters for: {}", connectionInfo);
            }
        }
    }

    /**
     * Create a new shared session factory from {@link HornetQConnectionInfo}.
     *
     * @param connectionInfo
     *            the connection info
     * @return the hornetQ shared session factory
     */
    private HornetQSharedSessionFactory newSharedSessionFactory(final HornetQConnectionInfo connectionInfo) {

        final HashMap<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(TransportConstants.HOST_PROP_NAME, connectionInfo.getHost());
        connectionParams.put(TransportConstants.PORT_PROP_NAME, Integer.valueOf(connectionInfo.getPort()));

        final ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(NettyConnectorFactory.class
                .getName(), connectionParams));
        serverLocator.setProducerWindowSize(connectionInfo.getWindowSizeMb());
        //reconnect by default
        serverLocator.setReconnectAttempts(-1);
        try {
            final ClientSessionFactory sessionFactory = serverLocator.createSessionFactory();
            log.debug("Created session factory for: {}", connectionInfo);
            return new HornetQSharedSessionFactory(serverLocator, sessionFactory);
        } catch (final Exception exc) {
            log.error("Exception while creating hornetq resources. Details: {}", exc.getMessage());
            throw new HornetQAdapterException(exc);
        }
    }
}
