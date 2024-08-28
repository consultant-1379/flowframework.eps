
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
package com.ericsson.component.aia.services.eps.deployer.modelservice;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.*;
import com.ericsson.component.aia.services.eps.modules.FlowRepositoryListener;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;

/**
 * @author epiemir
 *
 *         Listener for dynamic deploy of flows on model service.
 *
 * @see FlowRepositoryListener
 *
 */
public class ModelServiceListenerImpl implements FlowRepositoryListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);

    private final FlowModelServiceListener modelServiceListener;

    private final ModelInfo watchedFlows;

    private volatile boolean startedListening;

    /**
     * Instantiates a new model service listener impl.
     */
    public ModelServiceListenerImpl() {
        this.modelServiceListener = new FlowModelServiceListener(this);
        this.watchedFlows = findFirstApplicableUrn();
        if (this.watchedFlows != null) {
            this.modelServiceListener.setUrnFilter(watchedFlows);
            final boolean tryDeployingExistingModulesOnStartup = shouldDeployExistingModulesOnStartup();
            if (tryDeployingExistingModulesOnStartup) {
                findAndDeployExistingModules();
            } else {
                logger.info("Will not consider deploying existing modules found in [{}]", watchedFlows.toUrn());
            }
            logger.debug("Subscribing for new deployments...");
            ModelServiceUtil.getModelService().addModelsDeploymentEventListener(this.modelServiceListener);
        }
    }

    @Override
    public synchronized void startListeningForDeployments() {
        if (!startedListening) {
            this.modelServiceListener.setEnabled(true);
            startedListening = true;
            logger.info("Start to listening for new deployments");
        }
    }

    @Override
    public synchronized void stopListeningForDeployments() {
        if (startedListening) {
            this.modelServiceListener.setEnabled(false);
            startedListening = false;
            logger.info("Stopped listening for new deployments");
        }
    }

    /**
     * Find first applicable Model Service urn if available, otherwise null.
     *
     * @return the model info {@link ModelInfo}
     */
    private ModelInfo findFirstApplicableUrn() {
        logger.debug("Deciding where to look for deployments...");
        final String configuredSysPropUrn = ModelServiceUtil.getModelServiceUrn();
        if (!EpsUtil.isEmpty(configuredSysPropUrn)) {
            try {
                final ModelInfo modinfo = ModelInfo.fromImpliedUrn(configuredSysPropUrn.replaceFirst(SchemaConstants.FBP_FLOW, ""),
                        SchemaConstants.FBP_FLOW);
                logger.info("Watching urn [{}] for new eps module deployments", configuredSysPropUrn);
                return modinfo;
            } catch (final IllegalArgumentException ex) {
                logger.error("Configuration property [{}] contains invalid URN. details: [{}]",
                        EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN, ex.getMessage());
            }
        } else {
            logger.error("Expected configuration property [{}] not defined.", EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN);
        }
        return null;
    }

    /**
     * On startup will try to find all models that could possibly be deployable modules into EPS and then try to deploy them into EPS.
     */
    private void findAndDeployExistingModules() {
        logger.info("Trying to find already existing event-flow deployments in [{}]", watchedFlows.toUrn());
        final Collection<ModelInfo> models = ModelServiceUtil.getDeployedModelsInfo(watchedFlows);
        logger.trace("Found  [{}] in ModelService. Trying to deploy all {} flows in it", watchedFlows.toUrn(), models.size());
        int autoDeployedModules = 0;
        if (!models.isEmpty()) {
            for (final ModelInfo possibleModelFlow : models) {
                logger.debug("Found flow model [{}]. Will try to deploy it as module", possibleModelFlow.toUrn());
                final boolean parsedAndDeployed = deployModuleFromUrn(possibleModelFlow.toUrn());
                if (parsedAndDeployed) {
                    autoDeployedModules++;
                } else {
                    logger.debug("Path [{}] does not point to valid event-flow file", possibleModelFlow.toUrn());
                }
            }
            logger.info("Successfully auto-deployed {} event-flows on startup", autoDeployedModules);
        } else {
            logger.debug("Did not find any flow for {}", watchedFlows.toUrn());
        }
    }

    /**
     * Should deploy existing modules on startup.
     *
     * @return true, if successful
     */
    private boolean shouldDeployExistingModulesOnStartup() {
        final String deployModulesSysPropValue = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER);
        logger.trace("Configuration property [{}] has value [{}]", EpsConfigurationConstants.DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER,
                deployModulesSysPropValue);
        final boolean deployModulesOnStartup = !("false".equals(deployModulesSysPropValue));
        return deployModulesOnStartup;
    }

    /**
     *
     * Parse the flow description from model service and load the corresponding module in EPS
     *
     * @param flowUrn
     *            urn of the flow model
     * @return true if the flow has been deployed correctly
     */
    public final boolean deployModuleFromUrn(final String flowUrn) {
        try {
            logger.trace("Trying to deploy module from [{}]", flowUrn);
            final String moduleId = modulesManager.deployModuleFromModel(flowUrn);
            logger.debug("Successfully deployed module from [{}] - module identifier is {}", flowUrn, moduleId);
            return true;
        } catch (final Exception exc) {
            logger.error("Caught exception while processing event-flow model [{}]. Details {}", flowUrn, exc.getMessage());
        }
        return false;
    }

}
