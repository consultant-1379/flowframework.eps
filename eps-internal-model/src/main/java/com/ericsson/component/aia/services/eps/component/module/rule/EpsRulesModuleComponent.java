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
package com.ericsson.component.aia.services.eps.component.module.rule;

import java.util.*;

import com.ericsson.component.aia.services.eps.component.module.*;

/**
 * Represents single rule that can be attached to handler.
 *
 * @author eborziv
 *
 */
public class EpsRulesModuleComponent extends EpsModuleComponent {

    private final List<EpsModuleRule> rules = new LinkedList<EpsModuleRule>();

    private String inputRuleName;

    private final Set<String> outputRuleNames = new HashSet<>();

    private final List<String> ruleResources = new LinkedList<String>();

    /**
     * Instantiates a new eps rules module component.
     *
     * @param type
     *            the type
     * @param instanceId
     *            the instance id
     * @param module
     *            the module
     */
    public EpsRulesModuleComponent(final EpsModuleComponentType type, final String instanceId, final EpsModule module) {
        super(type, instanceId, module);
    }

    /**
     * Adds the rule.
     *
     * @param rule
     *            the rule
     */
    public void addRule(final EpsModuleRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule must not be null");
        }
        rules.add(rule);
    }

    /**
     * Gets the input rule name.
     *
     * @return the inputRuleName
     */
    public String getInputRuleName() {
        return inputRuleName;
    }

    /**
     * Sets the input rule name.
     *
     * @param inputRuleName
     *            the inputRuleName to set
     */
    public void setInputRuleName(final String inputRuleName) {
        if ((inputRuleName == null) || inputRuleName.isEmpty()) {
            throw new IllegalArgumentException("rule name must not be null or empty");
        }
        this.inputRuleName = inputRuleName;
    }

    /**
     * Gets the rules.
     *
     * @return the rules
     */
    public List<EpsModuleRule> getRules() {
        return rules;
    }

    /**
     * Gets the output rule names.
     *
     * @return the outputRuleNames
     */
    public Set<String> getOutputRuleNames() {
        return Collections.unmodifiableSet(outputRuleNames);
    }

    /**
     * Adds the output rule name.
     *
     * @param outputRuleName
     *            the output rule name
     */
    public void addOutputRuleName(final String outputRuleName) {
        if ((outputRuleName == null) || outputRuleName.isEmpty()) {
            throw new IllegalArgumentException("rule name must not be null or empty");
        }
        outputRuleNames.add(outputRuleName);
    }

    /**
     * Gets the rule resources.
     *
     * @return the ruleResource
     */
    public List<String> getRuleResources() {
        return ruleResources;
    }

    /**
     * Adds the rule resource.
     *
     * @param ruleResource
     *            the ruleResource to set
     */
    public void addRuleResource(final String ruleResource) {
        if (ruleResource == null) {
            throw new IllegalArgumentException("Rule resource must not be null");
        }
        ruleResources.add(ruleResource);
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "EpsRulesModuleComponent [inlineRules=" + rules + ", inputRuleName=" + inputRuleName + ", outputRuleNames=" + outputRuleNames
                + ", ruleResources=" + ruleResources + "]";
    }

}
