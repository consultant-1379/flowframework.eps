package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.NameAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Pair;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.IdAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source.IdSource;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Injectable;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Model;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.PolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.RuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;

/**
 * ID of the core policy is generated by {@link IdSource}.
 */
public interface Policy extends Model, NameAware, IdAware, Injectable {

    /**
     * Gets the rule group specs.
     *
     * @return the rule group specs
     */
    List<RuleGroupSpec> getRuleGroupSpecs();

    /**
     * Instantiate.
     *
     * @param policyConf
     *            the policy conf
     * @return the list
     */
    List<Pair<Guard, RuleGroup>> instantiate(PolicyConf policyConf);
}
