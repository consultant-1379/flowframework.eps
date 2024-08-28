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
package com.ericsson.component.aia.services.eps.core.util;

import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.*;

import org.junit.Assert;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.core.util.ModelServiceUtil;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelRepoBasedModelMetaInformation;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class EpsTestUtil {

    private final EpsProvider provider = EpsProvider.getInstance();
    private ExecutorService execService = Executors.newSingleThreadExecutor();
    private EpsInstanceManager epsInstanceManager;
    private HazelcastInstance hazelcastInstance = null;

    private String epsMSNameSpace = "";

    public EpsInstanceManager createEpsInstanceInNewThread() throws InterruptedException, ExecutionException {

        // Added for tests that share EpsTestUtil instances through inheritance,
        // like S1 and X2 correlations.
        if (execService.isShutdown()) {
            execService = Executors.newSingleThreadExecutor();
        }

        final Future<EpsInstanceManager> epsFuture = execService.submit(new Callable<EpsInstanceManager>() {

            @Override
            public EpsInstanceManager call() {
                return EpsInstanceManager.getInstance();
            }

        });

        epsInstanceManager = epsFuture.get();

        epsInstanceManager.start();

        Assert.assertNotNull("Eps instance not found " + epsInstanceManager);

        return epsInstanceManager;
    }

    public EpsInstanceManager getEpsInstance() {
        return epsInstanceManager;
    }

    public ModelService getModelService() {
        return ModelServiceUtil.getModelService();
    }

    public void shutdownEpsInstance() {
        epsInstanceManager.getModuleManager().undeployAllModules();
        epsInstanceManager.stop();
        execService.shutdownNow();
        provider.clean();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            hazelcastInstance.getLifecycleService().shutdown();
        }
    }

    public HazelcastInstance createHazelcastInstance() {
        return createHazelcastInstance(new Config());
    }

    public HazelcastInstance createHazelcastInstance(final Config cfg) {
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(cfg);
        return this.hazelcastInstance;
    }

    public String deployModule(final String flowPath) {
        if (epsMSNameSpace.isEmpty()) {
            final InputStream inputStream = Resources.getClasspathResource(flowPath).getInputStream();
            Assert.assertNotNull("Resource flow not found for: " + flowPath, inputStream);
            return deployModuleFromFile(inputStream);
        } else {
            final int indexOfSlash = flowPath.lastIndexOf("/");
            String simpleFileName = flowPath;
            if (indexOfSlash != -1) {
                simpleFileName = flowPath.substring(indexOfSlash + 1, flowPath.length());
                simpleFileName = simpleFileName.replace(".xml", "");
            }
            final String urn = epsMSNameSpace + "/" + simpleFileName + "/*";
            final Collection<ModelInfo> infos = getModelService().getModelMetaInformation().getLatestModelsFromUrn(urn);
            Assert.assertTrue("URN not found " + urn, !infos.isEmpty());
            return deployModuleFromModel(infos.iterator().next().toUrn());
        }
    }

    public String deployModuleFromFile(final InputStream inputStream) {
        if (epsInstanceManager == null) {
            throw new IllegalStateException("EPS not started!");
        }
        final ModuleManager manager = epsInstanceManager.getModuleManager();
        return manager.deployModuleFromFile(inputStream);
    }

    public String deployModuleFromModel(final String modelUrn) {
        if (epsInstanceManager == null) {
            throw new IllegalStateException("EPS not started!");
        }
        final ModuleManager manager = epsInstanceManager.getModuleManager();
        return manager.deployModuleFromModel(modelUrn);
    }
    
    


    /**
     * Configures how EPS flows will be deployed for testing.
     *
     * If epsMSNameSpace is NOT empty, EPS flows will be deployed from the ModelService after the test starts. Ensures that deployment of models to
     * model service is tested.
     *
     * If epsMSNameSpace IS empty, existing and new EPS flows will be deployed from the file system.
     *
     * @param epsMSNameSpace
     *            The namespace for EPS flows deployed in the ModelService, if empty flows will be deployed from the file system
     */
    public void setEpsRepositoryType(final String epsMSNameSpace) {
        if (!epsMSNameSpace.isEmpty()) {
            this.epsMSNameSpace = "/" + SchemaConstants.FBP_FLOW + "/" + epsMSNameSpace;
            System.setProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN, this.epsMSNameSpace + "/*/*");
            System.setProperty(EpsConfigurationConstants.DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER, "false");
        } else {
            this.epsMSNameSpace = "";
            System.setProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN, "");
            System.setProperty(EpsConfigurationConstants.DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER, "true");
        }
    }

    public boolean isModelService() {
        final String xmlRepo = System.getProperty(ModelRepoBasedModelMetaInformation.MODEL_REPO_PATH_PROPERTY);
        return (xmlRepo != null && !xmlRepo.isEmpty());
    }
    
    public int undeployAllModules() {
        if (epsInstanceManager == null) {
            throw new IllegalStateException("EPS not started!");
        }
        final ModuleManager manager = epsInstanceManager.getModuleManager();
        return manager.undeployAllModules();
    }
    
   

    public int getDeployedModulesCount() {
        if (epsInstanceManager == null) {
            throw new IllegalStateException("EPS not started!");
        }
        final ModuleManager manager = epsInstanceManager.getModuleManager();
        return manager.getDeployedModulesCount();
    }

}
