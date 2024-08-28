package com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interpreter object seems to be thread safe.
 *
 * @see ScriptCompiler
 */
public final class JythonScriptCompiler implements ScriptCompiler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PythonInterpreter interp;

    /**
     * Instantiates a new jython script compiler.
     */
    public JythonScriptCompiler() {
        super();
        interp = new PythonInterpreter();
    }

    /**
     * For now used only for testing.
     *
     * @return the interp {@link PythonInterpreter}
     */
    PythonInterpreter getInterp() {
        return interp;
    }

    /**
     * Compile the jython script.
     *
     * @param <T>
     *            the generic type
     * @param text
     *            the text
     * @return the t
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T compile(final String text) {
        if (log.isTraceEnabled()) {
            log.trace("Preparing to compile jython script: [{}]", text);
        }
        final long start = System.currentTimeMillis();
        final T genT = (T) interp.compile(text);
        final long duration = System.currentTimeMillis() - start;
        if (log.isTraceEnabled()) {
            log.trace("Script compilation took {} millis", duration);
        }
        return genT;
    }
}
