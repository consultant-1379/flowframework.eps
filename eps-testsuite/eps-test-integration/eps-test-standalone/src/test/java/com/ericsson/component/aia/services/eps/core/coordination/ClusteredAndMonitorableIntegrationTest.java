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
package com.ericsson.component.aia.services.eps.core.coordination;

import static com.ericsson.component.aia.services.eps.core.coordination.CoordinationUtil.*;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.eps.core.coordination.utils.AbstractZookeeperTest;
import com.ericsson.component.aia.services.eps.core.coordination.utils.TestingSingleton;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsTestUtil;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.oss.itpf.sdk.cluster.coordination.Layer;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.*;

public class ClusteredAndMonitorableIntegrationTest extends AbstractZookeeperTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredAndMonitorableIntegrationTest.class);
    TestingSingleton testingSingleton = TestingSingleton.getInstance();
    private static final String INPUT_IP = "inputIP";
    private static final String INPUT_PORT = "inputPort";
    private static final String USER_ID = "userId";
    private static final String FILTER_ID = "FilterId";
    private static final String GROUP_ID = "GroupId";
    private static final String STREAM_LOAD_MONITOR = "StreamLoadMonitor";
    private static final String MONITOR_PERIOD = "MonitorPeriod";
    private static final String MONITORING_INTERVAL = "monitoringInterval";
    private static final String SRC_TEST_RESOURCES_FLOWS = "src/test/resources/flows/flow_through_zookeeper_with_config_change.xml";
    private final EpsTestUtil epsTestUtil = new EpsTestUtil();
    private static final long expectedNumberOfCallsToGetStatus = 2;
    private static final int getStatustimeout = 5;

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        System.setProperty("com.ericsson.component.aia.services.eps.module.deployment.folder.path", "src/test/resources");
        System.setProperty("com.ericsson.component.aia.services.eps.deploy.already.existing.modules.on.startup", "false");
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDown() {
        epsTestUtil.shutdownEpsInstance();
    }

    private Map<String, Object> getTestData(final int port) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(INPUT_IP, "localhost");
        data.put(INPUT_PORT, Integer.toString(port));
        data.put(GROUP_ID, "123");
        data.put(FILTER_ID, "1");
        data.put(USER_ID, "1");
        data.put(STREAM_LOAD_MONITOR, "false");
        data.put(MONITOR_PERIOD, "5000");
        data.put(MONITORING_INTERVAL, "20000");
        return data;
    }

    @Test
    public void testConfigurationSetAndUpdate() throws Exception {
        final InputStream moduleInputStream = new FileInputStream(SRC_TEST_RESOURCES_FLOWS);
        Assert.assertNotNull(moduleInputStream);
        LOGGER.debug("Before module deployment");
        final String moduleId = epsTestUtil.deployModuleFromFile(moduleInputStream);
        final ModuleManager moduleManager = epsTestUtil.getEpsInstance().getModuleManager();
        assertEquals(1, moduleManager.getDeployedModulesCount());
        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", moduleId,
                getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()));
        final Node node = application.configure("streamInput");
        testingSingleton.setData(null);
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(getTestData(123));
        node.createOrUpdate(epsAdaptiveConfiguration);
        assertTrue("Control event data was not as expected after timeout", testingSingleton.verifyDataWithTimeOut(getTestData(123)));
        epsAdaptiveConfiguration.setConfiguration(getTestData(124));
        node.createOrUpdate(epsAdaptiveConfiguration);
        assertTrue("Control event data was not as expected after timeout", testingSingleton.verifyDataWithTimeOut(getTestData(124)));
    }

    @Test
    public void testBlankconfigurationAndSubsequentBothReceived() throws Exception {
        final InputStream moduleInputStream = new FileInputStream(SRC_TEST_RESOURCES_FLOWS);
        final String moduleId = epsTestUtil.deployModuleFromFile(moduleInputStream);
        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", moduleId,
                getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()));
        final Node node = application.configure("streamInput");
        testingSingleton.setData(null);
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        node.createOrUpdate(epsAdaptiveConfiguration);
        assertTrue("Control event data was not as expected after timeout", testingSingleton.verifyDataWithTimeOut(new HashMap<String, Object>()));
        epsAdaptiveConfiguration.setConfiguration(getTestData(124));
        node.createOrUpdate(epsAdaptiveConfiguration);
        assertTrue("Control event data was not as expected after timeout", testingSingleton.verifyDataWithTimeOut(getTestData(124)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConfigurationThrowsIllegalArgumentException() throws Exception {
        final InputStream moduleInputStream = new FileInputStream(SRC_TEST_RESOURCES_FLOWS);
        final String moduleId = epsTestUtil.deployModuleFromFile(moduleInputStream);
        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", moduleId,
                getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()));
        final Node node = application.configure("streamInput");
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(null);
        node.createOrUpdate(epsAdaptiveConfiguration);
    }

    @Test
    public void testConfigNotReceivedWhenDifferentApplicationIsConfigured() throws Exception {
        final InputStream moduleInputStream = new FileInputStream(SRC_TEST_RESOURCES_FLOWS);
        final String moduleId = epsTestUtil.deployModuleFromFile(moduleInputStream);
        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", moduleId,
                getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()));
        final Node node = application.configure("notStreamInput");
        testingSingleton.setData(null);
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        node.createOrUpdate(epsAdaptiveConfiguration);
        assertFalse("react should not have been called because a different application was configured",
                testingSingleton.verifyDataWithTimeOut(new HashMap<String, Object>()));
    }

    @Test
    public void testMonitorableGetStatusIsCalled() throws Exception {
        final InputStream moduleInputStream = new FileInputStream(SRC_TEST_RESOURCES_FLOWS);
        epsTestUtil.deployModuleFromFile(moduleInputStream);
        testingSingleton.setNumberOfCallsToGetStatus(0);
        for (int i = 0; i < getStatustimeout && testingSingleton.getNumberOfCallsToGetStatus() == 0; i++) {
            Thread.sleep(1000);
        }
        Thread.sleep(3000);
        final long numberOfCallsToGetStatus = testingSingleton.getNumberOfCallsToGetStatus();
        assertTrue("Number of calls to getStatus should have been >=" + expectedNumberOfCallsToGetStatus + " but was:" + numberOfCallsToGetStatus,
                numberOfCallsToGetStatus >= expectedNumberOfCallsToGetStatus);
    }
}
