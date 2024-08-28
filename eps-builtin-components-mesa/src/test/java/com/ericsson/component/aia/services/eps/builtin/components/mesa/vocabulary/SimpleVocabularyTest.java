package com.ericsson.component.aia.services.eps.builtin.components.mesa.vocabulary;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.vocabulary.VocabularyLoader;
import com.ericsson.component.aia.services.eps.mesa.vocabulary.BaseVocabulary;

public class SimpleVocabularyTest extends TestCase {

    public void test() {
        final BaseVocabulary vocabulary = VocabularyLoader.load();
        assertNotNull(vocabulary);
        assertNotNull(vocabulary.getName());
    }
}
