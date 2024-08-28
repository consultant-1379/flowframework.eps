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

import java.util.ArrayList;
import java.util.Collection;

import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class StubbedContext implements EventHandlerContext {

    private final Configuration configuration;

    private final EventSubscriber subscriber;

    public StubbedContext(final Configuration configuration, final EventSubscriber subscriber) {
        this.configuration = configuration;
        this.subscriber = subscriber;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        final Collection<EventSubscriber> subscribers = new ArrayList<EventSubscriber>();
        subscribers.add(subscriber);
        return subscribers;
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {

    }

    @Override
    public Object getContextualData(final String name) {

        if (EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME.equals(name)) {
            return (new EpsStatisticsRegisterImpl());
        }

        return null;
    }

}
