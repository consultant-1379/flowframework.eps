package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.RuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * Finds all policies that are interested in given view.
 */
public interface Selector {

    /**
     * Conf ID of the guard and rule group has to match. Each conf ID should have one pair of these.
     *
     * @param guard
     *            the guard
     * @param ruleGroup
     *            the rule group
     */
    void register(Guard guard, RuleGroup ruleGroup);

    /**
     * Given policy configuration as been removed ie entire conf file is gone.
     *
     * @param confId
     *            the conf id
     */
    void unregister(ConfId confId);

    /**
     * Find all rule groups that should process given view.
     *
     * @param view
     *            the view
     * @return the list
     */
    List<RuleGroup> select(View view);
}
