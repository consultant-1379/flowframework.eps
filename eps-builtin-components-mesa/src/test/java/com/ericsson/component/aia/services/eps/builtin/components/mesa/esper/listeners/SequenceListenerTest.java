package com.ericsson.component.aia.services.eps.builtin.components.mesa.esper.listeners;

import junit.framework.TestCase;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart.SmartSequenceUpdateListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.espertech.esper.client.*;
import com.espertech.esper.client.deploy.EPDeploymentAdmin;
import com.espertech.esper.client.deploy.Module;

public class SequenceListenerTest extends TestCase {
	private EPServiceProvider esper;
	private int sentRopId = -1;
	private int sentResourceId = -1;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final Configuration config = new Configuration();
		esper = EPServiceProviderManager.getProvider("SequenceTest", config);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testTheViewListenerGetsExpectedNumberOfEvents()
			throws Exception {
		esper.getEPAdministrator()
				.getConfiguration()
				.addEventType(
						"INTERNAL_SYSTEM_UTILIZATION_60MIN",
						"com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN");
		loadTestModule();
		loadTestDataSetInRopIdOrder();
		Thread.sleep(2000);

	}

	private void deploy(final String name) throws Exception {
		final EPDeploymentAdmin deployAdmin = esper.getEPAdministrator()
				.getDeploymentAdmin();
		final Module module = deployAdmin.read(Thread.currentThread()
				.getContextClassLoader().getResource(name));
		deployAdmin.deploy(module, null);
	}

	private void loadTestModule() throws Exception {
		deploy("esper/sliding_window_for_test.epl");
		final Id coreId = new SimpleId(1);
		final Id ruleId = new SimpleId(1);
		esper.getEPAdministrator()
				.getStatement("sliding_window_isu60_consumed_credits_threshold")
				.addListener(
						new SmartSequenceUpdateListener(
								new SimpleViewListener(), coreId, ruleId));
	}

	@SuppressWarnings(value = { "unused" })
	private void loadTestDataSetInResourceIdOrder() throws Exception {
		final int resourceCount = 10;
		final int resourceId[] = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
		final int ropId[] = { 10, 15, 20, 25, 30, 35, 40, 45, 50, 55 };

		final EPRuntime epRuntime = esper.getEPRuntime();
		for (int j = 0; j < resourceCount; j++) {
			for (int i = 0; i < resourceCount; i++) {
				final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
				sentResourceId = resourceId[j];
				event.setResourceId(resourceId[j]);
				sentRopId = ropId[i];
				event.setRopId(ropId[i]);
				epRuntime.sendEvent(event);
			}
		}
	}

	private void loadTestDataSetInRopIdOrder() throws Exception {
		final int resourceCount = 10;
		final int resourceId[] = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
		final int ropId[] = { 10, 15, 25, 20, 30, 35, 40, 45, 50, 55 };

		final EPRuntime epRuntime = esper.getEPRuntime();
		for (int j = 0; j < resourceCount; j++) {
			for (int i = 0; i < resourceCount; i++) {
				final INTERNAL_SYSTEM_UTILIZATION_60MIN event = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
				sentResourceId = resourceId[i];
				event.setResourceId(resourceId[i]);
				sentRopId = ropId[j];
				event.setRopId(ropId[j]);
				epRuntime.sendEvent(event);
			}
		}
	}

	private final class SimpleViewListener implements ViewListener {

		public SimpleViewListener() {
		}

		@Override
		public void on(final View view) {
			assertEquals(sentRopId, view.getFirst().getRopId());
			assertEquals(sentResourceId, view.getFirst().getResourceId());
		}

	}
}
