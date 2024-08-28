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
package com.ericsson.component.aia.services.eps.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to check arguments.
 *
 * Reduces boiler plate code
 *
 * @since 3.2.1
 *
 */
public class ArgChecker {

    private static final Logger log = LoggerFactory.getLogger(ArgChecker.class);

    /**
     * Verifies that a String argument is not null or empty.
     *
     * logs an error and throws IllegalArgumentException exception if string
     * value is null or empty.
     *
     * @param paramName
     *            the name of the String argument to check, used in error
     *            messages
     * @param paramValue
     *            a String argument to check
     * @throws IllegalArgumentException
     *             if the argument is null or empty
     */
    public static void verifyStringArgNotNullOrEmpty(final String paramName, final String paramValue) {
        if (EpsUtil.isEmpty(paramValue)) {
            log.error("{} should not be null or empty", paramName);
            throw new IllegalArgumentException(paramName + " name must not be null or empty");
        }
    }

    /**
     * Verifies that an Object argument is not null.
     *
     * logs an error and throws IllegalArgumentException exception if object is
     * null.
     *
     * @param paramName
     *            the name of the object to check, used in error messages
     * @param anObjectToCheck
     *            the object to check
     * @throws IllegalArgumentException
     *             if the argument is null
     * */
    public static void verifyNotNull(final String paramName, final Object anObjectToCheck) {
        if (anObjectToCheck == null) {
            log.error("{} should not be null", paramName);
            throw new IllegalArgumentException(paramName + " must not be null");
        }
    }

}
