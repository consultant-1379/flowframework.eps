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
package com.ericsson.component.aia.services.eps.deployer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.modules.ModuleListener;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.core.util.ModelServiceUtil;
import com.ericsson.component.aia.services.eps.deployer.file.FileSystemListenerImpl;
import com.ericsson.component.aia.services.eps.deployer.modelservice.ModelServiceListenerImpl;
import com.ericsson.component.aia.services.eps.modules.FlowRepositoryListener;

/**
 *
 * Select the right listener for watch dynamic add/remove of flows. If EPS is configured to use the ModelService, then the ModelService listener is
 * used otherwise the File System listener is used.
 *
 * @author epiemir
 *
 */
public class ModuleListenerImpl implements ModuleListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FlowRepositoryListener moduleListenerManager;

    /**
     * Gets the right listener (ModelService or FileSystem).
     *
     * @return the listener
     *
     * @see ModelServiceListenerImpl
     * @see FileSystemListenerImpl
     *
     */
    public FlowRepositoryListener getListener() {
        if (moduleListenerManager == null) {
            if (!EpsUtil.isEmpty(ModelServiceUtil.getModelServiceUrn())) {
                logger.info("EPS Using ModelService...");
                moduleListenerManager = new ModelServiceListenerImpl();
            } else {
                logger.info("EPS Using FileSystem...");
                moduleListenerManager = new FileSystemListenerImpl();
            }
        }
        return moduleListenerManager;
    }

    @Override
    public void startListeningForDeployments() {
        getListener().startListeningForDeployments();
    }

    @Override
    public void stopListeningForDeployments() {
        getListener().stopListeningForDeployments();
        moduleListenerManager = null;
    }

}
