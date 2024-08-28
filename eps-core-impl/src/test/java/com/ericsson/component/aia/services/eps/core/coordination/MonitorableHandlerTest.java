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
package com.ericsson.component.aia.services.eps.core.coordination;

import static com.ericsson.component.aia.services.eps.core.coordination.CoordinationUtil.getQualifiedEpsName;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;

import java.util.Map;
import java.util.concurrent.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.core.EpsConstants;
import com.ericsson.component.aia.services.eps.core.coordination.MonitorableHandler;
import com.ericsson.component.aia.services.eps.core.coordination.observer.CoordinationStateHandler;
import com.ericsson.component.aia.services.eps.core.coordination.reporter.MetricReporter;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.modules.*;
import com.ericsson.component.aia.itpf.common.Monitorable;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.oss.itpf.sdk.cluster.coordination.Layer;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MonitorableHandler.class, ApplicationFactory.class, Executors.class })
public class MonitorableHandlerTest {
    private static final String EPS_ID = "eps01";
    private static final String EPS_NAME_SPACE = "testNamespace";
    private static final String EPS_NAME = "testEpsName";
    private static final String EPS_VERSION = "1.0";
    private static final String COMPONENT_ID = "1234";
    private static final Integer MONITORING_INTERVAL = Integer.valueOf(7);

    private final EpsModule epsModule = new EpsModule();
    private final EpsModuleComponent epsModuleComponent = new EpsModuleComponent(EpsModuleComponentType.INPUT_ADAPTER, COMPONENT_ID, epsModule);

    @Mock
    private static Monitorable mockMonitorable;

    @Mock
    private static Application mockApplication;

    @Mock
    private static Node node;

    @Mock
    private static ScheduledFutureImpl scheduledFutureImpl;

    @Mock
    private static ScheduledExecutorService mockScheduledExecutorService;

    private final Configuration config = new Configuration() {

        @Override
        public String getStringProperty(final String paramName) {
            return null;
        }

        @Override
        public Integer getIntProperty(final String paramName) {
            if ("monitoringInterval".equals(paramName)) {
                return MONITORING_INTERVAL;
            }
            return Integer.valueOf(0);

        }

        @Override
        public Boolean getBooleanProperty(final String paramName) {
            return null;
        }

        @Override
        public Map<String, Object> getAllProperties() {
            return null;
        }
    };

    private final class ScheduledFutureImpl implements ScheduledFuture<Object> {

        @Override
        public long getDelay(final TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(final Delayed delayed) {
            return 0;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

    }

    @Before
    public void setup() {
        System.setProperty(EpsConstants.EPS_INSTANCE_ID_PROP_NAME, EPS_ID);
        epsModule.setNamespace(EPS_NAME_SPACE);
        epsModule.setName(EPS_NAME);
        epsModule.setVersion(EPS_VERSION);
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ApplicationFactory.class);
        PowerMockito.mockStatic(ApplicationFactory.class);
        PowerMockito.mockStatic(Executors.class);
        PowerMockito.mockStatic(CoordinationStateHandler.class);
        Mockito.when(
                ApplicationFactory.get(Layer.SERVICES, "eps", EPS_NAME_SPACE + "_" + EPS_NAME + "_" + EPS_VERSION,
                        getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()))).thenReturn(mockApplication);
        Mockito.when(Executors.newScheduledThreadPool(1)).thenReturn(mockScheduledExecutorService);
        epsModuleComponent.setConfiguration(config);
        doReturn(node).when(mockApplication).report(anyString());
    }

    @Test
    public void verifyScheduleOnStart() {
        Mockito.when(ApplicationFactory.isAvailableBlocking()).thenReturn(true);
        doReturn(scheduledFutureImpl).when(mockScheduledExecutorService).scheduleWithFixedDelay(any(MetricReporter.class), any(Integer.class),
                any(Integer.class), any(TimeUnit.class));
        final MonitorableHandler mHandler = new MonitorableHandler(epsModuleComponent, mockMonitorable);
        mHandler.start(mockApplication);
        verify(mockScheduledExecutorService,times(2)).scheduleWithFixedDelay(any(MetricReporter.class), any(Integer.class), any(Integer.class),
                any(TimeUnit.class));
    }

    @Test
    public void verifyShutdownAndCancelOnStop() {
        Mockito.when(ApplicationFactory.isAvailableBlocking()).thenReturn(true);
        Mockito.when(Executors.newScheduledThreadPool(1)).thenReturn(mockScheduledExecutorService);
        doReturn(scheduledFutureImpl).when(mockScheduledExecutorService).scheduleWithFixedDelay(any(MetricReporter.class), any(Integer.class),
                any(Integer.class), any(TimeUnit.class));
        final MonitorableHandler mHandler = new MonitorableHandler(epsModuleComponent, mockMonitorable);
        mHandler.start(mockApplication);
        mHandler.stop();
        verify(mockScheduledExecutorService,atLeastOnce()).shutdown();
        verify(scheduledFutureImpl,atLeastOnce()).cancel(true);
    }

}
