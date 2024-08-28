package com.ericsson.component.aia.services.eps.builtin.components.mesa.vocabulary;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.ericsson.component.aia.services.eps.mesa.vocabulary.BaseVocabulary;

/**
 * Currently only one instance of Vocabulary per JVM is allowed. This might change in the future, depending on requirements.
 */
public abstract class VocabularyLoader {

    private VocabularyLoader() {
    }

    /**
     * Loads instance of {@link BaseVocabulary} using {@link ServiceLoader} API.
     *
     * @return the base vocabulary
     */
    public static BaseVocabulary load() {
        final ServiceLoader<BaseVocabulary> loader = ServiceLoader.load(BaseVocabulary.class);
        final Iterator<BaseVocabulary> itr = loader.iterator();
        if (!itr.hasNext()) {
            throw new IllegalStateException("No Vocabulary detected thru ServiceLoader API");
        }
        final BaseVocabulary vocabulary = itr.next();
        if (itr.hasNext()) {
            throw new IllegalStateException("Duplicate vocabulary detected thru ServiceLoader API");
        }
        return vocabulary;
    }

}
