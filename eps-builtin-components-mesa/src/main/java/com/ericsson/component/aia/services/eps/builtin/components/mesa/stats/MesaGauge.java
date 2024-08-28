package com.ericsson.component.aia.services.eps.builtin.components.mesa.stats;

import com.codahale.metrics.Gauge;

/**
 * This class implements {@link Gauge}.
 */
@SuppressWarnings("hiding")
public class MesaGauge implements Gauge<Long> {

    private Long duration;

    @Override
    public Long getValue() {
        return duration;
    }

    /**
     * Sets the duration.
     *
     * @param duration
     *            the new duration value
     */
    public void setDuration(final Long duration) {
        this.duration = duration;
    }
}
