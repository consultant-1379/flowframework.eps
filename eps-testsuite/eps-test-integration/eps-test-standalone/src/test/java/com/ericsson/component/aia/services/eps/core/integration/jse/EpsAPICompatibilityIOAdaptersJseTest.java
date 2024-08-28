/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jse;

import java.io.InputStream;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.oss.itpf.sdk.resources.Resources;

public class EpsAPICompatibilityIOAdaptersJseTest {

    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    @Before
    public void setup() throws Exception {
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    @Test
    public void test_io_adapters_implementing_eps_api() throws Exception {

        Assert.assertEquals(0, epsTestUtil.getDeployedModulesCount());

        final InputStream moduleInputStream = Resources.getClasspathResource("/flows/compatibility_api_io_adapters.xml").getInputStream();
        Assert.assertNotNull(moduleInputStream);

        epsTestUtil.deployModuleFromFile(moduleInputStream);

        Assert.assertEquals(1, epsTestUtil.undeployAllModules());

    }

    @Test
    public void test_io_adapters_implementing_eps_and_ff_api() throws Exception {

        Assert.assertEquals(0, epsTestUtil.getDeployedModulesCount());

        final InputStream moduleInputStream = Resources.getClasspathResource("/flows/compatibility_api_mixed_io_adapters.xml").getInputStream();
        Assert.assertNotNull(moduleInputStream);

        epsTestUtil.deployModuleFromFile(moduleInputStream);

        Assert.assertEquals(1, epsTestUtil.undeployAllModules());
    }

}