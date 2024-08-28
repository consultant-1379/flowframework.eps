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
package com.ericsson.component.aia.services.eps.core.integration.jee;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.integration.jee.flow.components.JeeTestPassThroughEventHandler;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.Artifact;
import com.ericsson.component.aia.services.eps.core.integration.jee.util.FileUtil;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;

@RunWith(Arquillian.class)
public class EpsConfigPropertiesTest {

    private static final Logger LOG = LoggerFactory.getLogger(EpsConfigPropertiesTest.class);
    private static final String MODULE_TO_TEST_PATHS_IN_SEQUENCE = "flows/dummy_module_two.xml";
    private static String overwrittenPropertyName = "com.ericsson.component.aia.services.eps.test.overwritten.property";
    private static String testPropertyName = "com.ericsson.component.aia.services.eps.test.property";

    @BeforeClass
    public static void _setup() {
        FileUtil.deleteCsvFiles();
    }

    @Deployment(name = "EpsConfigPropertiesTest")
    public static WebArchive createDeployment() {
        final WebArchive archive = Artifact.getEpsArchive();
        archive.addClass(EpsConfigPropertiesTest.class);
        archive.addClass(JeeTestPassThroughEventHandler.class);
        archive.addClass(FileUtil.class);
        archive.addAsResource(MODULE_TO_TEST_PATHS_IN_SEQUENCE);
        archive.addAsResource("EpsConfiguration_testConfig.properties", "EpsConfiguration.properties");
        return archive;
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("EpsConfigPropertiesTest")
    public void csv_output_statistics_test() throws Exception {

        LOG.info("\n*\n*\n* EpsConfigPropertiesTest::test_statistics_csv_output\n*\n*");

        // given
        Artifact.cleanupConfiguredXMLFolder();
        FileUtil.deleteCsvFiles();

        Assert.assertEquals(0, FileUtil.getCsvFiles().size());

        // when
        final boolean moduleCopied = Artifact.copyXmlContentToConfiguredFolder("/" + MODULE_TO_TEST_PATHS_IN_SEQUENCE);
        Assert.assertTrue(moduleCopied);
        final boolean waited = Artifact.wait(10 * 1000);
        Assert.assertTrue(waited);

        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        Assert.assertEquals(1, modulesManager.getDeployedModulesCount());

        // then
        Assert.assertTrue("csv files should exist", FileUtil.getCsvFiles().size() > 1);

    }

    @Test
    @InSequence(4)
    @OperateOnDeployment("EpsConfigPropertiesTest")
    public void forPropertyInConfigFileAndJvm_jvmValueIsReturned() {

        final String overwrittenPropertyValue = EPSConfigurationLoader.getConfigurationProperty(overwrittenPropertyName);
        Assert.assertTrue("jbossValue".equalsIgnoreCase(overwrittenPropertyValue));
    }

    @Test
    @InSequence(3)
    @OperateOnDeployment("EpsConfigPropertiesTest")
    public void forPropertyInConfigFileAndSystem_systemValueIsReturned() {

        System.setProperty(testPropertyName, "systemPropertyValue");
        final String systemPropertyValue = EPSConfigurationLoader.getConfigurationProperty(testPropertyName);
        Assert.assertTrue("systemPropertyValue".equalsIgnoreCase(systemPropertyValue));

    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("EpsConfigPropertiesTest")
    public void forPropertyInConfigFileOnly_configFileValueIsReturned() {

        final String configuredPropertyValue = EPSConfigurationLoader.getConfigurationProperty(testPropertyName);
        Assert.assertTrue("configuredPropertyValue".equalsIgnoreCase(configuredPropertyValue));

    }

    @After
    public void resetFiles() {
        Artifact.cleanupConfiguredXMLFolder();
    }
}
