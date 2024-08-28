package com.ericsson.component.aia.services.eps.pe.esper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.*;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsModuleRule;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.modules.*;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngine;
import com.ericsson.component.aia.services.eps.pe.core.AbstractProcessingEngine;
import com.ericsson.component.aia.services.eps.pe.esper.EsperComponentsInstaller;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

public class EsperComponentsInstallerTest {

    private static final String STEP_INSTANCE_ID = "step_instance_id";

    private EpsModuleComponent mockedEpsModuleComponent;
    private EpsModuleStepComponent mockedEpsModuleStepComponent;
    private EpsModuleHandlerComponent mockedEpsModuleHandlerComponent;
    private EpsRulesModuleComponent mockedRulesModuleComponent;
    private EpsModule mockedEpsModule;
    private List<EpsModuleRule> mockedEpsModuleRules;
    private ProcessingEngine mockedProcessingEngine;
    private EventInputHandler mockedEventInputHandler;

    @Mock
    private EpsProvider mockedEpsProvider;

    @Mock
    private Configuration mockedConfiguration;

    @InjectMocks
    private EsperComponentsInstaller esperComponentsInstaller;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        mockedEpsModuleComponent = mock(EpsModuleComponent.class);
        mockedEpsModuleStepComponent = mock(EpsModuleStepComponent.class);
        mockedEpsModuleHandlerComponent = mock(EpsModuleHandlerComponent.class);
        mockedRulesModuleComponent = mock(EpsRulesModuleComponent.class);
        mockedEpsModule = mock(EpsModule.class);
        final List<EpsModuleRule> rules = new LinkedList<EpsModuleRule>();
        rules.add(mock(EpsModuleRule.class));
        mockedEpsModuleRules = rules;
        mockedProcessingEngine = mock(ProcessingEngine.class);
        mockedEventInputHandler = mock(EventInputHandler.class);
        mockedConfiguration = mock(Configuration.class);
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        mockedEventInputHandler = null;
        mockedProcessingEngine = null;
        mockedRulesModuleComponent = null;
        mockedEpsModule = null;
        mockedEpsModuleRules = null;
        mockedEpsModuleHandlerComponent = null;
        mockedEpsModuleStepComponent = null;
        mockedEpsModuleComponent = null;
    }

    @Test
    public void getSupportedTypes_CreatedObject_ReturnEsperComponent() throws Exception {
        final EpsModuleComponentType[] supportedTypes = esperComponentsInstaller.getSupportedTypes();
        assertArrayEquals(new EpsModuleComponentType[] { EpsModuleComponentType.ESPER_COMPONENT }, supportedTypes);
    }

    @Test
    public void getOrInstallComponent_NullComponent_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("component must not be null and must be of specific type");
        esperComponentsInstaller.getOrInstallComponent(null);
    }

    @Test
    public void getOrInstallComponent_UnsupportedComponent_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("component must not be null and must be of specific type");
        esperComponentsInstaller.getOrInstallComponent(mockedEpsModuleComponent);
    }

    @Test
    public void getOrInstallComponent_NonEsperComponent_ThrowIllegalArgumentException() throws Exception {
        when(mockedEpsModuleHandlerComponent.getHandlerType()).thenReturn(EpsModuleHandlerType.JAVA_HANDLER);
        when(mockedEpsModuleStepComponent.getHandler()).thenReturn(mockedEpsModuleHandlerComponent);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Only esper components are acceptable");
        esperComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
    }

    @Test
    public void getOrInstallComponent_EsperComponent_InstalledSuccessfully() throws Exception {
        final String HANDLER_INSTANCE_ID = "handler_instance_id01";

        final String uniqueModuleId = "com.ericsson.test_EsperSimpleTest_1.0.0";
        final String ruleText = "select * from com.ericsson.component.aia.services.eps.pe.esper.Ticker(symbol='GOOG')";
        final String esperHandlerNamed = "EsperHandler";

        when(mockedEpsModuleHandlerComponent.getHandlerType()).thenReturn(EpsModuleHandlerType.ESPER_HANDLER);
        when(mockedEpsModuleHandlerComponent.getInstanceId()).thenReturn(HANDLER_INSTANCE_ID);
        when(mockedEpsModuleHandlerComponent.getConfiguration()).thenReturn(mockedConfiguration);
        when(mockedEpsModuleHandlerComponent.getNamed()).thenReturn(esperHandlerNamed);

        when(mockedEpsModuleStepComponent.getInstanceId()).thenReturn(STEP_INSTANCE_ID);
        when(mockedEpsModuleStepComponent.getHandler()).thenReturn(mockedEpsModuleHandlerComponent);
        when(mockedEpsModuleStepComponent.getRule()).thenReturn(mockedRulesModuleComponent);

        when(mockedRulesModuleComponent.getRules()).thenReturn(mockedEpsModuleRules);
        final Set<String> outputRuleNames = new HashSet<>();
        outputRuleNames.add("outputStream1");
        when(mockedRulesModuleComponent.getOutputRuleNames()).thenReturn(outputRuleNames);
        when(mockedRulesModuleComponent.getInputRuleName()).thenReturn("inputStream1");
        when(mockedRulesModuleComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedEpsModule.getUniqueModuleIdentifier()).thenReturn(uniqueModuleId);

        when(mockedEpsModuleRules.get(0).getRuleText()).thenReturn(ruleText);
        when(mockedProcessingEngine.deployComponent(mockedRulesModuleComponent, null)).thenReturn(mockedEventInputHandler);
        when(mockedEpsProvider.loadProcessingEngine(EpsModuleHandlerType.ESPER_HANDLER.toString(), HANDLER_INSTANCE_ID, mockedConfiguration)).thenReturn(mockedProcessingEngine);

        final EventInputHandler installedHandler = esperComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);
        assertNotNull(installedHandler);

        final EpsProvider epsProvider = Whitebox.getInternalState(esperComponentsInstaller, "epsProvider");
        final ProcessingEngine processingEngine = epsProvider.loadProcessingEngine(EpsModuleHandlerType.ESPER_HANDLER.toString(), mockedEpsModuleStepComponent.getHandler().getInstanceId(), mockedConfiguration);
        assertNotNull(processingEngine);
        assertEquals(EpsModuleHandlerType.ESPER_HANDLER.toString(), processingEngine.getEngineType());
        assertEquals(HANDLER_INSTANCE_ID, processingEngine.getInstanceId());
        assertEquals(1, processingEngine.getDeployedModules().size());

        final String module = processingEngine.getDeployedModules().iterator().next();
        assertEquals(uniqueModuleId, module);

        processingEngine.destroy();
    }

    @Test
    public void uninstallModule_OneEsperComponent_UninstalledSuccessfully() throws Exception {
        final String HANDLER_INSTANCE_ID = "handler_instance_id02";
        final String uniqueModuleId = "com.ericsson.test_EsperSimpleTest_1.0.0";
        final String ruleText = "select * from com.ericsson.component.aia.services.eps.pe.esper.Ticker(symbol='GOOG')";
        final String esperHandlerNamed = "EsperHandler";

        when(mockedEpsModuleHandlerComponent.getHandlerType()).thenReturn(EpsModuleHandlerType.ESPER_HANDLER);
        when(mockedEpsModuleHandlerComponent.getInstanceId()).thenReturn(HANDLER_INSTANCE_ID);
        when(mockedEpsModuleHandlerComponent.getConfiguration()).thenReturn(mockedConfiguration);
        when(mockedEpsModuleHandlerComponent.getNamed()).thenReturn(esperHandlerNamed);

        when(mockedEpsModuleStepComponent.getInstanceId()).thenReturn(STEP_INSTANCE_ID);
        when(mockedEpsModuleStepComponent.getHandler()).thenReturn(mockedEpsModuleHandlerComponent);
        when(mockedEpsModuleStepComponent.getRule()).thenReturn(mockedRulesModuleComponent);

        when(mockedRulesModuleComponent.getRules()).thenReturn(mockedEpsModuleRules);
        final Set<String> outputRuleNames = new HashSet<>();
        outputRuleNames.add("outputStream1");
        when(mockedRulesModuleComponent.getOutputRuleNames()).thenReturn(outputRuleNames);
        when(mockedRulesModuleComponent.getInputRuleName()).thenReturn("inputStream1");
        when(mockedRulesModuleComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedEpsModule.getUniqueModuleIdentifier()).thenReturn(uniqueModuleId);

        when(mockedEpsModuleRules.get(0).getRuleText()).thenReturn(ruleText);
        when(mockedProcessingEngine.deployComponent(mockedRulesModuleComponent, null)).thenReturn(mockedEventInputHandler);
        when(mockedEpsProvider.loadProcessingEngine(EpsModuleHandlerType.ESPER_HANDLER.toString(), HANDLER_INSTANCE_ID, mockedConfiguration)).thenReturn(mockedProcessingEngine);

        esperComponentsInstaller.getOrInstallComponent(mockedEpsModuleStepComponent);

        final EpsProvider epsProvider = Whitebox.getInternalState(esperComponentsInstaller, "epsProvider");
        final ProcessingEngine processingEngine = epsProvider.getCreatedProcessingEngine(HANDLER_INSTANCE_ID);

        assertEquals(1, processingEngine.getDeployedModules().size());

        esperComponentsInstaller.uninstallModule(uniqueModuleId);
        assertEquals(0, processingEngine.getDeployedModules().size());

        final String state = Whitebox.getInternalState((AbstractProcessingEngine) processingEngine, "state").toString();
        assertEquals("STOPPED", state);

        processingEngine.destroy();
    }

    @Test
    public void uninstallModule_MultipleEsperComponent_UninstalledSuccessfully() throws Exception {
        final String HANDLER_INSTANCE_ID = "handler_instance_id03";
        final String uniqueModuleId01 = "com.ericsson.test_EsperSimpleTest01";
        final String uniqueModuleId02 = "com.ericsson.test_EsperSimpleTest02";

        final EpsModuleComponent epsModuleComponent1 = getMockEpsModuleComponentByUniqueId(uniqueModuleId01, HANDLER_INSTANCE_ID, "_1");
        esperComponentsInstaller.getOrInstallComponent(epsModuleComponent1);

        final EpsProvider epsProvider = Whitebox.getInternalState(esperComponentsInstaller, "epsProvider");
        final ProcessingEngine processingEngine = epsProvider.getCreatedProcessingEngine(HANDLER_INSTANCE_ID);

        assertEquals(1, processingEngine.getDeployedModules().size());

        final EpsModuleComponent epsModuleComponent2 = getMockEpsModuleComponentByUniqueId(uniqueModuleId02, HANDLER_INSTANCE_ID, "_2");
        esperComponentsInstaller.getOrInstallComponent(epsModuleComponent2);
        assertEquals(2, processingEngine.getDeployedModules().size());

        esperComponentsInstaller.uninstallModule(uniqueModuleId01);
        assertEquals(1, processingEngine.getDeployedModules().size());

        final String state = Whitebox.getInternalState((AbstractProcessingEngine) processingEngine, "state").toString();
        assertEquals("STARTED", state);

        processingEngine.destroy();
    }

    private EpsModuleComponent getMockEpsModuleComponentByUniqueId(final String uniqueModuleId, final String instanceId, final String stepSuffix) {
        final String ruleText = "select * from com.ericsson.component.aia.services.eps.pe.esper.Ticker(symbol='GOOG')";
        final String esperHandlerNamed = "EsperHandler";

        when(mockedEpsModuleHandlerComponent.getHandlerType()).thenReturn(EpsModuleHandlerType.ESPER_HANDLER);
        when(mockedEpsModuleHandlerComponent.getInstanceId()).thenReturn(instanceId);
        when(mockedEpsModuleHandlerComponent.getConfiguration()).thenReturn(mockedConfiguration);
        when(mockedEpsModuleHandlerComponent.getNamed()).thenReturn(esperHandlerNamed);

        when(mockedEpsModuleStepComponent.getInstanceId()).thenReturn(STEP_INSTANCE_ID + stepSuffix);
        when(mockedEpsModuleStepComponent.getHandler()).thenReturn(mockedEpsModuleHandlerComponent);
        when(mockedEpsModuleStepComponent.getRule()).thenReturn(mockedRulesModuleComponent);

        when(mockedRulesModuleComponent.getRules()).thenReturn(mockedEpsModuleRules);
        final Set<String> outputRuleNames = new HashSet<>();
        outputRuleNames.add("outputStream1");
        when(mockedRulesModuleComponent.getOutputRuleNames()).thenReturn(outputRuleNames);
        when(mockedRulesModuleComponent.getInputRuleName()).thenReturn("inputStream1");
        when(mockedRulesModuleComponent.getModule()).thenReturn(mockedEpsModule);
        when(mockedEpsModule.getUniqueModuleIdentifier()).thenReturn(uniqueModuleId);

        when(mockedEpsModuleRules.get(0).getRuleText()).thenReturn(ruleText);
        when(mockedProcessingEngine.deployComponent(mockedRulesModuleComponent, null)).thenReturn(mockedEventInputHandler);
        when(mockedEpsProvider.loadProcessingEngine(EpsModuleHandlerType.ESPER_HANDLER.toString(), instanceId, mockedConfiguration)).thenReturn(mockedProcessingEngine);

        return mockedEpsModuleStepComponent;
    }
}
