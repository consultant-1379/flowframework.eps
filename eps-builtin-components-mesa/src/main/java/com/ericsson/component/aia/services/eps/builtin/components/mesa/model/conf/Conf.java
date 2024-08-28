package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;

/**
 * The Interface Conf.
 */
public interface Conf {

    /**
     * Gets the conf id.
     *
     * @return the conf id
     */
    ConfId getConfId();

    /**
     * Gets the guard.
     *
     * @return the guard
     */
    Guard getGuard();

    /**
     * Gets the tenant.
     *
     * @return the tenant as string.
     */
    String getTenant();
}