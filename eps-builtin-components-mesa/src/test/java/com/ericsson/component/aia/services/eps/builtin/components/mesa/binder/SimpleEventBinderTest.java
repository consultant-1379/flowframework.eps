package com.ericsson.component.aia.services.eps.builtin.components.mesa.binder;

import junit.framework.TestCase;

import org.python.util.PythonInterpreter;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix.SimpleMatrixView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SimpleSequenceView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

public class SimpleEventBinderTest extends TestCase {

	private static final String BINDINGS_FILE = "src/test/resources/mesa-variable-bindings.properties";

	private final PythonInterpreter interpreter = new PythonInterpreter();

	public void test() throws Exception {
		EventBinder binder = new SimpleEventBinder(null);
		binder.bind(new SimpleSingletonView(new SimpleId(0), new SimpleId(0),
				new INTERNAL_SYSTEM_UTILIZATION()), interpreter);

		binder = new SimpleEventBinder(BINDINGS_FILE);
		binder.bind(new SimpleSingletonView(new SimpleId(0), new SimpleId(0),
				new INTERNAL_SYSTEM_UTILIZATION()), interpreter);

		binder = new SimpleEventBinder(BINDINGS_FILE);
		final Event event = new INTERNAL_SYSTEM_UTILIZATION();
		final SimpleMatrixView matrixView = new SimpleMatrixView(
				new SimpleId(0), new SimpleId(0));
		final SimpleSequenceView sequenceView = new SimpleSequenceView(
				new SimpleId(0), new SimpleId(0));
		sequenceView.append(event);
		matrixView.append(sequenceView);
		binder.bind(matrixView, interpreter);

	}
}
