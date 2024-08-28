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

import com.ericsson.oss.itpf.sdk.eventbus.model.classic.EventSenderBean;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.ModeledEvent;

/**
 * Allows an EPS flow to send {@code ModeledEvent}s to the SDK Modeled EventBus.
 *
 * <p>
 * The ModeledEventBusOutputAdapter is configured via attributes in the flow descriptor. All configuration parameters are read as strings.
 *
 * <p>
 * Configuration parameters:
 * <dl>
 *
 * <dt>modeledEventClassName</dt>
 * <dd>The {@link ModeledEvent} name. This should be the full canonical name, not the event simple name, and the class should be available in the
 * classpath.<br>
 * Cannot be empty.</dd>
 *
 * <dt>channelId</dt>
 * <dd>The channel to use when sending event, overriding default specified in model (@link ChannelDefinition.channelURI()). <br>
 * Can be empty, in which case the default channel specified in model is used.</dd>
 *
 * </dl>
 */
public class ModeledEventBusOutputAdapter extends AbstractModeledEventBusAdapter implements OutputAdapter {

    public static final String MODEL_EV_CHANNEL = "channelId";

    private EventSender<Object> modeledEventSender;

    @Override
    public void onEvent(final Object event) {

        if (event != null && !modeledEventClass.isAssignableFrom(event.getClass())) {
            log.error("Received event  is of the wrong type: {}", event.getClass().getName());
            throw new ModeledEventBusAdapterException("Received event  is of the wrong type");
        }

        if (channelId == null) {
            modeledEventSender.send(event);
        } else {
            modeledEventSender.send(event, channelId);
        }
        updateStatisticsWithEventReceived();
        log.trace("Sent event to registered destination. Event: {}", event);
    }

    @Override
    public String toString() {
        return "ModeledEventBusOutputAdapter [modeledEventClassName=" + modeledEventClassName + ", channelId=" + channelId + "]";
    }

    @Override
    protected void doInit() {

        readConfigParams();
        readModeledEventClass();

        modeledEventSender = new EventSenderBean<Object>(modeledEventClass);
        log.debug("Created EventSenderBean for {}", modeledEventClass.getName());
        initialiseStatistics();
    }

    @Override
    protected void readConfigParams() {

        channelId = getStringProperty(MODEL_EV_CHANNEL);
        log.debug("Channel id is configured to {}", channelId);
    }

}
