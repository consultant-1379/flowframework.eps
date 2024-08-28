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
package com.ericsson.component.aia.services.eps.core.parsing;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.util.ExpressionParser;
import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 *
 * @author eborziv
 *
 */
public class EpsComponentConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory
            .getLogger(EpsComponentConfiguration.class);

    private final Properties properties;

    /**
     *
     * @param properties
     *            the {@link Properties} to wrap
     */
    public EpsComponentConfiguration(final Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties must not be null");
        }
        this.properties = properties;
    }

    /**
     *
     * @param props
     *            a Map of properties to add to the wrapped properties
     */
    public void addAll(final Map<String, String> props) {
        if (props == null) {
            throw new IllegalArgumentException("unable to add null properties");
        }
        properties.putAll(props);
    }

    @Override
    public Integer getIntProperty(final String propertyName) {
        final String strVal = getStringValue(propertyName);
        if (strVal != null) {
            LOG.debug("Trying to parse [{}] as integer value!", strVal);
            final Integer val = Integer.valueOf(strVal);
            LOG.debug("Found configuration property {}={}", propertyName, val);
            return val;
        } else {
            LOG.debug(
                    "Could not find any value for configuration by name [{}]. Returning null.",
                    propertyName);
        }
        return null;
    }

    @Override
    public String getStringProperty(final String propertyName) {
        final String value = getStringValue(propertyName);
        final String parsedPropertyValue = ExpressionParser
                .replaceAllExpressions(value);
        LOG.debug("Found configuration property {}={}", propertyName,
                parsedPropertyValue);
        return parsedPropertyValue;
    }

    @Override
    public Boolean getBooleanProperty(final String propertyName) {
        final String val = getStringValue(propertyName);
        if (val != null) {
            return Boolean.valueOf(val);
        }
        return null;
    }

    @Override
    public String toString() {
        return "EpsComponentConfiguration [properties=" + properties + "]";
    }

    private String getStringValue(final String propName) {
        LOG.debug("Trying to find value for configuration parameter {}",
                propName);
        if ((propName == null) || propName.trim().isEmpty()) {
            throw new IllegalArgumentException("Property name must not be null");
        }
        final String val = properties.getProperty(propName);
        if (val == null) {
            LOG.warn(
                    "Cound not find any value for configuration property named {}",
                    propName);
        }
        return val;
    }

    @Override
    public Map<String, Object> getAllProperties() {
        final Map<String, Object> props = new HashMap<String, Object>();
        for (final String key : properties.stringPropertyNames()) {
            final Object value = properties.getProperty(key);
            props.put(key, value);
        }
        return props;
    }

}
