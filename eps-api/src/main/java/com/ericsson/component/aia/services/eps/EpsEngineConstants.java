package com.ericsson.component.aia.services.eps;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

/**
 * Constants used by EPS
 */
public interface EpsEngineConstants {
	
	/**
	 * The name under which module manager can be found in {@link EventHandlerContext#getContextualData(String)}
	 */
	String MODULE_MANAGER_CONTEXTUAL_DATA_NAME = "eps_module_manager";
	
	/**
	 * The name under which unique flow (module) identifier can be found in
	 * {@link EventHandlerContext#getContextualData(String)}
	 *
	 * @deprecated As of EPS release 3.2.1, replaced by {@link #FLOW_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME}
	 */
	@Deprecated
	String MODULE_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME = "eps_module_identifier";
	
	/**
	 * The name under which unique flow identifier (flow name and version) can be found in
	 * {@link EventHandlerContext#getContextualData(String)}
	 *
	 */
	String FLOW_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME = "eps_flow_identifier";
	
	/**
	 * The name under which component identifier can be found in {@link EventHandlerContext#getContextualData(String)}
	 */
	String COMPONENT_IDENTIFIER_CONTEXTUAL_DATA_NAME = "eps_component_identifier";
	
	/**
	 * The name used to request the EPS Statistics Register {@link EventHandlerContext#getContextualData(String)}
	 */
	String STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME = "eps_statistics_register";
	
	/**
	 * Parameter to indicate default monitoring interval of the monitorable component.
	 *
	 */
	Integer DEFAULT_MONITORING_PERIOD = 60000;
	
	/**
	 * Parameter to indicate inital delay for monitoring of the monitorable component.
	 *
	 */
	Integer INITIAL_MONITORING_DELAY = 6000;
	
	/**
	 * Parameter to indicate EPS Application Group for SWFK Coordination API
	 *
	 */
	String EPS = "eps";
	
}
