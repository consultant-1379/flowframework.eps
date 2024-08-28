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

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.EpsConstants;

/**
 *
 * @author eborziv
 *
 */
public class EpsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EpsUtil.class);

    private static final String NUMBER_ONLY_REGEX = "\\d+";

    private EpsUtil() {

    }

    /**
     * @return the value of the system property for the EPS instance Id
     */
    public static String getEpsInstanceIdentifier() {
        // first we try to find instance id in local classpath. If not there then we look into system properties
        final String instanceId = EPSConfigurationLoader.getConfigurationProperty(EpsConstants.EPS_INSTANCE_ID_PROP_NAME);
        if ((instanceId == null) || instanceId.isEmpty()) {
            LOG.warn(
                    "EPS instance started without assigned unique identifier. Use {} system property to change this",
                    EpsConstants.EPS_INSTANCE_ID_PROP_NAME);
        }
        LOG.debug("EPS unique identifier is {}", instanceId);
        return instanceId;
    }

    /**
     * Method returns whether a <code>String</code> object is <code>null</code>
     * or is empty.
     *
     * @param val
     *            a {@link String} to be tested
     * @return true if passed String value is null or empty, false otherwise
     */
    public static boolean isEmpty(final String val) {
        return (val == null) || (val.trim().length() == 0);
    }

    /**
     *
     * @param closeable
     *            a {@link Closeable} resource to close
     */
    public static void close(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException ignored) {
                LOG.error("IOException on StreamUtils close.", ignored);
            }
        }
    }

    /**
     * Tests if a <code>String</code> is a digit or not
     *
     * @param value
     *            a {@link String} to be tested
     * @return true if the value is a digit, false otherwise
     */
    public static boolean isDigit(final String value) {
        return value.matches(NUMBER_ONLY_REGEX);
    }

}
