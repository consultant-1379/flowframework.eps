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
package com.ericsson.component.aia.services.eps.core.statistics;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.*;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.*;

public class EpsStatisticsRegisterImplTest {

    private final EpsStatisticsRegisterImpl epsStatisticsRegisterUnderTest = new EpsStatisticsRegisterImpl();
    private final TestHandler handler = new TestHandler();
    private final Configuration mockConfiguration = mock(Configuration.class);
    private final Gauge mockGauge = mock(Gauge.class);
    private final Counter mockCounter = mock(Counter.class);
    private final EventHandlerContext mockContext = mock(EventHandlerContext.class);

    private String getRandomId(final String prefix) {
        final Random random = new Random();
        return prefix + random.nextInt(1000) + "_" + System.currentTimeMillis();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        System.setProperty(EpsConfigurationConstants.STATISTICS_OFF_SYS_PARAM_NAME, "false");

  //      final String flowId = getRandomId("flow_");

        when(mockContext.getContextualData(EpsEngineConstants.FLOW_UNIQUE_IDENTIFIER_CONTEXTUAL_DATA_NAME))
                .thenReturn(getRandomId("flow_"));
//                .thenReturn(flowId);
        when(mockContext.getContextualData(EpsEngineConstants.COMPONENT_IDENTIFIER_CONTEXTUAL_DATA_NAME))
                .thenReturn(getRandomId("component_"));
        when(mockContext.getEventHandlerConfiguration()).thenReturn(mockConfiguration);

        handler.init(mockContext);

    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#isStatisticsOn()}.
     */
    @Test
    public void isStatisticsOn_whenEnabled_returnsTrue() {
        assertTrue(epsStatisticsRegisterUnderTest.isStatisticsOn());
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#isStatisticsOn()}
     * .
     */
    @Test
    public void isStatisticsOn_whenDisabled_returnsFalse() {

        // given
        System.setProperty(EpsConfigurationConstants.STATISTICS_OFF_SYS_PARAM_NAME, "true");
        final EpsStatisticsRegisterImpl epsStatisticsRegister = new EpsStatisticsRegisterImpl();

        // then
        assertFalse(epsStatisticsRegister.isStatisticsOn());
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#createMeter(java.lang.String)}
     * .
     */
    @Test
    public void createMeter_whenNameValid_returnsMeter() {
        // when
        final Meter createdMeter = epsStatisticsRegisterUnderTest.createMeter("meterName1");

        // then
        assertNotNull(createdMeter);
    }

    /**
     * Test method for {@link
     * com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#
     * createMeter(java.lang.String, ,
     * com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)} .
     */
    @Test
    public void createMeter_whenNameAndHandlerValid_returnsMeter() {
        // when
        final Meter createdMeter = epsStatisticsRegisterUnderTest.createMeter("meterName2",
                handler);

        // then
        assertNotNull(createdMeter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#createMeter(java.lang.String)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void createMeter_whenNameIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.createMeter(null);
    }

    /**
     * Test method for {@link
     * com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#
     * createMeter(java.lang.String, ,
     * com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)} .
     */
    @Test(expected = IllegalArgumentException.class)
    public void createMeter_whenHandlerIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.createMeter("meterName3", null);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#createCounter(java.lang.String)}
     * .
     */
    @Test
    public void createCounter_whenNameValid_returnsCounter() {
        // when
        final Counter createdCounter = epsStatisticsRegisterUnderTest.createCounter("counterName1");

        // then
        assertNotNull(createdCounter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#createCounter(java.lang.String, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test
    public void createCounter_whenNameAndHandlerValid_returnsCounter() {
        // when
        final Counter createdCounter = epsStatisticsRegisterUnderTest.createCounter("counterName2",
                handler);

        // then
        assertNotNull(createdCounter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#createCounter(java.lang.String)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void createCounter_whenNameIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.createCounter(null);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#createCounter(java.lang.String, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void createCounter_whenHandlerIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.createCounter("counterName3", null);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerGuage(java.lang.String, com.codahale.metrics.Gauge)}
     * .
     */
    @Test
    public void registerGuage_whenNameAndGuageValid_returnsCounter() {
        // when
        epsStatisticsRegisterUnderTest.registerGuage("guageName1", mockGauge);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerGuage(java.lang.String, com.codahale.metrics.Gauge, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test
    public void registerGuage_whenNameAndGuageAndHandlerValid_returnsCounter() {
        // when
        epsStatisticsRegisterUnderTest.registerGuage("guageName2", mockGauge, handler);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerGuage(java.lang.String, com.codahale.metrics.Gauge, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerGuage_whenNameIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.registerGuage(null, mockGauge, handler);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerGuage(java.lang.String, com.codahale.metrics.Gauge, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerGuage_whenHandlerIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.registerGuage("guageName3", mockGauge, null);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerGuage(java.lang.String, com.codahale.metrics.Gauge, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerGuage_whenGuageIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.registerGuage("guageName4", null, handler);
    }

    /**
     * Test method for
     *
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerCounter(String, com.codahale.metrics.Counter)}
     * .
     */
    @Test
    public void registerCounter_whenNameAndCounterValid_returnsCounter() {
        // when
        epsStatisticsRegisterUnderTest.registerCounter("customCounterName1", mockCounter);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerCounter(String, com.codahale.metrics.Counter, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test
    public void registerCounter_whenNameAndCounterAndHandlerValid_returnsCounter() {
        // when
        epsStatisticsRegisterUnderTest.registerCounter("customCounterName2", mockCounter, handler);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerCounter(String, com.codahale.metrics.Counter, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerCounter_whenNameIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.registerCounter(null, mockCounter, handler);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerCounter(String, com.codahale.metrics.Counter, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerCounter_whenHandlerIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.registerCounter("customCounterName3", mockCounter, null);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl#registerCounter(String, com.codahale.metrics.Counter, com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler)}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerCounter_whenCounterIsNull_throwsException() {
        epsStatisticsRegisterUnderTest.registerGuage("customCounterName4", null, handler);
    }


    public class TestHandler extends AbstractEventHandler {

        @Override
        protected void doInit() {

        }

    }
}
