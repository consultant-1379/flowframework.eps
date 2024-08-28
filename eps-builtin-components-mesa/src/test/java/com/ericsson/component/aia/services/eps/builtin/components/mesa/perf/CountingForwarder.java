package com.ericsson.component.aia.services.eps.builtin.components.mesa.perf;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

public final class CountingForwarder implements Forwarder {

	private int count = 0;

	public void reset() {
		count = 0;
	}

	public int getCount() {
		return count;
	}

	@Override
	public void init() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void inject(final Context context) {
	}

	@Override
	public synchronized void on(final Event t) {
		++count;
	}
}
