package com.ericsson.component.aia.services.eps.builtin.components.mesa.locator;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.locator.ProxyLocator;

//As only a single locator can be configured at a time we only leave one assert active here,
//see src/test/resources/META-INF/services/
public class ProxyLocatorTest extends TestCase {

	public void test() {
		final ProxyLocator locator = new ProxyLocator();
		locator.init();

		// assertTrue(locator.canLocate(
		// INTERNAL_SYSTEM_UTILIZATION_60MIN.class.getSimpleName(), 0, 0));
		// assertTrue(locator.canLocate(
		// INTERNAL_SYSTEM_UTILIZATION.class.getSimpleName(), 0, 0));

		assertTrue(!locator.canLocate(new INTERNAL_SYSTEM_UTILIZATION(), 0));
		// assertTrue(locator
		// .canLocate(new INTERNAL_SYSTEM_UTILIZATION_60MIN(), 0));

		locator.shutdown();
	}
}
