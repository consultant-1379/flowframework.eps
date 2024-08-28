package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EnvironmentBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source.IdSource;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.ScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.alert.SimpleStandardAlert;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.EpsForwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.MesaEsperHandler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator.Validator;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.Selector;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.StateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.stats.MesaStatsProxy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class EpsForwarderTest extends TestCase {

	public void test() {
		CollectingEventSubscriber sub = new CollectingEventSubscriber();
		EpsForwarder forwarder = new EpsForwarder(Arrays.asList((EventSubscriber)sub));
		forwarder.inject(new DummyContext());
		
		forwarder.on(new SimpleStandardAlert());
		
		assertEquals(1, sub.events.size());
	}
	
	private static final class CollectingEventSubscriber implements EventSubscriber {

		private final List<Object> events = new ArrayList<Object>();
		
		@Override
		public String getIdentifier() {
			return getClass().getName();
		}

		@Override
		public void sendEvent(Object event) {
			events.add(event);
		}
	}
	
	private static final class DummyContext implements Context {

		@Override
		public ScriptCompiler scriptCompiler() {
			return null;
		}

		@Override
		public StateManager stateManager() {
			return null;
		}

		@Override
		public EventBinder eventBinder() {
			return null;
		}

		@Override
		public EnvironmentBinder environmentBinder() {
			return null;
		}

		@Override
		public Forwarder forwarder() {
			return null;
		}

		@Override
		public IdSource idSource() {
			return null;
		}

		@Override
		public Validator validator() {
			return null;
		}

		@Override
		public MesaStatsProxy stats() {
			return new MesaStatsProxy();
		}

		@Override
		public Selector selector() {
			return null;
		}

		@Override
		public MesaEsperHandler esperHandler() {
			return null;
		}

		@Override
		public ViewListener viewListener() {
			return null;
		}

		@Override
		public long getEvaluationTimeLimit() {
			return 0;
		}
	}
}
