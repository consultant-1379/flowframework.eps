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
package com.ericsson.component.aia.services.eps.io.adapter.hc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Message;

/**
 *
 * @author eborziv
 *
 */
final class EventSenderRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EventSenderRunnable.class);

    private Message msg;

    private final HazelcastInputAdapter adapter;

    /**
     * Instantiates a new event sender runnable.
     *
     * @param msg
     *            the message {@link Message}
     * @param adapter
     *            {@link HazelcastInputAdapter}, must be not null.
     */
    public EventSenderRunnable(final Message msg, final HazelcastInputAdapter adapter) {
        this.msg = msg;
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter must not be null");
        }
        this.adapter = adapter;
    }

    @Override
    public void run() {
        try {
            final Object obj = msg.getMessageObject();
            msg = null;
            adapter.sendEvent(obj);
        } catch (final Exception exc) {
            LOG.error("Exception while processing hazelcast message. Details: {}", exc.getMessage());
            LOG.error("Adapter is {}, message is {}", adapter, msg);
            exc.printStackTrace();
        }
    }

}