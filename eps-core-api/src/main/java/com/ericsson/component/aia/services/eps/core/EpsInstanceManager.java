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
package com.ericsson.component.aia.services.eps.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.modules.ModuleListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.Destroyable;

/**
 * Handler used to manage lifecycle of EPS instance and access to all other important parts of EPS.
 *
 * @author eborziv
 *
 */
public class EpsInstanceManager {

    private static final EpsInstanceManager INSTANCE;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ModuleListener moduleListener;

    private final ModuleManager moduleManager;

    private final EpsStatisticsRegisterImpl epsStatisticsRegister = new EpsStatisticsRegisterImpl();

    static {
        INSTANCE = new EpsInstanceManager();
    }

    /**
     * Creates new instance of {@link EpsInstanceManager}
     *
     */
    private EpsInstanceManager() {
        moduleListener = LoadingUtil.loadSingletonInstance(ModuleListener.class);
        moduleManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
    }

    /**
     * Creates new instance (and caches) of {@link EpsInstanceManager} or returns already created singleton instance.
     *
     * @return an EpsInstanceManager
     */
    public static EpsInstanceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Starts EPS instance. Automatically starts listening for new deployments.
     *
     */
    public void start() {
        log.info("Starting EPS instance...");
        moduleListener.startListeningForDeployments();
        epsStatisticsRegister.startStatisticsReporting();
    }

    /**
     * Stops EPS instance. Stops listening for new deployments and undeploys all already deployed modules. Sends destroy signal to all
     * {@link Destroyable} instances in all deployed modules.
     */
    public void stop() {
        log.warn("Asked to stop EPS instance! Will stop listening for new deployments, undeploy all modules and destroy all components!");
        moduleListener.stopListeningForDeployments();
        log.debug("Stopped listening for new deployments...");
        moduleManager.undeployAllModules();
    }

    /**
     * Returns associated {@link ModuleListener} instance. Can be used to start and stop listening for new deployments without affecting state of
     * current EPS instance.
     *
     * @return the moduleListener
     */
    public ModuleListener getModuleListener() {
        return moduleListener;
    }

    /**
     * Returns associated {@link ModuleManager} instances which can be used to manage modules deployed inside current EPS instance or to deploy new
     * modules.
     *
     * @return the moduleManager
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * @return the epsStatisticsRegister
     */
    public EpsStatisticsRegister getEpsStatisticsRegister() {
        return epsStatisticsRegister;
    }

}
