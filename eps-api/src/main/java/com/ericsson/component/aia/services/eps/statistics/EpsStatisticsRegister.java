/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.statistics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;

/**
 * @since 3.1.0
 */
public interface EpsStatisticsRegister {

	/**
	 *
	 * @return true if EPS statistics enabled, false otherwise
	 */
	boolean isStatisticsOn();

	/**
	 * Creates and registers a new Meter.
	 *
	 * The provided meterName will be appended to the handler flow name, flow version and handler instance id to
	 * construct a unique Meter name.
	 *
	 * @param meterName
	 *            the specific Meter name to use
	 * @param eventHandler
	 *            the eventHandler requesting the Meter
	 * @return a {@link Meter}
	 */
	Meter createMeter(final String meterName, final AbstractEventHandler eventHandler);

	/**
	 * Registers a Meter.
	 *
	 * The provided meterName will be used without modification. It is the users reponsibility to ensure uniqueness.
	 *
	 * @param meterName
	 *            the name of the Meter to create
	 * @return a {@link Meter}
	 */
	Meter createMeter(final String meterName);

	/**
	 * Registers a Counter.
	 *
	 * The provided counterName will be used without modification. It is the users reponsibility to ensure uniqueness.
	 *
	 * @param counterName
	 *            the name of the Counter to create
	 * @return a {@link Counter}
	 */
	Counter createCounter(final String counterName);

	/**
	 * Creates and registers a new Counter.
	 *
	 * The provided counterName will be appended to the handler flow name, flow version and handler instance id to
	 * construct a unique Counter name.
	 *
	 * @param counterName
	 *            the name of the Counter to create
	 * @param eventHandler
	 *            the eventHandler requesting the Counter
	 * @return a {@link Counter}
	 */
	Counter createCounter(final String counterName, final AbstractEventHandler eventHandler);

	/**
	 * Registers a Guage.
	 *
	 * The provided gaugeName will be appended to the handler flow name, flow version and handler instance id to
	 * construct a unique Gauge name.
	 *
	 *
	 * @param gaugeName
	 *            the name of the Gauge to create
	 * @param eventHandler
	 *            the eventHandler requesting the Gauge
	 * @param gauge
	 *            the {@link Counter} to register
	 */
	void registerGuage(final String gaugeName, final Gauge<Long> gauge, final AbstractEventHandler eventHandler);

	/**
	 * Registers a Guage.
	 *
	 * The provided gaugeName will be used without modification. It is the users reponsibility to ensure uniqueness.
	 *
	 * @param gaugeName
	 *            the name of the Gauge to create
	 * @param gauge
	 *            the {@link Counter} to register
	 */
	void registerGuage(final String gaugeName, final Gauge<Long> gauge);

	/**
	 * Registers a Counter.
	 * The provided counterName will be used without modification. It is the users responsibility to ensure uniqueness.
	 *
	 * @param counterName
	 *         the name of the Counter to create
	 * @param counter
	 *         the {@link Counter} to register
	 */
	void registerCounter(final String counterName, final Counter counter);

	/**
	 * Registers a Counter.
	 * The provided counterName will be appended to the handler flow name, flow version and handler instance id to
	 * construct a unique Counter name.
	 *
	 * @param counterName
	 *         the name of the Counter to create
	 * @param eventHandler
	 *         the eventHandler requesting the Counter
	 * @param counter
	 *         the {@link Counter} to register
	 */
	void registerCounter(final String counterName, final Counter counter, final AbstractEventHandler eventHandler);
}
