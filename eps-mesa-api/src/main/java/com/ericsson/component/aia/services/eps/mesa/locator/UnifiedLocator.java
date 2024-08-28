package com.ericsson.component.aia.services.eps.mesa.locator;

import java.util.ServiceLoader;

/**
 * Actual implementations of {@link Locator} API should use this interface so
 * that they can be loaded via {@link ServiceLoader} API.
 */
public interface UnifiedLocator extends SmartLocator, LegacyLocator {

}
