package com.ericsson.component.aia.services.eps.builtin.components.mesa;

import java.io.File;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyCoreBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Policy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.CapturingForwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;

public class ComplexTest extends TestCase {

    public void test() {
        final Context context = new SimpleContext(new SimpleEventBinder(null), new CapturingForwarder(), new SimpleViewListener(),
                "src/test/resources/config/esper-test-config.xml", "src/test/resources/template/", 1000l);

        final File file = new File("src/test/resources/policies/policy-set-a/core.xml");

        final PolicyCoreBuilder builder = new PolicyCoreBuilder(context);

        final Policy policy = (Policy) builder.build(file.toURI());
        policy.inject(context);

    }

    private final class SimpleViewListener implements ViewListener {

        @Override
        public void on(final View view) {
            assertNotNull(view);
        }

    }
}
