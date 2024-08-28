package com.ericsson.component.aia.services.eps.core.coordination.observer;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.coordination.*;
import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.*;

/**
 * automatically triggered when Eps dynamic configuration content is changed. Responsible for reacting change and updating the result of the
 * operation.
 *
 * @see NodeObserver
 */
public class EpsConfigurationChangeObserver implements NodeObserver {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String flowId;
    private final String componentId;
    private final Controllable controllableComponent;
    private final Application application;

    /**
     * This method is called when information about an EpsConfigurationChange which was previously requested using an asynchronous interface becomes
     * available.
     *
     * @param flowId
     *            the flow id
     * @param componentId
     *            the component id
     * @param controllableComponent
     *            the controllable component
     * @param application
     *            the application
     */
    public EpsConfigurationChangeObserver(final String flowId, final String componentId, final Controllable controllableComponent,
                                          final Application application) {
        this.flowId = flowId;
        this.componentId = componentId;
        this.controllableComponent = controllableComponent;
        this.application = application;
        log.info("EpsConfigurationChangeObserver for component {} inside flow {} for EPS {}, is created. ", componentId, flowId, application.name());
    }

    @Override
    public void onCreate(final NodeType location, final String path, final Serializable value) {
        log.debug("New Configuration Detected for component {} inside flow {} for EPS {}. ", componentId, flowId, application.name());
        handle(value);

    }

    @Override
    public void onRemove(final NodeType location, final String path) {
        log.debug("Configuration Removed for component {} inside flow {} for EPS {}. ", componentId, flowId, application.name());
        handle(null);
    }

    @Override
    public void onUpdate(final NodeType location, final String path, final Serializable value) {
        log.debug("Configuration Updated for component {} inside flow {} for EPS {}. ", componentId, flowId, application.name());
        handle(value);
    }

    private void handle(final Serializable data) {
        if (data == null) {
            log.error("Null Data for handler {} in flow {} , Could not be Reacted!, Only HandlerConfiguration type can be reacted. ", componentId,
                    flowId);
            return;
        }
        if (!(data instanceof String)) {
            log.error("Handler Configuration should be stored as String XML in Zookeeper {} in flow {} , "
                    + "Could not be Reacted!, Only HandlerConfiguration type can be reacted. ", componentId, flowId);
            return;
        }
        final ControlEvent controlEvent = new ControlEvent(ControlEvent.CONFIGURATION_CHANGED);
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = convertToHandlerConfiguration(data);
        updateControlEvent(controlEvent, epsAdaptiveConfiguration);
        log.info("Data for handler [{}] of application [{}] in flow [{}] changed to [{}]", componentId, application.name(), flowId,
                epsAdaptiveConfiguration.toString());
        reactAndSaveStatus(controlEvent);
        log.debug("FINISHED Data for handler [{}] of application [{}]  in flow [{}] changed to [{}]", componentId, application.name(), flowId, data);
    }

    private void reactAndSaveStatus(final ControlEvent controlEvent) {
        try {
            controllableComponent.react(controlEvent);
            if (isEmpty(controlEvent)) {
                CoordinationStatus.setIdle(application, componentId);
                log.info("Empty Control Event received and Status set to IDLE for component [{}] of Application [{}]", componentId, application
                        .name());
                return;
            }
            CoordinationStatus.setActive(application, componentId);
            log.info("Component [{}] status is ACTIVE now. Component(Handler) react successfully to the control event ", componentId);
        } catch (final Exception exception) {
            log.error("Component [{}]  of Application [{}] status is ERROR. Component(Handler) could not react to control event  ", componentId,
                    application.name());
            log.error("Exception ", exception);
            CoordinationStatus.setError(application, componentId);
        }
    }

    private static EpsAdaptiveConfiguration convertToHandlerConfiguration(final Serializable data) {
        return XMLDeSerializer.unmarshal((String) data, EpsAdaptiveConfiguration.class);
    }

    private void updateControlEvent(final ControlEvent controlEvent, final EpsAdaptiveConfiguration epsAdaptiveConfiguration) {
        if (epsAdaptiveConfiguration.getConfiguration() == null) {
            return;
        }
        controlEvent.getData().putAll(epsAdaptiveConfiguration.getConfiguration());
    }

    private boolean isEmpty(final ControlEvent controlEvent) {
        return controlEvent.getData().isEmpty();
    }

}
