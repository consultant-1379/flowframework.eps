package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper;

import java.util.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Pair;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.*;

/**
 * Thi class manages the deployment configuration.
 */
public class DeploymentConfig {

    private final List<String> eventList;

    private final RuleSpec rule;
    private final RuleGroupSpec ruleGroup;
    private final Policy policy;
    private final Map<String, MesaUpdateListener> listeners;
    private final Pair<Id, Id> key;

    /**
     * Instantiates a new deployment config.
     *
     * @param eventList
     *            the event list
     * @param rule
     *            {@link RuleSpec}
     * @param ruleGroup
     *            {@link RuleGroupSpec}
     * @param policy
     *            {@link Policy}
     */
    DeploymentConfig(final List<String> eventList, final RuleSpec rule, final RuleGroupSpec ruleGroup, final Policy policy) {
        this.eventList = eventList;
        this.rule = rule;
        this.ruleGroup = ruleGroup;
        this.policy = policy;
        listeners = new HashMap<String, MesaUpdateListener>();
        key = new Pair<Id, Id>(ruleGroup.getId(), rule.getId());
    }

    /**
     * Gets the event list.
     *
     * @return the event list
     */
    public List<String> getEventList() {
        return eventList;
    }

    /**
     * Gets the rule.
     *
     * @return the rule
     */
    public RuleSpec getRule() {
        return rule;
    }

    /**
     * Gets the rule group.
     *
     * @return the rule group
     */
    public RuleGroupSpec getRuleGroup() {
        return ruleGroup;
    }

    /**
     * Gets the policy.
     *
     * @return the policy
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * Gets the listeners.
     *
     * @return the listeners
     */
    public Map<String, MesaUpdateListener> getListeners() {
        return listeners;
    }

    /**
     * Update the listener.
     *
     * @param ModuleId
     *            the module id
     * @param listener
     *            the listener
     */
    public void update(final String ModuleId, final MesaUpdateListener listener) {
        listeners.put(ModuleId, listener);
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public Pair<Id, Id> getKey() {
        return key;
    }

}
