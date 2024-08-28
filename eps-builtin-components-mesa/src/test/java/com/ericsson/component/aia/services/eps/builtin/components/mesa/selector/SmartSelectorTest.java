package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector;

import java.util.*;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.MatchMode;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.RuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.SimpleRuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateless.StatelessRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.SmartSelector;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.ReflectionGuard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View.ViewType;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;

public class SmartSelectorTest extends TestCase {

	private final ConfId confId = new ConfId(new SimpleId(0), new SimpleId(0),
			new SimpleId(0), new SimpleId(0));
	private final ConfId confId1 = new ConfId(new SimpleId(1), new SimpleId(1),
			new SimpleId(1), new SimpleId(1));

	private final ConfId badConfId = new ConfId(new SimpleId(2),
			new SimpleId(2), new SimpleId(2), new SimpleId(2));
	private final ConfId badConfId1 = new ConfId(new SimpleId(1), new SimpleId(
			2), new SimpleId(2), new SimpleId(2));

	public void test() {

		final SmartSelector selector = new SmartSelector();
		{
			final Set<Long> set1 = new HashSet<Long>();
			set1.add(1L);
			set1.add(2L);
			final Guard guard1 = new ReflectionGuard<Long>(confId, "assure",
					true, set1, "C_ID_1");
			final SimpleRuleGroup group1 = new SimpleRuleGroup(confId,
					MatchMode.ALL, ViewType.SINGLETON);
			group1.append(new StatelessRule(new SimpleId(0), "dummy1", true));
			selector.register(guard1, group1);

			boolean expectedException = false;
			try {
				selector.register(guard1, group1);
			} catch (final IllegalArgumentException e) {
				expectedException = true;
			}
			assertTrue(expectedException);

		}

		{
			final Set<Long> set2 = new HashSet<Long>();
			set2.add(2L);
			set2.add(3L);
			final Guard guard2 = new ReflectionGuard<Long>(confId1, "assure",
					true, set2, "C_ID_1");
			final SimpleRuleGroup group2 = new SimpleRuleGroup(confId1,
					MatchMode.ALL, ViewType.SINGLETON);
			group2.append(new StatelessRule(new SimpleId(1), "dummy2", true));
			selector.register(guard2, group2);
		}

		{

			final SimpleRuleGroup group1 = new SimpleRuleGroup(confId,
					MatchMode.ALL, ViewType.SINGLETON);
			group1.append(new StatelessRule(new SimpleId(0), "dummy1", true));
			final Set<Long> set2 = new HashSet<Long>();
			set2.add(2L);
			set2.add(3L);
			final Guard guard2 = new ReflectionGuard<Long>(confId1, "assure",
					true, set2, "C_ID_1");

			boolean expectedException = false;
			try {
				selector.register(guard2, group1);
			} catch (final IllegalArgumentException e) {
				expectedException = true;
			}
			assertTrue(expectedException);
		}

		{

			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setC_ID_1(1);
			final View view = new SimpleSingletonView(new SimpleId(0),
					new SimpleId(0), event);

			final List<RuleGroup> list = selector.select(view);

			assertTrue(list.size() == 1);
			assertTrue(list.get(0).getRules().get(0).getName().equals("dummy1"));
		}

		{
			final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
			event.setC_ID_1(3);
			final View view = new SimpleSingletonView(new SimpleId(1),
					new SimpleId(1), event);

			final List<RuleGroup> list = selector.select(view);

			assertTrue(list.size() == 1);
			assertTrue(list.get(0).getRules().get(0).getName().equals("dummy2"));
		}

		{
			selector.unregister(badConfId);
			selector.unregister(badConfId1);
			selector.unregister(confId);
		}
	}
}
