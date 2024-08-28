package com.ericsson.component.aia.services.eps.builtin.components.mesa.model;

import java.io.File;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.XmlUtil;

import junit.framework.TestCase;

public class ValidateXmlAgainstSchema extends TestCase {

	public void testPolicyConf() {
		File xml = new File("src/test/resources/policies/policy-set-a/conf.xml");
		File xsd = new File("src/main/resources/xsd/policy-conf.xsd");
		XmlUtil.validate(xml, xsd);
	}
	
	public void testPolicyCore() {
		File xml = new File("src/test/resources/policies/policy-set-a/core.xml");
		File xsd = new File("src/main/resources/xsd/policy-core.xsd");
		XmlUtil.validate(xml, xsd);
	}
}
