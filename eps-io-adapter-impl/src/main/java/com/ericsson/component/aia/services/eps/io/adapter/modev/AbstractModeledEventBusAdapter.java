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

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.oss.itpf.modeling.annotation.EModel;
import com.ericsson.oss.itpf.modeling.annotation.eventtype.EventTypeDefinition;


/**
 * This class provides common functionalities for {@link ModeledEventBusInputAdapter} and {@link ModeledEventBusOutputAdapter}
 *
 * @see AbstractEventHandler
 * @see Adapter
 */
public abstract class AbstractModeledEventBusAdapter extends AbstractEventHandler implements Adapter {

    public static final String MODEL_EV_CLASS = "modeledEventClassName";
    public static final String MODEL_EV_URI_SCHEME = "modeled-eb:/";

    protected String channelId;
    protected Class<?> modeledEventClass;
    protected String modeledEventClassName;

    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(MODEL_EV_URI_SCHEME);
    }

    /**
     * Gets a string property, given its name
     *
     * @param propertyName
     *            the name of the string property
     * @return the requested string property
     */
    protected String getStringProperty(final String propertyName) {
        final String propertyValue = getConfiguration().getStringProperty(propertyName);
        return (propertyValue != null && propertyValue.trim().isEmpty()) ? null : propertyValue;
    }

    /**
     * Each Adapter should read configuration params.
     *
     */
    protected abstract void readConfigParams();

    /**
     * Read Modeled Event FQCN from configuration in flow definition and loads corresponding Class. Throws ModelledEventBusAdapterException if modeled
     * event class name is not configured, is empty or class is not in classpath.
     *
     */
    protected void readModeledEventClass() {

        modeledEventClassName = getStringProperty(MODEL_EV_CLASS);

        if (modeledEventClassName == null || modeledEventClassName.isEmpty()) {
            throw new ModeledEventBusAdapterException("Modeled Event class name must be configured");
        } else {
            try {
                modeledEventClass = Class.forName(modeledEventClassName);
            } catch (final ClassNotFoundException e) {
                log.debug("Exception while crating modeled event instance.", e);
                throw new ModeledEventBusAdapterException("Could not instantiate modeled event of type " + modeledEventClassName, e);
            }
        }
        final EventTypeDefinition eventTypeDefinition = modeledEventClass.getAnnotation(EventTypeDefinition.class);
        final EModel emodel = modeledEventClass.getAnnotation(EModel.class);
        if (eventTypeDefinition == null || emodel == null) {
            throw new ModeledEventBusAdapterException("Configured class" + modeledEventClassName + " is not a modeled event.");
        }
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
            .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(modeledEventClassName + ".eventsReceived", this);
            }
        }
    }

    /**
     * update metric
     */
    protected void updateStatisticsWithEventReceived() {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }

}
