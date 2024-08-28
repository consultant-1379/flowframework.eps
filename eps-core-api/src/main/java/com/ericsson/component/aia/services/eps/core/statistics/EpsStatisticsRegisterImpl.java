/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.*;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.core.util.ArgChecker;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;

/**
 * Implements {@code EpsStatisticsRegister}
 *
 * Creates {@link https://dropwizard.github.io/metrics}
 *
 * Can be used directly by EPS classes, used via {@see EpsStatisticsRegister} by EPS extensions.
 *
 * @since 3.0.4
 */
public class EpsStatisticsRegisterImpl implements EpsStatisticsRegister {

    private static final String METER_NAME = "Meter";
    private static final String COUNTER_NAME = "Counter";
    private static final String GUAGE_NAME = "Gauge";
    private static final String DOT_DELIMETER = ".";
    private Boolean statisticsOn;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MetricRegistry metricsRegistry = StatisticsRegistry.getMetricRegistry();

    @Override
    public boolean isStatisticsOn() {
        if (statisticsOn == null) {
            final String configuredStatsDisabledValue = EPSConfigurationLoader
                    .getConfigurationProperty(EpsConfigurationConstants.STATISTICS_OFF_SYS_PARAM_NAME);
            final boolean statisticsTurnedOff = "true".equalsIgnoreCase(configuredStatsDisabledValue);
            statisticsOn = !statisticsTurnedOff;
        }
        return statisticsOn.booleanValue();
    }

    @Override
    public Meter createMeter(final String meterName) {
        ArgChecker.verifyStringArgNotNullOrEmpty(METER_NAME, meterName);
        log.debug("Creating Meter {}", meterName);
        return metricsRegistry.meter(MetricRegistry.name(meterName));
    }

    @Override
    public Meter createMeter(final String meterName, final AbstractEventHandler eventHandler) {
        ArgChecker.verifyStringArgNotNullOrEmpty(METER_NAME, meterName);
        ArgChecker.verifyNotNull("EventHandler", eventHandler);

        final String fullMeterName = getComponentUniqueName(eventHandler) + DOT_DELIMETER + meterName;
        log.debug("Creating Meter {}", fullMeterName);
        return metricsRegistry.meter(MetricRegistry.name(fullMeterName));
    }

    @Override
    public Counter createCounter(final String counterName) {
        ArgChecker.verifyStringArgNotNullOrEmpty(COUNTER_NAME, counterName);
        log.debug("Creating Counter {}", counterName);
        return metricsRegistry.counter(MetricRegistry.name(counterName));
    }

    @Override
    public Counter createCounter(final String counterName, final AbstractEventHandler eventHandler) {
        ArgChecker.verifyStringArgNotNullOrEmpty(COUNTER_NAME, counterName);
        ArgChecker.verifyNotNull("EventHandler", eventHandler);

        final String fullCounterName = getComponentUniqueName(eventHandler) + DOT_DELIMETER + counterName;
        log.debug("Creating Counter {}", fullCounterName);
        return metricsRegistry.counter(MetricRegistry.name(fullCounterName));
    }

    @Override
    public void registerGuage(final String gaugeName, final Gauge<Long> gauge, final AbstractEventHandler eventHandler) {
        ArgChecker.verifyStringArgNotNullOrEmpty(GUAGE_NAME, gaugeName);
        ArgChecker.verifyNotNull(GUAGE_NAME, gauge);
        ArgChecker.verifyNotNull("EventHandler", eventHandler);

        final String fullGuageName = getComponentUniqueName(eventHandler) + DOT_DELIMETER + gaugeName;
        log.debug("Registering Guage {}", fullGuageName);
        metricsRegistry.register(fullGuageName, gauge);
    }

    @Override
    public void registerGuage(final String gaugeName, final Gauge<Long> gauge) {
        ArgChecker.verifyStringArgNotNullOrEmpty(GUAGE_NAME, gaugeName);
        ArgChecker.verifyNotNull(GUAGE_NAME, gauge);

        log.debug("Registering Guage {}", gaugeName);
        metricsRegistry.register(gaugeName, gauge);
    }

    @Override
    public void registerCounter(final String counterName, final Counter counter) {
        ArgChecker.verifyStringArgNotNullOrEmpty(COUNTER_NAME, counterName);
        ArgChecker.verifyNotNull(COUNTER_NAME, counter);

        log.debug("Registering Counter {}", counterName);
        metricsRegistry.register(counterName, counter);
    }

    @Override
    public void registerCounter(final String counterName, final Counter counter, final AbstractEventHandler eventHandler) {
        ArgChecker.verifyStringArgNotNullOrEmpty(COUNTER_NAME, counterName);
        ArgChecker.verifyNotNull(COUNTER_NAME, counter);
        ArgChecker.verifyNotNull("EventHandler", eventHandler);

        final String fullCounterName = getComponentUniqueName(eventHandler) + DOT_DELIMETER + counterName;
        log.debug("Registering Counter {}", fullCounterName);
        metricsRegistry.register(fullCounterName, counter);
    }

    /**
     * Chains the event handler instance unique flow Id (flow name + version) with the handler instance id as specified in the flow.
     *
     * @param eventHandler
     *            the event handler instance to query
     * @return the handler instance unique identifier as a String
     */
    private String getComponentUniqueName(final AbstractEventHandler eventHandler) {
        final String flowName = (String) eventHandler.getEventHandlerContext().getContextualData(
                EpsEngineConstants.FLOW_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME);
        final String componentName = (String) eventHandler.getEventHandlerContext().getContextualData(
                EpsEngineConstants.COMPONENT_IDENTIFIER_CONTEXTUAL_DATA_NAME);

        return flowName + DOT_DELIMETER + componentName;
    }

    /**
     * Start statistics reporting.
     */
    public void startStatisticsReporting() {
        log.debug("start statistics reporting");
        StatisticsRegistry.start();
    }

}
