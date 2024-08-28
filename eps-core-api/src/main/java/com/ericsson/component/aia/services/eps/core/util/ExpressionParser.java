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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for parsing expressions and replacing them in text with real
 * values.
 * <p>
 * Currently supported expressions:
 * <ul>
 * <li>${sys.SYSTEM_PROPERTY_NAME}
 * </ul>
 *
 * @author eborziv
 *
 */
public abstract class ExpressionParser {

    private static final Logger LOG = LoggerFactory
            .getLogger(ExpressionParser.class);
    private static final String DEBUG_MSG = "Replacing all system property expressions - "
            + "for example ${sys.XYZ} will be replaced with the value of system property XYZ. Raw text = {}";

    /**
     * Replaces all system property expressions with the value of the system
     * property.
     *
     * @param rawText
     *            the text to be parsed
     * @return the parsed input String, where the system properties are
     *         replaced
     */
    public static String replaceAllExpressions(final String rawText) {
        if ((rawText != null) && !rawText.trim().isEmpty()) {
            LOG.trace("Replacing all placeholders in {}", rawText);
            final String text = replaceAllSystemProperties(rawText);
            return text;
        }
        return rawText;
    }

    private static String replaceAllSystemProperties(final String rawText) {
        // LOG.debug(
        // "Replacing all system property expressions - "
        // +
        // "for example ${sys.XYZ} will be replaced with the value of system property XYZ. Raw text = {}",
        // rawText);
        LOG.debug(DEBUG_MSG, rawText);
        String text = rawText;
        for (final Object systemPropertyName : System.getProperties().keySet()) {
            final String sysPropertyNameString = (String) systemPropertyName;
            text = replaceSystemProperty(sysPropertyNameString, text);
        }
        return text;
    }

    private static String replaceSystemProperty(
            final String systemPropertyName, final String rawText) {
        final String systemPropertyValue = System
                .getProperty(systemPropertyName);
        final String systemPropertyExpression = "${sys." + systemPropertyName
                + "}";
        LOG.trace("Replacing {} with {}", systemPropertyExpression,
                systemPropertyValue);
        final String result = rawText.replace(systemPropertyExpression,
                systemPropertyValue);
        LOG.trace("Result of replacement is {}", result);
        return result;
    }

}
