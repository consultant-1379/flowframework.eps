package com.ericsson.component.aia.services.eps.coordination;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.component.aia.itpf.common.Monitorable;

/**
 * Stores a measurement.
 * <p>
 * A <code>EpsAdaptiveMetric</code> is reported by a {@link Monitorable} component and used to monitor the status of the
 * component. The meaning of the measurement must be understood by both the Monitorable component and the
 * <code>EpsAdaptiveMetric</code> receiver.
 * <p>
 * Examples of measurements:
 * <ul>
 * <li>moving 1 minute average of events processed per second</li>
 * <li>total size of files read in last scheduled period</li>
 * <li>number of bytes processed</li>
 * <li>number of active connections</li>
 * </ul>
 * 
 * @see Monitorable
 * @since 1.1.102
 */

@XmlRootElement
public class EpsAdaptiveMetric implements Serializable {
	
	private static final long serialVersionUID = -450584822274846895L;
	
	private double metric;
	
	/**
	 * Default constructor needed for serialization.
	 */
	public EpsAdaptiveMetric() {
	}
	
	/**
	 * 
	 * @param metric
	 *            The metric to store. Must not be null.
	 */
	public EpsAdaptiveMetric(final Double metric) {
		ArgumentChecker.verifyArgumentNotNull("metric", metric);
		this.metric = metric;
	}
	
	/**
	 * 
	 * @return metric The stored metric. May be null.
	 */
	public Double getMetric() {
		return this.metric;
	}
	
	/**
	 * 
	 * @param metric
	 *            The metric to store. Must not be null.
	 */
	public void setMetric(final Double metric) {
		ArgumentChecker.verifyArgumentNotNull("metric", metric);
		this.metric = metric;
	}
}