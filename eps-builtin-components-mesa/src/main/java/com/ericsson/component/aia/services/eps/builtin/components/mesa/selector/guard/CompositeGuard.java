package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * "Cells 1 & 10 for IMSIs 100 and 101."
 */
public final class CompositeGuard extends AbstractGuard {

    private final List<Guard> guards;

    /**
     * Instantiates a new composite guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     */
    public CompositeGuard(final ConfId confId, final String tenant) {
        super(confId, tenant);
        guards = new ArrayList<Guard>();
    }

    /**
     * Append.
     *
     * @param guard
     *            the guard
     */
    public void append(final Guard guard) {
        guards.add(guard);
    }

    @Override
    public Mode getMode() {
        return Mode.COMPOSITE;
    }

    @Override
    public boolean mayPass(final View view) {
        for (final Guard guard : guards) {
            if (!guard.mayPass(view)) {
                return false;
            }
        }
        return true;
    }
}
