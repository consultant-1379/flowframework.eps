package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Name;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * Thin wrapper around list of {@link RuleGroup} objects.
 */
public final class RuleGroups {

    private final Name name;
    private final List<RuleGroup> list = new CopyOnWriteArrayList<RuleGroup>();

    /**
     * Instantiates a new rule groups.
     *
     * @param name
     *            the name
     */
    public RuleGroups(final Name name) {
        super();
        this.name = name;
    }

    /**
     * Append.
     *
     * @param ruleGroup
     *            the rule group
     */
    public void append(final RuleGroup ruleGroup) {
        list.add(ruleGroup);
    }

    /**
     * Gets the policy name.
     *
     * @return the policy name
     */
    public Name getPolicyName() {
        return name;
    }

    /**
     * Evaluate.
     *
     * @param view
     *            the view
     */
    public void evaluate(final View view) {
        for (final RuleGroup group : list) {
            group.evaluate(view);
        }
    }
}
