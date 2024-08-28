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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.*;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry;


public class StatisticsRegistryTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        StatisticsRegistry.stop();

        StatisticsRegistry.jmxReporter = null;
        StatisticsRegistry.csvReporter = null;
        StatisticsRegistry.slf4jReporter = null;

        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME, "");
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "");
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_FREQUENCY_PARAM_NAME, "");

    }


    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#start()}
     * .
     */
    @Test
    public final void start_whenJmxConfigured_JmxReporterStarted() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "JMX");

        // when
        StatisticsRegistry.start();
        assertNull(StatisticsRegistry.csvReporter);
        assertNotNull(StatisticsRegistry.jmxReporter);
        assertNull(StatisticsRegistry.slf4jReporter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#start()}
     * .
     */
    @Test
    public final void start_whenCsvConfigured_csvReporterStarted() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "CSV");
        System.setProperty(
                EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME,
                "temp");

        // when
        StatisticsRegistry.start();

        // then
        assertNotNull(StatisticsRegistry.csvReporter);
        assertNull(StatisticsRegistry.jmxReporter);
        assertNull(StatisticsRegistry.slf4jReporter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#start()}
     * .
     */
    @Test
    public final void start_whenSlf4JConfigured_slf4jReporterStarted() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "SLF4J");

        // when
        StatisticsRegistry.start();

        // then
        assertNull(StatisticsRegistry.csvReporter);
        assertNull(StatisticsRegistry.jmxReporter);
        assertNotNull(StatisticsRegistry.slf4jReporter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#start()}
     * .
     */
    @Test
    public final void start_whenNoOutputConfigured_JmxReporterStarted() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "none");

        // when
        StatisticsRegistry.start();

        // then
        assertNull(StatisticsRegistry.csvReporter);
        assertNotNull(StatisticsRegistry.jmxReporter);
        assertNull(StatisticsRegistry.slf4jReporter);

    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#start()}
     * .
     */
    @Test
    public final void start_whenCalledAfterStop_canStartDifferentReporter() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "CSV");
        StatisticsRegistry.start();
        assertNotNull(StatisticsRegistry.csvReporter);
        assertNull(StatisticsRegistry.jmxReporter);
        assertNull(StatisticsRegistry.slf4jReporter);

        // when
        StatisticsRegistry.stop();
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "SLF4J");
        StatisticsRegistry.start();

        // then
        assertNotNull(StatisticsRegistry.csvReporter);
        assertNull(StatisticsRegistry.jmxReporter);
        assertNotNull(StatisticsRegistry.slf4jReporter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#start()}
     * .
     */
    @Test
    public final void start_whenCalledTwice_doesNotStartDifferentReporter() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "CSV");
        StatisticsRegistry.start();
        assertNotNull(StatisticsRegistry.csvReporter);
        assertNull(StatisticsRegistry.jmxReporter);
        assertNull(StatisticsRegistry.slf4jReporter);

        // when
        System.setProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME, "SLF4J");
        StatisticsRegistry.start();

        // then
        assertNotNull(StatisticsRegistry.csvReporter);
        assertNull(StatisticsRegistry.jmxReporter);
        assertNull(StatisticsRegistry.slf4jReporter);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.statistics.StatisticsRegistry#getMetricRegistry()}.
     */
    @Test
    public final void getMetricRegistry_returnsMetricRegistry() {
        // when
        final MetricRegistry metricRegistry = StatisticsRegistry.getMetricRegistry();

        // then
        assertNotNull(metricRegistry);
    }

}
