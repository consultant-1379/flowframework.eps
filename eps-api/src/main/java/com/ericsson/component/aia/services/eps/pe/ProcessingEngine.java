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

import java.util.Set;

import com.ericsson.component.aia.itpf.common.Destroyable;
import com.ericsson.component.aia.itpf.common.Identifiable;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;

/**
 * Interface implemented by every processing engine used by EPS.
 * 
 * @author eborziv
 * @see ModuleManager
 * @see EpsInstanceManager
 * 
 */
public interface ProcessingEngine extends Identifiable, Destroyable {
	
	/**
	 * Returns engine type of this processing engine. Must not be null or empty String. It is the responsibility of
	 * every processing engine to ensure correctness of returned value. For example types might be <code>esper</code> or
	 * <code>java</code>. Type determines how configuration will be found for particular engine.
	 * 
	 * @return non null unique identifier of this processing engine.
	 */
	String getEngineType();
	
	/**
	 * Invoked by EPS when starting processing engine. Can be invoked multiple times and does not have any effect if
	 * processing engine has already been started.
	 * 
	 * @param peContext
	 *            non null context
	 */
	void start(ProcessingEngineContext peContext);
	
	/**
	 * Deploy component inside processing engine and start it.
	 * 
	 * @param epsComponent
	 *            the component to be deployed. Must not be null
	 * @param portName
	 *            the name of port we are interested in. The component to be deployed must have this port available. If
	 *            this value is null then all available ports will be used.
	 * @return {@link EventInputHandler} representing deployed component
	 */
	EventInputHandler deployComponent(EpsModuleComponent epsComponent, String portName);
	
	/**
	 * Gets already deployed component from processing engine.
	 * 
	 * @param epsComponent
	 *            the component to fetch. Must not be null.
	 * @param portName
	 *            the name of port we are interested in. The component to be deployed must have this port available.
	 *            Must not be null or empty string.
	 * @return {@link EventInputHandler} representing deployed component
	 */
	EventInputHandler getDeployedComponent(EpsModuleComponent epsComponent, String portName);
	
	/**
	 * Returns a set of identifiers of all deployed modules inside this processing engine. Module identifiers are the
	 * ones returned by {@link ModuleManager#deployModuleFromFile(InputStream)} or
	 * {@link ModuleManager#deployModuleFromModel(String modelUrn)} and can be used as parameter to
	 * {@link #undeployModule(String)}.
	 * 
	 * @return unmodifiable collections of all module identifiers deployed inside this engine.
	 */
	Set<String> getDeployedModules();
	
	/**
	 * Undeploys module with given identifier - provided that it was deployed inside this module.
	 * 
	 * @param moduleIdentifier
	 *            unique identifier of this module. Must not be null or empty string
	 * @return true if module was undeployed or false otherwise
	 */
	boolean undeployModule(String moduleIdentifier);
	
}
