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
 * Policy to apply if a flow fails to deploy.
 *
 * @since 3.2.2
 */
public enum FlowDeploymentFailurePolicy {
	
	/**
	 * EPS will log details about the flow descriptor failure, then continue to process successfully deployed flows.
	 *
	 */
	CONTINUE,
	
	/**
	 * EPS will log details about the flow descriptor failure, then stop. This means that successfully deployed flows
	 * will not be processed.
	 *
	 */
	STOP_JVM;
	
}
