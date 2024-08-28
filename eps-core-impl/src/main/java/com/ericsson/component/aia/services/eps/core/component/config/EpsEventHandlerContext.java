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
package com.ericsson.component.aia.services.eps.core.component.config;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.util.ArgChecker;
import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.*;

/**
 * The Class EpsEventHandlerContext.
 *
 * @author eborziv
 * @see EventHandlerContext
 */
public class EpsEventHandlerContext implements EventHandlerContext {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Configuration configuration;

    private final Set<EventSubscriber> subscribers = new TreeSet<>();

    /**
     * Here we are caching unmodifiable collection to be returned - do not want to create new collection every time someone asks for all subscribers.
     */
    private Set<EventSubscriber> unmodifiableSubscribersCollection = new TreeSet<>();

    private final EpsModule module;
    private final String componentId;

    /**
     * Creates the context, setting related configuration and component instance id.
     *
     * @param configuration
     *            the configuration
     * @param module
     *            the module
     * @param componentId
     *            the componentId
     */
    public EpsEventHandlerContext(final Configuration configuration, final EpsModule module, final String componentId) {
        ArgChecker.verifyNotNull("Configuration", configuration);
        ArgChecker.verifyNotNull("EpsModule", module);
        ArgChecker.verifyStringArgNotNullOrEmpty("componentId", componentId);
        this.configuration = configuration;
        this.module = module;
        this.componentId = componentId;
    }

    /**
     * Gets the event handler configuration.
     *
     * @return the event handler configuration
     */
    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    /**
     * Gets the event subscribers.
     *
     * @return the event subscribers
     */
    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        return unmodifiableSubscribersCollection;
    }

    /**
     * Convenient method to add component event subscribers, instead of having to get subscriber collection to add.
     *
     * @param eventHandler
     *            the event handler
     * @param identifier
     *            the identifier
     * @return true, if successful
     */
    public boolean addEventSubscriber(final EventInputHandler eventHandler, final String identifier) {
        ArgChecker.verifyNotNull("Subscriber", eventHandler);
        log.trace("Adding subscriber {} to {}", eventHandler, this);
        final EpsEventSubscriberImpl subscriber = new EpsEventSubscriberImpl(identifier, eventHandler);
        final boolean added = subscribers.add(subscriber);
        unmodifiableSubscribersCollection = Collections.unmodifiableSet(subscribers);
        if (added) {
            log.debug("Successfully added subscriber. There are {} subscribers in total. All subscribers are {}", subscribers.size(), subscribers);
        } else {
            log.info("Was not able to add subscriber {} to {}. Already exists in set", subscriber, this);
        }
        return added;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "EpsComponentContext [configuration=" + configuration + ", subscribers=" + subscribers + "]";
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {
        ArgChecker.verifyNotNull("ControlEvent", controlEvent);
        log.debug("Sending control event {}", controlEvent);
        for (final Controllable controllableComponent : module.getControllableComponents()) {
            controllableComponent.react(controlEvent);
            log.trace("Informed {} about {}", controllableComponent, controlEvent);
        }
    }

    @Override
    public Object getContextualData(final String name) {
        ArgChecker.verifyStringArgNotNullOrEmpty("Name", name);
        log.debug("Trying to find contextual data by name [{}]", name);
        if (EpsEngineConstants.MODULE_MANAGER_CONTEXTUAL_DATA_NAME.equals(name)) {
            return EpsInstanceManager.getInstance().getModuleManager();
        }
        if ((EpsEngineConstants.MODULE_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME.equals(name))
            || (EpsEngineConstants.FLOW_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME.equals(name))) {
            return module.getUniqueModuleIdentifier();
        }
        if (EpsEngineConstants.COMPONENT_IDENTIFIER_CONTEXTUAL_DATA_NAME.equals(name)) {
            return componentId;
        }
        if (EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME.equals(name)) {
            return EpsInstanceManager.getInstance().getEpsStatisticsRegister();
        }
        log.debug("Was not able to find contextual data by name [{}]", name);
        return null;
    }
}
