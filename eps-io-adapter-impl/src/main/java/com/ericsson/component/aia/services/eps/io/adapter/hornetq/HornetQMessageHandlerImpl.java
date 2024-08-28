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
package com.ericsson.component.aia.services.eps.io.adapter.hornetq;

import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.io.adapter.util.DefaultJavaSerializer;
import com.ericsson.component.aia.services.eps.io.adapter.util.ObjectSerializer;

/**
 *
 * @author eborziv
 *
 */
public class HornetQMessageHandlerImpl implements MessageHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ObjectSerializer serializer = new DefaultJavaSerializer();

    private final HornetQInputAdapter inputAdapter;

    /**
     * Instantiates a new hornetq message handler impl from {@link HornetQInputAdapter}.
     *
     * @param inputAdapter
     *            the input adapter
     */
    public HornetQMessageHandlerImpl(final HornetQInputAdapter inputAdapter) {
        if (inputAdapter == null) {
            throw new IllegalArgumentException("adapter must not be null");
        }
        this.inputAdapter = inputAdapter;
    }

    private void sendMessageToAllSubscribers(final Object evt) {
        inputAdapter.sendEvent(evt);
        log.trace("Sent message {} to subscribers", evt);
    }

    @Override
    public void onMessage(final ClientMessage message) {
        log.trace("Received message {}", message);
        final byte[] messageBytes = new byte[message.getBodySize()];
        message.getBodyBuffer().readBytes(messageBytes);
        try {
            final Object messageObject = serializer.bytesToObject(messageBytes);
            if (messageObject != null) {
                sendMessageToAllSubscribers(messageObject);
            } else {
                log.warn("Message received {} is converted to null object. Will not send it downstream!", message);
            }
            message.acknowledge();
        } catch (final Exception exc) {
            log.error("Exception while parsing received message. Details: {}", exc.getMessage());
            exc.printStackTrace();
        }
    }
}
