package com.ericsson.component.aia.services.eps.mesa.locator;

/**
 * Locator using legacy data naming and identification conventions. i.e.
 * mechanics that we have today. This locator would transitively depend on ADS -
 * Analytical Data Store (OLAP database) since that is the only persistent
 * analytical data store we have today. <p>
 *
 * Note: This interface will become non-empty once first real business feature
 * is implemented.
 */
public interface LegacyLocator extends Locator {
}
