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
package com.ericsson.component.aia.services.eps.core.coordination.utils;

import java.io.IOException;

import org.apache.curator.test.TestingServer;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;


public abstract class AbstractZookeeperTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZookeeperTest.class);
    @Rule
    public Timeout globalTimeout = new Timeout(3000_000);

    @Rule
    public TestName name = new TestName();

    protected static TestingServer testServer;

    @BeforeClass
    public static void setUpZooKeeper(){
        LOGGER.info(" ***** starting mocked zookeper ***** ");
        synchronized (AbstractZookeeperTest.class) {
            if (testServer == null) {
                try {
                    testServer = new TestingServer(2181);
                } catch (Exception exception) {
                    fail("Failed to set up zookeeper with error:" + exception.getMessage());
                }
            }
        }
    }

    @AfterClass
    public static void tearDownZooKeeper() throws IOException, InterruptedException {
        LOGGER.info(" ***** stopping mocked zookeper ***** ");
        //testServer.stop();
    }

    @Before
    public void before() {
        LOGGER.debug("before test: [{}]", this.name.getMethodName());
    }

    @After
    public void after() {
        LOGGER.debug("after test: [{}]", this.name.getMethodName());
    }
}
