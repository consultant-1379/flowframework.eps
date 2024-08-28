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
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.util.UrnMatcher;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.listener.ModelsDeploymentEventListener;

/**
 * Listen for deployed flow models in ModelService. For relevant changes in flow models received message process the related flows.
 *
 * @author epiemir
 */
public class FlowModelServiceListener implements ModelsDeploymentEventListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UrnMatcher matcher = new UrnMatcher();
    private final ModelServiceListenerImpl msImpl;

    private class CustomThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(final Runnable run) {
            final Thread thr = new Thread(run);

            // The classloader passed is the EPS classloader, should be set because the callback function is invoked by the ModelService using his own
            // context where the EPS external components are not visible.
            thr.setContextClassLoader(this.getClass().getClassLoader());

            return thr;
        }
    }

    private final ExecutorService execService = Executors.newCachedThreadPool(new CustomThreadFactory());

    private boolean enabled;

    /**
     * Instantiates a new flow model service listener.
     *
     * @param msImpl
     *            the {@link ModelServiceListenerImpl}
     *
     */
    public FlowModelServiceListener(final ModelServiceListenerImpl msImpl) {
        this.msImpl = msImpl;
    }

    /**
     * Sets the urn filter.
     *
     * @param watchedFlows
     *            the new urn filter {@link ModelInfo}
     */
    public void setUrnFilter(final ModelInfo watchedFlows) {
        matcher.set(watchedFlows);
    }

    /**
     *
     * Filter the URN models with {@link UrnMatcher}, parse and deploy the matching models as flows in the EPS.
     *
     * @param flowUrns
     *            list of flow urns
     * @return the number flow deployed correctly
     */
    private int processNewFlows(final Collection<String> flowUrns) {
        int deployed = 0;
        for (final String urn : flowUrns) {
            final ModelInfo modelUrn = ModelInfo.fromUrn(urn);
            if (matcher.match(modelUrn)) {
                if (msImpl.deployModuleFromUrn(modelUrn.toUrn())) {
                    deployed++;
                    logger.trace("Model {} is matching eps namespace, successfully deployed as flow", modelUrn);
                } else {
                    logger.error("Model {} is matching namespace, failed to deploy it as flow", modelUrn);
                }
            }
        }
        return deployed;
    }

    /**
     *
     * Run a thread for asynchronously processing the models added in Model Service. The new Thread is to avoid blocking the ModelService dispatch
     * process (the function is a plain callback), which is shared with other clients.
     *
     *
     * @param flowUrns
     *            list of model urns
     * @return a {@link Future} with the number flow deployed correctly
     *
     */
    public Future<Integer> processDeployedFlows(final Collection<String> flowUrns) {

        if (!execService.isShutdown()) {
            final Future<Integer> deployFuture = execService.submit(new Callable<Integer>() {

                @Override
                public Integer call() {
                    logger.trace("Received {} new deployed models from Model Service", flowUrns.size());
                    final int deployed = processNewFlows(flowUrns);
                    logger.debug("Received {} new models, {} flows deployed", flowUrns.size(), deployed);
                    return deployed;
                }
            });

            return deployFuture;
        }
        logger.error("Tryed to async deploy " + flowUrns + " while sutting down");
        return null;
    }

    @Override
    public void modelsDeployed(final Collection<String> modelsDeployed) {
        logger.trace("FlowModelServiceListener Received something... [{}]", modelsDeployed);
        if (enabled) {
            processDeployedFlows(modelsDeployed);
        }
    }

    @Override
    public void modelsUndeployed(final Collection<String> modelsUndeployed) {

    }

    /**
     * Checks if Model Service is enabled.
     *
     * @return true, if is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets Model Service as enabled.
     *
     * @param enabled
     *            the new enabled
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        logger.debug("ModelService deployment listening mode for EPS " + (enabled ? "enabled" : "disabled"));
    }

}
