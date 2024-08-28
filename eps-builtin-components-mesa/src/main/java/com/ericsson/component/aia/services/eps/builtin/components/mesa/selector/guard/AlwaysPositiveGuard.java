package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * The Class AlwaysPositiveGuard.
 */
public final class AlwaysPositiveGuard extends AbstractGuard {

    /**
     * Instantiates a new always positive guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     */
    public AlwaysPositiveGuard(final ConfId confId, final String tenant) {
        super(confId, tenant);
    }

    @Override
    public Mode getMode() {
        return Mode.ALWAYS_PASS;
    }

    @Override
    public boolean mayPass(final View view) {
        return true;
    }
}
