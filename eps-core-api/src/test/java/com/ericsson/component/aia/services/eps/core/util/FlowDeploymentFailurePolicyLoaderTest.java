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

import org.junit.*;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.FlowDeploymentFailurePolicy;
import com.ericsson.component.aia.services.eps.core.util.FlowDeploymentFailurePolicyLoader;

public class FlowDeploymentFailurePolicyLoaderTest {

    @Test
    public void getFlowDeploymentFailurePolicy_whenPropertyConfigured_propertyIsReturned() {
        System.setProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY, FlowDeploymentFailurePolicy.STOP_JVM.toString());
        Assert.assertEquals(FlowDeploymentFailurePolicy.STOP_JVM, FlowDeploymentFailurePolicyLoader.getFlowDeploymentFailurePolicy());
    }

    @Test
    public void getFlowDeploymentFailurePolicy_whenPropertyNotConfigured_defaultIsReturned() {
        System.clearProperty(EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY);
        Assert.assertEquals(FlowDeploymentFailurePolicy.CONTINUE, FlowDeploymentFailurePolicyLoader.getFlowDeploymentFailurePolicy());
    }

}
