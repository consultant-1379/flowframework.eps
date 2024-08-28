package com.ericsson.component.aia.services.eps.core.coordination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.core.coordination.observer.CoordinationStateHandler;
import com.ericsson.component.aia.services.eps.core.coordination.observer.EpsConfigurationChangeObserver;
import com.ericsson.component.aia.itpf.common.Clustered;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.*;

/**
 * Each Clustered Component within the EPS flow has one ClusteredHandler. ClusteredHandler responsible with the list below;
 * <p/>
 * 1). create a node, 2). observer changes on the node. 3). when a change happens, load the changes.
 */
public class ClusteredHandler implements AdaptiveComponentHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private EpsConfigurationChangeObserver handlerConfigurationObserver;
    private Node handler;
    private final String componentId;
    private final String flowId;
    private final Clustered clustered;

    /**
     * Instantiates a new clustered handler.
     *
     * @param clusteredComponent
     *            the clustered component
     * @param component
     *            the component
     */
    public ClusteredHandler(final Clustered clusteredComponent, final EpsModuleComponent component) {
        clustered = clusteredComponent;
        componentId = component.getInstanceId();
        flowId = component.getModule().getUniqueModuleIdentifier();
        registerApplication();
        log.info("ClusteredHandler created to control Clustered Component [{}] of flow [{}]. ", componentId, flowId);
    }

    /**
     * Register application.
     */
    private void registerApplication() {
        CoordinationStateHandler.trackConnectionState(flowId, this);
    }

    @Override
    public void start(final Application application) {

        if (handlerConfigurationObserver == null) {
            handlerConfigurationObserver = new EpsConfigurationChangeObserver(flowId, componentId, clustered, application);
        }
        handler = application.configure(componentId);
        if (!handler.exists()) {
            handler.create();
        }
        handler.observe().deregister(handlerConfigurationObserver);
        handler.observe().register(handlerConfigurationObserver, NodeObserverType.NODE_ONLY);
        log.info("Clustered Component [{}] inside flow [{}]  is started. ", componentId, flowId);
    }

    @Override
    public void stop() {
        if (handler != null) {
            log.debug("handler != null, deregister component [{}] inside flow [{}]. ", componentId, flowId);
            handler.observe().deregister(handlerConfigurationObserver);
        }
        log.info("Clustered Component [{}] inside flow [{}]  is stopped. ", componentId, flowId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((componentId == null) ? 0 : componentId.hashCode());
        result = (prime * result) + ((flowId == null) ? 0 : flowId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return isAttributesEqual(obj);
    }

    /**
     * Checks if is attributes equal.
     *
     * @param obj
     *            the obj
     * @return true, if is attributes equal
     */
    private boolean isAttributesEqual(final Object obj) {
        final ClusteredHandler other = (ClusteredHandler) obj;
        if (componentId == null) {
            if (other.componentId != null) {
                return false;
            }
        } else if (!componentId.equals(other.componentId)) {
            return false;
        }
        if (flowId == null) {
            if (other.flowId != null) {
                return false;
            }
        } else if (!flowId.equals(other.flowId)) {
            return false;
        }
        return true;
    }

}
