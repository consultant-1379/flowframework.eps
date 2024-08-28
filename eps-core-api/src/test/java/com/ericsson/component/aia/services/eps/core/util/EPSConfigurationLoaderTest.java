/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
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

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;

public class EPSConfigurationLoaderTest {

    @Test
    public void testSystemProperties() {
        System.clearProperty("non_existent_property");
        Assert.assertNull(EPSConfigurationLoader.getConfigurationProperty("non_existent_property"));
        System.setProperty("non_existent_property", "abc");
        Assert.assertEquals("abc", EPSConfigurationLoader.getConfigurationProperty("non_existent_property"));
    }

    @Test
    public void testClasspathProperties() {
        System.clearProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);
        Assert.assertEquals("/a/b/c/", EPSConfigurationLoader.getConfigurationProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME));
    }

}
