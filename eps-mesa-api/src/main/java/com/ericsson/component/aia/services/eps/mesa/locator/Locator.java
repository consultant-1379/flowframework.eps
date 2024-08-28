package com.ericsson.component.aia.services.eps.mesa.locator;

import java.util.ServiceLoader;

import com.ericsson.component.aia.services.eps.mesa.common.Lifecycle;

/**
 * Marker interface for all locators.
 * <p>
 *
 * Used to access historical analytical data. Visible within rules .
 * <p>
 *
 * Proxy for actual YMER's Query Service and/or KPI Service.
 * <p>
 *
 * Implementations of locator should be loaded via {@link ServiceLoader} API.
 */
public interface Locator extends Lifecycle {

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();
}
