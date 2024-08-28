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
import com.hazelcast.core.MessageListener;

/**
 *
 * @author eborziv
 *
 */
class HazelcastEventListener implements MessageListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final HazelcastInputAdapter hazelcastInputAdapter;

    private volatile boolean isDestroyed;

    /**
     * Instantiates a new hazelcast event listener from {@link HazelcastInputAdapter}.
     *
     * @param hazelcastInputAdapter
     *            the hazelcast input adapter
     */
    HazelcastEventListener(final HazelcastInputAdapter hazelcastInputAdapter) {
        if (hazelcastInputAdapter == null) {
            throw new IllegalArgumentException("adapter must not be null");
        }
        this.hazelcastInputAdapter = hazelcastInputAdapter;
    }

    @Override
    public void onMessage(final Message msg) {
        if (isDestroyed) {
            log.warn("Listener destroyed but still receiving messages. Will not forward them further!");
            return;
        }
        final EventSenderRunnable esr = new EventSenderRunnable(msg, hazelcastInputAdapter);
        hazelcastInputAdapter.executorService.execute(esr);
    }

    /**
     * Destroy listener.
     */
    void destroyListener() {
        isDestroyed = true;
        log.info("Destroying hazelcast event listener, will not process any more messages!");
    }

}