package com.ericsson.component.aia.services.eps.core.coordination;

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.core.coordination.observer.CoordinationStateHandler;
import com.ericsson.component.aia.services.eps.core.coordination.reporter.MetricReporter;
import com.ericsson.component.aia.itpf.common.Monitorable;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Node;

/**
 * Each monitorable component inside the EPS flow has one MonitorableHandler. (Starts)Initiates/Restarts/Stops monitorable component.
 *
 */
public class MonitorableHandler implements AdaptiveComponentHandler {

    private static final Integer AWAIT_TERMINATION_TIMEOUT = 5000;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final Monitorable monitorable;

    private ScheduledExecutorService scheduledExecutorService;

    private final String componentId;

    private final String flowId;

    private ScheduledFuture<?> scheduledFuture;

    private final Integer initialDelay = EpsEngineConstants.INITIAL_MONITORING_DELAY;

    private Integer monitoringInterval = EpsEngineConstants.DEFAULT_MONITORING_PERIOD;

    /**
     * Instantiates a new monitorable handler.
     *
     * @param component
     *            the component
     * @param monitorableComponent
     *            the monitorable component
     */
    public MonitorableHandler(final EpsModuleComponent component, final Monitorable monitorableComponent) {
        componentId = component.getInstanceId();
        flowId = component.getModule().getUniqueModuleIdentifier();
        monitorable = monitorableComponent;
        setMonitoringInterval(component);
        registerApplication();
        LOGGER.info("Monitorable Component [{}] is detected inside flow [{}], is created with monitoring interval [{}]. ", componentId, flowId,
                monitoringInterval);
    }

    /**
     * Sets the monitoring interval.
     *
     * @param component
     *            the new monitoring interval
     */
    private void setMonitoringInterval(final EpsModuleComponent component) {
        final Integer configuredInterval = component.getConfiguration().getIntProperty("monitoringInterval");
        if (configuredInterval != null) {
            this.monitoringInterval = configuredInterval;
        }
    }

    @Override
    public void start(final Application application) {
        final Node metricReportNode = application.report(componentId);
        if (!metricReportNode.exists()) {
            LOGGER.info("Metric node created for component [{}] of flow [{}].", flowId, componentId);
            metricReportNode.create();
        }
        shutdownExecutor();
        LOGGER.info("Metric Reporter Started.");
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        final MetricReporter metricReporter = new MetricReporter(monitorable, metricReportNode, application);
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(metricReporter, initialDelay, monitoringInterval, TimeUnit.MILLISECONDS);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Monitorable Component [{}] is started inside flow [{}] with monitoring interval [{}] milliseconds. ", componentId,
                    flowId, monitoringInterval);
        }
    }

    /**
     * Checks if is scheduler passive.
     *
     * @return true, if is scheduler passive
     */
    private boolean isSchedulerPassive() {
        return (scheduledFuture == null) || scheduledFuture.isCancelled();
    }

    @Override
    public void stop() {
        shutdownExecutor();
    }

    /**
     * Register application.
     */
    private void registerApplication() {
        CoordinationStateHandler.trackConnectionState(flowId, this);
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
        final MonitorableHandler other = (MonitorableHandler) obj;
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

    /**
     * Shutdown executor.
     */
    private void shutdownExecutor() {
        if (!isSchedulerPassive()) {
            scheduledExecutorService.shutdown();
            scheduledFuture.cancel(true);
            try {
                scheduledExecutorService.awaitTermination(AWAIT_TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException interruptedException) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Cannot awaitTermination of scheduledExecutorService", interruptedException);
                }
            }
        }
    }
}
