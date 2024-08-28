package com.ericsson.component.aia.services.eps.builtin.components.mesa.binder;

import org.python.util.PythonInterpreter;

/**
 * Binds common services, such as locator, vocabulary.
 */
public interface EnvironmentBinder extends Binder {

    /**
     * Bind.
     *
     * @param interpreter
     *            the interpreter
     */
    void bind(PythonInterpreter interpreter);
}
