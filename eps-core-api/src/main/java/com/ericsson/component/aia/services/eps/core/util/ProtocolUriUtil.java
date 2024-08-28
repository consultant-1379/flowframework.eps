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

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class ProtocolUriUtil {

    private static final String URI_DELIMITER = ":/";
    private static final String PARAMETER_DELIMITER = "&";
    private static final String PARAMETER_VALUE_DELIMITER = "=";

    /**
     * Parses the protocolUri to extract configuration properties.
     *
     * Configuration properties keys and values are separated by
     * {@value #PARAMETER_VALUE_DELIMITER}. Key value pairs are separated by
     * {@value #PARAMETER_DELIMITER}.
     *
     * @param protocolUri
     *            must not be null
     * @return a {@link Map} which contains the configuration properties in the
     *         protocol uri
     * @throws IllegalArgumentException
     *             if the protocolUri is not valid
     */
    public static Map<String, String> extractConfigurationFromProtocolUri(
            final String protocolUri) {
        if (!isValidProtocolUri(protocolUri)) {
            throw new IllegalArgumentException(protocolUri
                    + " is not valid URI!");
        }
        final Map<String, String> configuration = new HashMap<String, String>();
        final int indexOfDelimiter = protocolUri.indexOf(URI_DELIMITER);
        final String configurationSubstring = protocolUri
                .substring(indexOfDelimiter + URI_DELIMITER.length());
        final String[] values = configurationSubstring
                .split(PARAMETER_DELIMITER);
        if ((values != null) && (values.length > 0)) {
            for (final String paramNameValue : values) {
                if ((paramNameValue != null) && !paramNameValue.isEmpty()) {
                    final String[] nameVal = paramNameValue
                            .split(PARAMETER_VALUE_DELIMITER);
                    if ((nameVal != null) && (nameVal.length == 2)) {
                        final String paramName = nameVal[0];
                        final String paramVal = nameVal[1];
                        if ((paramName != null) && (paramVal != null)) {
                            configuration.put(paramName, paramVal);
                        }
                    }
                }
            }
        }
        return configuration;
    }

    /**
     * Tests if the protocol uri contains {@value #URI_DELIMITER}
     *
     * @param protocolUri
     *            a {@link String}, the protocol uri to test
     * @return true if the protocol uri contains {@value #URI_DELIMITER}, false
     *         otherwise
     */
    public static boolean isValidProtocolUri(final String protocolUri) {
        if ((protocolUri == null) || protocolUri.isEmpty()) {
            return false;
        }
        return protocolUri.contains(URI_DELIMITER);
    }

}
