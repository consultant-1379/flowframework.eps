/*------------------------------------------------------------------------------
 *******************************************************************************
 * © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.io.adapter.modev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.eventbus.classic.EMessageListener;
import com.ericsson.oss.itpf.sdk.eventbus.model.ModeledEvent;

/**
 * @author epiemir
 *
 */
@SuppressWarnings("rawtypes")
class ModeledEventBusEListener implements EMessageListener<ModeledEvent> {

    private final ModeledEventBusInputAdapter inputAdapter;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Class modeledEventClass;

    /**
     * Constructs a {@code ModeledEventBusEListener} given an input adapter
     *
     * @param inputAdapter
     *            input adapter
     * @param modeledEventClass
     *            it will be used to extract an instance of the requested modeled event class type
     */
    public ModeledEventBusEListener(final ModeledEventBusInputAdapter inputAdapter, final Class modeledEventClass) {
        if (inputAdapter == null) {
            throw new IllegalArgumentException("inputAdapter must not be null");
        }
        if (modeledEventClass == null) {
            throw new IllegalArgumentException("modeledEventClass must not be null");
        }

        this.inputAdapter = inputAdapter;
        this.modeledEventClass = modeledEventClass;
    }

    @Override
    public void onMessage(final ModeledEvent modeledEvent) {

        log.trace("Listener Received message {}", modeledEvent);

        if (modeledEvent != null) {
            final Object extractedEvent = modeledEvent.extractAs(modeledEventClass);
            inputAdapter.sendEvent(extractedEvent);
            log.trace("Listener Sent message {} to subscribers", extractedEvent);
        }

    }
}
