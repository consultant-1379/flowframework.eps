package com.ericsson.component.aia.services.eps.mesa.common;

/**
 * Marker interface for all modules which support 2-step lifecycle.
 */
public interface Lifecycle {

    /**
     * Inits the.
     */
    void init();

    /**
     * Shutdown.
     */
    void shutdown();
}
