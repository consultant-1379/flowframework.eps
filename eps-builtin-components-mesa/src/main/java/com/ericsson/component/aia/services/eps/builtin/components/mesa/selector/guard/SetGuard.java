package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * The Class SetGuard.
 *
 * @param <T>
 *            the generic type
 */
public abstract class SetGuard<T> extends AbstractGuard {

    // if true, it means list of resources allowed to pass
    protected final boolean allowed;
    private final Set<T> set;

    /**
     * Instantiates a new sets the guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     * @param allowed
     *            the allowed
     * @param set
     *            the set
     */
    public SetGuard(final ConfId confId, final String tenant, final boolean allowed, final Set<T> set) {
        super(confId, tenant);
        this.allowed = allowed;
        this.set = set;
    }

    /**
     * Instantiates a new sets the guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     * @param allowed
     *            the allowed
     */
    public SetGuard(final ConfId confId, final String tenant, final boolean allowed) {
        super(confId, tenant);
        this.allowed = allowed;
        this.set = new HashSet<T>();
    }

    /**
     * Gets the set of Guard.
     *
     * @return the sets the
     */
    protected final Set<T> getSet() {
        return set;
    }

    /**
     * Append the value to Set.
     *
     * @param value
     *            the value
     */
    public final void append(final T value) {
        set.add(value);
    }

    /**
     * Append the set to Set.
     *
     * @param set
     *            the set
     */
    public final void append(final Set<T> set) {
        this.set.addAll(set);
    }

    @Override
    public final Mode getMode() {
        return Mode.SET;
    }

    @Override
    public final boolean mayPass(final View view) {
        return matches(view);
    }

    /**
     * Definition of Matches.
     *
     * @param view
     *            the view
     * @return true, if successful
     */
    protected abstract boolean matches(View view);
}
