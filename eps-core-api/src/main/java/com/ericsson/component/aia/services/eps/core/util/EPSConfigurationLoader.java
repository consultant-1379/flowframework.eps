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
package com.ericsson.component.aia.services.eps.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Loads configuration files found in classpath of the currently running service. It is up to every service to make sure that correct information is
 * provided in required file. Automated way to provide this file (like Maven plugin) must be used.
 *
 * @author eborziv
 *
 */
public final class EPSConfigurationLoader {

    public static final String CONFIGURATION_FILE_NAME = "EpsConfiguration.properties";

    private static final Logger log = LoggerFactory.getLogger(EPSConfigurationLoader.class);

    /**
     * Returns value for specified configuration property or null if it can not be found. Performs search by priority (classpath, JVM property, OS
     * property).
     *
     * @param propName
     *            the name of the property to get
     * @return the value of the requested property
     */
    public static String getConfigurationProperty(final String propName) {

        if (EpsUtil.isEmpty(propName)) {
            throw new IllegalArgumentException("Property name must not be null or empty");
        }
        log.debug("Trying to find EPS configuration property by name [{}]", propName);

        final String valueFromSystemProperty = System.getProperty(propName);
        if (!EpsUtil.isEmpty(valueFromSystemProperty)) {
            log.debug("Value for [{}] as specified in JVM property is {}", propName, valueFromSystemProperty);
            return valueFromSystemProperty;
        }

        log.debug("Was not able to find value for config property {} in JVM properties. Looking into OS properties", propName);

        final String valueFromOsEnv = System.getenv(propName);
        if (!EpsUtil.isEmpty(valueFromOsEnv)) {
            log.debug("Value for [{}] as specified in OS properties is {}", propName, valueFromOsEnv);
            return valueFromOsEnv;
        }

        log.debug(
                "Not able to find value for configuration property [{}] in OS properties. Searching for configuration property with the same name",
                propName);

        final String valueFromConfig = loadPropertiesFromFile().getProperty(propName);
        if (!EpsUtil.isEmpty(valueFromConfig)) {
            log.debug("Found value [{}] for EPS configuration property [{}] in configuration file", valueFromConfig, propName);
            return valueFromConfig;
        }

        return null;
    }

    private static Properties loadPropertiesFromFile() {
        final Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            log.debug("Loading properties from {}", CONFIGURATION_FILE_NAME);
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIGURATION_FILE_NAME);
            if (inputStream != null) {
                properties.load(inputStream);
                log.debug("Successfully loaded file {} from classpath", CONFIGURATION_FILE_NAME);
            } else {
                log.warn("Was not able to find file {}", CONFIGURATION_FILE_NAME);
            }
        } catch (final IOException ie) {
            log.error("loadProperties IOexception on loading file {} from classpath", ie);
        } finally {
            EpsUtil.close(inputStream);
        }
        return properties;
    }

}
