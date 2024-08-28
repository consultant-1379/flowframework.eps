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
package com.ericsson.component.aia.services.eps.builtin.components;

import static com.ericsson.component.aia.services.eps.builtin.components.ThreadManagementComponent.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.ThreadManagementComponent;
import com.ericsson.component.aia.services.eps.builtin.components.ThreadManagementComponent.LoggedDiscardPolicy;
import com.ericsson.component.aia.services.eps.builtin.components.utils.StubbedConfiguration;
import com.ericsson.component.aia.services.eps.builtin.components.utils.StubbedContext;
import com.ericsson.component.aia.services.eps.builtin.components.utils.StubbedEventSubscriber;

public class ThreadManagementComponentTest {

    @Test
    public void testConfigParamsWithValidValues() {
        final ThreadManagementComponent component = new ThreadManagementComponent();
        final StubbedConfiguration stubbedConfiguration = new StubbedConfiguration();
        final StubbedEventSubscriber stubbedEventSubscriber = new StubbedEventSubscriber(false);
        stubbedConfiguration.addProperty(THREAD_POOL_SIZE_PROPERTY_NAME, "9");
        stubbedConfiguration.addProperty(THREAD_PRIORITY_PROPERTY_NAME, "1");
        stubbedConfiguration.addProperty(QUEUE_SIZE_PROPERTY_NAME, "100");
        stubbedConfiguration.addProperty(REJECTED_EXECUTION_POLICY, "LoggedDiscardPolicy");
        stubbedConfiguration.addProperty(REJECTED_EVENTS_COUNTER_NAME_PREFIX, "XYZ");
        stubbedConfiguration.addProperty(REJECTED_EVENTS_COUNTER_NAME, "ABC");
        final StubbedContext stubbedContext = new StubbedContext(stubbedConfiguration, stubbedEventSubscriber);
        component.init(stubbedContext);

        assertEquals(9, component.getThreadPoolSize());
        assertEquals(1, component.getThreadPriority());
        assertEquals(100, component.getRunnableQueueSize().intValue());
        assertEquals(100, component.getForwarderThreadPool().getQueue().remainingCapacity());
        assertEquals("com.ericsson.component.aia.services.eps.builtin.components.ThreadManagementComponent.LoggedDiscardPolicy", component
                .getForwarderThreadPool().getRejectedExecutionHandler().getClass().getCanonicalName());

        final LoggedDiscardPolicy rejectedExecutionHandler = (LoggedDiscardPolicy) component.getRejectedExecutionHandler();
        assertEquals("XYZ", rejectedExecutionHandler.getRejectedEventsCounterNamePrefixConfigParam());
        assertEquals("ABC", rejectedExecutionHandler.getRejectedEventsCounterNameConfigParam());
        component.destroy();
    }

    @Test
    public void testConfigParamsWithInValidValuesShouldSetToDefault() {
        final ThreadManagementComponent component = new ThreadManagementComponent();
        final StubbedConfiguration stubbedConfiguration = new StubbedConfiguration();
        final StubbedEventSubscriber stubbedEventSubscriber = new StubbedEventSubscriber(false);
        stubbedConfiguration.addProperty(THREAD_POOL_SIZE_PROPERTY_NAME, "-1");
        stubbedConfiguration.addProperty(THREAD_PRIORITY_PROPERTY_NAME, "XYZ");
        stubbedConfiguration.addProperty(QUEUE_SIZE_PROPERTY_NAME, "-1");
        final StubbedContext stubbedContext = new StubbedContext(stubbedConfiguration, stubbedEventSubscriber);
        component.init(stubbedContext);

        assertEquals(DEFAULT_THREAD_POOL_SIZE, component.getThreadPoolSize());
        assertEquals(DEFAULT_THREAD_PRIORITY, component.getThreadPriority());
        assertEquals(null, component.getRunnableQueueSize());
        assertEquals(Integer.MAX_VALUE, component.getForwarderThreadPool().getQueue().remainingCapacity());
        assertEquals("java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy", component.getForwarderThreadPool().getRejectedExecutionHandler()
                .getClass().getCanonicalName());

        component.destroy();
    }

    @Test
    public void testConfigParamsWithNoValuesDefinedShouldSetToDefault() {
        final ThreadManagementComponent component = new ThreadManagementComponent();
        final StubbedConfiguration stubbedConfiguration = new StubbedConfiguration();
        final StubbedEventSubscriber stubbedEventSubscriber = new StubbedEventSubscriber(false);
        final StubbedContext stubbedContext = new StubbedContext(stubbedConfiguration, stubbedEventSubscriber);
        component.init(stubbedContext);

        assertEquals(DEFAULT_THREAD_POOL_SIZE, component.getThreadPoolSize());
        assertEquals(DEFAULT_THREAD_PRIORITY, component.getThreadPriority());
        assertEquals(null, component.getRunnableQueueSize());
        assertEquals(Integer.MAX_VALUE, component.getForwarderThreadPool().getQueue().remainingCapacity());
        assertEquals("java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy", component.getForwarderThreadPool().getRejectedExecutionHandler()
                .getClass().getCanonicalName());
        component.destroy();
    }

    @Test
    public void testRejectedEventsCounterNameConfigParamsWithNoValueDefinedShouldSetToDefault() {
        final ThreadManagementComponent component = new ThreadManagementComponent();
        final StubbedConfiguration stubbedConfiguration = new StubbedConfiguration();
        final StubbedEventSubscriber stubbedEventSubscriber = new StubbedEventSubscriber(false);
        stubbedConfiguration.addProperty(REJECTED_EXECUTION_POLICY, "LoggedDiscardPolicy");
        final StubbedContext stubbedContext = new StubbedContext(stubbedConfiguration, stubbedEventSubscriber);
        component.init(stubbedContext);

        final LoggedDiscardPolicy rejectedExecutionHandler = (LoggedDiscardPolicy) component.getRejectedExecutionHandler();
        assertEquals("com.ericsson.component.aia.services.eps.builtin.components.ThreadManagementComponent$LoggedDiscardPolicy", rejectedExecutionHandler
                .getRejectedEventsCounterNamePrefixConfigParam());
        assertEquals(DEFAULT_REJECTED_EVENTS_COUNTER_NAME, rejectedExecutionHandler.getRejectedEventsCounterNameConfigParam());
        component.destroy();
    }

    @Test
    public void testDroppedRecords() {
        final ThreadManagementComponent component = new ThreadManagementComponent();
        final StubbedConfiguration stubbedConfiguration = new StubbedConfiguration();
        final StubbedEventSubscriber stubbedEventSubscriber = new StubbedEventSubscriber(true);
        stubbedConfiguration.addProperty(THREAD_POOL_SIZE_PROPERTY_NAME, "1");
        stubbedConfiguration.addProperty(THREAD_PRIORITY_PROPERTY_NAME, "1");
        stubbedConfiguration.addProperty(QUEUE_SIZE_PROPERTY_NAME, "1");
        final StubbedContext stubbedContext = new StubbedContext(stubbedConfiguration, stubbedEventSubscriber);
        component.init(stubbedContext);
        for (int i = 0; i < 900; i++) {
            component.onEvent("ABC");
        }
        assertTrue(component.getDropCount() > 0);
        component.destroy();
    }

    @Test
    public void testRejectedRecordsCounter() {
        final ThreadManagementComponent component = new ThreadManagementComponent();
        final StubbedConfiguration stubbedConfiguration = new StubbedConfiguration();
        final StubbedEventSubscriber stubbedEventSubscriber = new StubbedEventSubscriber(true);
        stubbedConfiguration.addProperty(THREAD_POOL_SIZE_PROPERTY_NAME, "1");
        stubbedConfiguration.addProperty(THREAD_PRIORITY_PROPERTY_NAME, "1");
        stubbedConfiguration.addProperty(QUEUE_SIZE_PROPERTY_NAME, "1");
        stubbedConfiguration.addProperty(REJECTED_EXECUTION_POLICY, "LoggedDiscardPolicy");
        final StubbedContext stubbedContext = new StubbedContext(stubbedConfiguration, stubbedEventSubscriber);
        component.init(stubbedContext);
        for (int i = 0; i < 900; i++) {
            component.onEvent("ABC");
        }
        final LoggedDiscardPolicy rejectedDiscardPolicy = (LoggedDiscardPolicy) component.getRejectedExecutionHandler();
        assertTrue(rejectedDiscardPolicy.getRejectedEventsCounter().getCount() > 0);
        component.destroy();
    }

}
