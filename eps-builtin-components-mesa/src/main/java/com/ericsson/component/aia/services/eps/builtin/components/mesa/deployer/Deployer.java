package com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer;

/**
 * Scans directories for new core & conf policies.
 */
public interface Deployer {

    /**
     * Should be called only once.
     */
    void start();
}
