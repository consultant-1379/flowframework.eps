package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Subscription;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.MatchMode;

/**
 * The Class SimpleRuleGroupSpec.
 *
 * @see RuleGroupSpec
 */
public final class SimpleRuleGroupSpec implements RuleGroupSpec {

    private final Id id;
    private final MatchMode matchMode;
    private final Subscription subscription;
    private final List<RuleSpec> ruleSpecs;

    /**
     * Instantiates a new simple rule group spec.
     *
     * @param id
     *            the id
     * @param matchMode
     *            the match mode
     * @param subscription
     *            the subscription
     */
    public SimpleRuleGroupSpec(final Id id, final MatchMode matchMode, final Subscription subscription) {
        super();
        this.id = id;
        this.matchMode = matchMode;
        this.subscription = subscription;
        ruleSpecs = new ArrayList<RuleSpec>();
    }

    /**
     * Append the rule spec.
     *
     * @param ruleSpec
     *            the rule spec
     */
    public void append(final RuleSpec ruleSpec) {
        ruleSpecs.add(ruleSpec);
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public MatchMode getMatchMode() {
        return matchMode;
    }

    @Override
    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public List<RuleSpec> getRules() {
        return ruleSpecs;
    }
}
