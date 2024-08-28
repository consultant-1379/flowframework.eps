/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.util;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.core.util.ProtocolUriUtil;

public class ProtocolUriUtilTest {

	@Test
	public void test_is_valid_uri() {
		Assert.assertFalse(ProtocolUriUtil.isValidProtocolUri(null));
		Assert.assertFalse(ProtocolUriUtil.isValidProtocolUri(" "));
		Assert.assertFalse(ProtocolUriUtil.isValidProtocolUri("ab"));
		Assert.assertFalse(ProtocolUriUtil.isValidProtocolUri("doRule1"));
		Assert.assertTrue(ProtocolUriUtil.isValidProtocolUri("ab:/"));
		Assert.assertTrue(ProtocolUriUtil.isValidProtocolUri("jms://"));
		Assert.assertTrue(ProtocolUriUtil.isValidProtocolUri("ejb:/"));
		Assert.assertTrue(ProtocolUriUtil.isValidProtocolUri("ejb:/a=b&c=d&p=q"));
		Assert.assertTrue(ProtocolUriUtil.isValidProtocolUri("test:/a=b&"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_extract_configuration_null_uri() {
		ProtocolUriUtil.extractConfigurationFromProtocolUri(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_extract_configuration_invalid_uri() {
		ProtocolUriUtil.extractConfigurationFromProtocolUri("abc");
	}

	@Test
	public void test_extract_configuration() {
		final Map<String, String> config = ProtocolUriUtil.extractConfigurationFromProtocolUri("jms:/a=b");
		Assert.assertEquals(1, config.size());
		Assert.assertEquals("b", config.get("a"));

		final Map<String, String> config2 = ProtocolUriUtil.extractConfigurationFromProtocolUri("jms:/a=b&c=d&e=f");
		Assert.assertEquals(3, config2.size());
		Assert.assertEquals("b", config2.get("a"));
		Assert.assertEquals("d", config2.get("c"));
		Assert.assertEquals("f", config2.get("e"));
	}

}
