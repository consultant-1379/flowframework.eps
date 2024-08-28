package com.ericsson.component.aia.services.eps.mesa.common.rop;

/**
 * Marker interface for all events which are ROP aligned.
 */
public interface RopAware {

    long NULL_ROP_ID = -1;

    /**
     * Gets the rop type.
     *
     * @return the rop type
     *
     * @see RopType
     */
    RopType getRopType();

    /**
     * Timestamp of the beginning of the ROP. Based on Java's System.currentTimeMillis().
     *
     * @return the rop id as long value.
     */
    long getRopId();

    /**
     * If false, this object does not have ROP ID, for whatever reason.
     *
     * @return true, if successful
     */
    boolean hasRopId();
}
