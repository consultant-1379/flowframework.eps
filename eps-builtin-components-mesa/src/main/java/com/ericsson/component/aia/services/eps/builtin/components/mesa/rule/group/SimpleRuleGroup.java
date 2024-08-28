package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.NotYetImplementedException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.StateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View.ViewType;

/**
 * The Class SimpleRuleGroup manages rule from {@link RuleGroup}.
 */
public final class SimpleRuleGroup implements RuleGroup {

    private final ConfId confId;
    private final MatchMode mode;
    private final List<Rule> rules;
    private final ViewType viewType;

    private StateManager stateManager;

    /**
     * Instantiates a new simple rule group.
     *
     * @param confId
     *            the conf id
     * @param mode
     *            the mode
     * @param viewType
     *            the view type
     */
    public SimpleRuleGroup(final ConfId confId, final MatchMode mode, final ViewType viewType) {
        super();
        this.confId = confId;
        this.mode = mode;
        this.viewType = viewType;
        rules = new ArrayList<Rule>();
    }

    /**
     * Append.
     *
     * @param rule
     *            the rule
     */
    public void append(final Rule rule) {
        rules.add(rule);
    }

    @Override
    public void inject(final Context context) {
        stateManager = context.stateManager();
        for (final Rule rule : rules) {
            final BaseJythonRule jythonRule = (BaseJythonRule) rule;
            jythonRule.setEnvBinder(context.environmentBinder());
            jythonRule.setEventBinder(context.eventBinder());
            jythonRule.setForwarder(context.forwarder());
        }
    }

    @Override
    public void init() {
        for (final Rule rule : rules) {
            rule.init();
        }
    }

    @Override
    public void shutdown() {
        for (final Rule rule : rules) {
            rule.shutdown();
        }
    }

    @Override
    public ConfId getConfId() {
        return confId;
    }

    @Override
    public ViewType getInputKind() {
        return viewType;
    }

    @Override
    public void evaluate(final View view) {
        switch (mode) {
            case ALL:
                for (final Rule rule : rules) {
                    final State state = stateManager.get(rule.getId());
                    rule.evaluate(view, state);
                }
                break;
            case STOP_ON_FIRST_MATCH:
            default:
                throw new NotYetImplementedException("Match mode " + mode.toString() + " is not supported yet. Only ALL mode is supported.");
        }
    }

    @Override
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        return "SimpleRuleGroup [confId=" + confId + ", mode=" + mode + ", viewType=" + viewType + "]";
    }
}
