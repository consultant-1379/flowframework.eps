package com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.CapturingForwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.InMemoryState;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.SimpleStateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.StateManager;

public class SimpleStateManagerTest extends TestCase {

	public void test() {
		Forwarder forwarder = new CapturingForwarder();
		Context context = new SimpleContext(null, forwarder, null, "src/test/resources/config/esper-test-config.xml",
				"src/test/resources/template/", 1000l);
		
		StateManager manager = new SimpleStateManager();
		manager.inject(context);
		
		State state = manager.get(new SimpleId(0));
		assertNotNull(state);
		assertTrue(state instanceof InMemoryState);
		
		state.put("test", Boolean.FALSE);
		manager.put(state);
		
		state = manager.get(new SimpleId(0));
		assertNotNull(state.get("test"));
		assertTrue(Boolean.FALSE.equals(state.get("test")));
	}
}
