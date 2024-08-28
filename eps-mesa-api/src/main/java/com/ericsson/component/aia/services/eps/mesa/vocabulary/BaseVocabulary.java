package com.ericsson.component.aia.services.eps.mesa.vocabulary;

import java.util.ServiceLoader;

import com.ericsson.component.aia.services.eps.mesa.vocabulary.misc.MiscVocabulary;
import com.ericsson.component.aia.services.eps.mesa.vocabulary.topo.TopoVocabulary;

/**
 * Actual implementations of vocabulary should subclass this class so that it can be loaded via {@link ServiceLoader} API.
 */
public interface BaseVocabulary extends MiscVocabulary, TopoVocabulary {

    /**
     * Gets the Base Vocabulary name.
     *
     * @return the name
     */
    String getName();
}
