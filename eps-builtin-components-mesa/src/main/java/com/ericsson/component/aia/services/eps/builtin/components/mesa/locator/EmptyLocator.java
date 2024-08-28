package com.ericsson.component.aia.services.eps.builtin.components.mesa.locator;

import com.ericsson.component.aia.services.eps.mesa.event.Event;
import com.ericsson.component.aia.services.eps.mesa.locator.UnifiedLocator;

/**
 * The Class EmptyLocator manages the empty event locator
 *
 * @see UnifiedLocator.
 */
public final class EmptyLocator implements UnifiedLocator {

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public String getName() {
        return "empty-locator";
    }

    @Override
    public boolean canLocate(final Event event, final int ropCount) {
        return false;
    }

    @Override
    public boolean canLocate(final String eventTypeName, final long resourceId, final long ropId) {
        return false;
    }

    @Override
    public Event locate(final Event source, final int ropCount) {
        return null;
    }

    @Override
    public Event locate(final String eventTypeName, final long resourceId, final long ropId) {
        return null;
    }
}
