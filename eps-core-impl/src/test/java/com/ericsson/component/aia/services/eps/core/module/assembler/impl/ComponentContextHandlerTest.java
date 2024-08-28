package com.ericsson.component.aia.services.eps.core.module.assembler.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.core.EpsComponentConstants;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventHandlerContext;
import com.ericsson.component.aia.services.eps.core.module.assembler.impl.ComponentContextHandler;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class ComponentContextHandlerTest {

    private final ComponentContextHandler handler = ComponentContextHandler.getInstance();
    private EpsModuleComponent mockModuleComponent;
    private Configuration mockMonfiguration;
    private EpsModule mockModule;
    private EventInputHandler mockComponentHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        mockMonfiguration = mock(Configuration.class);
        mockModuleComponent = mock(EpsModuleComponent.class);
        mockModule = mock(EpsModule.class);
        mockComponentHandler = mock(EventInputHandler.class);

        when(mockMonfiguration.getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME)).thenReturn("test-input");
        when(mockMonfiguration.getStringProperty(EpsComponentConstants.LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME)).thenReturn("test-channel");

        when(mockModuleComponent.getInstanceId()).thenReturn("releaseFilter");
        when(mockModuleComponent.getComponentType()).thenReturn(EpsModuleComponentType.ESPER_COMPONENT);
        when(mockModuleComponent.getConfiguration()).thenReturn(mockMonfiguration);
        when(mockModuleComponent.getModule()).thenReturn(mockModule);

        when(mockModule.getUniqueModuleIdentifier()).thenReturn("com.ericsson.test_EpsReleaseCorrelationTest_1.0.0");
        when(mockModule.getComponent("releaseFilter")).thenReturn(mockModuleComponent);
        when(mockModule.findComponentOrParentById("releaseFilter")).thenReturn(mockModuleComponent);
    }

    @Test
    public void test_null_getOrCreateComponentContextForComponent_1() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("module component must not be null");

        handler.getOrCreateComponentContextForComponent(null, null, null);
    }

    @Test
    public void test_null_getOrCreateComponentContextForComponent_2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("module must not be null");

        handler.getOrCreateComponentContextForComponent(mockModuleComponent, null, null);
    }

    @Test
    public void test_null_getOrCreateComponentContextForComponent_3() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("component handler must not be null");

        handler.getOrCreateComponentContextForComponent(mockModuleComponent, mockModule, null);
    }

    @Test
    public void test_valid_getOrCreateComponentContextForComponent() {
        assertTrue(handler.getOrCreateComponentContextForComponent(mockModuleComponent, mockModule, mockComponentHandler) instanceof EpsEventHandlerContext);
    }

    @Test
    public void test_localAdapter_getOrCreateComponentContextForComponent_1() {
        when(mockModuleComponent.getComponentType()).thenReturn(EpsModuleComponentType.INPUT_ADAPTER);
        when(mockMonfiguration.getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME)).thenReturn("local:/test");

        assertTrue(handler.getOrCreateComponentContextForComponent(mockModuleComponent, mockModule, mockComponentHandler) instanceof EpsEventHandlerContext);
    }

    @Test
    public void test_localAdapter_getOrCreateComponentContextForComponent_2() {
        when(mockModuleComponent.getComponentType()).thenReturn(EpsModuleComponentType.OUTPUT_ADAPTER);
        when(mockMonfiguration.getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME)).thenReturn("local:/test");

        assertTrue(handler.getOrCreateComponentContextForComponent(mockModuleComponent, mockModule, mockComponentHandler) instanceof EpsEventHandlerContext);
    }

    @Test
    public void test_null_getComponentContextForComponentById() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("component must not be null");

        handler.getComponentContextForComponentById(null);
    }

    @Test
    public void test_valid_getComponentContextForComponentById() {
        final EpsEventHandlerContext mockedEpsEventHandlerContext = mock(EpsEventHandlerContext.class);

        final Map<String, EpsEventHandlerContext> moduleContexts = new HashMap<>();
        moduleContexts.put("releaseFilter", mockedEpsEventHandlerContext);

        final Map<String, Map<String, EpsEventHandlerContext>> NON_LOCAL_CTX = Whitebox.getInternalState(ComponentContextHandler.class,
                "NON_LOCAL_CTX");
        NON_LOCAL_CTX.put("com.ericsson.test_EpsReleaseCorrelationTest_1.0.0", moduleContexts);

        assertTrue(handler.getComponentContextForComponentById(mockModuleComponent) instanceof EpsEventHandlerContext);
    }
}
