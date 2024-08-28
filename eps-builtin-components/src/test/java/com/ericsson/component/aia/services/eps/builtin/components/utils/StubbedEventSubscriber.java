/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.builtin.components.utils;

import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class StubbedEventSubscriber implements EventSubscriber {

    private final String identifier;

    private Object receviedEvent;

    private boolean sleepOnEvent;

    /**
     * 
     */
    public StubbedEventSubscriber(final boolean sleepOnEvent) {
        this.sleepOnEvent = sleepOnEvent;
        identifier = "StubbedEventSubscriber";
    }

    public StubbedEventSubscriber(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void sendEvent(final Object receviedEvent) {
        if (sleepOnEvent) {
            sleepforTwoSecond();
        }
        this.receviedEvent = receviedEvent;
    }

    /**
     * try to slow down consumber
     */
    private void sleepforTwoSecond() {
        try {
            Thread.sleep(2);
        } catch (final InterruptedException e) {
        }
    }

    /**
     * @return the event
     */
    public Object getReceviedEvent() {
        return receviedEvent;
    }

}
