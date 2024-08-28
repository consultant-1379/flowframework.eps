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
package com.ericsson.component.aia.services.eps.pe;

import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * Context passed to {@link ProcessingEngine} instances when they are started.
 * 
 * @see ProcessingEngine
 * 
 * @author eborziv
 * 
 */
public class ProcessingEngineContext {
	
	private final Configuration configuration;
	
	/**
	 * @param configuration
	 *            the {@link Configuration} properties for the ProcessingEngineContext
	 * @throws IllegalArgumentException
	 *             if configuration is null
	 */
	public ProcessingEngineContext(final Configuration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException("Configuration must not be null");
		}
		this.configuration = configuration;
	}
	
	/**
	 * Returns configuration associated with current {@link ProcessingEngineContext}
	 * 
	 * @return the configuration. Never returns null.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
	
}