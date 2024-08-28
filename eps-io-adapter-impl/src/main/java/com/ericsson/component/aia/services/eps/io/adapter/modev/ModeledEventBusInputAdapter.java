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

import com.ericsson.oss.itpf.sdk.eventbus.model.classic.ModeledEventConsumerBean;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.oss.itpf.sdk.eventbus.classic.EMessageListener;
import com.ericsson.oss.itpf.sdk.eventbus.model.ModeledEvent;

/**
 * Allows an EPS flow to receive {@code ModeledEvent}s from ModeledEventBus.
 *
 * <p>
 * The ModeledEventBusInputAdapter is configured via attributes in the flow descriptor. All configuration parameters are read as strings.
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
 *
 * <dt>filter</dt>
 * <dd>Filter expression to apply for received {@code ModeledEvent}. See {@link ModeledEventConsumerBean#setFilter(java.lang.String)} <br>
 * Can be empty.</dd>
 *
 * <dt>acceptSubclass</dt>
 * <dd>Should the InputAdapter accept subClasses. See {@link ModeledEventConsumerBean#setAcceptSubclasses(boolean)} <br>
 * if "false", subclasses will not be accepted, otherwise subclasses will be accepted and an instance of the configured ModeledEvent will be sent
 * downstream to flow subscribers. <br>
 * Can be empty.</dd>
 * </dl>
 */
public class ModeledEventBusInputAdapter extends AbstractModeledEventBusAdapter implements InputAdapter {

    public static final String MODEL_EV_ACCEPT_SUBCLASS = "acceptSubClass";
    public static final String MODEL_EV_FILTER = "filter";

    private String filter;
    private ModeledEventConsumerBean modeledConsumerBean;
    private Boolean modEvAcceptSubClass;

    @Override
    public void destroy() {
        if (modeledConsumerBean != null && modeledConsumerBean.isListeningForMessages()) {
            modeledConsumerBean.stopListening();
            log.debug("ModeledEventConsumerBean stopped");
        }
    }

    @Override
    public void onEvent(final Object arg0) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * Send event to all the Subscribers and updates the statistics if enabled.
     *
     * @param event
     *            The event to be sent out
     */
    public void sendEvent(final Object event) {
        log.debug("Received event {}", event);
        updateStatisticsWithEventReceived();
        ModeledEventBusInputAdapter.this.sendToAllSubscribers(event);
    }

    @Override
    public String toString() {
        return "ModeledEventBusInputAdapter [modeledEventClassName=" + modeledEventClassName + ", modEvAcceptSubClass=" + modEvAcceptSubClass
                + ", filter=" + filter + "]";
    }

    @Override
    protected void doInit() {

        readConfigParams();
        readModeledEventClass();

        modeledConsumerBean = new ModeledEventConsumerBean(modeledEventClass);
        modeledConsumerBean.setAcceptSubclasses(modEvAcceptSubClass);
        modeledConsumerBean.setFilter(filter);

        final EMessageListener<?> listener = new ModeledEventBusEListener(this, modeledEventClass);
        modeledConsumerBean.startListening(listener);
        log.debug("Created ModeledEventConsumerBean and attached ModeledEventBusEListener");
        initialiseStatistics();
    }

    @Override
    protected void readConfigParams() {
        modeledEventClassName = getStringProperty(MODEL_EV_CLASS);
        final String acceptSubclassProperty = getStringProperty(MODEL_EV_ACCEPT_SUBCLASS);
        if (acceptSubclassProperty == null) {
            modEvAcceptSubClass = true;
            log.debug("You did not set value for configuration property {} and automatically we will set it to true", MODEL_EV_ACCEPT_SUBCLASS);
        } else {
            modEvAcceptSubClass = !"false".equals(acceptSubclassProperty);
            log.debug("Configuration property {} is set to {}", MODEL_EV_ACCEPT_SUBCLASS, modEvAcceptSubClass);
        }
        filter = getStringProperty(MODEL_EV_FILTER);
        log.debug("Configuration property {} is set to {}.", MODEL_EV_FILTER, filter);
    }

}
