package com.ericsson.component.aia.services.eps.builtin.components.mesa.perf;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.python.core.PyCode;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EnvironmentBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEnvironmentBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Util;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.JythonScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.ScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateless.StatelessRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SimpleSequenceView;

@Ignore
// Test to be run manually if performance figures are required
public class SequenceViewPerformanceTest extends TestCase {

	private static final String BINDINGS_FILE = "src/test/resources/mesa-variable-bindings.properties";

	private static final String DL_VAR_NAME = "var_cc_dl";
	private static final String UL_VAR_NAME = "var_cc_ul";

	private final EventBinder eventBinder = new SimpleEventBinder(BINDINGS_FILE);
	private final EnvironmentBinder envBinder = new SimpleEnvironmentBinder();
	private final CountingForwarder forwarder = new CountingForwarder();
	private final ScriptCompiler compiler = new JythonScriptCompiler();

	private final StatelessRule rule = new StatelessRule(new SimpleId(0),
			"test", true);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final Context context = new SimpleContext(null, forwarder, null, "src/test/resources/config/esper-test-config.xml",
				"src/test/resources/template/", 1000l);

		rule.setForwarder(forwarder);
		rule.setEnvBinder(envBinder);
		rule.setEventBinder(eventBinder);
		rule.register(DL_VAR_NAME, 90);
		rule.register(UL_VAR_NAME, 90);
		rule.inject(context);
	}

	public void testSequence() {
		final String text = Util
				.readTextFileFully("src/test/resources/perf/stateless_sequence.py");

		rule.setCode((PyCode) compiler.compile(text));
		rule.init();

		{
			while (true) {
				final long start = System.currentTimeMillis();
				for (int i = 0; i < 30000; i++) {
					rule.evaluate(gimmeSequenceView(), null);
				}
				final long duration = System.currentTimeMillis() - start;
				System.out.println("duration " + duration + " and count "
						+ forwarder.getCount());
				forwarder.reset();
			}
		}
	}

	private SequenceView gimmeSequenceView() {
		final int size = 8;
		final SimpleSequenceView view = new SimpleSequenceView(new SimpleId(0),
				new SimpleId(0));
		for (int i = 0; i < size; i++) {
			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(95);
			event.setCONSUMED_CREDITS_UL(95);
			view.append(event);
		}
		return view;
	}
}
