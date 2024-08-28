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
package com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.sdk.eventbus.model.classic.EventSenderBean;
import com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent.TestInputModeledEvent;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;

public class TestModeledEventGenerator {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final EventSender<TestInputModeledEvent> modeledEventSender = new EventSenderBean<TestInputModeledEvent>(TestInputModeledEvent.class);

    public void sendModeledMessage(final String msg) {
        final TestInputModeledEvent modeledEventInstance = new TestInputModeledEvent();
        sendModeledEvent(msg, modeledEventInstance);
    }

    public void sendModeledMessage(final String msg, final Class<?> clazz) throws ReflectiveOperationException {
        final TestInputModeledEvent modeledEventInstance = (TestInputModeledEvent) clazz.newInstance();
        sendModeledEvent(msg, modeledEventInstance);
    }

    private void sendModeledEvent(final String msg, final TestInputModeledEvent modeledEventInstance) {
        modeledEventInstance.setMyTestValue(msg);
        LOGGER.trace("TestModeledEventGenerator - About to send modeled event {}", modeledEventInstance.toString());
        try {
            modeledEventSender.send(modeledEventInstance);
        } catch (final Exception e) {
            LOGGER.error("TestModeledEventGenerator - Exception sending modeled event {}", e);
            throw e;
        }
        LOGGER.trace("TestModeledEventGenerator - Sent modeled event {}", modeledEventInstance.toString());
    }

}
