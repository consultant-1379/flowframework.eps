package com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler;

import junit.framework.TestCase;

import org.python.core.PyObject;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.JythonScriptCompiler;

public class JythonScriptCompilerTest extends TestCase {

	public void test() {
		final JythonScriptCompiler compiler = new JythonScriptCompiler();

		final PyObject code = compiler.compile("x = False");
		compiler.getInterp().exec(code);
		final Object object = compiler.getInterp().get("x")
				.__tojava__(Boolean.class);
		assertTrue(Boolean.FALSE.equals(object));
	}
}
