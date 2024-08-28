package com.ericsson.component.aia.services.eps.builtin.components.mesa.view;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;

public final class PrintingViewListener implements ViewListener {

	@Override
	public void on(final View view) {
		System.out.println("received view " + view);
	}
}
