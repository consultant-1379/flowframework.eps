package com.ericsson.component.aia.services.eps.builtin.components.mesa.locator;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION;
import com.ericsson.component.aia.services.eps.mesa.event.Event;
import com.ericsson.component.aia.services.eps.mesa.locator.UnifiedLocator;

public final class TestAdsLocator implements UnifiedLocator {

	@Override
	public void init() {
	}

	@Override
	public void shutdown() {
	}
	
	@Override
	public boolean canLocate(Event source, int rops) {
		return source.getClass().getSimpleName().equalsIgnoreCase(INTERNAL_SYSTEM_UTILIZATION.class.getSimpleName());
	}

	@Override
	public boolean canLocate(String eventTypeName, long resourceId, long ropId) {
		return false;
	}

	@Override
	public Event locate(Event source, int rops) {
		return null;
	}

	@Override
	public Event locate(String eventTypeName, long resourceId, long ropId) {
		return null;
	}

	@Override
	public String getName() {
		return "ads-locator";
	}
}
