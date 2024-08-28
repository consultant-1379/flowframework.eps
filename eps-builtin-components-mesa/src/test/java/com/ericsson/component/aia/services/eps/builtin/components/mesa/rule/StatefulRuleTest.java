package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import junit.framework.TestCase;

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
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.alarm.SimpleStandardAlarm;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateful.StatefulRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.InMemoryState;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SingletonView;

public class StatefulRuleTest extends TestCase {

	private static final String BINDINGS_FILE = "src/test/resources/mesa-variable-bindings.properties";

	private static final String DL_VAR_NAME = "var_cc_dl";
	private static final String UL_VAR_NAME = "var_cc_ul";

	private final EventBinder eventBinder = new SimpleEventBinder(BINDINGS_FILE);
	private final EnvironmentBinder envBinder = new SimpleEnvironmentBinder();
	private final CapturingForwarder forwarder = new CapturingForwarder();
	private final ScriptCompiler compiler = new JythonScriptCompiler();

	private final StatefulRule rule = new StatefulRule(new SimpleId(0), "test",
			true);

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

	public void testSingleton() throws Exception {
		final String text = Util
				.readTextFileFully("src/test/resources/rules/stateful_singleton.py");

		rule.setCode((PyCode) compiler.compile(text));
		rule.init();

		final State state = new InMemoryState(new SimpleId(0));
		{
			forwarder.clear();
			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(95);
			event.setCONSUMED_CREDITS_UL(95);
			final SingletonView view = new SimpleSingletonView(new SimpleId(0),
					new SimpleId(0), event);
			rule.evaluate(view, state);
			assertTrue(forwarder.getEvent() != null);
			assertTrue(forwarder.getEvent() instanceof SimpleStandardAlarm);
			final SimpleStandardAlarm alarm = (SimpleStandardAlarm) forwarder
					.getEvent();
			assertTrue(Integer.valueOf(5).equals(alarm.getPerceivedSeverity()));
		}

		{
			forwarder.clear();
			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(33);
			event.setCONSUMED_CREDITS_UL(22);
			final SingletonView view = new SimpleSingletonView(new SimpleId(0),
					new SimpleId(0), event);
			rule.evaluate(view, state);
			assertTrue(forwarder.getEvent() != null);
			assertTrue(forwarder.getEvent() instanceof SimpleStandardAlarm);
			final SimpleStandardAlarm alarm = (SimpleStandardAlarm) forwarder
					.getEvent();
			assertTrue(Integer.valueOf(1).equals(alarm.getPerceivedSeverity()));
		}
	}
}
