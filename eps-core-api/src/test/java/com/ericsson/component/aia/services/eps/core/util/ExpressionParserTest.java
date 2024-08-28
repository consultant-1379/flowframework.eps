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

import org.junit.Assert;
import org.junit.Test;

import com.ericsson.component.aia.services.eps.core.util.ExpressionParser;

public class ExpressionParserTest {

	@Test
	public void test_null_empty() {
		Assert.assertNull(ExpressionParser.replaceAllExpressions(null));
		Assert.assertEquals("", ExpressionParser.replaceAllExpressions(""));
		Assert.assertEquals(" ", ExpressionParser.replaceAllExpressions(" "));
	}

	@Test
	public void test_no_replacement() {
		Assert.assertEquals("ab", ExpressionParser.replaceAllExpressions("ab"));
		Assert.assertEquals("${abc}", ExpressionParser.replaceAllExpressions("${abc}"));
		Assert.assertEquals("${abc} 123", ExpressionParser.replaceAllExpressions("${abc} 123"));
	}

	@Test
	public void test_system_properties() {
		final String propName = "ab_prop_test";
		System.setProperty(propName, "123");
		String res = ExpressionParser.replaceAllExpressions("Test ${sys." + propName + "} ");
		Assert.assertEquals("Test 123 ", res);
		res = ExpressionParser.replaceAllExpressions("${sys." + propName + "} " + "${sys." + propName + "} ");
		Assert.assertEquals("123 123 ", res);
		System.clearProperty(propName);
		res = ExpressionParser.replaceAllExpressions("${sys." + propName + "} ");
		Assert.assertEquals("${sys." + propName + "} ", res);
	}
}
