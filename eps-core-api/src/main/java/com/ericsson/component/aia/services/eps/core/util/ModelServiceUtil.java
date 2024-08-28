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
package com.ericsson.component.aia.services.eps.core.util;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.ModelServiceImpl;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelMetaInformation;

/**
 * Utility class to wrap access to {@link ModelService}
 */
public class ModelServiceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EpsUtil.class);

    private static ModelService modelServiceInstance;

    private ModelServiceUtil() {

    }

    /**
     * @return a valid {@link ModelsService} instance, for JSE environment will just create a new instance of ModelServiceImpl as described in
     *         ModelService documentation
     */
    public static ModelService getModelService() {
        if (modelServiceInstance == null) {
            try {
                modelServiceInstance = LoadingUtil.loadSingletonInstance(ModelService.class);
            } catch (final IllegalStateException ex) {
                modelServiceInstance = new ModelServiceImpl();
                LOG.debug("modelService created locally", modelServiceInstance);
            }
        }
        return modelServiceInstance;
    }

    /**
     * Retrieve the models satisfying the {@link ModelInfo} given. Returns <b>only</b> the latest version of the models and supports the wildcard
     * character '*' in any of the {@link ModelInfo} parts. for further details @See ModelMetaInformation#getLatestModelsFromUrn(String)
     *
     * @param modInfo
     *            a {@link ModelInfo} matching all the wanted models.
     * @return the collection of {@link ModelInfo} found inModelServicegetModelMetaInformation
     */
    public static Collection<ModelInfo> getDeployedModelsInfo(final ModelInfo modInfo) {
        LOG.info("find existing event-flow deployments in [{}]", modInfo.toUrn());
        final ModelMetaInformation mti = getModelService().getModelMetaInformation();
        return mti.getLatestModelsFromUrn(modInfo.toUrn());
    }

    /**
     *
     * Retrieve the Configuration Property for enabling and setting the Model Service as source for Flow definitions
     *
     * @return the URN for selecting flows form model service
     */
    public static String getModelServiceUrn() {
        return EPSConfigurationLoader.getConfigurationProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN);
    }

}
