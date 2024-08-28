package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

public final class CapturingForwarder implements Forwarder {

	private Event event;
	
	public void clear() {
		event = null;
	}
	
	public Event getEvent() {
		return event;
	}

	@Override
	public void init() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void on(Event event) {
		this.event = event;
	}

	@Override
	public void inject(Context simpleContext) {
		// TODO Auto-generated method stub
	}
}
