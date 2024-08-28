/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.builtin.components.utils;

import java.util.Collection;
import java.util.LinkedList;

import com.ericsson.component.aia.services.eps.core.component.config.EpsEventSubscriberImpl;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.*;

public class StubbedEventHandlerContext implements EventHandlerContext {

    private final Configuration configuration;

    private final EventInputHandler eventInputHandler;

    private final String identifier;

    /**
     * 
     */
    public StubbedEventHandlerContext(final Configuration configuration, final String identifier, final EventInputHandler eventInputHandler) {
        this.configuration = configuration;
        this.identifier = identifier;
        this.eventInputHandler = eventInputHandler;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        final Collection<EventSubscriber> receivers = new LinkedList<>();
        final EpsEventSubscriberImpl subscriber = new EpsEventSubscriberImpl(identifier, eventInputHandler);
        receivers.add(subscriber);
        return receivers;
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getContextualData(final String name) {
        // TODO Auto-generated method stub
        return null;
    }

}
