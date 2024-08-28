/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.coordination;

import java.util.Map;

/**
 * @since 1.1.108
 */
public class ArgumentChecker {
	
	/**
	 * Verfies that a argument is not null
	 * 
	 * @param argumentName
	 *            the name of the argument to check
	 * @param parameter
	 *            the parameter argument to check
	 * @throws IllegalArgumentException
	 *             is the map is null
	 */
	public static void verifyArgumentNotNull(final String argumentName, final Object parameter) {
		if (parameter == null) {
			throw new IllegalArgumentException(argumentName + " cannot be null.");
		}
	}
	
	/**
	 * Verfies that a map is not null or empty
	 * 
	 * @param argumentName
	 *            the name of the argument to check
	 * @param map
	 *            the map argument to check
	 * @throws IllegalArgumentException
	 *             is the map is null or empty
	 */
	public static void verifyMapNotEmpty(final String argumentName, final Map map) {
		if ((map == null) || map.isEmpty()) {
			throw new IllegalArgumentException(argumentName + " cannot be empty.");
		}
	}
	
}
