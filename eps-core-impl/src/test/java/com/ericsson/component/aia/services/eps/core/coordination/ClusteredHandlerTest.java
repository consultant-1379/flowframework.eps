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

import static com.ericsson.component.aia.services.eps.core.coordination.CoordinationUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.core.EpsConstants;
import com.ericsson.component.aia.services.eps.core.coordination.ClusteredHandler;
import com.ericsson.component.aia.services.eps.core.coordination.observer.CoordinationStateHandler;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.modules.*;
import com.ericsson.component.aia.itpf.common.Clustered;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.oss.itpf.sdk.cluster.coordination.Layer;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApplicationFactory.class, Executors.class })
public class ClusteredHandlerTest {
    private static final String epsId = "eps01";
    private static final String epsNameSpace = "testNamespace";
    private static final String epsName = "testEpsName";
    private static final String epsVersion = "1.0";
    private static final String componentId = "1234";
    private static final Integer monitoringInterval = new Integer(7);
    private boolean monitoringIntervalWasQueried = false;

    private final EpsModule epsModule = new EpsModule();
    private final EpsModuleComponent epsModuleComponent = new EpsModuleComponent(EpsModuleComponentType.INPUT_ADAPTER, componentId, epsModule);

    @Mock
    Clustered mockControllable;

    @Mock
    private Application mockApplication;

    @Mock
    private Node mockNode;

    @Mock
    NodeObserverRegistry mockNodeObserverRegistry;

    private final Configuration config = new Configuration() {

        @Override
        public String getStringProperty(final String paramName) {
            return null;
        }

        @Override
        public Integer getIntProperty(final String paramName) {
            if ("monitoringInterval".equals(paramName)) {
                monitoringIntervalWasQueried = true;
                return monitoringInterval;
            }
            return new Integer(0);

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

    @Before
    public void setup() {
        System.setProperty(EpsConstants.EPS_INSTANCE_ID_PROP_NAME, epsId);
        epsModule.setNamespace(epsNameSpace);
        epsModule.setName(epsName);
        epsModule.setVersion(epsVersion);
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ApplicationFactory.class);
        PowerMockito.mockStatic(CoordinationStateHandler.class);
        PowerMockito.mockStatic(Executors.class);
        Mockito.when(
                ApplicationFactory.get(Layer.SERVICES, "eps", epsNameSpace + "_" + epsName + "_" + epsVersion,
                        getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()))).thenReturn(mockApplication);
        epsModuleComponent.setConfiguration(config);
        doReturn(mockNode).when(mockApplication).configure(anyString());
        doReturn(mockNodeObserverRegistry).when(mockNode).observe();
    }

    @Test
    public void verifyInstantiation() {
        Mockito.when(ApplicationFactory.isAvailableBlocking()).thenReturn(true);

        final ClusteredHandler controllableHandler = new ClusteredHandler(mockControllable, epsModuleComponent);
        controllableHandler.toString(); //This is here to avoid a 'not used' warning

        verify(mockApplication).registered();
        verify(mockApplication).register();
        verify(mockApplication).configure(componentId);
    }

    @Test
    public void verifyNotRegisteredWhenApplicationFactoryIsNotAvailable() {
        Mockito.when(ApplicationFactory.isAvailableBlocking()).thenReturn(false);

        new ClusteredHandler(mockControllable, epsModuleComponent);

        verifyZeroInteractions(mockNodeObserverRegistry);
        assertFalse(monitoringIntervalWasQueried);
    }
}