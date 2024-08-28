package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder;

import java.io.File;

import junit.framework.TestCase;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyConfBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyCoreBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.SimplePolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.SimplePolicy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.CapturingForwarder;

public class PolicyBuilderTest extends TestCase {

	Forwarder forwarder = new CapturingForwarder();
	private final Context ctx = new SimpleContext(null, forwarder, null,
			"src/test/resources/config/esper-test-config.xml",
			"src/test/resources/template/", 1000l);

	public void testPolicyCore() {

		final File file = new File(
				"src/test/resources/policies/policy-set-a/core.xml");

		final PolicyCoreBuilder builder = new PolicyCoreBuilder(ctx);

		final SimplePolicy policy = (SimplePolicy) builder.build(file.toURI());
		System.out.println(policy.getName());
	}

	public void testPolicyConf() {
		final File file = new File(
				"src/test/resources/policies/policy-set-a/conf.xml");

		final PolicyConfBuilder builder = new PolicyConfBuilder(ctx,
				new SimpleId(1));

		final SimplePolicyConf policyConf = (SimplePolicyConf) builder
				.build(file.toURI());
		System.out.println(policyConf.getPolicyConfId());
	}
}
