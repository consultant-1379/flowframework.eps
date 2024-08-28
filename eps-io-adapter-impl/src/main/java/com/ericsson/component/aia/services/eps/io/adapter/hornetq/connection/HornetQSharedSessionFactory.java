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

import java.util.concurrent.atomic.AtomicInteger;

import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;

/**
 * A factory for creating HornetQSharedSession objects.
 */
class HornetQSharedSessionFactory {

    private final ServerLocator serverLocator;

    private final ClientSessionFactory sessionFactory;

    private final AtomicInteger users = new AtomicInteger(0);

    /**
     * Instantiates a new hornetQ shared session factory.
     *
     * @param serverLocator
     *            the server locator
     * @param sessionFactory
     *            the session factory
     *
     * @see ServerLocator
     * @see ClientSessionFactory
     *
     */
    HornetQSharedSessionFactory(final ServerLocator serverLocator, final ClientSessionFactory sessionFactory) {
        super();
        this.serverLocator = serverLocator;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Gets the session factory.
     *
     * @return the session factory
     */
    ClientSessionFactory getSessionFactory() {
        users.incrementAndGet();
        return sessionFactory;
    }

    /**
     * Destroy the session factory if there are no more users.
     *
     * @return true, if successful
     */
    boolean destroy() {

        final int pendingUsers = users.decrementAndGet();
        if (pendingUsers == 0) {
            sessionFactory.close();
            serverLocator.close();
            return true;
        }
        return false;
    }
}
