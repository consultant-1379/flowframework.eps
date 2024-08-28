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
package com.ericsson.component.aia.services.eps.core.coordination.observer;

import static com.ericsson.component.aia.services.eps.EpsEngineConstants.*;
import static com.ericsson.component.aia.services.eps.core.coordination.CoordinationUtil.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.coordination.AdaptiveComponentHandler;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.oss.itpf.sdk.cluster.coordination.*;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.ApplicationFactory;

/**
 *
 * Keeps track of the coordination connection state.
 *
 * Maintains the state if the connection is dropped & reconnects, ensuring the same adaptive behavior. Instance of CoordinationStateHandler has to be
 * unique within the JVM.
 *
 * @see CoordinationConnectionStateListener
 */
public class CoordinationStateHandler implements CoordinationConnectionStateListener {

    private static final String CONNECTION_TO_COORDINATOR_IS_IN_STATE = "connection to coordinator is in state: [{}]";
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinationStateHandler.class);
    private static CoordinationStateHandler stateHandler;
    private static Map<String, Set<AdaptiveComponentHandler>> registeredFlows;
    private static Map<String, Application> registeredApplications;
    private static String epsName;

    /**
     * Instantiates a new coordination state handler.
     */
    private CoordinationStateHandler() {
    }

    /**
     * Track connection state.
     *
     * @param flowId
     *            the flow id
     * @param adaptiveComponentHandler
     *            the adaptive component handler
     */
    public static synchronized void trackConnectionState(final String flowId, final AdaptiveComponentHandler adaptiveComponentHandler) {
        init(flowId, adaptiveComponentHandler);
        register(flowId);
    }

    @Override
    public synchronized void stateChanged(final CoordinationConnectionState state) {
        try {
            handleState(state);
        } catch (final Exception exception) {
            LOGGER.error("Exception during handling cluster connection state [{}]", exception.getMessage(), exception);
        }
    }

    /**
     * Inits the.
     *
     * @param flowId
     *            the flow id
     * @param adaptiveComponentHandler
     *            the adaptive component handler
     */
    private static void init(final String flowId, final AdaptiveComponentHandler adaptiveComponentHandler) {
        if (registeredFlows == null) {
            registeredFlows = new HashMap<String, Set<AdaptiveComponentHandler>>();
        }

        if (registeredApplications == null) {
            registeredApplications = new HashMap<String, Application>();
        }

        if (epsName == null) {
            epsName = getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier());
        }

        if (stateHandler == null) {
            stateHandler = new CoordinationStateHandler();
            ApplicationFactory.registerConnectionStateListener(stateHandler);
        }

        Set<AdaptiveComponentHandler> adaptiveComponentHandlers = registeredFlows.get(flowId);
        if (adaptiveComponentHandlers == null) {
            adaptiveComponentHandlers = new HashSet<AdaptiveComponentHandler>();
            registeredFlows.put(flowId, adaptiveComponentHandlers);
        }
        adaptiveComponentHandlers.add(adaptiveComponentHandler);
    }

    /**
     * Register.
     */
    private static void register() {
        for (final Map.Entry<String, Set<AdaptiveComponentHandler>> registeredFlow : registeredFlows.entrySet()) {
            register(registeredFlow.getKey());
        }
    }

    /**
     * Register.
     *
     * @param flowId
     *            the flow id
     */
    private static void register(final String flowId) {
        if (!ApplicationFactory.isAvailableBlocking()) {
            LOGGER.error("SWFK-Coordination is not available yet!, Dynamic Configuration usage is not possible. "
                    + "Eps Will try to connect later.");
            return;
        }

        registerSingleApplication(flowId);

    }

    /**
     * Register single application.
     *
     * @param flowId
     *            the flow id
     */
    private static void registerSingleApplication(final String flowId) {
        Application application = registeredApplications.get(flowId);
        if (application == null) {
            application = ApplicationFactory.get(Layer.SERVICES, EPS, flowId, epsName);
            registeredApplications.put(flowId, application);
        }

        if (application == null) {
            LOGGER.error("Eps Application could not be retrieved. ");
            return;
        }

        if (!application.registered()) {
            register(application);
            LOGGER.info("[{}] Eps Application is registered ", application.name());
        } else {
            application.deregister();
            register(application);
            LOGGER.info("[{}] Eps Application registration status reset.", application.name());
        }

        startAdaptiveComponentHandlers(flowId, application);
    }

    /**
     * Register.
     *
     * @param application
     *            the application
     */
    private static void register(final Application application) {
        try {
            application.register();
        } catch (final CoordinationException coordinationException) {
            LOGGER.info("CoordinationException [{}] during registration of application [{}]", coordinationException.getMessage(), application
                    .name());
        }

    }

    /**
     * Handle state.
     *
     * @param state
     *            the state
     */
    private void handleState(final CoordinationConnectionState state) {
        switch (state) {
            case LOST:
                LOGGER.info(CONNECTION_TO_COORDINATOR_IS_IN_STATE, state);
                stop();
                break;
            case SUSPENDED:
                LOGGER.info(CONNECTION_TO_COORDINATOR_IS_IN_STATE, state);
                stop();
                break;
            case READ_ONLY:
                LOGGER.info(CONNECTION_TO_COORDINATOR_IS_IN_STATE, state);
                stop();
                break;
            case CONNECTED:
                LOGGER.info(CONNECTION_TO_COORDINATOR_IS_IN_STATE, state);
                register();
                break;
            case RECONNECTED:
                LOGGER.info(CONNECTION_TO_COORDINATOR_IS_IN_STATE, state);
                register();
                break;
            default:
                LOGGER.warn("connection to coordinator is in state: [UNCONTROLLED!] [{}]", state);
                break;
        }
    }

    /**
     * Start adaptive component handlers.
     *
     * @param flowId
     *            the flow id
     * @param application
     *            the application
     */
    private static void startAdaptiveComponentHandlers(final String flowId, final Application application) {
        LOGGER.info("Starting AdaptiveComponent Handlers of flow [{}]", flowId);
        for (final AdaptiveComponentHandler adaptiveComponentHandler : registeredFlows.get(flowId)) {
            start(application, adaptiveComponentHandler);
        }
    }

    /**
     * Stop.
     */
    private static void stop() {
        String flowId;

        for (final Map.Entry<String, Set<AdaptiveComponentHandler>> registeredFlow : registeredFlows.entrySet()) {
            flowId = registeredFlow.getKey();
            LOGGER.info("Stopping AdaptiveComponent Handlers of flow [{}]", flowId);
            stopAdaptiveComponentHandlers(flowId);
        }
    }

    /**
     * Stop adaptive component handlers.
     *
     * @param flowId
     *            the flow id
     */
    private static void stopAdaptiveComponentHandlers(final String flowId) {
        for (final AdaptiveComponentHandler adaptiveComponentHandler : registeredFlows.get(flowId)) {
            stop(adaptiveComponentHandler);
        }
    }

    /**
     * Stop.
     *
     * @param adaptiveComponentHandler
     *            the adaptive component handler
     */
    private static void stop(final AdaptiveComponentHandler adaptiveComponentHandler) {
        try {
            adaptiveComponentHandler.stop();
        } catch (final Exception exception) {
            LOGGER.error("Exception stopping adaptive component handler [{}]", exception.getMessage(), exception);
        }
    }

    /**
     * Start.
     *
     * @param application
     *            the application
     * @param adaptiveComponentHandler
     *            the adaptive component handler
     */
    private static void start(final Application application, final AdaptiveComponentHandler adaptiveComponentHandler) {
        try {
            adaptiveComponentHandler.start(application);
        } catch (final Exception exception) {
            LOGGER.error("Exception starting adaptive component handler [{}]", exception.getMessage(), exception);
        }

    }
}
