package com.ericsson.component.aia.services.eps.pe.esper;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.services.eps.pe.esper.EsperEventInputHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.espertech.esper.client.*;

public class EsperEventInputHandlerTest {

    private static final String ESPER_EVENT_NAME = "esper_event_name";
    private static final String NULL_OUTPUT_RULE_NAME = "output_rule_name_null";
    private static final String EXIST_OUTPUT_RULE_NAME_1 = "output_rule_name_exist_a";
    private static final String EXIST_OUTPUT_RULE_NAME_2 = "output_rule_name_exist_m";
    private static final String EXIST_OUTPUT_RULE_NAME_3 = "output_rule_name_exist_z";

    private EsperEventInputHandler esperEventInputHandler;

    private EpsRulesModuleComponent mockedEpsRulesModuleComponent;
    private EPServiceProvider mockedEPServiceProvider;
    private EPRuntime mockedEPRuntime;
    private EventHandlerContext mockedEventHandlerContextWithoutSubscribers;
    private EventHandlerContext mockedEventHandlerContextWithSubscribers;
    private EPAdministrator mockedEPAdministrator;
    private EPStatement mockedEPStatement1;
    private EPStatement mockedEPStatement2;
    private EPStatement mockedEPStatement3;
    private Set<String> outputRuleNames;
    private EpsModule epsModule;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Collection<EventSubscriber> createSubscribers() {
        final List<EventSubscriber> subs = new LinkedList<EventSubscriber>();
        subs.add(new EventSubscriber() {

            @Override
            public void sendEvent(final Object arg0) {
                System.out.println("Got event");
            }

            @Override
            public String getIdentifier() {
                return "mockSub";
            }
        });
        return subs;
    }

    @Before
    public void setUp() {
        epsModule = mock(EpsModule.class);
        when(epsModule.getUniqueModuleIdentifier()).thenReturn("com.ericsson.test.EpsX2CorrelationTest_1.0.0");
        when(epsModule.getName()).thenReturn("EpsX2CorrelationTest");

        outputRuleNames = new HashSet<String>();
        mockedEpsRulesModuleComponent = mock(EpsRulesModuleComponent.class);
        mockedEPServiceProvider = mock(EPServiceProvider.class);
        mockedEPRuntime = mock(EPRuntime.class);
        mockedEventHandlerContextWithoutSubscribers = mock(EventHandlerContext.class);
        mockedEventHandlerContextWithSubscribers = mock(EventHandlerContext.class);
        when(mockedEventHandlerContextWithSubscribers.getEventSubscribers()).thenReturn(createSubscribers());
        mockedEPAdministrator = mock(EPAdministrator.class);
        mockedEPStatement1 = mock(EPStatement.class);
        when(mockedEPStatement1.getName()).thenReturn("st1");
        mockedEPStatement2 = mock(EPStatement.class);
        when(mockedEPStatement2.getName()).thenReturn("st2");
        mockedEPStatement3 = mock(EPStatement.class);
        when(mockedEPStatement3.getName()).thenReturn("st3");
        when(mockedEpsRulesModuleComponent.getInputRuleName()).thenReturn(ESPER_EVENT_NAME);
        when(mockedEpsRulesModuleComponent.getOutputRuleNames()).thenReturn(outputRuleNames);
        when(mockedEpsRulesModuleComponent.getInstanceId()).thenReturn("mockInstanceId");
        when(mockedEpsRulesModuleComponent.getModule()).thenReturn(epsModule);
        when(mockedEPServiceProvider.getEPRuntime()).thenReturn(mockedEPRuntime);
        when(mockedEPServiceProvider.getEPAdministrator()).thenReturn(mockedEPAdministrator);
        when(mockedEPAdministrator.getStatement(startsWith(NULL_OUTPUT_RULE_NAME))).thenReturn(null);
        when(mockedEPAdministrator.getStatement(startsWith(EXIST_OUTPUT_RULE_NAME_1))).thenReturn(mockedEPStatement1);
        when(mockedEPAdministrator.getStatement(startsWith(EXIST_OUTPUT_RULE_NAME_2))).thenReturn(mockedEPStatement2);
        when(mockedEPAdministrator.getStatement(startsWith(EXIST_OUTPUT_RULE_NAME_3))).thenReturn(mockedEPStatement3);
        esperEventInputHandler = new EsperEventInputHandler(mockedEpsRulesModuleComponent, mockedEPServiceProvider, null);
    }

    @After
    public void tearDown() {
        outputRuleNames = null;
        mockedEpsRulesModuleComponent = null;
        mockedEPServiceProvider = null;
        mockedEPRuntime = null;
        mockedEventHandlerContextWithoutSubscribers = null;
        mockedEPAdministrator = null;
        mockedEPStatement1 = null;
        mockedEPStatement2 = null;
        mockedEPStatement3 = null;
        esperEventInputHandler = null;
    }

    @Test
    public void onEvent_InputEventInMapFormat_SendEventToEsper() throws Exception {
        final Map inputEventInMapFormat = new HashMap();
        esperEventInputHandler.onEvent(inputEventInMapFormat);
        verify(mockedEPRuntime).sendEvent(inputEventInMapFormat, ESPER_EVENT_NAME);
    }

    @Test
    public void onEvent_InputEventInOtherFormat_SendEventToEsper() throws Exception {
        final Object inputEventInObjectFormat = new Object();
        esperEventInputHandler.onEvent(inputEventInObjectFormat);
        verify(mockedEPRuntime).sendEvent(inputEventInObjectFormat);
    }

    @Test
    public void onEvent_InputEventIsNull_NothingHappens() throws Exception {
        esperEventInputHandler.onEvent(null);
    }

    @Test
    public void init_AllStatementsExist_AddListenersToAllStatements() throws Exception {
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_1 + "1");
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_2 + "1");
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_3 + "1");
        esperEventInputHandler.init(mockedEventHandlerContextWithSubscribers);
        verify(mockedEPStatement1).addListener(any(UpdateListener.class));
        verify(mockedEPStatement2).addListener(any(UpdateListener.class));
        verify(mockedEPStatement3).addListener(any(UpdateListener.class));
    }

    @Test
    public void init_AllStatementsExist_AddListenersToAllStatementsNoSubscribersInContext() throws Exception {
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_1 + "1");
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_2 + "1");
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_3 + "1");
        esperEventInputHandler.init(mockedEventHandlerContextWithoutSubscribers);
        verify(mockedEPStatement1, never()).addListener(any(UpdateListener.class));
        verify(mockedEPStatement2, never()).addListener(any(UpdateListener.class));
        verify(mockedEPStatement3, never()).addListener(any(UpdateListener.class));
    }

    @Test
    public void init_OneStatementDoesNotExist_ThrowIllegalArgumentException() throws Exception {
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_1 + "1");
        outputRuleNames.add(NULL_OUTPUT_RULE_NAME + "1");
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_2 + "1");
        outputRuleNames.add(EXIST_OUTPUT_RULE_NAME_3 + "1");
        thrown.expect(IllegalStateException.class);
        esperEventInputHandler.init(mockedEventHandlerContextWithSubscribers);
    }
}
