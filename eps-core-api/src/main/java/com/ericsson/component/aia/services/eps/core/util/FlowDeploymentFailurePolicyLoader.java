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
package com.ericsson.component.aia.services.eps.core.util;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.FlowDeploymentFailurePolicy;

/**
 * Loads the {@code FlowDeploymentFailurePolicy} to apply if a flow deployment
 * fails.
 *
 * @since 3.2.2
 *
 */
public abstract class FlowDeploymentFailurePolicyLoader {


    /**
     * Returns the policy to apply if a flow deployment fails.
     *
     * Supported policies are:
     * <ul>
     * <li>CONTINUE</li>
     * <li>STOP_JVM</li>
     * </ul>
     *
     * If there is no configured flow deployment failure policy, then the
     * default value, CONTINUE, is returned.
     *
     * @return FlowDeploymentFailurePolicy the policy to apply if flow
     *         deployment fails
     */
    public static FlowDeploymentFailurePolicy getFlowDeploymentFailurePolicy() {
        String configuredUserPolicy = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY);
        if (EpsUtil.isEmpty(configuredUserPolicy)) {
            configuredUserPolicy = FlowDeploymentFailurePolicy.CONTINUE.toString();
        }
        if (configuredUserPolicy.toUpperCase().equals(FlowDeploymentFailurePolicy.CONTINUE.toString())) {
            return FlowDeploymentFailurePolicy.CONTINUE;
        } else if (configuredUserPolicy.toUpperCase().equals(
                FlowDeploymentFailurePolicy.STOP_JVM.toString())) {
            return FlowDeploymentFailurePolicy.STOP_JVM;
        } else {
            return FlowDeploymentFailurePolicy.CONTINUE;
        }
    }

}
