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
package com.ericsson.component.aia.services.eps.pe.java;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.modules.*;
import com.ericsson.component.aia.services.eps.pe.java.JavaComponentsInstaller;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class JavaComponentsInstallerTest {

    private final static String EPS_MODULE_ID = "module_id";
    private final static String EPS_MODULE_COMPONENT_ID = "component_id";
    private final static String SECOND_EPS_MODULE_COMPONENT_ID = "second_component_id";

    private EpsModule mockedEpsModule;
    private EpsModuleComponent mockedNonEpsModuleHandlerComponent;
    private EpsModuleStepComponent mockedEpsModuleStepComponent;
    private EpsModuleStepComponent mockedSecondEpsModuleStepComponent;
    private EpsModuleStepComponent mockedEmptyEpsModuleStepComponent;
    private JavaComponentsInstaller javaComponentsInstaller;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockedEpsModule = mock(EpsModule.class);
        mockedNonEpsModuleHandlerComponent = mock(EpsModuleComponent.class);
        mockedEpsModuleStepComponent = mock(EpsModuleStepComponent.class);
        mockedSecondEpsModuleStepComponent = mock(EpsModuleStepComponent.class);
        mockedEmptyEpsModuleStepComponent = mock(EpsModuleStepComponent.class);
        javaComponentsInstaller = new JavaComponentsInstaller();

        when(mockedEpsModule.getUniqueModuleIdentifier()).thenReturn(EPS_MODULE_ID);

        when(mockedNonEpsModuleHandlerComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedNonEpsModuleHandlerComponent.getInstanceId()).thenReturn(EPS_MODULE_COMPONENT_ID);

        when(mockedEpsModuleStepComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedEpsModuleStepComponent.getInstanceId()).thenReturn(EPS_MODULE_COMPONENT_ID);

        when(mockedSecondEpsModuleStepComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedSecondEpsModuleStepComponent.getInstanceId()).thenReturn(SECOND_EPS_MODULE_COMPONENT_ID);

        when(mockedEmptyEpsModuleStepComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedEmptyEpsModuleStepComponent.getInstanceId()).thenReturn(SECOND_EPS_MODULE_COMPONENT_ID);
    }

    @After
    public void tearDown() throws Exception {
        javaComponentsInstaller = null;
        mockedEpsModuleStepComponent = null;
        mockedNonEpsModuleHandlerComponent = null;
        mockedEpsModule = null;
    }

    @Test
    public void getSupportedTypes_CreatedObject_ReturnJavaComponent() throws Exception {
        final EpsModuleComponentType[] supportedTypes = javaComponentsInstaller.getSupportedTypes();
        assertArrayEquals(new EpsModuleComponentType[] { EpsModuleComponentType.JAVA_COMPONENT }, supportedTypes);
    }

    @Test
    public void getOrInstallComponent_NullComponent_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        javaComponentsInstaller.getOrInstallComponent(null);
    }

    @Test
    public void getOrInstallComponent_UnsupportedComponent_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unsupported component " + mockedNonEpsModuleHandlerComponent + " for this installer");
        javaComponentsInstaller.getOrInstallComponent(mockedNonEpsModuleHandlerComponent);
    }

    @Test
    public void getOrInstallComponent_ComponentPreviouslyLoaded_ReturnPreviouslyLoadedComponent() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(ForTestEventInputHandlerImpl.class.getName());
        final EventInputHandler previouslyLoadedComponent = javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
        final EventInputHandler currentComponent = javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
        assertSame(previouslyLoadedComponent, currentComponent);
    }

    @Test
    public void getOrInstallComponent_ModulePreviouslyLoadedButNewComponent_InstalledSuccessfully() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(ForTestEventInputHandlerImpl.class.getName());
        when(mockedSecondEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(ForTestEventInputHandlerImpl.class.getName());
        final EventInputHandler previouslyLoadedComponent = javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
        final EventInputHandler currentComponent = javaComponentsInstaller.getOrInstallComponent(mockedSecondEpsModuleStepComponent);
        assertNotNull(currentComponent);
        assertThat(currentComponent, instanceOf(EventInputHandler.class));
        assertNotSame(previouslyLoadedComponent, currentComponent);
    }

    @Test
    public void getOrInstallComponent_NewEventInputHandlerComponent_InstalledSuccessfully() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(ForTestEventInputHandlerImpl.class.getName());
        final Object newlyInstalledComponent = javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
        assertNotNull(newlyInstalledComponent);
        assertThat(newlyInstalledComponent, instanceOf(EventInputHandler.class));
    }

    @Test
    public void getOrInstallComponent_NewNonEventInputHandlerComponent_ThrowIllegalArgumentException() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(Object.class.getName());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(both(containsString("Instance")).and(containsString("is not of required type")));
        javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
    }

    @Test
    public void getOrInstallComponent_ComponentClassNotFound_ThrowIllegalArgumentException() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn("com.not.exist");
        thrown.expect(IllegalArgumentException.class);
        javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
    }

    @Test
    public void getOrInstallComponent_ComponentCantBeInstantiated_ThrowIllegalArgumentException() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(ForTestInterfaceComponent.class.getName());
        thrown.expect(IllegalArgumentException.class);
        javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
    }

    @Test
    public void getOrInstallComponent_ComponentCantBeAccessed_ThrowIllegalArgumentException() throws Exception {
        when(mockedEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(ForTestUnaccessibleComponent.class.getName());
        thrown.expect(IllegalArgumentException.class);
        javaComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
    }

    @Test
    public void getOrInstallComponent_ComponentWithEmptyClassName_ThrowIllegalArgumentException() throws Exception {
        when(mockedEmptyEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn("");
        when(mockedEmptyEpsModuleStepComponent.getJavaHandlerNamed()).thenReturn(null);
        thrown.expect(IllegalArgumentException.class);
        assertNull(javaComponentsInstaller.getOrInstallComponent(mockedEmptyEpsModuleStepComponent));
    }

    @Test
    public void getOrInstallComponent_ComponentWithNoClassNameAndNamed_ThrowIllegalArgumentException() throws Exception {
        when(mockedEmptyEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn(null);
        when(mockedEmptyEpsModuleStepComponent.getJavaHandlerNamed()).thenReturn(null);
        thrown.expect(IllegalArgumentException.class);
        assertNull(javaComponentsInstaller.getOrInstallComponent(mockedEmptyEpsModuleStepComponent));
    }

    @Test
    public void getOrInstallComponent_ComponentWithBothClassNameAndNamed_ThrowIllegalArgumentException() throws Exception {
        when(mockedEmptyEpsModuleStepComponent.getJavaHandlerClassName()).thenReturn("com.ericsson.anything");
        when(mockedEmptyEpsModuleStepComponent.getJavaHandlerNamed()).thenReturn("anything");
        thrown.expect(IllegalArgumentException.class);
        assertNull(javaComponentsInstaller.getOrInstallComponent(mockedEmptyEpsModuleStepComponent));
    }
}