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
package com.ericsson.component.aia.services.eps.core.parsing;

import java.io.Serializable;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsModuleRule;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.services.eps.core.EpsComponentConstants;
import com.ericsson.component.aia.services.eps.core.util.*;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.AttributeGroupRefType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.AttributeGroupType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.AttributeValueType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.ClonedStepType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.FlowDefinition;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.HandlerType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.InputOutputType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.PathType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.PortType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.RuleResourceType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.RuleType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.StepType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.ToManyType;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.ToType;
import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 *
 * Produce a new {@link EpsModule} from a {@link FlowDefinition} instance.
 *
 * @author epiemir
 *
 */
public class EpsFlowDefinitionParser {

    private static final String ESPER_INSTANCE_NAME = "esperInstanceName";

    private static final String NO_DOT_ERROR_MSG_PREFIX = "It is not allowed to use . (dot) character inside name of handler or input/output adapter "
            + "since this is how input/output ports are referenced! Problematic name is ";

    private static final String NO_DOT_ERROR_MSG_SUFFIX = " {}. This will cause unpredictable behaviour.";

    private static final String NO_DOT_ERROR_MSG = NO_DOT_ERROR_MSG_PREFIX + NO_DOT_ERROR_MSG_SUFFIX;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Parse a {@link FlowDefinition}.
     *
     * @param flowDefinition
     *            the {@link FlowDefinition} to parse
     * @return the parsed {@link EpsModule}
     */
    public EpsModule parseFlowDef(final FlowDefinition flowDefinition) {
        log.debug("Parsing FlowDefinition: {}", flowDefinition.getName());
        final EpsModule module = new EpsModule();
        this.collectAllEpsComponents(module, flowDefinition);
        if ((flowDefinition.getPath() != null) && (flowDefinition.getPath().size() > 0)) {
            for (final PathType pathType : flowDefinition.getPath()) {
                this.convertPath(pathType, module);
            }
        }
        final String parsedFlowName = ExpressionParser.replaceAllExpressions(flowDefinition.getName());
        module.setName(parsedFlowName);
        final String parsedNamespace = ExpressionParser.replaceAllExpressions(flowDefinition.getNs());
        module.setNamespace(parsedNamespace);
        final String parsedVersion = ExpressionParser.replaceAllExpressions(flowDefinition.getVersion());
        module.setVersion(parsedVersion);
        log.trace("Parsed FlowDefinition to Module {}", module);
        return module;
    }

    private void processNodes(final ToType toType, final String sourceId, final EpsModule module) {
        final String uri = toType.getUri();
        final boolean hasUri = (uri != null) && !uri.trim().isEmpty();
        if (!hasUri) {
            throw new IllegalArgumentException("URI is required in <to> unless nested!");
        }
        log.debug("Adding subscriber {} to {}", uri, sourceId);
        module.addEventFlow(sourceId, uri);
        if (ProtocolUriUtil.isValidProtocolUri(uri)) {
            log.debug("Adding implicit output adapter for {}", uri);
            final EpsModuleComponent component = this.convertImplicitIOAdapter(EpsModuleComponentType.OUTPUT_ADAPTER, module, uri);
            module.addModuleComponent(component);
        }
    }

    private void convertPath(final PathType pathType, final EpsModule module) {
        String fromUri = pathType.getFrom().getUri();
        if (ProtocolUriUtil.isValidProtocolUri(fromUri)) {
            log.debug("Adding implicit input adapter for {}", fromUri);
            final EpsModuleComponent component = this.convertImplicitIOAdapter(EpsModuleComponentType.INPUT_ADAPTER, module, fromUri);
            module.addModuleComponent(component);
        }
        for (final Serializable serializable : pathType.getTo()) {
            if (serializable instanceof ToType) {
                final ToType toType = (ToType) serializable;
                final String toUri = toType.getUri();
                processNodes(toType, fromUri, module);
                if (toUri != null) {
                    fromUri = toUri;
                }
            }
        }
        final ToManyType toMany = pathType.getToMany();
        if (toMany != null) {
            for (final Serializable serializable : toMany.getTo()) {
                if (serializable instanceof ToType) {
                    final ToType toType = (ToType) serializable;
                    this.processNodes(toType, fromUri, module);
                }
            }
        }
        log.debug("Parsed module - paths are {}", module.getEventFlows());
    }

    private void collectAllEpsComponents(final EpsModule module, final FlowDefinition flowDefinition) {
        this.collectAllInputOutputType(module, flowDefinition);
        this.collectAllSteps(module, flowDefinition);
    }

    private void collectAllSteps(final EpsModule module, final FlowDefinition flowDefinition) {
        final Map<String, StepType> stepTypeMap = new HashMap<String, StepType>();
        log.debug("Collecting all steps");
        if ((flowDefinition.getStep() != null) && (flowDefinition.getStep().size() > 0)) {
            for (final StepType stepType : flowDefinition.getStep()) {
                final String stepTypeName = stepType.getName();
                this.checkStepNameValidity(stepTypeName);
                if (stepTypeMap.containsKey(stepTypeName)) {
                    throw new IllegalArgumentException("The flowDefinition [" + flowDefinition.getName()
                            + "] can not contain two stepType with the same name [" + stepTypeName + "]");
                }
                stepTypeMap.put(stepTypeName, stepType);
                final EpsModuleComponent comp = this.convertStep(stepTypeName, stepType, module, flowDefinition);
                module.addModuleComponent(comp);
            }
        }
        log.debug("Collected in total {} steps", stepTypeMap.size());
        this.collectAllClonedSteps(module, flowDefinition, stepTypeMap);
    }

    /*
     * Since input/output ports are using . syntax we should not allow normal step names to contain dot character
     */
    private void checkStepNameValidity(final String stepName) {
        if (stepName != null) {
            if (stepName.indexOf('.') != -1) {
                log.error(NO_DOT_ERROR_MSG, stepName);
                throw new IllegalStateException(NO_DOT_ERROR_MSG_PREFIX + stepName + ". This will cause unpredictable behaviour.");
            }
        }
    }

    private void collectAllClonedSteps(final EpsModule module, final FlowDefinition flowDefinition, final Map<String, StepType> stepTypeMap) {
        final Set<String> clonedStepTypeSet = new HashSet<String>();
        log.debug("Collecting all cloned steps");
        if ((flowDefinition.getClonedStep() != null) && (flowDefinition.getClonedStep().size() > 0)) {
            for (final ClonedStepType clonedStepType : flowDefinition.getClonedStep()) {
                final String clonedStepTypeName = clonedStepType.getName();
                this.checkStepNameValidity(clonedStepTypeName);
                if (!clonedStepTypeSet.add(clonedStepTypeName)) {
                    throw new IllegalArgumentException("The flowDefinition [" + flowDefinition.getName()
                            + "] can not contain two clonedStepType with the same name [" + clonedStepTypeName + "]");
                }
                final StepType referenceStepType = stepTypeMap.get(clonedStepType.getClonedStepRef());
                if (referenceStepType == null) {
                    throw new IllegalArgumentException("Can not find the reference stepType for clonedStepType [" + clonedStepTypeName + "]");
                }
                final EpsModuleComponent comp = this.convertStep(clonedStepTypeName, referenceStepType, module, flowDefinition);
                module.addModuleComponent(comp);
            }
        }
        log.debug("Collected {} cloned steps in total", clonedStepTypeSet.size());
    }

    private EpsModuleHandlerComponent convertHandler(final StepType stepType, final EpsModule module, final FlowDefinition flowDefinition) {
        final HandlerType handlerType = stepType.getHandler();
        final EpsModuleHandlerType compType = EpsModuleUtil.determineHandlerType(handlerType.getClassName(), handlerType.getNamed());

        final Configuration config = this.convertConfiguration(stepType.getAttribute(), stepType.getAttributeGroupRef(), flowDefinition);
        final String handlerName = this.determineExplicitHandlerName(config);
        final EpsModuleHandlerComponent handlerComponent = new EpsModuleHandlerComponent(compType, handlerName, module, handlerType.getClassName(),
                handlerType.getNamed());

        handlerComponent.setConfiguration(config);
        log.debug("Created handler {}", handlerComponent);

        return handlerComponent;
    }

    private String determineExplicitHandlerName(final Configuration config) {
        final String esperName = config.getStringProperty(ESPER_INSTANCE_NAME);
        if ((esperName != null) && !esperName.trim().isEmpty()) {
            return esperName;
        }
        return UUID.randomUUID().toString();
    }

    private EpsRulesModuleComponent convertRule(final RuleType ruleType, final boolean inlineRule, final boolean parseResourceAsRuleText,
            final EpsModule module) {
        String ruleName = ruleType.getName();
        if (inlineRule && ((ruleName == null) || ruleName.isEmpty())) {
            log.debug("Assigning random name to inline rule...");
            ruleName = "eps_rule_" + UUID.randomUUID().toString();
        }

        final EpsRulesModuleComponent component = new EpsRulesModuleComponent(EpsModuleComponentType.ESPER_COMPONENT, ruleName, module);

        if ((ruleType.getOutputPort() != null) && (ruleType.getOutputPort().size() > 0)) {
            for (final PortType portType : ruleType.getOutputPort()) {
                component.addOutputRuleName(portType.getName());
            }
        }

        if ((ruleType.getInputPort() != null) && (ruleType.getInputPort().size() > 0)) {
            component.setInputRuleName(ruleType.getInputPort().get(0).getName());
        }

        if ((ruleType.getRuleInlineOrRuleResource() != null) && (ruleType.getRuleInlineOrRuleResource().size() > 0)) {
            for (final Serializable serializable : ruleType.getRuleInlineOrRuleResource()) {
                if (serializable instanceof String) {
                    final String ruleText = (String) serializable;
                    final EpsModuleRule rule = new EpsModuleRule(ruleText);
                    component.addRule(rule);
                } else if (serializable instanceof RuleResourceType) {
                    final RuleResourceType ruleResourceType = (RuleResourceType) serializable;
                    String resourceUri = ruleResourceType.getUri();
                    if (!parseResourceAsRuleText) {
                        resourceUri = ResourceManagerUtil.normalizeURI(resourceUri);
                    }
                    component.addRuleResource(resourceUri);
                    if (parseResourceAsRuleText) {
                        log.debug("Parsing resource [{}] as rule text", resourceUri);
                        final String loadedResource = ResourceManagerUtil.loadResourceAsTextFromURI(resourceUri);
                        log.trace("Loaded {} from uri [{}]", loadedResource, resourceUri);
                        final EpsModuleRule rule = new EpsModuleRule(loadedResource);
                        component.addRule(rule);
                    }
                }
            }
        }

        return component;
    }

    private EpsModuleComponent convertStep(final String name, final StepType stepType, final EpsModule module, final FlowDefinition flowDefinition) {

        final EpsModuleHandlerComponent handlerComponent = this.convertHandler(stepType, module, flowDefinition);
        final EpsModuleHandlerType handlerType = handlerComponent.getHandlerType();
        final EpsModuleComponentType type = EpsModuleUtil.determineComponentTypeBasedOnHandlerType(handlerType);
        final EpsModuleStepComponent stepComponent = new EpsModuleStepComponent(type, name, module);

        stepComponent.setHandler(handlerComponent);

        // assign empty configuration
        final Configuration config = new EpsComponentConfiguration(new Properties());
        stepComponent.setConfiguration(config);

        final boolean parseResourceAsText = handlerType != EpsModuleHandlerType.JAVA_HANDLER;

        if (stepType.getRule() != null) {
            final EpsRulesModuleComponent rule = this.convertRule(stepType.getRule(), true, parseResourceAsText, module);
            stepComponent.setRule(rule);
        }

        log.debug("Created handler {}", stepComponent);

        return stepComponent;
    }

    private void collectAllInputOutputType(final EpsModule module, final FlowDefinition flowDefinition) {
        // convert input adapter
        if ((flowDefinition.getInput() != null) && (flowDefinition.getInput().size() > 0)) {
            for (final InputOutputType inputType : flowDefinition.getInput()) {
                final EpsModuleComponent component = this.convertInputOutput(inputType, EpsModuleComponentType.INPUT_ADAPTER, module, flowDefinition);
                module.addModuleComponent(component);
            }
        }

        // convert output adapter
        if ((flowDefinition.getOutput() != null) && (flowDefinition.getOutput().size() > 0)) {
            for (final InputOutputType inputType : flowDefinition.getOutput()) {
                final EpsModuleComponent component =
                        this.convertInputOutput(inputType, EpsModuleComponentType.OUTPUT_ADAPTER, module, flowDefinition);
                module.addModuleComponent(component);
            }
        }
    }

    private EpsModuleComponent convertImplicitIOAdapter(final EpsModuleComponentType type, final EpsModule module, final String adapterUri) {
        final EpsModuleComponent component = new EpsModuleComponent(type, adapterUri, module);
        final Map<String, String> configurationFromUri = ProtocolUriUtil.extractConfigurationFromProtocolUri(adapterUri);

        configurationFromUri.put(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME, adapterUri);
        final EpsComponentConfiguration config = new EpsComponentConfiguration(new Properties());

        config.addAll(configurationFromUri);
        component.setConfiguration(config);

        log.debug("Converted new implicit adapter {}", component);

        return component;
    }

    private EpsModuleComponent convertInputOutput(final InputOutputType inputType, final EpsModuleComponentType type, final EpsModule module,
            final FlowDefinition flowDefinition) {

        final EpsModuleComponent component = new EpsModuleComponent(type, inputType.getName(), module);

        final Configuration config = this.convertConfiguration(inputType.getAttribute(), inputType.getAttributeGroupRef(), flowDefinition);
        component.setConfiguration(config);

        log.debug("Converted new adapter {}", component);

        return component;
    }

    private Configuration convertConfiguration(final List<AttributeValueType> attributes, final AttributeGroupRefType attributeGroupRef,
            final FlowDefinition flowDefinition) {
        final List<AttributeValueType> allAttributes = new ArrayList<AttributeValueType>();
        allAttributes.addAll(attributes);
        if (attributeGroupRef != null) {
            final List<AttributeGroupType> attributeGroups = flowDefinition.getAttributeGroup();
            for (final AttributeGroupType attributeGroup : attributeGroups) {
                if (attributeGroupRef.getGroupName().equals(attributeGroup.getName())) {
                    allAttributes.addAll(attributeGroup.getAttribute());
                }
            }

        }
        return this.convertAttributes(allAttributes);
    }

    private Configuration convertAttributes(final List<AttributeValueType> attributes) {
        final Properties properties = new Properties();
        if (attributes != null && !attributes.isEmpty()) {
            for (final AttributeValueType attribute : attributes) {
                final String name = attribute.getName();
                final String value = attribute.getValue();
                properties.put(name, value);
            }
        }
        return new EpsComponentConfiguration(properties);
    }

}
