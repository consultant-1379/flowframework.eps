package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;

/**
 * The Class SimpleConf.
 */
public final class SimpleConf implements Conf {

    private final ConfId confId;
    private final String tenant;
    private final Guard guard;

    /**
     * Instantiates a new simple conf.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     * @param guard
     *            the guard
     */
    public SimpleConf(final ConfId confId, final String tenant, final Guard guard) {
        super();
        this.confId = confId;
        this.tenant = tenant;
        this.guard = guard;
    }

    @Override
    public ConfId getConfId() {
        return confId;
    }

    @Override
    public Guard getGuard() {
        return guard;
    }

    @Override
    public String getTenant() {
        return tenant;
    }
}
