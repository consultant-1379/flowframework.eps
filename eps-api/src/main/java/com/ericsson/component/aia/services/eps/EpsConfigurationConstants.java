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
package com.ericsson.component.aia.services.eps;

/**
 * All system parameters used by EPS for configuration.
 *
 * @since 3.2.2
 *
 */
public interface EpsConfigurationConstants {
	
	/**
	 * We are able to turn off statistics in case it has impact on performance - set this property to true, by default
	 * it is set to false
	 */
	String STATISTICS_OFF_SYS_PARAM_NAME = "com.ericsson.component.aia.services.eps.core.statistics.off";
	
	/**
	 * What method of reporting will be used for statistics. Allowed values are: JMX, CSV and SLF4J. Defaults to JMX.
	 */
	String STATISTICS_REPORTING_METHOD_PARAM_NAME = "com.ericsson.component.aia.services.eps.core.statistics.reporting.method";
	
	/**
	 * If reporting method is CSV then we can set location where to output data to. Defaults to java.io.tmpdir
	 */
	String STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME = "com.ericsson.component.aia.services.eps.core.statistics.reporting.csv.location";
	
	/**
	 * If reporting method is CSV or SLF4J then we can set the frequency in seconds for polling. Defaults to one second
	 */
	String STATISTICS_REPORTING_FREQUENCY_PARAM_NAME = "com.ericsson.component.aia.services.eps.core.statistics.reporting.frequency";
	
	/**
	 * Modifying folder to be watched for new deployments. Set this property to the absolute path of readable folder on
	 * file system.
	 */
	String MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME = "com.ericsson.component.aia.services.eps.module.deployment.folder.path";
	
	/**
	 * Urn of flow models to be deployed from ModelService, Can contain '*' as wildcard, All models matching are
	 * deployed.
	 */
	String MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN = "com.ericsson.component.aia.services.eps.module.deployment.modelService.urn";
	
	/**
	 * Should the modules found in watched folder or ModelService during startup of EPS be automatically deployed.
	 * Default value is 'true'. Applicable values are 'true' and 'false'
	 */
	String DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER = "com.ericsson.component.aia.services.eps.deploy.already.existing.modules.on.startup";
	
	/**
	 * System property to enable CDI support in EPS. Applicable values are 'true' and 'false'
	 */
	String EVENT_INPUT_HANDLER_CDI_ENABLED = "com.ericsson.component.aia.services.eps.module.cdi.enabled";
	
	/**
	 * System property to decide what EPS should do in case when deployment of flow fails @see
	 * FlowDeploymentFailurePolicy
	 */
	String ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY = "com.ericsson.component.aia.services.eps.core.flow.deployment.failure.policy";
	
}
