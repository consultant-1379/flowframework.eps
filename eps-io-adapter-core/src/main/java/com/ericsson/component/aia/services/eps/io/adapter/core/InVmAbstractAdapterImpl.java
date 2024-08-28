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
package com.ericsson.component.aia.services.eps.io.adapter.core;

import com.ericsson.component.aia.services.eps.core.EpsComponentConstants;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.Adapter;

/**
 * Implementation of in-memory (local) IO adapter. This is used for connecting different flows deployed inside same EPS instance - in case when output
 * of one flow should be input to another. By using this IO adapter there is no performance penalty for exchanging events between flows.
 *
 * @author eborziv
 *
 */
public abstract class InVmAbstractAdapterImpl extends AbstractEventHandler implements Adapter {

    public static final String IN_VM_URI_PREFIX = EpsComponentConstants.LOCAL_IO_ADAPTER_PROTOCOL;

    protected String channelId;

    private volatile boolean destroyed;

    protected String getUri() {
        return getConfiguration().getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (destroyed) {
            log.debug("Component destroyed but still receiving events! Will not send {} downstream!", inputEvent);
            return;
        }
        if (log.isDebugEnabled()) {
            int subscriberCount = 0;
            if ((getEventHandlerContext() != null) && (getEventHandlerContext().getEventSubscribers() != null)) {
                subscriberCount = getEventHandlerContext().getEventSubscribers().size();
            }
            if (subscriberCount > 0) {
                log.debug("Local output adapter at named point [{}]. Sending event [{}] to {} subscribers in total", channelId, inputEvent,
                        subscriberCount);
            } else {
                log.debug("Local adapter at named point {} does not have any subscribers attached. It is possible that some of "
                        + "future flows will be attached to this local named point - but for now events will be lost!", channelId);
            }
        }
        sendToAllSubscribers(inputEvent);
    }

    @Override
    public void destroy() {
        destroyed = true;
        super.destroy();
    }

    @Override
    public boolean understandsURI(final String uri) {
        return (uri != null) && uri.startsWith(IN_VM_URI_PREFIX);
    }

    @Override
    protected void doInit() {
        channelId = getConfiguration().getStringProperty(EpsComponentConstants.LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME);
        if ((channelId == null) || channelId.isEmpty()) {
            throw new IllegalStateException("Was not able to find required property "
                    + EpsComponentConstants.LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME + " for adapter with uri " + getUri());
        }
        log.debug("Found {}={}", EpsComponentConstants.LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME, channelId);
        log.info("Initialized local adapter on named point {}", channelId);
    }

}
