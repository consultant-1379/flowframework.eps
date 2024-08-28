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

import static com.ericsson.oss.itpf.sdk.cluster.coordination.application.Node.PATH_SEPARATOR;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Node;

/**
 * @since 1.1.108
 */
/**
 *
 */
@XmlRootElement
public enum CoordinationStatus {
	
	/**
	 * The component or application is active.
	 */
	ACTIVE,
	/**
	 * The component or application is passive.
	 */
	IDLE,
	/**
	 * The component or application has an error.
	 */
	ERROR;
	
	private static final String COMPONENT_NAME_ARG_NAME = "componentName";
	private static final String APPLICATION_ARG_NAME = "application";
	private static final String STATUS_NODE_ARG_NAME = "statusNode";
	private static final String STATUS = "status";
	private static final String EPS_APPLICATION = "eps-application";
	
	private static CoordinationStatus getStatus(final Node statusNode) {
		if (statusNode == null) {
			return null;
		}
		final Serializable serializableStatus = statusNode.get();
		if (serializableStatus == null) {
			return null;
		}
		return XMLDeSerializer.unmarshal((String) serializableStatus, CoordinationStatus.class);
	}
	
	/**
	 * @param application
	 *            the {@link Application} to test
	 * @return true if the application CoordinationStatus is ACTIVE, otherwise false
	 * @throws IllegalArgumentException
	 *             if the application is null
	 */
	public static boolean isActive(final Application application) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		return isActive(application, EPS_APPLICATION);
	}
	
	/**
	 * @param application
	 *            the {@link Application} which has the component to test
	 * @param componentName
	 *            the flow component to test
	 * @return true if the componentName CoordinationStatus is ACTIVE, otherwise false
	 * @throws IllegalArgumentException
	 *             if the application or componentName is null
	 */
	public static boolean isActive(final Application application, final String componentName) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		ArgumentChecker.verifyArgumentNotNull(COMPONENT_NAME_ARG_NAME, componentName);
		return isActive(application.report(STATUS + PATH_SEPARATOR + componentName));
	}
	
	/**
	 * @param statusNode
	 *            the {@link Node} to test
	 * @return true if the statusNode CoordinationStatus is ACTIVE, otherwise false
	 * @throws IllegalArgumentException
	 *             if the statusNode is null
	 */
	public static boolean isActive(final Node statusNode) {
		ArgumentChecker.verifyArgumentNotNull(STATUS_NODE_ARG_NAME, statusNode);
		
		final CoordinationStatus status = getStatus(statusNode);
		return status == null ? false : ACTIVE.equals(status);
	}
	
	/**
	 * @param application
	 *            the {@link Application} which has the component to test
	 * @param componentName
	 *            the flow component to test
	 * @return true if the componentName CoordinationStatus is ERROR, otherwise false
	 * @throws IllegalArgumentException
	 *             if the application is null
	 */
	public static boolean isError(final Application application, final String componentName) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		return isError(application.report(STATUS + PATH_SEPARATOR + componentName));
	}
	
	/**
	 * @param statusNode
	 *            the {@link Node} to test
	 * @return true if the statusNode CoordinationStatus is ERROR, otherwise false
	 * @throws IllegalArgumentException
	 *             if the statusNode is null
	 */
	public static boolean isError(final Node statusNode) {
		ArgumentChecker.verifyArgumentNotNull(STATUS_NODE_ARG_NAME, statusNode);
		final CoordinationStatus status = getStatus(statusNode);
		return status == null ? false : ERROR.equals(status);
	}
	
	/**
	 * @param application
	 *            the {@link Application} to test
	 * @return true if the application CoordinationStatus is IDLE or not set, otherwise false
	 * @throws IllegalArgumentException
	 *             if the application is null
	 */
	public static boolean isIdle(final Application application) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		return isIdle(application, EPS_APPLICATION);
	}
	
	/**
	 * @param application
	 *            the {@link Application} which has the component to test
	 * @param componentName
	 *            the flow component to test
	 * @return true if the application CoordinationStatus is IDLE or not set, otherwise false
	 * @throws IllegalArgumentException
	 *             if the application or componentName is null
	 */
	public static boolean isIdle(final Application application, final String componentName) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		ArgumentChecker.verifyArgumentNotNull(COMPONENT_NAME_ARG_NAME, componentName);
		return isIdle(application.report(STATUS + PATH_SEPARATOR + componentName));
	}
	
	/**
	 * @param statusNode
	 *            the {@link Node} to test
	 * @return true if the statusNode CoordinationStatus is IDLE or not set, otherwise false
	 * @throws IllegalArgumentException
	 *             if the statusNode is null
	 */
	public static boolean isIdle(final Node statusNode) {
		ArgumentChecker.verifyArgumentNotNull(STATUS_NODE_ARG_NAME, statusNode);
		final CoordinationStatus status = getStatus(statusNode);
		return status == null ? true : IDLE.equals(status);
	}
	
	/**
	 * Sets the {@link Application} CoordinationStatus to ACTIVE
	 * 
	 * @param application
	 *            the Application to update
	 * @throws IllegalArgumentException
	 *             if the application is null
	 */
	public static void setActive(final Application application) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		application.report(STATUS + PATH_SEPARATOR + EPS_APPLICATION).createOrUpdate(ACTIVE);
	}
	
	/**
	 * Sets the {@link Application} component CoordinationStatus to ACTIVE
	 * 
	 * @param application
	 *            the Application which has the component to update
	 * @param componentName
	 *            the flow component to update
	 * @throws IllegalArgumentException
	 *             if the application or componentName is null
	 */
	public static void setActive(final Application application, final String componentName) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		ArgumentChecker.verifyArgumentNotNull(COMPONENT_NAME_ARG_NAME, componentName);
		application.report(STATUS + PATH_SEPARATOR + componentName).createOrUpdate(ACTIVE);
	}
	
	/**
	 * Sets the {@link Application} component CoordinationStatus to ERROR
	 * 
	 * @param application
	 *            the Application which has the component to update
	 * @param componentName
	 *            the flow component to update
	 * @throws IllegalArgumentException
	 *             if the application or componentName is null
	 */
	public static void setError(final Application application, final String componentName) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		ArgumentChecker.verifyArgumentNotNull(COMPONENT_NAME_ARG_NAME, componentName);
		application.report(STATUS + PATH_SEPARATOR + componentName).createOrUpdate(ERROR);
	}
	
	/**
	 * Sets the {@link Application} CoordinationStatus to IDLE
	 * 
	 * @param application
	 *            the Application to update
	 * @throws IllegalArgumentException
	 *             if the application is null
	 */
	public static void setIdle(final Application application) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		application.report(STATUS + PATH_SEPARATOR + EPS_APPLICATION).createOrUpdate(IDLE);
	}
	
	/**
	 * Sets the {@link Application} component CoordinationStatus to IDLE
	 * 
	 * @param application
	 *            the Application which has the component to update
	 * @param componentName
	 *            the flow component to update
	 * @throws IllegalArgumentException
	 *             if the application or componentName is null
	 */
	public static void setIdle(final Application application, final String componentName) {
		ArgumentChecker.verifyArgumentNotNull(APPLICATION_ARG_NAME, application);
		ArgumentChecker.verifyArgumentNotNull(COMPONENT_NAME_ARG_NAME, componentName);
		application.report(STATUS + PATH_SEPARATOR + componentName).createOrUpdate(IDLE);
	}
	
}