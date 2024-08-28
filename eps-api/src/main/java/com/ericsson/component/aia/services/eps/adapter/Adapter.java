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
package com.ericsson.component.aia.services.eps.adapter;

import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Interface to be implemented by all IO adapters. Adapters can be either input or output adapters. Input adapters
 * receive events from outside world and pass them to flows in EPS. Output adapters receive events from flows in EPS and
 * pass them to the external world.
 * <p>
 * Adapters react on URIs. Every URI starts with scheme (<tt>file:/</tt>, <tt>http:/</tt>, <tt>jms:/</tt>,
 * <tt>hazelcast:/</tt>). Adapters can choose to support one or more schemes.
 * 
 * @see InputAdapter
 * @see OutputAdapter
 * @author eborziv
 * 
 * @deprecated As of EPS release 2.1.x, replaced by {@link com.ericsson.component.aia.itpf.common.io.Adapter}
 */
@Deprecated
public interface Adapter extends EventInputHandler {
	
	/**
	 * Whether current instance of IO adapter is capable of handling URI in specified format. Adapters are uniquely
	 * identified by protocol they understand. For every IO adapter used in flow descriptor EPS will try to find
	 * appropriate adapter by asking all possible SPI implementations found in classpath whether they understand
	 * particular URI.
	 * 
	 * @param uri
	 *            the URI to be handled. Must not be null or empty. Should be self-explanatory.
	 * @return true if current instance is able to handle URI in specified format or false otherwise. Usually adapters
	 *         should consider only scheme part of the URI when deciding whether they understand URI or not.
	 */
	boolean understandsURI(String uri);
	
}
