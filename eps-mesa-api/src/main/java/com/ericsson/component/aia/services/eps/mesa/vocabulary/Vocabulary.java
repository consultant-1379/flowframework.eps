package com.ericsson.component.aia.services.eps.mesa.vocabulary;

import java.util.ServiceLoader;

import com.ericsson.component.aia.services.eps.mesa.common.Lifecycle;

/**
 * Marker interface for all vocabularies. <p>
 *
 * Implementations of vocabulary should be loaded via {@link ServiceLoader} API. <p>
 *
 * Note: This interface will become non-empty once first real business feature
 * is implemented.
 */
public interface Vocabulary extends Lifecycle {
}
