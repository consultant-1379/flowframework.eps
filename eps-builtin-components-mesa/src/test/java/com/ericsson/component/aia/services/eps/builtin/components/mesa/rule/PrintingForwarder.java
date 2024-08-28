package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

public final class PrintingForwarder implements Forwarder {

	@Override
	public void init() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void on(Event event) {
		System.out.println("received event " + event);
	}

	@Override
	public void inject(Context simpleContext) {
		// TODO Auto-generated method stub
	}
}
