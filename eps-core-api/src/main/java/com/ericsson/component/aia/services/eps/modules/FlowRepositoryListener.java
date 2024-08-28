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
package com.ericsson.component.aia.services.eps.modules;

/**
 *
 * Interface for external repository listeners.
 *
 * @author epiemir
 *
 */
public interface FlowRepositoryListener {

    /**
     * Starts listening for new deployments
     */
    void startListeningForDeployments();

    /**
     * Stops listening for new deployments
     */
    void stopListeningForDeployments();

}