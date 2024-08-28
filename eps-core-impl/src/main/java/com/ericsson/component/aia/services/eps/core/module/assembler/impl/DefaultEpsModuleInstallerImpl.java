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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleComponentInstaller;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleInstaller;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventHandlerContext;
import com.ericsson.component.aia.services.eps.core.coordination.ClusteredHandler;
import com.ericsson.component.aia.services.eps.core.coordination.MonitorableHandler;
import com.ericsson.component.aia.services.eps.core.execution.EventFlow;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.itpf.common.*;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * The Class DefaultEpsModuleInstallerImpl.
 *
 * @author eborziv
 *
 * @see EpsModuleInstaller
 */
public class DefaultEpsModuleInstallerImpl implements EpsModuleInstaller {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EpsProvider provider;

    private final ComponentContextHandler componentCtxHandler;

    private final EpsStatisticsRegisterImpl epsStatisticsRegister = new EpsStatisticsRegisterImpl();

    private final boolean isStatisticsOn;

    /**
     * Instantiates a new default eps module installer impl.
     */
    public DefaultEpsModuleInstallerImpl() {
        provider = EpsProvider.getInstance();
        componentCtxHandler = ComponentContextHandler.getInstance();
        isStatisticsOn = isStatisticsOn();
    }

    /**
     * Used for testing only.
     *
     * @param epsProvider
     *            the eps provider
     * @param isStatisticsOn
     *            indicates if statistics should be enabled or not
     */
    DefaultEpsModuleInstallerImpl(final EpsProvider epsProvider, final boolean isStatisticsOn) {
        provider = epsProvider;
        componentCtxHandler = ComponentContextHandler.getInstance();
        this.isStatisticsOn = isStatisticsOn;
    }

    /**
     * Install component.
     *
     * @param module
     *            the module
     * @param componentId
     *            the component id
     * @return the event input handler
     */
    private EventInputHandler installComponent(final EpsModule module, final String componentId) {
        log.debug("Installing component {}", componentId);
        final String componentName = EpsModuleUtil.getComponentName(componentId);
        final String portName = EpsModuleUtil.getPortName(componentId);
        final EpsModuleComponent moduleComponent = module.getComponent(componentName);
        final EpsModuleComponentInstaller componentInstaller = provider.loadEpsModuleComponentInstaller(moduleComponent.getComponentType());
        if (componentInstaller == null) {
            throw new IllegalArgumentException("Was not able to find component installer for " + moduleComponent);
        }
        EventInputHandler component = null;
        if (!EpsUtil.isEmpty(portName)) {
            component = componentInstaller.getOrInstallComponentAndReferencePort(moduleComponent, portName);
        } else {
            component = componentInstaller.getOrInstallComponent(moduleComponent);
        }
        if (component instanceof Controllable) {
            log.debug("Detected controllable component {}", component);
            final Controllable controllableComponent = (Controllable) component;
            module.addControllableComponent(controllableComponent, componentId);
            log.trace("Controllable components found so far are {}", module.getControllableComponents());
        }
        return component;
    }

    /**
     * Convert component to event input handler.
     *
     * @param componentId
     *            the component id
     * @param module
     *            the module
     * @return the event input handler
     */
    private EventInputHandler convertComponentToEventInputHandler(final String componentId, final EpsModule module) {
        log.debug("Converting component [{}] to input handler", componentId);
        if (EpsModuleUtil.isPortReferenceComponentIdentifier(componentId)) {
            final String portName = EpsModuleUtil.getPortName(componentId);
            log.debug("{} is referencing port {}", componentId, portName);
        } else {
            log.debug("{} is not referencing any port", componentId);
            final EpsModuleComponent component = module.getComponent(componentId);
            if (component == null) {
                throw new IllegalStateException("Was not able to find component by id [" + componentId + "]");
            }
        }
        final EventInputHandler componentHandler = installComponent(module, componentId);
        return componentHandler;
    }

    @Override
    public void installEpsModuleComponents(final EpsModule module, final ModuleManager moduleManager) {
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        log.debug("Assembling {}", module);

        final Map<String, EventInputHandler> componentHandlers = createEventInputHandlersForComponents(module);
        log.debug("All registered components are {}", componentHandlers.keySet());
        for (final EventFlow flow : module.getEventFlows()) {
            log.trace("Assembling flow {}", flow);
            for (final String toComponentId : flow.getOutputComponentIdentifiers()) {
                // not used by has to be created
                final EpsModuleComponent toComponent = module.getComponent(toComponentId);
                final EventInputHandler toComponentHandler = componentHandlers.get(toComponentId);
                if (toComponentHandler == null) {
                    throw new IllegalStateException("Was not able to find TO component with id [" + toComponentId + "]");
                }

                componentCtxHandler.getOrCreateComponentContextForComponent(toComponent, module, toComponentHandler);

                final String fromComponentId = flow.getInputComponentId();
                final EpsModuleComponent fromComponent = module.findComponentOrParentById(fromComponentId);
                fromComponent.setInstanceId(fromComponentId);
                final EventInputHandler fromComponentHandler = componentHandlers.get(fromComponentId);
                final EpsEventHandlerContext fromContext = componentCtxHandler.getOrCreateComponentContextForComponent(fromComponent, module,
                        fromComponentHandler);
                if (isStatisticsOn) {
                    log.debug("Statistics is on - statistics for every individual handler will be tracked automatically");
                    final StatisticsAwareEventInputHandlerDelegate delegate = new StatisticsAwareEventInputHandlerDelegate(toComponentHandler,
                            module, toComponentId);
                    fromContext.addEventSubscriber(delegate, toComponentId);
                } else {
                    log.debug("Statistics is off - statistics for every individual handler will NOT be tracked automatically");
                    fromContext.addEventSubscriber(toComponentHandler, toComponentId);
                }
                log.debug("Component with id [{}] receives events from component with id [{}]", toComponentId, fromComponentId);
            }
        }
        // first initialize all internal components, then output adapters and only then input adapters - to avoid receiving events too early
        // we want to ensure that all components are properly initialized before first event arrives into the flow
        final Set<String> alreadyInitializedComponents = new HashSet<>();
        initializeNonIOComponents(componentHandlers, module, alreadyInitializedComponents);
        initializeOutputAdapterComponents(componentHandlers, module, alreadyInitializedComponents);
        initializeInputAdapterComponents(componentHandlers, module, alreadyInitializedComponents);
        log.info("Successfully initialized all components in {}. All initialized components are {}", module, alreadyInitializedComponents);
    }

    /**
     * Initialize non io components.
     *
     * @param componentHandlers
     *            the component handlers
     * @param module
     *            the module
     * @param alreadyInitializedComponents
     *            the already initialized components
     */
    private void initializeNonIOComponents(final Map<String, EventInputHandler> componentHandlers, final EpsModule module,
                                           final Set<String> alreadyInitializedComponents) {
        log.debug("Trying to initialize all non-IO components in {}", componentHandlers);
        for (final String componentId : componentHandlers.keySet()) {
            final EpsModuleComponent component = module.findComponentOrParentById(componentId);
            component.setInstanceId(componentId);
            final EpsModuleComponentType type = component.getComponentType();
            if ((type != EpsModuleComponentType.INPUT_ADAPTER) && (type != EpsModuleComponentType.OUTPUT_ADAPTER)) {
                log.debug("Initializing non-IO component [{}]", componentId);
                initializeSingleComponent(componentHandlers, componentId, component, alreadyInitializedComponents);
            }
        }
    }

    /**
     * Initialize output adapter components.
     *
     * @param componentHandlers
     *            the component handlers
     * @param module
     *            the module
     * @param alreadyInitializedComponents
     *            the already initialized components
     */
    private void initializeOutputAdapterComponents(final Map<String, EventInputHandler> componentHandlers, final EpsModule module,
                                                   final Set<String> alreadyInitializedComponents) {
        log.debug("Trying to initialize all output adapters in {}", componentHandlers);
        for (final String componentId : componentHandlers.keySet()) {
            final EpsModuleComponent component = module.findComponentOrParentById(componentId);
            component.setInstanceId(componentId);
            final EpsModuleComponentType type = component.getComponentType();
            if (type == EpsModuleComponentType.OUTPUT_ADAPTER) {
                log.debug("Initializing output adapter [{}]", componentId);
                initializeSingleComponent(componentHandlers, componentId, component, alreadyInitializedComponents);
            }
        }
    }

    /**
     * Initialize input adapter components.
     *
     * @param componentHandlers
     *            the component handlers
     * @param module
     *            the module
     * @param alreadyInitializedComponents
     *            the already initialized components
     */
    private void initializeInputAdapterComponents(final Map<String, EventInputHandler> componentHandlers, final EpsModule module,
                                                  final Set<String> alreadyInitializedComponents) {
        log.debug("Trying to initialize all input adapters in {}", componentHandlers);
        for (final String componentId : componentHandlers.keySet()) {
            final EpsModuleComponent component = module.findComponentOrParentById(componentId);
            component.setInstanceId(componentId);
            final EpsModuleComponentType type = component.getComponentType();
            if (type == EpsModuleComponentType.INPUT_ADAPTER) {
                log.debug("Initializing input adapter [{}]", componentId);
                initializeSingleComponent(componentHandlers, componentId, component, alreadyInitializedComponents);
            }
        }
    }

    /**
     * Initialize single component.
     *
     * @param componentHandlers
     *            the component handlers
     * @param componentId
     *            the component id
     * @param component
     *            the component
     * @param alreadyInitializedComponents
     *            the already initialized components
     */
    private void initializeSingleComponent(final Map<String, EventInputHandler> componentHandlers, final String componentId,
                                           final EpsModuleComponent component, final Set<String> alreadyInitializedComponents) {
        if (alreadyInitializedComponents.contains(componentId)) {
            log.info("Component [{}] already initialized. Skipping it!", componentId);
            return;
        }
        log.debug("Initializing component {} - previously initialized components {}", componentId, alreadyInitializedComponents);
        final EpsEventHandlerContext ctx = componentCtxHandler.getComponentContextForComponentById(component);
        if (ctx == null) {
            throw new IllegalStateException("Was not able to find context for component with id " + componentId);
        }
        final EventInputHandler eventInputHandler = componentHandlers.get(componentId);
        log.info("Initializing {} with {}", componentId, ctx);
        try {
            eventInputHandler.init(ctx);
            alreadyInitializedComponents.add(componentId);
            handleCoordinationComponents(component, eventInputHandler);
        } catch (final Exception exc) {
            log.error("Caught exception while initializing component [{}]. Details: {}."
                    + " Will invoke destroy() method on this component and propagate exception!", componentId, exc.getMessage(), exc);
            try {
                eventInputHandler.destroy();
                log.debug("destroy() method was successfully invoked on {}", eventInputHandler);
            } catch (final Exception destroyExc) {
                log.error("There was exception while invoking destroy() method on {}. Details {}", eventInputHandler, destroyExc.getMessage());
                log.debug("There was exception while invoking destroy() method on {}", eventInputHandler, destroyExc);
            }
            throw new IllegalStateException(exc);
        }
        log.info("Successfully initialized component with id [{}]", componentId);
    }

    /**
     * Handle coordination components.
     *
     * @param component
     *            the component
     * @param eventInputHandler
     *            the event input handler
     */
    private void handleCoordinationComponents(final EpsModuleComponent component, final EventInputHandler eventInputHandler) {
        handleClustered(component, eventInputHandler);
        handleMonitorable(component, eventInputHandler);
    }

    /**
     * Handle clustered.
     *
     * @param component
     *            the component
     * @param handler
     *            the handler
     */
    private void handleClustered(final EpsModuleComponent component, final EventInputHandler handler) {
        if (handler instanceof Clustered) {
            new ClusteredHandler((Clustered) handler, component);
        }
    }

    /**
     * Handle monitorable.
     *
     * @param component
     *            the component
     * @param handler
     *            the handler
     */
    private void handleMonitorable(final EpsModuleComponent component, final EventInputHandler handler) {
        if (handler instanceof Monitorable) {
            new MonitorableHandler(component, (Monitorable) handler);
        }
    }

    /**
     * Creates the event input handlers for components.
     *
     * @param module
     *            the module
     * @return the map
     */
    private Map<String, EventInputHandler> createEventInputHandlersForComponents(final EpsModule module) {
        log.debug("Creating event input handlers for {}", module);
        final ConcurrentHashMap<String, EventInputHandler> componentEventInputHandlers = new ConcurrentHashMap<String, EventInputHandler>();
        for (final EventFlow flow : module.getEventFlows()) {
            final String fromComponentId = flow.getInputComponentId();
            log.debug("FROM component id [{}]", fromComponentId);

            if (!componentEventInputHandlers.containsKey(fromComponentId)) {
                final EventInputHandler fromComponentHandler = convertComponentToEventInputHandler(fromComponentId, module);
                componentEventInputHandlers.putIfAbsent(fromComponentId, fromComponentHandler);
            } else {
                log.debug("Component {} already installed. Skipping it!", fromComponentId);
            }

            for (final String toComponentId : flow.getOutputComponentIdentifiers()) {
                log.debug("TO component id [{}]", toComponentId);

                if (!componentEventInputHandlers.containsKey(toComponentId)) {
                    final EventInputHandler toComponentHandler = convertComponentToEventInputHandler(toComponentId, module);
                    componentEventInputHandlers.putIfAbsent(toComponentId, toComponentHandler);
                } else {
                    log.debug("Component {} already installed. Skipping it!", toComponentId);
                }
            }
        }
        log.info("Created event input handlers {}", componentEventInputHandlers);
        return componentEventInputHandlers;
    }

    private boolean isStatisticsOn() {
        return epsStatisticsRegister.isStatisticsOn();
    }
}
