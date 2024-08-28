package com.ericsson.component.aia.services.eps.builtin.components.mesa.binder;

import junit.framework.TestCase;

import org.python.util.PythonInterpreter;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EnvironmentBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEnvironmentBinder;

public class SimpleEnvironmentBinderTest extends TestCase {

	private final PythonInterpreter interpreter = new PythonInterpreter();

	public void test() {
		final EnvironmentBinder binder = new SimpleEnvironmentBinder();
		binder.bind(interpreter);
	}
}
