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
package com.ericsson.component.aia.services.eps.component.module;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.services.eps.core.execution.EventFlow;

/**
 * The Class EpsModule.
 */
public class EpsModule {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String namespace;

    private String name;

    private String version;

    private final Map<String, EpsModuleComponent> moduleComponents = new HashMap<>();

    private final Map<String, EpsModuleHandlerComponent> moduleHandlers = new HashMap<>();

    private final Map<String, Controllable> controllableComponents = new HashMap<String, Controllable>();

    private final List<EventFlow> eventFlows = new ArrayList<EventFlow>();

    /**
     * Gets the namespace.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace.
     *
     * @param namespace            the namespace to set
     */
    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name            the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version            the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "EpsModule [namespace=" + namespace + ", name=" + name + ", version=" + version + ", eventFlows=" + eventFlows
                + ", moduleComponents=" + moduleComponents + "]";
    }

    /**
     * Gets the module components.
     *
     * @return the componentsIds
     */
    public Map<String, EpsModuleComponent> getModuleComponents() {
        return Collections.unmodifiableMap(moduleComponents);
    }

    /**
     * Gets the component.
     *
     * @param instanceId the instance id
     * @return the component
     */
    public EpsModuleComponent getComponent(final String instanceId) {
        if (instanceId == null || instanceId.isEmpty()) {
            throw new IllegalArgumentException("Id must not be null or empty");
        }
        return getModuleComponents().get(instanceId);
    }

    /**
     * Adds the module component.
     *
     * @param component the component
     */
    public void addModuleComponent(final EpsModuleComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null");
        }
        moduleComponents.put(component.getInstanceId(), component);
    }

    /**
     * Adds the module handler.
     *
     * @param handler the handler
     */
    public void addModuleHandler(final EpsModuleHandlerComponent handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        moduleHandlers.put(handler.getInstanceId(), handler);
    }

    /**
     * Adds the controllable component.
     *
     * @param controllable the controllable
     * @param componentIdentifier the component identifier
     */
    public void addControllableComponent(final Controllable controllable, final String componentIdentifier) {
        if (controllable == null) {
            throw new IllegalArgumentException("Controllable must not be null");
        }
        if (componentIdentifier == null || componentIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Component identifier must not be null or empty string");
        }
        controllableComponents.put(componentIdentifier, controllable);
    }

    /**
     * Gets the controllable components.
     *
     * @return the controllableComponents
     */
    public Set<Controllable> getControllableComponents() {
        final Set<Controllable> allControllableComponents = new HashSet<>();
        allControllableComponents.addAll(controllableComponents.values());
        return Collections.unmodifiableSet(allControllableComponents);
    }

    /**
     * Gets the event flows.
     *
     * @return the eventFlows
     */
    public List<EventFlow> getEventFlows() {
        return Collections.unmodifiableList(eventFlows);
    }

    /**
     * Gets the event flow by from uri.
     *
     * @param fromUri the from uri
     * @return the event flow by from uri
     */
    public EventFlow getEventFlowByFromUri(final String fromUri) {
        if (fromUri == null || fromUri.isEmpty()) {
            throw new IllegalArgumentException("from uri must not be null or empty");
        }
        for (final EventFlow eventFlow : eventFlows) {
            if (fromUri.equals(eventFlow.getInputComponentId())) {
                return eventFlow;
            }
        }
        return null;
    }

    /**
     * Adds the event flow.
     *
     * @param input the input
     * @param output the output
     */
    public synchronized void addEventFlow(final String input, final String output) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        final EventFlow existing = getEventFlowByFromUri(input);
        if (existing == null) {
            final EventFlow flow = new EventFlow();
            flow.setInputComponentId(input);
            flow.addOutputComponentIdentifier(output);
            eventFlows.add(flow);
        } else {
            existing.getOutputComponentIdentifiers().add(output);
        }
    }

    /**
     * Validate.
     */
    private void validate() {
        if (getNamespace() == null || getNamespace().isEmpty()) {
            throw new IllegalStateException("Module namespace must not be null or empty");
        }
        if (getName() == null || getName().isEmpty()) {
            throw new IllegalStateException("Module name must not be null or empty");
        }
        if (getVersion() == null || getVersion().isEmpty()) {
            throw new IllegalStateException("Module version must not be null or empty");
        }
    }

    /**
     * Gets the unique module identifier.
     *
     * @return the unique module identifier
     */
    public String getUniqueModuleIdentifier() {
        validate();
        return getNamespace() + "_" + getName() + "_" + getVersion();
    }

    /**
     * Gets the controllable component.
     *
     * @param componentIdentifier the component identifier
     * @return the controllable component
     */
    public Controllable getControllableComponent(final String componentIdentifier) {
        if (componentIdentifier == null || componentIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Component identifier must not be null or empty string");
        }
        return controllableComponents.get(componentIdentifier);
    }

    /**
     * Finds component in the module by provided identifier. In case of referencing ports it will find component itself.
     *
     * @param componentId the component id
     * @return the eps module component
     */
    public EpsModuleComponent findComponentOrParentById(final String componentId) {
        EpsModuleComponent component = this.getComponent(componentId);
        if (component == null) {
            if (EpsModuleUtil.isPortReferenceComponentIdentifier(componentId)) {
                final String parentComponentId = EpsModuleUtil.getComponentName(componentId);
                component = this.getComponent(parentComponentId);
            } else {
                log.warn("Was not able to find component by id {} for initialization", componentId);
            }
        }
        return component;
    }
}
