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
package com.ericsson.component.aia.services.eps.core.modules;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;

/**
 * Interface for all module listeners. Module listener implementations are responsible to detect newly deployed modules.
 *
 * @see EpsInstanceManager#getModuleListener()
 * @author eborziv
 *
 */
public interface ModuleListener {

    /**
     * Starts listening for new deployments
     */
    void startListeningForDeployments();

    /**
     * Stops listening for new deployments
     */
    void stopListeningForDeployments();

}
