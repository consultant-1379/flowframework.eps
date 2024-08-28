/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.CompositeGuard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.ReflectionGuard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;

public class CompositeGuardTest {

	@Test
	public void test() {
		final Set<Long> set = new HashSet<Long>();
		set.add(1L);
		set.add(2L);

		final ConfId id = new ConfId(new SimpleId(0), new SimpleId(0),
				new SimpleId(0), new SimpleId(0));
		final CompositeGuard compositeGuard = new CompositeGuard(id, "assure");

		final Guard guard1 = new ReflectionGuard<Long>(null, "assure", true,
				set, "C_ID_1");

		compositeGuard.append(guard1);

		INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(1);
		View view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0),
				event);
		assertTrue(compositeGuard.mayPass(view));

		event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
		event.setC_ID_1(0);
		view = new SimpleSingletonView(new SimpleId(0), new SimpleId(0), event);
		assertFalse(compositeGuard.mayPass(view));
	}
}
