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

import org.hornetq.api.core.client.ClientSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.io.adapter.hornetq.connection.HornetQConnectionInfo;
import com.ericsson.component.aia.services.eps.io.adapter.hornetq.connection.HornetQConnections;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.Adapter;

/**
 * This class AbstractHornetQAdapter provides easy way to create HornetQAdapter.
 *
 * @see AbstractEventHandler
 * @see Adapter
 */
public abstract class AbstractHornetQAdapter extends AbstractEventHandler implements Adapter {

    public static final String HORNETQ_SERVER_ADDRESS_PROP_NAME = "hornetqServerIPAddress";
    public static final String HORNETQ_SERVER_PORT_PROP_NAME = "hornetqServerPort";
    public static final String WINDOW_SIZE_MB_PROP_NAME = "windowSizeMB";
    public static final String HORNETQ_CHANNEL_NAME = "channelName";

    protected static final String URI = "hornetq:/";

    private static final int DEFAULT_WINDOW_SIZE_MB = 50;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected HornetQConnectionInfo connectionInfo;
    protected String channelName;
    protected EventHandlerContext eventHandlerContext;

    /**
     * Inits the shared resources from the {@link EventHandlerContext}.
     *
     * @param ctx
     *            {@link EventHandlerContext}
     */
    protected void initSharedResources(final EventHandlerContext ctx) {
        eventHandlerContext = ctx;
        final int connectionWindowMb = getConfigurationParamIfExistsOrDefault(WINDOW_SIZE_MB_PROP_NAME, DEFAULT_WINDOW_SIZE_MB, false);

        connectionInfo = new HornetQConnectionInfo(ctx.getEventHandlerConfiguration().getStringProperty(HORNETQ_SERVER_ADDRESS_PROP_NAME), ctx
                .getEventHandlerConfiguration().getStringProperty(HORNETQ_SERVER_PORT_PROP_NAME), connectionWindowMb);
        channelName = ctx.getEventHandlerConfiguration().getStringProperty(HORNETQ_CHANNEL_NAME);
    }

    @Override
    public boolean understandsURI(final String uri) {
        return (uri != null) && uri.startsWith(URI);
    }

    /**
     * Gets the configuration param if exists or default.
     *
     * @param configParamName
     *            the configuration parameter name as string
     * @param defaultValue
     *            must be greater then 0
     * @param checkPositive
     *            specify if value positive check must be done.
     * @return the configuration param if exists or default otherwise it throws {@link IllegalArgumentException}
     *
     */
    protected int getConfigurationParamIfExistsOrDefault(final String configParamName, final int defaultValue, final boolean checkPositive) {
        final String numSessionsString = eventHandlerContext.getEventHandlerConfiguration().getStringProperty(configParamName);
        if ((numSessionsString != null) && !numSessionsString.trim().isEmpty()) {
            try {
                final int val = Integer.parseInt(numSessionsString);
                if (checkPositive && (val <= 0)) {
                    throw new IllegalArgumentException(configParamName + " value must be > 0");
                }
                return val;
            } catch (final Exception exc) {
                log.error("Exception while parsing {}. Details {}", configParamName, exc.getMessage());
            }
        }
        return defaultValue;
    }

    /**
     * Gets the session factory.
     *
     * @return the session factory {@link ClientSessionFactory}
     */
    protected ClientSessionFactory getSessionFactory() {
        return HornetQConnections.INSTANCE.getSessionFactory(connectionInfo);
    }

    /**
     * Free connection factory.
     */
    protected void freeConnectionFactory() {
        HornetQConnections.INSTANCE.destroy(connectionInfo);
    }

}
