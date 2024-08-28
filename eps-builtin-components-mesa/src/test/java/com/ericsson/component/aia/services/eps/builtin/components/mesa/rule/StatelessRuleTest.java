package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import java.io.Serializable;

import junit.framework.TestCase;

import org.python.core.PyCode;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Util;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.JythonScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.ScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.AbstractEvent;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateless.StatelessRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SimpleSequenceView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SingletonView;

public final class StatelessRuleTest extends TestCase {

	private static final String BINDINGS_FILE = "src/test/resources/mesa-variable-bindings.properties";

	private static final String DL_VAR_NAME = "var_cc_dl";
	private static final String UL_VAR_NAME = "var_cc_ul";

	private final EventBinder eventBinder = new SimpleEventBinder(BINDINGS_FILE);
	private final EnvironmentBinder envBinder = new SimpleEnvironmentBinder();
	private final CapturingForwarder forwarder = new CapturingForwarder();
	private final ScriptCompiler compiler = new JythonScriptCompiler();

	private final StatelessRule rule = new StatelessRule(new SimpleId(0),
			"test", true);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final Context context = new SimpleContext(null, forwarder, null,
				"src/test/resources/config/esper-test-config.xml",
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
				.readTextFileFully("src/test/resources/rules/stateless_singleton.py");

		rule.setCode((PyCode) compiler.compile(text));
		rule.init();

		{
			forwarder.clear();
			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(95);
			event.setCONSUMED_CREDITS_UL(95);
			final SingletonView view = new SimpleSingletonView(new SimpleId(0),
					new SimpleId(0), event);
			rule.evaluate(view, null);
			assertTrue(forwarder.getEvent() != null);
		}
		{
			forwarder.clear();

			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(33);
			event.setCONSUMED_CREDITS_UL(22);
			final SingletonView view = new SimpleSingletonView(new SimpleId(0),
					new SimpleId(0), event);
			rule.evaluate(view, null);
			assertTrue(forwarder.getEvent() == null);
		}

		{
			forwarder.clear();
			final DummyEvent event = new DummyEvent();
			final SingletonView view = new SimpleSingletonView(new SimpleId(0),
					new SimpleId(0), event);
			rule.evaluate(view, null);
			assertTrue(forwarder.getEvent() == null);
		}

		rule.shutdown();
	}

	public void testSequence() {
		final String text = Util
				.readTextFileFully("src/test/resources/rules/stateless_sequence.py");

		rule.setCode((PyCode) compiler.compile(text));
		rule.init();

		{
			forwarder.clear();
			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(95);
			event.setCONSUMED_CREDITS_UL(95);

			final SimpleSequenceView view = new SimpleSequenceView(
					new SimpleId(0), new SimpleId(0));
			view.append(event);
			view.append(event);

			rule.evaluate(view, null);
			assertTrue(forwarder.getEvent() != null);
		}
		{
			forwarder.clear();

			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setCONSUMED_CREDITS_DL(33);
			event.setCONSUMED_CREDITS_UL(22);

			final SimpleSequenceView view = new SimpleSequenceView(
					new SimpleId(0), new SimpleId(0));
			view.append(event);
			view.append(event);

			rule.evaluate(view, null);
			assertTrue(forwarder.getEvent() == null);
		}
		rule.shutdown();
	}

	// renable once matrix view has been implemented
	// public void testMatrix() throws Exception {
	// String text =
	// Util.readTextFileFully("src/test/resources/rules/stateless_matrix.py");
	//
	// rule.setCode((PyCode)compiler.compile(text));
	// rule.init();
	//
	// {
	// forwarder.clear();
	//
	// INTERNAL_SYSTEM_UTILIZATION_60MIN aggEvent = new
	// INTERNAL_SYSTEM_UTILIZATION_60MIN();
	// aggEvent.setCONSUMED_CREDITS_DL(95);
	// aggEvent.setCONSUMED_CREDITS_UL(95);
	// SimpleSequenceView aggView = new SimpleSequenceView();
	// aggView.append(aggEvent);
	// aggView.append(aggEvent);
	//
	// INTERNAL_SYSTEM_UTILIZATION rawEvent = new INTERNAL_SYSTEM_UTILIZATION();
	// rawEvent.setCONSUMED_CREDITS_DL(95);
	// rawEvent.setCONSUMED_CREDITS_UL(95);
	// SimpleSequenceView rawView = new SimpleSequenceView();
	// rawView.append(rawEvent);
	// rawView.append(rawEvent);
	//
	// SimpleMatrixView view = new SimpleMatrixView();
	// view.append(aggView);
	// view.append(rawView);
	//
	// rule.evaluate(view, null);
	// assertTrue(forwarder.getEvent() != null);
	// }
	// }

	public class DummyEvent extends AbstractEvent implements Serializable {

		private static final long serialVersionUID = 1L;

	}
}
