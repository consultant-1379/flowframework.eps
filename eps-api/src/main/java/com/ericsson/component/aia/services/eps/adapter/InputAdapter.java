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

import java.util.ServiceLoader;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandler;

/**
 * All input adapters must implement this interface. Input adapters are able to receive events from the outside world
 * and pass them downstream to flow for further processing. All implementations of this interface must be valid
 * <a href="http://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html">SPI</a> implementations.
 * 
 * @see Adapter
 * @see EventHandler
 * @see ServiceLoader
 * @author eborziv
 * 
 * @deprecated As of EPS release 2.1.x, replaced by {@link com.ericsson.component.aia.itpf.common.io.InputAdapter}
 */
@Deprecated
public interface InputAdapter extends Adapter, EventHandler {
	
}