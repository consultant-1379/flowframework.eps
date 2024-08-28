package com.ericsson.component.aia.services.eps.builtin.components.mesa.binder;

import org.python.util.PythonInterpreter;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.locator.ProxyLocator;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.vocabulary.VocabularyLoader;
import com.ericsson.component.aia.services.eps.mesa.vocabulary.BaseVocabulary;

/**
 * The Class SimpleEnvironmentBinder implements {@link EnvironmentBinder}.
 */
public final class SimpleEnvironmentBinder implements EnvironmentBinder {

    @Override
    public void bind(final PythonInterpreter interpreter) {
        final BaseVocabulary vocabulary = VocabularyLoader.load();
        vocabulary.init();
        final ProxyLocator locator = new ProxyLocator();
        locator.init();

        interpreter.set("v", vocabulary);
        interpreter.set("vocabulary", vocabulary);
        interpreter.set("l", locator);
        interpreter.set("locator", locator);
    }
}
