package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.IdAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Subscription;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.MatchMode;

/**
 * The Interface RuleGroupSpec extends {@link IdAware}.
 */
public interface RuleGroupSpec extends IdAware {

    /**
     * Gets the match mode.
     *
     * @return the match mode
     */
    MatchMode getMatchMode();

    /**
     * Gets the subscription.
     *
     * @return the subscription
     */
    Subscription getSubscription();

    /**
     * Gets the rules.
     *
     * @return the rules
     */
    List<RuleSpec> getRules();
}
