package com.ericsson.component.aia.services.eps.mesa.event;

import com.ericsson.component.aia.services.eps.mesa.common.resource.ResourceIdAware;
import com.ericsson.component.aia.services.eps.mesa.common.rop.RopAware;
import com.ericsson.component.aia.itpf.common.event.ComponentEvent;

/**
 * Root interface of all events that are used internally MESA, both ingress (i.e. aggregated ROP aligned events) and egress (i.e. generated standard
 * alarms/incidents).
 * <p>
 *
 * TODO review from code guardian is essential in order to make sure event class hierarchy is aligned with overall architecture
 */
public interface Event extends ResourceIdAware, RopAware, ComponentEvent {

    /**
     * Timestamp when this event was generated at the source (i.e. at network element).
     *
     * @return the timestamp as long value.
     */
    long getTimestamp();
}