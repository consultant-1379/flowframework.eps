package com.ericsson.component.aia.services.eps.builtin.components.mesa.stats;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.*;
import com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;

/**
 * The Mesa statistic proxy.
 */
public class MesaStatsProxy {

    private static final ConcurrentHashMap<String, Counter> knownCounters = new ConcurrentHashMap<String, Counter>();
    private static final ConcurrentHashMap<String, Meter> knownMeters = new ConcurrentHashMap<String, Meter>();
    private static final ConcurrentHashMap<String, Gauge<Long>> knownGauges = new ConcurrentHashMap<String, Gauge<Long>>();

    private static final String mesaIdentifer = "mesa.";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final EpsStatisticsRegister statisticsRegister = new EpsStatisticsRegisterImpl();

    /**
     * Count increment.
     *
     * @param counterName
     *            the counter name
     * @param eventHandler
     *            the EventHandler incrementing the Counter
     */
    public void countIncrement(final String counterName, final AbstractEventHandler eventHandler) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            Counter counter = null;
            if (!knownCounters.containsKey(counterName)) {
                counter = statisticsRegister.createCounter(mesaIdentifer + counterName, eventHandler);
                knownCounters.putIfAbsent(counterName, counter);
                counter.inc();
            } else {
                counter = knownCounters.get(counterName);
                counter.inc();
            }
        }
    }
    /**
     * Count increment.
     *
     * @param counterName
     *            the counter name
     */
    public void countIncrement(final String counterName) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            Counter counter = null;
            if (!knownCounters.containsKey(counterName)) {
                counter = statisticsRegister.createCounter(mesaIdentifer + counterName);
                knownCounters.putIfAbsent(counterName, counter);
                counter.inc();
            } else {
                counter = knownCounters.get(counterName);
                counter.inc();
            }
        }
    }

    /**
     * Count decrement.
     *
     * @param counterName
     *            the counter name
     */
    public void countDecrement(final String counterName) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            Counter counter = null;
            if (!knownCounters.containsKey(counterName)) {
                log.error("Trying to decrement the counter {} before it has been created", counterName);
            } else {
                counter = knownCounters.get(counterName);
                counter.dec();
            }
        }
    }

    /**
     * Creates a named Meter.
     *
     * @param name
     *            the name of the Meter to create
     * @param eventHandler
     *            the EventHandler creating the Meter
     */
    public void meter(final String name, final AbstractEventHandler eventHandler) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            Meter meter = null;
            if (!knownMeters.containsKey(name)) {
                meter = statisticsRegister.createMeter(mesaIdentifer + name, eventHandler);
                knownMeters.putIfAbsent(name, meter);
                meter.mark();
            } else {
                meter = knownMeters.get(name);
                meter.mark();
            }
        }
    }

    /**
     * Creates a named Meter.
     *
     * @param name
     *            the name of the Meter to create
     */
    public void meter(final String name) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            Meter meter = null;
            if (!knownMeters.containsKey(name)) {
                meter = statisticsRegister.createMeter(mesaIdentifer + name);
                knownMeters.putIfAbsent(name, meter);
                meter.mark();
            } else {
                meter = knownMeters.get(name);
                meter.mark();
            }
        }
    }

    /**
     * Register a named Gauge.
     *
     * @param gaugeName
     *            the Gauge name
     * @param gauge
     *            the Gauge to register
     * @param eventHandler
     *            the EventHandler registering the Gauge
     */
    public void registerGauge(final String gaugeName, final Gauge<Long> gauge,
        final AbstractEventHandler eventHandler) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            if (!knownGauges.containsKey(gaugeName)) {
                statisticsRegister.registerGuage(mesaIdentifer + gaugeName, gauge, eventHandler);
                knownGauges.putIfAbsent(gaugeName, gauge);
            } else {
                log.error("This gauge has already been registered");
            }
        }
    }

    /**
     * Register a named Gauge.
     *
     * @param gaugeName
     *            the Gauge name
     * @param gauge
     *            the Gauge to register
     */
    public void registerGauge(final String gaugeName, final Gauge<Long> gauge) {
        final boolean shouldCount = statisticsRegister.isStatisticsOn();
        if (shouldCount) {
            if (!knownGauges.containsKey(gaugeName)) {
                statisticsRegister.registerGuage(mesaIdentifer + gaugeName, gauge);
                knownGauges.putIfAbsent(gaugeName, gauge);
            } else {
                log.error("This gauge has already been registered");
            }
        }
    }
}
