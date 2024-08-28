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
package com.ericsson.component.aia.services.eps.core.coordination;

import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;

/**
 *
 * Marks Adaptive Component Handlers of EPS For Each Adaptive Component(Monitorable or Clustered) there has to be one Adaptive Component handler start
 * and stop methods has to be idempotent as it may called more than once.
 *
 * @see Application
 *
 *
 */
public interface AdaptiveComponentHandler {

    /**
     * Start the Adaptive Component.
     *
     * @param application
     *            the application
     *
     * @see Application
     *
     */
    void start(Application application);

    /**
     * Stop the Adaptive Component.
     */
    void stop();

}
