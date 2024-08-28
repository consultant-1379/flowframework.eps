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
package com.ericsson.component.aia.services.eps.core.module.assembler.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.core.EpsComponentConstants;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventHandlerContext;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Class responsible to handle all {@link EpsEventHandlerContext} instances. There are two types local ones (connecting two flows inside same EPS
 * instance) and non-local ones (associated with module and connecting handlers inside one flow).
 *
 * @author eborziv
 */
public class ComponentContextHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentContextHandler.class);

    // these contexts are per module (all non-local contexts)
    private static final Map<String, Map<String, EpsEventHandlerContext>> NON_LOCAL_CTX = new HashMap<String, Map<String, EpsEventHandlerContext>>();

    // local contexts are not per module - they are used to connect different
    // modules to each other, so we have a map per EPS instance
    private static final Map<String, EpsEventHandlerContext> LOCAL_IO_ADAPTER_CONTEXTS = new HashMap<String, EpsEventHandlerContext>();

    private static ComponentContextHandler instance;

    /**
     * Instantiates a new component context handler.
     */
    private ComponentContextHandler() {
    }

    static {
        instance = new ComponentContextHandler();
    }

    /**
     * Gets the single instance of ComponentContextHandler.
     *
     * @return single instance of ComponentContextHandler
     */
    public static synchronized ComponentContextHandler getInstance() {
        return instance;
    }

    /**
     * Gets the or create component context for component.
     *
     * @param moduleComponent
     *            the module component
     * @param module
     *            the module
     * @param componentHandler
     *            the component handler
     * @return the or create component context for component
     */
    public EpsEventHandlerContext getOrCreateComponentContextForComponent(final EpsModuleComponent moduleComponent, final EpsModule module,
            final EventInputHandler componentHandler) {
        if (moduleComponent == null) {
            throw new IllegalArgumentException("module component must not be null");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        if (componentHandler == null) {
            throw new IllegalArgumentException("component handler must not be null");
        }
        final String componentId = moduleComponent.getInstanceId();
        final EpsModuleComponentType type = moduleComponent.getComponentType();
        final String moduleId = module.getUniqueModuleIdentifier();
        final Map<String, EpsEventHandlerContext> moduleContexts = getOrCreateModuleNonLocalContexts(moduleId);
        if ((type == EpsModuleComponentType.INPUT_ADAPTER) || (type == EpsModuleComponentType.OUTPUT_ADAPTER)) {
            final EpsEventHandlerContext ctx = getContextForLocalIOAdapters(moduleComponent, componentHandler, module);
            if (ctx != null) {
                // after we handled local IO adapter we also add it to non-local
                // map
                moduleContexts.put(componentId, ctx);
                return ctx;
            }
        }
        // else
        EpsEventHandlerContext context = moduleContexts.get(componentId);
        if (context == null) {
            context = getInitialContextForComponent(componentId, module);
            moduleContexts.put(componentId, context);
        }
        return context;
    }

    /**
     * Gets the or create module non local contexts.
     *
     * @param moduleId
     *            the module id
     * @return the or create module non local contexts
     */
    private Map<String, EpsEventHandlerContext> getOrCreateModuleNonLocalContexts(final String moduleId) {
        Map<String, EpsEventHandlerContext> moduleContexts = NON_LOCAL_CTX.get(moduleId);
        if (moduleContexts == null) {
            moduleContexts = new HashMap<>();
            NON_LOCAL_CTX.put(moduleId, moduleContexts);
        }
        return moduleContexts;
    }

    /**
     * Gets the component context for component by id.
     *
     * @param component
     *            the component
     * @return the component context for component by id
     */
    public EpsEventHandlerContext getComponentContextForComponentById(final EpsModuleComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        final String moduleId = component.getModule().getUniqueModuleIdentifier();
        final String componentId = component.getInstanceId();
        final Map<String, EpsEventHandlerContext> moduleContexts = NON_LOCAL_CTX.get(moduleId);
        EpsEventHandlerContext ctx = null;
        if (moduleContexts != null) {
            ctx = moduleContexts.get(componentId);
        }
        if (ctx == null) {
            ctx = LOCAL_IO_ADAPTER_CONTEXTS.get(componentId);
        }
        return ctx;
    }

    /**
     * Undeploy module.
     *
     * @param moduleId
     *            the module id
     */
    public void undeployModule(final String moduleId) {
        if (moduleId != null) {
            LOG.debug("Removing cached components for module {}", moduleId);
            NON_LOCAL_CTX.remove(moduleId);
            LOG.debug("Removed all cached components for module {}", moduleId);
        }
    }

    /**
     * Gets the initial context for component.
     *
     * @param componentId
     *            the component id
     * @param module
     *            the module
     * @return the initial context for component
     */
    private EpsEventHandlerContext getInitialContextForComponent(final String componentId, final EpsModule module) {
        final EpsModuleComponent component = module.findComponentOrParentById(componentId);
        final Configuration componentConfig = component.getConfiguration();
        final EpsEventHandlerContext context = new EpsEventHandlerContext(componentConfig, module,
                componentId);
        return context;
    }

    /**
     * Gets the context for local io adapters.
     *
     * @param component
     *            the component
     * @param componentHandler
     *            the component handler
     * @param module
     *            the module
     * @return the context for local io adapters
     */
    private EpsEventHandlerContext getContextForLocalIOAdapters(final EpsModuleComponent component, final EventInputHandler componentHandler,
            final EpsModule module) {
        LOG.trace("trying to find context for local IO adapter {}", component);
        final Configuration componentConfig = component.getConfiguration();
        final EpsModuleComponentType type = component.getComponentType();
        final String uri = componentConfig.getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME);
        final boolean isLocalAdapter = isLocalAdapter(uri);
        if (isLocalAdapter) {
            final String channelId = componentConfig.getStringProperty(EpsComponentConstants.LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME);
            if ((channelId == null) || channelId.isEmpty()) {
                throw new IllegalStateException("Was not able to find required property by name "
                        + EpsComponentConstants.LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME);
            }
            final String adapterId = component.getInstanceId();
            LOG.debug("Local adapter {} - channel is [{}]", adapterId, channelId);
            LOG.trace("Existing local contexts are {}", LOCAL_IO_ADAPTER_CONTEXTS);
            EpsEventHandlerContext existingContext = LOCAL_IO_ADAPTER_CONTEXTS.get(channelId);
            if (existingContext == null) {
                existingContext = new EpsEventHandlerContext(componentConfig, module, adapterId);
                LOCAL_IO_ADAPTER_CONTEXTS.put(channelId, existingContext);
                LOG.debug("Created local context {} for [{}]", existingContext, channelId);
            } else {
                LOG.debug("Found existing local context for channel [{}], context = [{}]", channelId, existingContext);
            }
            if (type == EpsModuleComponentType.INPUT_ADAPTER) {
                handleInputAdapter(component, componentHandler, uri, channelId, adapterId, existingContext);
            } else if (type == EpsModuleComponentType.OUTPUT_ADAPTER) {
                LOG.trace("For output adapter {} returning context {}", component, existingContext);
                return existingContext;
            }
        }
        return null;
    }

    /**
     * Handle input adapter.
     *
     * @param component
     *            the component
     * @param componentHandler
     *            the component handler
     * @param uri
     *            the uri
     * @param channelId
     *            the channel id
     * @param adapterId
     *            the adapter id
     * @param existingContext
     *            the existing context
     */
    private void handleInputAdapter(final EpsModuleComponent component, final EventInputHandler componentHandler, final String uri,
            final String channelId, final String adapterId, final EpsEventHandlerContext existingContext) {
        if (componentHandler == null) {
            throw new IllegalStateException("Was not able to find component handler for " + component);
        }
        LOG.trace("Adding {} to be listener for {}", componentHandler, uri);
        existingContext.addEventSubscriber(componentHandler, adapterId);
        if (LOG.isDebugEnabled()) {
            final int numberOfSubscribersSoFar = existingContext.getEventSubscribers() != null ? existingContext.getEventSubscribers().size() : 0;
            LOG.debug("Added {} as subscriber to local adapter on named point {}."
                    + " There are {} subscribers (so far) on this named point and those are {}", componentHandler, channelId,
                    numberOfSubscribersSoFar, existingContext.getEventSubscribers());
        }
    }

    /**
     * Checks if is local adapter.
     *
     * @param adapterUri
     *            the adapter uri
     * @return true, if is local adapter
     */
    private boolean isLocalAdapter(final String adapterUri) {
        return adapterUri.startsWith(EpsComponentConstants.LOCAL_IO_ADAPTER_PROTOCOL);
    }

}
