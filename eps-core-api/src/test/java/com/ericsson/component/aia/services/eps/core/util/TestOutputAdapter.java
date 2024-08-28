package com.ericsson.component.aia.services.eps.core.util;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

public class TestOutputAdapter implements OutputAdapter {

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith("test-out:");
    }

    @Override
    public void onEvent(final Object inputEvent) {

    }

    @Override
    public void init(final EventHandlerContext ctx) {

    }

    @Override
    public void destroy() {

    }

}
