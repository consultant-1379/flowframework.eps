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
package com.ericsson.component.aia.services.eps.builtin.components.mesa.perf;

import java.io.File;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.manager.SimpleManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyConfBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyCoreBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.SimplePolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.SimplePolicy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SimpleSingletonView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SingletonView;

public class MultiThreadInternalsTest extends TestCase {

	private SimpleManager manager;
	private static final String BINDINGS_FILE = "src/test/resources/mesa-variable-bindings.properties";
	private CountingForwarder forwarder;

	@Override
	protected void setUp() throws Exception {
		forwarder = new CountingForwarder();
		manager = SimpleManager.getOrCreateManager();
		final Context context = new SimpleContext(new SimpleEventBinder(
				BINDINGS_FILE), forwarder, manager,
				"src/test/resources/config/esper-test-config2.xml",
				"src/test/resources/template/", 34);

		manager.inject(context);

		final File fileCore = new File(
				"src/test/resources/policiesMultiThreadTest/com.ericsson.component.aia.wcdma_policy-1_1.0/core.xml");
		final PolicyCoreBuilder builderCore = new PolicyCoreBuilder(context);
		final SimplePolicy policy = (SimplePolicy) builderCore.build(fileCore
				.toURI());
		policy.inject(context);
		final File fileConf = new File(
				"src/test/resources/policiesMultiThreadTest/com.ericsson.component.aia.wcdma_policy-1_1.0/conf_1.xml");
		final PolicyConfBuilder builder = new PolicyConfBuilder(context,
				new SimpleId(1));
		final SimplePolicyConf policyConf = (SimplePolicyConf) builder
				.build(fileConf.toURI());

		manager.deployCore(policy);
		manager.deployConf(policyConf);
	}

	public void testMultiThreadWithSingleton() throws Exception {
		final Thread gen1 = new Thread(new GenAndSendView(1));
		final Thread gen2 = new Thread(new GenAndSendView(2));
		final Thread gen3 = new Thread(new GenAndSendView(3));
		final Thread gen4 = new Thread(new GenAndSendView(4));
		gen1.start();
		gen2.start();
		gen3.start();
		gen4.start();
		gen1.join();
		gen2.join();
		gen3.join();
		gen4.join();
		assertTrue(forwarder.getCount() == 400000);
	}

	public class GenAndSendView implements Runnable {

		int id;

		public GenAndSendView(final int id) {
			this.id = id;
		}

		@Override
		public void run() {

			for (int i = 0; i < 100000; i++) {
				final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
				event.setCONSUMED_CREDITS_DL(95);
				event.setCONSUMED_CREDITS_UL(95);
				event.setResourceId(2);
				final SingletonView view = new SimpleSingletonView(
						new SimpleId(1), new SimpleId(1), event);
				manager.on(view);
			}

		}
	}
}
