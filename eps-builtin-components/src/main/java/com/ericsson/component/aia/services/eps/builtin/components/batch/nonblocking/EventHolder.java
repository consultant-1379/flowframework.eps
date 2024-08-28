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
package com.ericsson.component.aia.services.eps.builtin.components.batch.nonblocking;

import com.lmax.disruptor.EventFactory;

/**
 *
 * @author eborziv
 *
 */
public class EventHolder {

    public static final EventFactory<EventHolder> FACTORY = new EventFactory<EventHolder>() {
        @Override
        public EventHolder newInstance() {
            return new EventHolder();
        }
    };

    private Object event;

    public void setEvent(final Object event) {
        this.event = event;
    }

    /**
     * @return the event
     */
    public Object getEvent() {
        return event;
    }

}
