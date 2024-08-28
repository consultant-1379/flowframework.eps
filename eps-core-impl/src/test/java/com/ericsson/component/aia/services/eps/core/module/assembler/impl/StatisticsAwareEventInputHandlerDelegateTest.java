package com.ericsson.component.aia.services.eps.core.module.assembler.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.module.assembler.impl.StatisticsAwareEventInputHandlerDelegate;
import com.ericsson.component.aia.services.eps.core.modules.ModuleListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoadingUtil.class })
public class StatisticsAwareEventInputHandlerDelegateTest {

    private StatisticsAwareEventInputHandlerDelegate handlerDelegate;
    private EventInputHandler mockInputHandler;
    private EpsModule mockEpsModule;
    private ModuleListener mockModuleListener;
    private ModuleManager mockModuleManager;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        mockInputHandler = mock(EventInputHandler.class);

        mockEpsModule = mock(EpsModule.class);
        Mockito.when(mockEpsModule.getUniqueModuleIdentifier()).thenReturn("test_module_id");

        mockEpsInstanceManager();
    }

    /**
     * need to mock calls to LoadingUtil
     */
    private void mockEpsInstanceManager() {
        PowerMockito.mockStatic(LoadingUtil.class);
        Mockito.when(LoadingUtil.loadSingletonInstance(ModuleListener.class)).thenReturn(
                        mockModuleListener);
        Mockito.when(LoadingUtil.loadSingletonInstance(ModuleManager.class)).thenReturn(
                        mockModuleManager);
    }

    @Test
    public void test_null_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("event input handler must not be null");
        handlerDelegate = new StatisticsAwareEventInputHandlerDelegate(null, mockEpsModule, "abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_valid_constructor_instanceId_null() {
        handlerDelegate = new StatisticsAwareEventInputHandlerDelegate(mockInputHandler, mockEpsModule, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_valid_constructor_module_null() {
        handlerDelegate = new StatisticsAwareEventInputHandlerDelegate(mockInputHandler, null, "not_null");
    }

    @Test
    public void test_valid_constructor_instanceId_notNull() {
        handlerDelegate = new StatisticsAwareEventInputHandlerDelegate(mockInputHandler, mockEpsModule, "test_Id");
        final Meter meter = Whitebox.getInternalState(handlerDelegate, "eventMeter");
        assertNotNull(meter);
    }
}
