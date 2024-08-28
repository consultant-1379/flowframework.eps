package com.ericsson.component.aia.services.eps.mesa.common.rop;

/**
 * Granularity of the ROP - report output period.
 */
public enum RopType {

    ROP_RAW(0), ROP_5MIN(5 * 60 * 1000), ROP_15MIN(15 * 60 * 1000), ROP_60MIN(60 * 60 * 1000), ;

    private final long duration;

    private RopType(final long duration) {
        this.duration = duration;
    }

    /**
     * In millis.
     *
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }
}
