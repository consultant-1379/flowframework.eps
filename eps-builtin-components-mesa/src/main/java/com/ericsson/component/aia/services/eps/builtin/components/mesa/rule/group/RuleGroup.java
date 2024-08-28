package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Injectable;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.Rule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.Evaluatable;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View.ViewType;
import com.ericsson.component.aia.services.eps.mesa.common.Lifecycle;

/**
 * Wrapper around list of rules.
 *
 * @see Lifecycle
 * @see Evaluatable
 * @see Injectable
 *
 */
public interface RuleGroup extends Lifecycle, Evaluatable, Injectable {

    /**
     * Gets the conf id.
     *
     * @return the conf id
     */
    ConfId getConfId();

    /**
     * Type of view this rule group accepts.
     *
     * @return the input kind as {@link ViewType}
     */
    ViewType getInputKind();

    /**
     * Gets the rules.
     *
     * @return the rules as List
     */
    List<Rule> getRules();
}
