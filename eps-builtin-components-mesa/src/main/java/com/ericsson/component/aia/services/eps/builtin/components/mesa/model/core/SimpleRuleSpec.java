package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import java.util.SortedSet;
import java.util.TreeSet;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.RuleOutput;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.Rule.Type;

/**
 * The Class SimpleRuleSpec.
 *
 * @see RuleSpec
 */
public final class SimpleRuleSpec implements RuleSpec {

    private final Id id;
    private final String name;
    private final Type type;
    private final RuleOutput ruleOutput;
    private final boolean isFlexi;
    private final String reference;
    private final SortedSet<Param> set;

    /**
     * Instantiates a new simple rule spec.
     *
     * @param id
     *            the id
     * @param name
     *            the name
     * @param type
     *            the type
     * @param ruleOutput
     *            the rule output
     * @param isFlexi
     *            the is flexi
     * @param reference
     *            the reference
     */
    public SimpleRuleSpec(final Id id, final String name, final Type type, final RuleOutput ruleOutput, final boolean isFlexi,
                          final String reference) {
        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.ruleOutput = ruleOutput;
        this.isFlexi = isFlexi;
        this.reference = reference;
        set = new TreeSet<Param>();
    }

    /**
     * Append a param.
     *
     * @param param
     *            the param
     */
    public void append(final Param param) {
        set.add(param);
    }

    @Override
    public SortedSet<Param> getParams() {
        return set;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public RuleOutput getOutput() {
        return ruleOutput;
    }

    @Override
    public boolean isFlexiJythonNames() {
        return isFlexi;
    }

    @Override
    public String getReference() {
        return reference;
    }
}
