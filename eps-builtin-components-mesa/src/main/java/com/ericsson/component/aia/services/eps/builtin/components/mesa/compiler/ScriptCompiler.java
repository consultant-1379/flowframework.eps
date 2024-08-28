package com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler;

/**
 * Used to compile text scripts to native, executable version.
 */
public interface ScriptCompiler {

    /**
     * Compile the jython script.
     *
     * @param <T>
     *            the generic type
     * @param text
     *            the text
     * @return the t
     */
    <T> T compile(String text);
}
