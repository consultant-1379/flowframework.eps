package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;

/**
 * The abstract implementation of {@link Guard}.
 */
public abstract class AbstractGuard implements Guard {

    private final ConfId confId;
    private final String tenant;

    /**
     * Instantiates a new abstract guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     */
    public AbstractGuard(final ConfId confId, final String tenant) {
        super();
        this.confId = confId;
        this.tenant = tenant;
    }

    @Override
    public final ConfId getConfId() {
        return confId;
    }

    @Override
    public final String getTenant() {
        return tenant;
    }

    @Override
    public String toString() {
        return "AbstractGuard [confId=" + confId + ", tenant=" + tenant + "]";
    }
}
