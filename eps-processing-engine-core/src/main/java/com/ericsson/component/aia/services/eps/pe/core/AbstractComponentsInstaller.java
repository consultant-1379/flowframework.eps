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
package com.ericsson.component.aia.services.eps.pe.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleComponentInstaller;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * This Class is for AbstractComponentsInstaller management.
 *
 * @see EpsModuleComponentInstaller
 */
public abstract class AbstractComponentsInstaller implements EpsModuleComponentInstaller {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Map<String, Map<String, EventInputHandler>> loadedComponentsPerModule = new HashMap<>();

    /**
     * Adds the component to module.
     *
     * @param moduleId
     *            The module id as string
     * @param componentId
     *            The component id as string
     * @param component
     *            The component {@link EventInputHandler}
     */
    protected synchronized void addComponentToModule(final String moduleId, final String componentId, final EventInputHandler component) {
        if ((moduleId == null) || moduleId.isEmpty()) {
            throw new IllegalArgumentException("Module identifier must not be null or empty");
        }
        if (component == null) {
            throw new IllegalArgumentException("Unable to add null component to module.");
        }
        log.debug("Trying to add {} to module with id [{}]", component, moduleId);
        Map<String, EventInputHandler> moduleComponents = loadedComponentsPerModule.get(moduleId);
        if (moduleComponents == null) {
            moduleComponents = new HashMap<>();
        }
        moduleComponents.put(componentId, component);
        loadedComponentsPerModule.put(moduleId, moduleComponents);
        log.debug("Added component {} to module with id [{}]", component, moduleId);
        log.trace("Loaded components for module {} are {}", moduleId, moduleComponents);
    }

    /**
     * Gets the event input handler if previously loaded from the specific component {@link EpsModuleComponent}.
     *
     * @param component
     *            the component
     * @return the event input handler if previously loaded
     */
    protected EventInputHandler getEventInputHandlerIfPreviouslyLoaded(final EpsModuleComponent component) {
        final String componentId = component.getInstanceId();
        log.debug("Trying to find component by id {}", componentId);
        final String moduleId = component.getModule().getUniqueModuleIdentifier();
        final Map<String, EventInputHandler> previouslyLoadedModuleComponents = loadedComponentsPerModule.get(moduleId);
        if (previouslyLoadedModuleComponents != null) {
            final EventInputHandler previouslyLoadedComponent = previouslyLoadedModuleComponents.get(componentId);
            if (previouslyLoadedComponent != null) {
                log.debug("Found previously loaded component {}", previouslyLoadedComponent);
                return previouslyLoadedComponent;
            }
        }
        log.debug("Was not able to find previously loaded component with id {}", componentId);
        return null;
    }

    @Override
    public void uninstallModule(final String moduleId) {
        log.debug("Uninstalling module {}", moduleId);
        if ((moduleId == null) || moduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("moduleId must not be null or empty");
        }
        final Map<String, EventInputHandler> moduleComponents = loadedComponentsPerModule.get(moduleId);
        log.trace("In module {} found components {} to be uninstalled", moduleId, moduleComponents);
        if (moduleComponents != null) {
            for (final EventInputHandler eih : moduleComponents.values()) {
                try {
                    log.debug("Notifying component {} of destroy event", eih);
                    eih.destroy();
                    log.info("Destroyed component {}", eih);
                } catch (final Exception exc) {
                    log.error("Caught exception while notifying component of destroy event. Details {}", exc.getMessage());
                    exc.printStackTrace();
                }
            }
            moduleComponents.clear();
            loadedComponentsPerModule.remove(moduleId);
            log.info("Removed all components for module [{}]", moduleId);
        }
    }

    @Override
    public EventInputHandler getOrInstallComponentAndReferencePort(final EpsModuleComponent component, final String portName) {
        throw new UnsupportedOperationException("Referencing port not allowed for this type of installer!");
    }

}
