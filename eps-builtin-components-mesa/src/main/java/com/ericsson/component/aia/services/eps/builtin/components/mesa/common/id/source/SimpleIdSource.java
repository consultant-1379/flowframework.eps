package com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source;

import java.util.concurrent.atomic.AtomicLong;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;

/**
 * The Class SimpleIdSource.
 */
public final class SimpleIdSource implements IdSource {

    private final AtomicLong source = new AtomicLong();

    @Override
    public Id newId() {
        return new SimpleId(source.incrementAndGet());
    }
}
