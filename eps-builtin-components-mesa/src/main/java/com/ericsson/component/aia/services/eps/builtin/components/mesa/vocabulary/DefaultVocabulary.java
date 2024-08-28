package com.ericsson.component.aia.services.eps.builtin.components.mesa.vocabulary;

import com.ericsson.component.aia.services.eps.mesa.vocabulary.BaseVocabulary;

/**
 * Default empty vocabulary which does nothing.
 */
public final class DefaultVocabulary implements BaseVocabulary {

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public String getName() {
        return "default-empty-vocabulary";
    }
}
