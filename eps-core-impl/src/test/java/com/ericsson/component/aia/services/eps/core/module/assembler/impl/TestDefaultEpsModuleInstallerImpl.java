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
package com.ericsson.component.aia.services.eps.core.module.assembler.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleComponentInstaller;
import com.ericsson.component.aia.services.eps.core.component.config.EpsEventHandlerContext;
import com.ericsson.component.aia.services.eps.core.execution.EventFlow;
import com.ericsson.component.aia.services.eps.core.module.assembler.impl.ComponentContextHandler;
import com.ericsson.component.aia.services.eps.core.module.assembler.impl.DefaultEpsModuleInstallerImpl;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

public class TestDefaultEpsModuleInstallerImpl {
    private DefaultEpsModuleInstallerImpl defaultEpsModuleInstallerImpl;

    private static final String MODULE_NAME = "test-module";
    private static final String COMPONENT_NAME = "test-component";
    private static final String INPUT_ADATER_URI = "in:";
    private static final String INPUT_ADAPTER_NAME = "test-input";
    private static final String OUTPUT_ADAPTER_URI = "out:";
    private static final String OUTPUT_ADAPTER_NAME = "test-output";
    private static final String UNIQUE_MODULE_IDENTIFIER = "com.ericsson.test_EpsReleaseCorrelationTest_1.0.0";

    private EpsProvider mockedProvider;
    private EpsModuleComponentInstaller mockedJavaComponentInstaller;
    private EpsModuleComponentInstaller mockedIOAdapterComponentInstaller;

    private EpsModule mockedModule;
    private EpsModuleComponent mockedModuleComponent;
    private EpsModuleComponent mockedModuleInput;
    private EpsModuleComponent mockedModuleOutput;

    private Configuration mockedInputConfiguration;
    private Configuration mockedHandlerConfiguration;
    private Configuration mockedOutputConfiguration;

    private EventInputHandler mockedHandler;
    private InputAdapter mockedInputAdapter;
    private OutputAdapter mockedOutputAdapter;

    private void mockProviderMethods() {
        when(mockedProvider.loadInputAdapter(INPUT_ADATER_URI, INPUT_ADAPTER_NAME)).thenReturn(mockedInputAdapter);
        when(mockedProvider.loadOutputAdapter(OUTPUT_ADAPTER_URI, OUTPUT_ADAPTER_NAME)).thenReturn(mockedOutputAdapter);
        when(mockedProvider.loadEpsModuleComponentInstaller(EpsModuleComponentType.JAVA_COMPONENT)).thenReturn(mockedJavaComponentInstaller);

        when(mockedJavaComponentInstaller.getOrInstallComponent(mockedModuleComponent)).thenReturn(mockedHandler);
        when(mockedIOAdapterComponentInstaller.getOrInstallComponent(mockedModuleInput)).thenReturn(mockedInputAdapter);
        when(mockedIOAdapterComponentInstaller.getOrInstallComponent(mockedModuleOutput)).thenReturn(mockedOutputAdapter);

        when(mockedProvider.loadEpsModuleComponentInstaller(EpsModuleComponentType.INPUT_ADAPTER)).thenReturn(mockedIOAdapterComponentInstaller);
        when(mockedProvider.loadEpsModuleComponentInstaller(EpsModuleComponentType.OUTPUT_ADAPTER)).thenReturn(mockedIOAdapterComponentInstaller);
    }

    private void mockModuleMethods() {
        when(mockedModule.getEventFlows()).thenReturn(getMockedEventFlows());

        when(mockedModule.getComponent(COMPONENT_NAME)).thenReturn(mockedModuleComponent);
        when(mockedModule.getComponent(INPUT_ADAPTER_NAME)).thenReturn(mockedModuleInput);
        when(mockedModule.getComponent(OUTPUT_ADAPTER_NAME)).thenReturn(mockedModuleOutput);
        when(mockedModule.findComponentOrParentById(COMPONENT_NAME)).thenReturn(mockedModuleComponent);
        when(mockedModule.findComponentOrParentById(INPUT_ADAPTER_NAME)).thenReturn(mockedModuleInput);
        when(mockedModule.findComponentOrParentById(OUTPUT_ADAPTER_NAME)).thenReturn(mockedModuleOutput);

        when(mockedModule.getName()).thenReturn(MODULE_NAME);
        when(mockedModule.getUniqueModuleIdentifier()).thenReturn(UNIQUE_MODULE_IDENTIFIER);
    }

    private List<EventFlow> getMockedEventFlows() {
        final List<EventFlow> eventFlows = new ArrayList<>();
        EventFlow eventFlow = null;

        eventFlow = new EventFlow();
        eventFlow.setInputComponentId(INPUT_ADAPTER_NAME);
        eventFlow.addOutputComponentIdentifier(COMPONENT_NAME);
        eventFlows.add(eventFlow);

        eventFlow = new EventFlow();
        eventFlow.setInputComponentId(COMPONENT_NAME);
        eventFlow.addOutputComponentIdentifier(OUTPUT_ADAPTER_NAME);
        eventFlows.add(eventFlow);

        return eventFlows;
    }

    private void mockModuleComponentMethods() {
        when(mockedModuleComponent.getConfiguration()).thenReturn(mockedHandlerConfiguration);
        when(mockedModuleComponent.getComponentType()).thenReturn(EpsModuleComponentType.JAVA_COMPONENT);
        when(mockedModuleComponent.getModule()).thenReturn(mockedModule);
        when(mockedModuleComponent.getInstanceId()).thenReturn(COMPONENT_NAME);
        when(mockedModuleComponent.getModule().getUniqueModuleIdentifier()).thenReturn(UNIQUE_MODULE_IDENTIFIER);

        when(mockedModuleInput.getConfiguration()).thenReturn(mockedInputConfiguration);
        when(mockedModuleInput.getComponentType()).thenReturn(EpsModuleComponentType.INPUT_ADAPTER);
        when(mockedModuleInput.getModule()).thenReturn(mockedModule);
        when(mockedModuleInput.getInstanceId()).thenReturn(INPUT_ADAPTER_NAME);
        when(mockedModuleInput.getModule().getUniqueModuleIdentifier()).thenReturn(UNIQUE_MODULE_IDENTIFIER);

        when(mockedModuleOutput.getConfiguration()).thenReturn(mockedOutputConfiguration);
        when(mockedModuleOutput.getComponentType()).thenReturn(EpsModuleComponentType.OUTPUT_ADAPTER);
        when(mockedModuleOutput.getModule()).thenReturn(mockedModule);
        when(mockedModuleOutput.getInstanceId()).thenReturn(OUTPUT_ADAPTER_NAME);
        when(mockedModuleOutput.getModule().getUniqueModuleIdentifier()).thenReturn(UNIQUE_MODULE_IDENTIFIER);
    }

    private void mockConfigurations() {
        when(mockedInputConfiguration.getStringProperty("uri")).thenReturn(INPUT_ADAPTER_NAME);
        when(mockedHandlerConfiguration.getStringProperty("uri")).thenReturn(COMPONENT_NAME);
        when(mockedOutputConfiguration.getStringProperty("uri")).thenReturn(OUTPUT_ADAPTER_NAME);
    }

    @Before
    public void setup() {
        mockedProvider = mock(EpsProvider.class);
        mockedJavaComponentInstaller = mock(EpsModuleComponentInstaller.class);
        mockedIOAdapterComponentInstaller = mock(EpsModuleComponentInstaller.class);

        mockedModule = mock(EpsModule.class);
        mockedModuleComponent = mock(EpsModuleComponent.class);
        mockedModuleInput = mock(EpsModuleComponent.class);
        mockedModuleOutput = mock(EpsModuleComponent.class);

        mockedHandler = mock(EventInputHandler.class);
        mockedInputAdapter = mock(InputAdapter.class);
        mockedOutputAdapter = mock(OutputAdapter.class);

        mockedInputConfiguration = mock(Configuration.class);
        mockedHandlerConfiguration = mock(Configuration.class);
        mockedOutputConfiguration = mock(Configuration.class);

        mockProviderMethods();
        mockModuleMethods();
        mockModuleComponentMethods();
        mockConfigurations();

        defaultEpsModuleInstallerImpl = new DefaultEpsModuleInstallerImpl(mockedProvider, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_installEpsModuleComponents() {
        defaultEpsModuleInstallerImpl.installEpsModuleComponents(null, null);
    }

    @Test
    public void test_notnull_installEpsModuleComponents() {
        defaultEpsModuleInstallerImpl.installEpsModuleComponents(mockedModule, mock(ModuleManager.class));

        final ComponentContextHandler componentCtxHandler = Whitebox.getInternalState(defaultEpsModuleInstallerImpl, "componentCtxHandler");
        assertNotNull(componentCtxHandler);

        assertNotNull(componentCtxHandler.getComponentContextForComponentById(mockedModuleComponent));
        assertTrue(componentCtxHandler.getComponentContextForComponentById(mockedModuleComponent) instanceof EpsEventHandlerContext);

        assertNotNull(componentCtxHandler.getComponentContextForComponentById(mockedModuleInput));
        assertTrue(componentCtxHandler.getComponentContextForComponentById(mockedModuleInput) instanceof EpsEventHandlerContext);

        assertNotNull(componentCtxHandler.getComponentContextForComponentById(mockedModuleOutput));
        assertTrue(componentCtxHandler.getComponentContextForComponentById(mockedModuleOutput) instanceof EpsEventHandlerContext);
    }
}
