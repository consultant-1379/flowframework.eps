package com.ericsson.component.aia.services.eps.builtin.components.mesa.binder;

import org.python.util.PythonInterpreter;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * Binds events from {@link View} to meaningful variable names.
 */
public interface EventBinder extends Binder {

    /**
     * Bind.
     *
     * @param view
     *            the view
     * @param interpreter
     *            the interpreter
     */
    void bind(View view, PythonInterpreter interpreter);
}
