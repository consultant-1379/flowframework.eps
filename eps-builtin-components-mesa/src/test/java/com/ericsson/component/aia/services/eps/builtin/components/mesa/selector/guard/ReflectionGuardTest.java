package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.AbstractEvent;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.ReflectionGuard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;

public class ReflectionGuardTest extends TestCase {

	public void test() {
		final Set<Long> set = new HashSet<Long>();
		set.add(1L);
		set.add(2L);

		final Guard guard = new ReflectionGuard<Long>(null, "assure", true,
				set, "C_ID_1");

		INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(1);
		View view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0),
				event);
		assertTrue(guard.mayPass(view));

		event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(2);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event);
		assertTrue(guard.mayPass(view));

		event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(3);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event);
		assertFalse(guard.mayPass(view));

		final Guard guard2 = new ReflectionGuard<Long>(null, "assure", true,
				set, "testObject");

		final TestEvent event2 = new TestEvent();
		event2.setTestObject(null);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event2);
		assertFalse(guard2.mayPass(view));

		final Guard guard3 = new ReflectionGuard<Long>(null, "assure", false,
				set, "C_ID_1");
		event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(1);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event);
		assertFalse(guard3.mayPass(view));

		event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(0);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event);
		assertTrue(guard3.mayPass(view));

		boolean expectedException = false;
		try {
			final Guard guard4 = new ReflectionGuard<Long>(null, "assure",
					false, set, "xxxxxxxxxx");
			event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setC_ID_1(0);
			view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0),
					event);
			assertTrue(guard4.mayPass(view));
		} catch (final MesaException e) {
			expectedException = true;
		}
		assertTrue(expectedException);

		final ReflectionGuard<Long> guard4 = new ReflectionGuard<Long>(null,
				"assure", true, "C_ID_1");
		guard4.append(set);
		guard4.append(3l);

		event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(3);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event);
		assertTrue(guard4.mayPass(view));

		expectedException = false;
		try {
			final Guard guard5 = new ReflectionGuard<Long>(null, "assure",
					false, set, "someField");
			Guard.Mode.parse("nonPresetValueInEnum");
		} catch (final IllegalArgumentException e) {
			expectedException = true;
		}
		assertTrue(expectedException);

	}

	private class TestEvent extends AbstractEvent implements Serializable {

		private static final long serialVersionUID = 1L;
		private Object testObject;

		public Object getTestObject() {
			return testObject;
		}

		public void setTestObject(final Object testObject) {
			this.testObject = testObject;
		}
	}
}
