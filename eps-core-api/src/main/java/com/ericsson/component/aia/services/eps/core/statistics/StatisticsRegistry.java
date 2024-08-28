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
package com.ericsson.component.aia.services.eps.core.statistics;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.*;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;

/**
 * The Class StatisticsRegistry.
 *
 * @author eborziv
 */
abstract class StatisticsRegistry {

    /** A reporter which exposes application metric as JMX. */
    protected static JmxReporter jmxReporter;

    /** A reporter which exposes application metric as CSV. */
    protected static CsvReporter csvReporter;

    /** A reporter which exposes application metric as slf4j. */
    protected static Slf4jReporter slf4jReporter;

    private static final String REPORTING_CSV = "CSV";

    private static final String REPORTING_JMX = "JMX";

    private static final String REPORTING_SLF4J = "SLF4J";

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsRegistry.class);


    /**
     * A registry of metric instances. The MetricsRegistry is entry point to use the metrics
     */
    private static final MetricRegistry metricRegistry = new MetricRegistry();

    /** The started. */
    private static boolean started;

    /**
     * Start statistics reporter.
     */
    private static void startStatisticsReporter() {
        String reportingMethod = EPSConfigurationLoader.getConfigurationProperty(EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME);
        if ((reportingMethod == null) || reportingMethod.isEmpty()) {
            reportingMethod = REPORTING_JMX;
            LOG.info("JVM property [{}] was not set. Using default setting: JMX.", EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME);
        }
        LOG.info("Reporting method set to {}", reportingMethod);

        if (REPORTING_CSV.equalsIgnoreCase(reportingMethod)) {
            LOG.debug("Will use CSV reporter for statistics. Trying to find output location and polling frequency");
            final String csvOutputFolder = getStatisticsOutputFolder();
            final long csvOutputFrequencyAsInt = getPollingFrequency();
            startCsvReporter(csvOutputFolder, csvOutputFrequencyAsInt);
        } else if (REPORTING_JMX.equalsIgnoreCase(reportingMethod)) {
            LOG.debug("Will output statistics to JMX.");
            startJmxReporter();
        } else if (REPORTING_SLF4J.equalsIgnoreCase(reportingMethod)) {
            LOG.debug("Will use SLF4 Reporter for statistics.  Trying to find polling frequency");
            final long slf4jOutputFrequencyAsInt = getPollingFrequency();
            startSlf4jReporter(slf4jOutputFrequencyAsInt);
        } else {
            LOG.warn("Will output statistics to JMX. {} has wrong value {}", EpsConfigurationConstants.STATISTICS_REPORTING_METHOD_PARAM_NAME,
                    reportingMethod);
            startJmxReporter();
        }
    }

    /**
     * Gets the statistics output folder.
     *
     * @return the statistics output folder
     */
    private static String getStatisticsOutputFolder() {
        String csvOutputFolder = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME);
        if ((csvOutputFolder == null) || csvOutputFolder.isEmpty()) {
            csvOutputFolder = System.getProperty("java.io.tmpdir");
            LOG.debug("{} was not set. Will use default output location for Statistics files {}",
                    EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME, csvOutputFolder);
        }
        return csvOutputFolder;
    }

    /**
     * Gets the polling frequency.
     *
     * @return the polling frequency
     */
    private static long getPollingFrequency() {
        final String outputFrequency = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.STATISTICS_REPORTING_FREQUENCY_PARAM_NAME);
        long outputFrequencyAsLong = 1;
        if ((outputFrequency != null) && !outputFrequency.isEmpty() && EpsUtil.isDigit(outputFrequency)) {
            outputFrequencyAsLong = Long.parseLong(outputFrequency);
        } else {
            LOG.debug("{} was not set. Will use default polling frequency {}", EpsConfigurationConstants.STATISTICS_REPORTING_FREQUENCY_PARAM_NAME,
                    outputFrequencyAsLong);
        }
        return outputFrequencyAsLong;
    }

    /**
     * Start jmx reporter.
     */
    private static void startJmxReporter() {
        final String jmxDomain = StatisticsRegistry.class.getPackage().getName()+ "." + EpsUtil.getEpsInstanceIdentifier();
        jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(jmxDomain).build();
        jmxReporter.start();
        LOG.debug("Successfully started statistics JMX reporter");
    }

    /**
     * Start csv reporter.
     *
     * @param outputFolder
     *            the output folder
     * @param pollingFrequency
     *            the polling frequency
     */
    private static void startCsvReporter(final String outputFolder, final long pollingFrequency) {
        LOG.debug("Starting CSV reporting, polling frequency [{}], location [{}]", pollingFrequency, outputFolder);
        csvReporter = CsvReporter.forRegistry(metricRegistry).build(new File(outputFolder));
        csvReporter.start(pollingFrequency, TimeUnit.SECONDS);
        LOG.debug("Successfully started statistics CSV reporter");
    }


    /**
     * Start slf4j reporter.
     *
     * @param pollingFrequency
     *            the polling frequency
     */
    private static void startSlf4jReporter(final long pollingFrequency) {

        LOG.debug("Starting SLF4J reporting, polling frequency [{}]", pollingFrequency);
        slf4jReporter = Slf4jReporter.forRegistry(metricRegistry).outputTo(LoggerFactory.getLogger(StatisticsRegistry.class))
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.SECONDS).build();
        slf4jReporter.start(pollingFrequency, TimeUnit.SECONDS);
        LOG.debug("Successfully started statistics SLF4J Reporter");
    }


    /**
     * Start the metrics Reporter.
     */
    static void start() {
        if (!started) {
            startStatisticsReporter();
            started = true;
        }
    }

    /**
     * Shutdown of the metric reporter and registry.
     */
    static void stop() {
        if (started) {
            if (jmxReporter != null) {
                jmxReporter.close();
            }
            if (csvReporter != null) {
                csvReporter.close();
            }
            if (slf4jReporter != null) {
                slf4jReporter.close();
            }
            started = false;
        }
    }

    /**
     * Gets the metric registry.
     *
     * @return MetricsRegistry the instance of the MetricsRegistriy for reporting purpose and register more metrics
     */
    static MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
