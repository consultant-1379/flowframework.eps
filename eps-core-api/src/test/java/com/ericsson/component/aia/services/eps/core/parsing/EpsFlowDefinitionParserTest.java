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
package com.ericsson.component.aia.services.eps.core.parsing;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsModuleRule;
import com.ericsson.component.aia.services.eps.core.EpsComponentConstants;
import com.ericsson.component.aia.services.eps.core.parsing.EpsFlowDefinitionParser;
import com.ericsson.component.aia.services.eps.core.parsing.EpsModeledFlowParser;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.FlowDefinition;
import com.ericsson.oss.itpf.modeling.schema.util.DtdModelHandlingUtil;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.component.aia.services.eps.core.execution.EventFlow;

public class EpsFlowDefinitionParserTest {

    private final EpsFlowDefinitionParser flowDefinitionParser = new EpsFlowDefinitionParser();

    @Test
    public void testEpsModeledFlowParser_1() throws Exception {
        final EpsModeledFlowParser result = new EpsModeledFlowParser();
        assertNotNull(result);
    }

    @Test
    public void testParseModule_1() {
        final FlowDefinition flowDef = getFlowdef("/flow_01.xml");
        assertNotNull(flowDef);
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("test_name", module.getName());
        assertEquals("1.1.0", module.getVersion());
    }

    @Test
    public void test_flow_02() {
        final FlowDefinition flowDef = getFlowdef("/flow_02.xml");
        assertNotNull(flowDef);
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("test_name", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(2, module.getModuleComponents().size());
        final String inputUri = "file://tmp/file1";
        final String outputUri = "file://tmp/file2";
        final EpsModuleComponent inputComp = module.getModuleComponents().get(inputUri);
        assertNotNull(inputComp);
        assertEquals(EpsModuleComponentType.INPUT_ADAPTER, inputComp.getComponentType());
        assertEquals(inputUri, inputComp.getInstanceId());
        assertEquals(inputUri, inputComp.getConfiguration().getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME));

        final EpsModuleComponent inputComp2 = module.getModuleComponents().get(outputUri);
        assertNotNull(inputComp2);
        assertEquals(EpsModuleComponentType.OUTPUT_ADAPTER, inputComp2.getComponentType());
        assertEquals(outputUri, inputComp2.getInstanceId());
        assertEquals(outputUri, inputComp2.getConfiguration().getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME));
        assertEquals(1, module.getEventFlows().size());
        final EventFlow flow = module.getEventFlows().get(0);
        assertNotNull(flow);
        assertEquals(inputUri, flow.getInputComponentId());
        assertEquals(1, flow.getOutputComponentIdentifiers().size());
        assertEquals(outputUri, flow.getOutputComponentIdentifiers().iterator().next());
    }

    @Test
    public void test_flow_03() {
        final FlowDefinition flowDef = getFlowdef("/flow_03.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("test_name", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(6, module.getModuleComponents().size());
        assertEquals(3, module.getEventFlows().size());

        final EventFlow flow1 = module.getEventFlows().get(0);
        assertNotNull(flow1);
        assertEquals("file://tmp/file1", flow1.getInputComponentId());
        assertEquals(1, flow1.getOutputComponentIdentifiers().size());
        assertEquals("doRule1", flow1.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow2 = module.getEventFlows().get(1);
        assertNotNull(flow2);
        assertEquals("doRule1", flow2.getInputComponentId());
        assertEquals(1, flow2.getOutputComponentIdentifiers().size());
        assertEquals("doRule2", flow2.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow3 = module.getEventFlows().get(2);
        assertNotNull(flow3);
        assertEquals("doRule2", flow3.getInputComponentId());
        assertEquals(1, flow3.getOutputComponentIdentifiers().size());
        assertEquals("local:/channelId=channelToAnotherFlow", flow3.getOutputComponentIdentifiers().iterator().next());

        assertNotNull(module.getModuleComponents().get("file://tmp/file1"));
        final EpsModuleComponent localIOComp = module.getModuleComponents().get("local:/channelId=channelToAnotherFlow");
        assertNotNull(localIOComp);
        assertNotNull(localIOComp.getConfiguration());
        assertEquals(2, localIOComp.getConfiguration().getAllProperties().size());
        assertEquals("channelToAnotherFlow", localIOComp.getConfiguration().getStringProperty("channelId"));
        assertEquals("local:/channelId=channelToAnotherFlow", localIOComp.getConfiguration().getStringProperty("uri"));
        assertNotNull(module.getModuleComponents().get("doRule1"));
        assertNotNull(module.getModuleComponents().get("doRule2"));

        test_flow_03_checkRule1(module);
        test_flow_03_checkRule2(module);
        test_flow_03_checkRule3(module);

        final EpsModuleComponent comp4 = module.getModuleComponents().get("jythonRuleHandler");
        assertEquals(EpsModuleComponentType.JVM_SCRIPTING_COMPONENT, comp4.getComponentType());
        assertTrue(comp4 instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent step4 = (EpsModuleStepComponent) comp4;
        assertEquals(EpsModuleComponentType.JVM_SCRIPTING_COMPONENT, step4.getComponentType());
        assertNotNull(step4.getHandler());
        assertEquals(EpsModuleHandlerType.JVM_SCRIPTING_HANDLER, step4.getHandler().getHandlerType());
        assertNotNull(step4.getHandler().getInstanceId());
        assertNotNull(step4.getConfiguration());
        assertNotNull(step4.getRule());
        assertEquals("jythonRule", step4.getRule().getInstanceId());
        assertEquals("jython", step4.getConfiguration().getStringProperty("script_handler_name"));
        assertEquals(1, step4.getRule().getRules().size());
        assertEquals(1, step4.getRule().getRuleResources().size());
        assertNotNull(step4.getRule().getRules().get(0));

        assertEquals("classpath:/scripts/script.py", step4.getRule().getRuleResources().get(0));
        assertEquals("JvmScriptingHandler", step4.getJavaHandlerNamed());

        final EpsModuleRule moduleRule4 = step4.getRule().getRules().get(0);
        assertTrue(moduleRule4.getRuleText().contains("def"));
        assertTrue(moduleRule4.getRuleText().contains("return self.__instanceId"));
    }

    private void test_flow_03_checkRule1(final EpsModule module) {

        // handler for rule 1
        final EpsModuleComponent comp = module.getModuleComponents().get("doRule1");

        assertTrue(comp instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent step = (EpsModuleStepComponent) comp;
        assertNotNull(step.getHandler());

        assertEquals(EpsModuleHandlerType.ESPER_HANDLER, step.getHandler().getHandlerType());
        assertEquals("EsperHandler", step.getJavaHandlerNamed());

        assertNotNull(step.getRule());
        assertEquals(0, step.getRule().getRuleResources().size());
        assertEquals("in", step.getRule().getInputRuleName());
        assertEquals(1, step.getRule().getOutputRuleNames().size());
        assertTrue(step.getRule().getOutputRuleNames().contains("out"));

        assertEquals(1, step.getRule().getRules().size());
        final EpsModuleRule moduleRule = step.getRule().getRules().get(0);
        assertEquals("some rule", moduleRule.getRuleText());

    }

    private void test_flow_03_checkRule2(final EpsModule module) {

        // handler for rule 2
        final EpsModuleComponent comp2 = module.getModuleComponents().get("doRule2");
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, comp2.getComponentType());
        assertTrue(comp2 instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent step2 = (EpsModuleStepComponent) comp2;
        assertNotNull(step2.getHandler());
        assertEquals(EpsModuleHandlerType.JAVA_HANDLER, step2.getHandler().getHandlerType());
        assertNotNull(step2.getHandler().getInstanceId());
        assertNotNull(step2.getConfiguration());
        // // handler always returns configuration of the underlying rule
        assertEquals(1, step2.getConfiguration().getAllProperties().size());

        assertEquals("com.ericsson.component.aia.examples.Foo", step2.getJavaHandlerClassName());

        assertEquals("val1", step2.getConfiguration().getStringProperty("prop1"));
        assertNull(step2.getConfiguration().getStringProperty("prop2"));
        assertNull(step2.getRule());

    }

    private void test_flow_03_checkRule3(final EpsModule module) {

        // handler for rule 3
        final EpsModuleComponent comp3 = module.getModuleComponents().get("doRule3");
        assertTrue(comp3 instanceof EpsModuleStepComponent);
        assertEquals(EpsModuleComponentType.ESPER_COMPONENT, comp3.getComponentType());
        final EpsModuleStepComponent step3 = (EpsModuleStepComponent) comp3;
        assertNotNull(step3.getHandler());
        assertEquals(EpsModuleHandlerType.ESPER_HANDLER, step3.getHandler().getHandlerType());
        assertEquals(EpsModuleComponentType.ESPER_COMPONENT, step3.getComponentType());
        assertNotNull(step3.getHandler().getInstanceId());
        assertNotNull(step3.getConfiguration());

        assertNotNull(step3.getRule());
        assertEquals("externalRule3", step3.getRule().getInstanceId());
        assertEquals("val1", step3.getConfiguration().getStringProperty("prop1"));
        assertEquals("val2", step3.getConfiguration().getStringProperty("prop2"));
        assertEquals("val3", step3.getConfiguration().getStringProperty("prop3"));
        assertEquals(2, step3.getRule().getRules().size());
        assertEquals(2, step3.getRule().getRuleResources().size());

        final EpsModuleRule rule1 = step3.getRule().getRules().get(0);
        assertNotNull(rule1);

        assertEquals("classpath:/rule3.epl", step3.getRule().getRuleResources().get(0));
        assertEquals("EsperHandler", step3.getJavaHandlerNamed());

        assertEquals("select count(*) from abc;", rule1.getRuleText());

        final EpsModuleRule rule2 = step3.getRule().getRules().get(1);
        assertNotNull(rule2);

        assertEquals("classpath:/rule4.epl", step3.getRule().getRuleResources().get(1));
        assertEquals("EsperHandler", step3.getJavaHandlerNamed());

        assertEquals("select count(*) from abc where d=1;", rule2.getRuleText());
    }

    @Test
    public void test_flow_04() {
        final String systemPropertyName = "abc_prop";
        final String systemPropertyValue = "01";
        System.setProperty(systemPropertyName, systemPropertyValue);
        final FlowDefinition flowDef = getFlowdef("/flow_04.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("flows04-01", module.getName());
        assertEquals("com.ericsson.test-01", module.getNamespace());
        assertEquals("1.1.0-01", module.getVersion());
        final EpsModuleComponent inputAdapter = module.getComponent("hcInput");
        assertNotNull(inputAdapter);
        assertTrue(inputAdapter.getModule() == module);
        assertEquals(inputAdapter.getComponentType(), EpsModuleComponentType.INPUT_ADAPTER);
        assertEquals(inputAdapter.getInstanceId(), "hcInput");
        assertEquals(2, inputAdapter.getConfiguration().getAllProperties().size());
        assertEquals("hazelcast:/", inputAdapter.getConfiguration().getStringProperty("uri"));
        assertEquals("eps-input-topic-" + systemPropertyValue, inputAdapter.getConfiguration().getStringProperty("channelName"));
        // test output adapters
        final EpsModuleComponent outputAdapter = module.getComponent("hcOutput");
        assertNotNull(outputAdapter);
        assertTrue(outputAdapter.getModule() == module);
        assertEquals(outputAdapter.getComponentType(), EpsModuleComponentType.OUTPUT_ADAPTER);
        assertEquals(outputAdapter.getInstanceId(), "hcOutput");
        assertEquals(2, outputAdapter.getConfiguration().getAllProperties().size());
        assertEquals("hazelcast:/", outputAdapter.getConfiguration().getStringProperty("uri"));
        assertEquals("eps-output-topic", outputAdapter.getConfiguration().getStringProperty("channelName"));
        System.clearProperty(systemPropertyName);
    }

    @Test
    public void test_flow_multi_subscribers() {
        final FlowDefinition flowDef = getFlowdef("/flow_multiple_subscribers.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("test_multi_subscribers", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(9, module.getModuleComponents().size());
        assertEquals(7, module.getEventFlows().size());
        final EventFlow flow1 = module.getEventFlowByFromUri("file://tmp/file1");
        assertNotNull(flow1);
        assertEquals("file://tmp/file1", flow1.getInputComponentId());
        assertEquals(1, flow1.getOutputComponentIdentifiers().size());
        assertEquals("choose", flow1.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow2 = module.getEventFlowByFromUri("choose");
        assertEquals(2, flow2.getOutputComponentIdentifiers().size());
        final Iterator<String> flow2Iter = flow2.getOutputComponentIdentifiers().iterator();
        assertEquals("firstA", flow2Iter.next());
        assertEquals("firstB", flow2Iter.next());

        final EventFlow flow3 = module.getEventFlowByFromUri("firstA");
        assertNotNull(flow3);
        assertEquals(1, flow3.getOutputComponentIdentifiers().size());
        final Iterator<String> flow3Iter = flow3.getOutputComponentIdentifiers().iterator();
        assertEquals("firstA", flow3.getInputComponentId());
        assertEquals("secondA", flow3Iter.next());

        final EventFlow flow4 = module.getEventFlowByFromUri("firstB");
        assertNotNull(flow4);
        assertEquals(2, flow4.getOutputComponentIdentifiers().size());
        assertEquals("firstB", flow4.getInputComponentId());
        final Iterator<String> flow4Iter = flow4.getOutputComponentIdentifiers().iterator();
        assertEquals("final", flow4Iter.next());
        assertEquals("secondB", flow4Iter.next());

        final EventFlow flow5 = module.getEventFlowByFromUri("secondA");
        assertNotNull(flow5);
        assertEquals(1, flow5.getOutputComponentIdentifiers().size());
        final Iterator<String> flow5Iter = flow5.getOutputComponentIdentifiers().iterator();
        assertEquals("secondA", flow5.getInputComponentId());
        assertEquals("firstB", flow5Iter.next());

        final EventFlow flow6 = module.getEventFlowByFromUri("secondB");
        assertNotNull(flow6);
        assertEquals(1, flow6.getOutputComponentIdentifiers().size());
        final Iterator<String> flow6Iter = flow6.getOutputComponentIdentifiers().iterator();
        assertEquals("secondB", flow6.getInputComponentId());
        assertEquals("thirdB", flow6Iter.next());

        final EventFlow flow7 = module.getEventFlowByFromUri("thirdB");
        assertNotNull(flow7);
        assertEquals(1, flow7.getOutputComponentIdentifiers().size());
        final Iterator<String> flow7Iter = flow7.getOutputComponentIdentifiers().iterator();
        assertEquals("thirdB", flow7.getInputComponentId());
        assertEquals("fourthB", flow7Iter.next());
    }

    @Test
    public void test_one_to_many_flow() {
        final FlowDefinition flowDef = getFlowdef("/flow_one_to_many.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("test_one_to_many", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(11, module.getModuleComponents().size());
        assertEquals(8, module.getEventFlows().size());
        final EventFlow flow1 = module.getEventFlowByFromUri("file://tmp/file1");
        assertNotNull(flow1);
        assertEquals("file://tmp/file1", flow1.getInputComponentId());
        assertEquals(1, flow1.getOutputComponentIdentifiers().size());
        assertEquals("choose", flow1.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow2 = module.getEventFlowByFromUri("choose");
        assertEquals(3, flow2.getOutputComponentIdentifiers().size());
        final Iterator<String> flow2Iter = flow2.getOutputComponentIdentifiers().iterator();
        assertEquals("firstA", flow2Iter.next());
        assertEquals("firstB", flow2Iter.next());
        assertEquals("firstC", flow2Iter.next());

        final EventFlow flow3 = module.getEventFlowByFromUri("firstA");
        assertNotNull(flow3);
        assertEquals(1, flow3.getOutputComponentIdentifiers().size());
        final Iterator<String> flow3Iter = flow3.getOutputComponentIdentifiers().iterator();
        assertEquals("firstA", flow3.getInputComponentId());
        assertEquals("secondA", flow3Iter.next());

        final EventFlow flow4 = module.getEventFlowByFromUri("secondA");
        assertNotNull(flow4);
        assertEquals(1, flow4.getOutputComponentIdentifiers().size());
        assertEquals("secondA", flow4.getInputComponentId());
        assertEquals("file://tmp/fileA", flow4.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow5 = module.getEventFlowByFromUri("firstB");
        assertNotNull(flow5);
        assertEquals(1, flow5.getOutputComponentIdentifiers().size());
        assertEquals("firstB", flow5.getInputComponentId());
        assertEquals("secondB", flow5.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow6 = module.getEventFlowByFromUri("secondB");
        assertNotNull(flow6);
        assertEquals(1, flow6.getOutputComponentIdentifiers().size());
        final Iterator<String> flow6Iter = flow6.getOutputComponentIdentifiers().iterator();
        assertEquals("secondB", flow6.getInputComponentId());
        assertEquals("thirdB", flow6Iter.next());

        final EventFlow flow7 = module.getEventFlowByFromUri("thirdB");
        assertNotNull(flow7);
        assertEquals(1, flow7.getOutputComponentIdentifiers().size());
        final Iterator<String> flow7Iter = flow7.getOutputComponentIdentifiers().iterator();
        assertEquals("thirdB", flow7.getInputComponentId());
        assertEquals("fourthB", flow7Iter.next());

        final EventFlow flow8 = module.getEventFlowByFromUri("fourthB");
        assertNotNull(flow8);
        assertEquals(1, flow8.getOutputComponentIdentifiers().size());
        assertEquals("fourthB", flow8.getInputComponentId());
        assertEquals("file://tmp/fileB", flow8.getOutputComponentIdentifiers().iterator().next());
    }

    @Test
    public void test_multi_input_multi_path() throws Exception {
        final FlowDefinition flowDef = getFlowdef("/multiple_inputs_multiple_paths_flow.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test1", module.getNamespace());
        assertEquals("test_name1", module.getName());
        assertEquals("1.2.0", module.getVersion());
        assertEquals(5, module.getModuleComponents().size());
        assertEquals(4, module.getEventFlows().size());
        final EventFlow flow1 = module.getEventFlowByFromUri("mInput1");
        assertNotNull(flow1);
        assertEquals("mInput1", flow1.getInputComponentId());
        assertEquals(1, flow1.getOutputComponentIdentifiers().size());
        assertEquals("s1", flow1.getOutputComponentIdentifiers().iterator().next());

        final EventFlow flow2 = module.getEventFlowByFromUri("mInput2");
        assertEquals(1, flow2.getOutputComponentIdentifiers().size());
        final Iterator<String> flow2Iter = flow2.getOutputComponentIdentifiers().iterator();
        assertEquals("s1", flow2Iter.next());

        final EventFlow flow4 = module.getEventFlowByFromUri("s1");
        assertNotNull(flow4);
        assertEquals(2, flow4.getOutputComponentIdentifiers().size());
        assertEquals("s1", flow4.getInputComponentId());
        final Iterator<String> flow4Iter = flow4.getOutputComponentIdentifiers().iterator();
        assertEquals("s2", flow4Iter.next());
        assertEquals("s3", flow4Iter.next());

        final EventFlow flow5 = module.getEventFlowByFromUri("s2");
        assertNotNull(flow5);
        assertEquals(1, flow5.getOutputComponentIdentifiers().size());
        final Iterator<String> flow5Iter = flow5.getOutputComponentIdentifiers().iterator();
        assertEquals("s2", flow5.getInputComponentId());
        assertEquals("s3", flow5Iter.next());
    }

    @Test
    public void test_cdi_event_handler() throws Exception {
        final FlowDefinition flowDef = getFlowdef("/simple_cdi_noncdi_flow.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("test_cdi_event_handler", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(4, module.getModuleComponents().size());

        final EpsModuleComponent cdiEventHandler = module.getComponent("cdiEventHandler");
        assertNotNull(cdiEventHandler);
        assertTrue(cdiEventHandler instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent cdiStep = (EpsModuleStepComponent) cdiEventHandler;
        assertNotNull(cdiStep.getHandler());
        assertEquals(EpsModuleHandlerType.JAVA_HANDLER, cdiStep.getHandler().getHandlerType());
        assertNotNull(cdiStep.getHandler().getInstanceId());
        assertEquals("cdiEventHandler", cdiStep.getInstanceId());
        assertEquals(module, cdiStep.getModule());
        assertNull(cdiStep.getJavaHandlerClassName());
    }

    @Test
    public void test_attribute_group() throws Exception {
        final FlowDefinition flowDef = getFlowdef("/flow_with_attribute_group.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("attribute_group_flow", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(6, module.getModuleComponents().size());

        final EpsModuleComponent hcInputAdapter = module.getComponent("hcInput");
        assertNotNull(hcInputAdapter);
        assertEquals(EpsModuleComponentType.INPUT_ADAPTER, hcInputAdapter.getComponentType());
        final Configuration hcInputAdapterConf = hcInputAdapter.getConfiguration();
        final Map<String, Object> hcInputAdapterProps = hcInputAdapterConf.getAllProperties();
        assertEquals(3, hcInputAdapterProps.size());
        assertEquals("hazelcast:/", hcInputAdapterProps.get("uri"));
        assertEquals("true", hcInputAdapterProps.get("hazelcast.manage.events.as.byte.arrays"));
        assertEquals("eps-topic-in", hcInputAdapterProps.get("channelName"));

        final EpsModuleComponent hcOutputAdapterA = module.getComponent("hcOutputA");
        assertNotNull(hcOutputAdapterA);
        assertEquals(EpsModuleComponentType.OUTPUT_ADAPTER, hcOutputAdapterA.getComponentType());
        final Configuration hcOutputAdapterAConf = hcOutputAdapterA.getConfiguration();
        final Map<String, Object> hcOutputAdapterAProps = hcOutputAdapterAConf.getAllProperties();
        assertEquals(3, hcOutputAdapterAProps.size());
        assertEquals("hazelcast:/", hcOutputAdapterAProps.get("uri"));
        assertEquals("true", hcOutputAdapterAProps.get("hazelcast.manage.events.as.byte.arrays"));
        assertEquals("eps-topic-out-A", hcOutputAdapterAProps.get("channelName"));

        final EpsModuleComponent hcOutputAdapterB = module.getComponent("hcOutputB");
        assertNotNull(hcOutputAdapterB);
        assertEquals(EpsModuleComponentType.OUTPUT_ADAPTER, hcOutputAdapterB.getComponentType());
        final Configuration hcOutputAdapterBConf = hcOutputAdapterB.getConfiguration();
        final Map<String, Object> hcOutputAdapterBProps = hcOutputAdapterBConf.getAllProperties();
        assertEquals(3, hcOutputAdapterBProps.size());
        assertEquals("hazelcast:/", hcOutputAdapterBProps.get("uri"));
        assertEquals("true", hcOutputAdapterBProps.get("hazelcast.manage.events.as.byte.arrays"));
        assertEquals("eps-topic-out-B", hcOutputAdapterBProps.get("channelName"));

        final EpsModuleComponent choose = module.getComponent("choose");
        assertNotNull(choose);
        assertTrue(choose instanceof EpsModuleStepComponent);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, choose.getComponentType());
        final Configuration chooseConf = choose.getConfiguration();
        final Map<String, Object> chooseProps = chooseConf.getAllProperties();
        assertEquals(1, chooseProps.size());
        assertEquals("val1", chooseProps.get("prop1"));

        final EpsModuleComponent batchA = module.getComponent("batchA");
        assertNotNull(batchA);
        assertTrue(batchA instanceof EpsModuleStepComponent);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, batchA.getComponentType());
        final Configuration batchAConf = batchA.getConfiguration();
        final Map<String, Object> batchAProps = batchAConf.getAllProperties();
        assertEquals(4, batchAProps.size());
        assertEquals("-1", batchAProps.get("maxBatchSize"));
        assertEquals("300", batchAProps.get("flushBatchPeriodMillis"));
        assertEquals("common", batchAProps.get("commonProp"));
        assertEquals("valA1", batchAProps.get("batchPropA1"));

        final EpsModuleComponent batchB = module.getComponent("batchB");
        assertNotNull(batchB);
        assertTrue(batchB instanceof EpsModuleStepComponent);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, batchB.getComponentType());
        final Configuration batchBConf = batchB.getConfiguration();
        final Map<String, Object> batchBProps = batchBConf.getAllProperties();
        assertEquals(5, batchBProps.size());
        assertEquals("-1", batchBProps.get("maxBatchSize"));
        assertEquals("300", batchBProps.get("flushBatchPeriodMillis"));
        assertEquals("common", batchBProps.get("commonProp"));
        assertEquals("valB1", batchBProps.get("batchPropB1"));
        assertEquals("valB2", batchBProps.get("batchPropB2"));
    }

    @Test
    public void test_cloned_step() throws Exception {
        final FlowDefinition flowDef = getFlowdef("/flow_with_cloned_steps.xml");
        assertNotNull(flowDef.getModelCreationInfo());
        final EpsModule module = flowDefinitionParser.parseFlowDef(flowDef);
        assertNotNull(module);
        assertEquals("com.ericsson.test", module.getNamespace());
        assertEquals("cloned_steps_flow", module.getName());
        assertEquals("1.1.0", module.getVersion());
        assertEquals(6, module.getModuleComponents().size());

        final EpsModuleComponent hcInputAdapter = module.getComponent("hcInput");
        assertNotNull(hcInputAdapter);
        assertEquals(EpsModuleComponentType.INPUT_ADAPTER, hcInputAdapter.getComponentType());

        final EpsModuleComponent handlerA = module.getComponent("handlerA");
        assertNotNull(handlerA);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, handlerA.getComponentType());
        assertTrue(handlerA instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent stepA = (EpsModuleStepComponent) handlerA;
        assertEquals("com.ericsson.component.aia.examples.Foo", stepA.getJavaHandlerClassName());
        final Configuration stepAConf = stepA.getConfiguration();
        final Map<String, Object> stepAProps = stepAConf.getAllProperties();
        assertEquals(1, stepAProps.size());
        assertEquals("val", stepAProps.get("prop"));

        final EpsModuleComponent handlerB = module.getComponent("handlerB");
        assertNotNull(handlerB);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, handlerB.getComponentType());
        assertTrue(handlerB instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent stepB = (EpsModuleStepComponent) handlerB;
        assertEquals("com.ericsson.component.aia.examples.Foo", stepB.getJavaHandlerClassName());
        final Configuration stepBConf = stepB.getConfiguration();
        final Map<String, Object> stepBProps = stepBConf.getAllProperties();
        assertEquals(1, stepBProps.size());
        assertEquals("val", stepBProps.get("prop"));

        final EpsModuleComponent handlerC = module.getComponent("handlerC");
        assertNotNull(handlerC);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, handlerC.getComponentType());
        assertTrue(handlerC instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent stepC = (EpsModuleStepComponent) handlerC;
        assertEquals("com.ericsson.component.aia.examples.Foo", stepC.getJavaHandlerClassName());
        final Configuration stepCConf = stepC.getConfiguration();
        final Map<String, Object> stepCProps = stepCConf.getAllProperties();
        assertEquals(1, stepCProps.size());
        assertEquals("val", stepCProps.get("prop"));

        test_cloned_step_checkRules(module);

        assertFalse(handlerA == handlerB);
        assertFalse(stepA == stepB);
        assertFalse(stepA.getHandler() == stepB.getHandler());
        assertFalse(stepA.getRule() == stepB.getRule());

        assertFalse(handlerC == handlerB);
        assertFalse(stepC == stepB);
        assertFalse(stepC.getHandler() == stepB.getHandler());
        assertFalse(stepC.getRule() == stepB.getRule());

        assertFalse(handlerC == handlerA);
        assertFalse(stepC == stepA);
        assertFalse(stepC.getHandler() == stepA.getHandler());
        assertFalse(stepC.getRule() == stepA.getRule());

        assertEquals(3, module.getEventFlows().size());
        final EventFlow flow1 = module.getEventFlowByFromUri("hcInput");
        final Set<String> outputComponents1 = flow1.getOutputComponentIdentifiers();
        assertEquals(3, outputComponents1.size());
        assertTrue(outputComponents1.equals(new HashSet<String>(Arrays.asList("handlerA", "handlerB", "handlerC"))));

        final EventFlow flow2 = module.getEventFlowByFromUri("handlerA");
        final Set<String> outputComponents2 = flow2.getOutputComponentIdentifiers();
        assertEquals(1, outputComponents2.size());
        assertTrue(outputComponents2.equals(new HashSet<String>(Arrays.asList("doRuleA"))));

        final EventFlow flow3 = module.getEventFlowByFromUri("handlerB");
        final Set<String> outputComponents3 = flow3.getOutputComponentIdentifiers();
        assertEquals(1, outputComponents3.size());
        assertTrue(outputComponents3.equals(new HashSet<String>(Arrays.asList("doRuleB"))));
    }

    public void test_cloned_step_checkRules(final EpsModule module) throws Exception {

        final EpsModuleComponent doRuleA = module.getComponent("doRuleA");
        assertNotNull(doRuleA);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, doRuleA.getComponentType());
        assertTrue(doRuleA instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent doStepA = (EpsModuleStepComponent) doRuleA;
        assertEquals("com.ericsson.component.aia.examples.foo.DoRule", doStepA.getJavaHandlerClassName());
        final Configuration doStepConfA = doStepA.getConfiguration();
        final Map<String, Object> doStepPropsA = doStepConfA.getAllProperties();
        assertEquals(1, doStepPropsA.size());
        assertEquals("val1", doStepPropsA.get("prop1"));

        final EpsModuleComponent doRuleB = module.getComponent("doRuleB");
        assertNotNull(doRuleB);
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, doRuleB.getComponentType());
        assertTrue(doRuleB instanceof EpsModuleStepComponent);
        final EpsModuleStepComponent doStepB = (EpsModuleStepComponent) doRuleB;
        assertEquals("com.ericsson.component.aia.examples.foo.DoRule", doStepB.getJavaHandlerClassName());
        final Configuration doStepConfB = doStepB.getConfiguration();
        final Map<String, Object> doStepPropsB = doStepConfB.getAllProperties();
        assertEquals(1, doStepPropsB.size());
        assertEquals("val1", doStepPropsB.get("prop1"));

        //		assertFalse(handlerA == handlerB);
        //		assertFalse(stepA == stepB);
        //		assertFalse(stepA.getHandler() == stepB.getHandler());
        //		assertFalse(stepA.getRule() == stepB.getRule());
        //
        //		assertFalse(handlerC == handlerB);
        //		assertFalse(stepC == stepB);
        //		assertFalse(stepC.getHandler() == stepB.getHandler());
        //		assertFalse(stepC.getRule() == stepB.getRule());
        //
        //		assertFalse(handlerC == handlerA);
        //		assertFalse(stepC == stepA);
        //		assertFalse(stepC.getHandler() == stepA.getHandler());
        //		assertFalse(stepC.getRule() == stepA.getRule());

        assertFalse(doRuleA == doRuleB);
        assertFalse(doStepA == doStepB);
        assertFalse(doStepA.getHandler() == doStepB.getHandler());

    }

    private FlowDefinition getFlowdef(final String flowURI) {
        final InputStream input = Resources.getClasspathResource(flowURI).getInputStream();
        assertNotNull("Resource flow file " + flowURI + " not found", input);
        final Unmarshaller unmarshaller = DtdModelHandlingUtil.getUnmarshaller(SchemaConstants.FBP_FLOW);
        try {
            return (FlowDefinition) unmarshaller.unmarshal(new StreamSource(input));
        } catch (final JAXBException jaxbexc) {
            fail("Invalid module - unable to parse it! Details: " + jaxbexc.getMessage());
        }
        return null;
    }

}
