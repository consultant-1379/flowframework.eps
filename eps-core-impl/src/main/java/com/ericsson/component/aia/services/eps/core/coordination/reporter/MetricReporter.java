package com.ericsson.component.aia.services.eps.core.coordination.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveMetric;
import com.ericsson.component.aia.itpf.common.Monitorable;
import com.ericsson.oss.itpf.sdk.cluster.coordination.CoordinationException;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Node;

/**
 * Collects periodic status report from the Monitorable Flow Component.
 *
 */
public class MetricReporter implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Monitorable monitorableComponent;
    private final Node node;
    private final Application epsApplication;

    /**
     * Instantiates a new metric reporter.
     *
     * @param monitorable
     *            the monitorable
     * @param node
     *            the node
     * @param application
     *            the application
     */
    public MetricReporter(final Monitorable monitorable, final Node node, final Application application) {
        this.monitorableComponent = monitorable;
        this.node = node;
        this.epsApplication = application;
    }

    @Override
    public void run() {
        if ((epsApplication == null) || !epsApplication.registered()) {
            log.debug("will not run if epsApplication is null or not registered.");
            return;
        }

        final Object monitorObject = getStatus();

        if (monitorObject == null) {
            log.error("Null Metric is ignored");
            return;
        }
        if (!(monitorObject instanceof Double)) {
            log.error("Metric should be Double typed.");
            return;
        }
        final Double metricValue = (Double) monitorObject;
        if (metricValue != null) {
            try {
                node.createOrUpdate(new EpsAdaptiveMetric(metricValue));
            } catch (CoordinationException | IllegalArgumentException exception) {
                log.error("Exception thrown while updating node {}", node, exception);
            }
        }
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    private Object getStatus() {
        try {
            return monitorableComponent.getStatus();
        } catch (final Exception exception) {
            log.error("exception on get status message [{}] ", exception.getMessage(), exception);
        }
        return null;
    }

}
