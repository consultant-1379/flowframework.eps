package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import java.util.ArrayList;
import java.util.List;

import org.python.core.PyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.BaseJythonRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.RuleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.RuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.SimpleRuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateful.StatefulRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateless.StatelessRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;

/**
 * Java representation of core policy XML descriptor.
 */
public final class SimplePolicy implements Policy {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // this ID does not come from XML; it is system assigned
    private final Id id;
    private final Name name;
    private final List<RuleGroupSpec> ruleGroupSpecs;

    private Context context;

    /**
     * Instantiates a new simple policy.
     *
     * @param id
     *            the id
     * @param name
     *            the name
     */
    public SimplePolicy(final Id id, final Name name) {
        super();
        this.id = id;
        this.name = name;
        ruleGroupSpecs = new ArrayList<RuleGroupSpec>();
    }

    /**
     * Append the rule group spec.
     *
     * @param ruleGroupSpec
     *            the rule group spec
     */
    public void append(final RuleGroupSpec ruleGroupSpec) {
        ruleGroupSpecs.add(ruleGroupSpec);
    }

    @Override
    public void inject(final Context context) {
        this.context = context;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public List<RuleGroupSpec> getRuleGroupSpecs() {
        return ruleGroupSpecs;
    }

    @Override
    public List<Pair<Guard, RuleGroup>> instantiate(final PolicyConf policyConf) {
        final List<Pair<Guard, RuleGroup>> result = new ArrayList<Pair<Guard, RuleGroup>>();
        for (final Conf conf : policyConf.getConfs()) {
            final Pair<Guard, RuleGroup> pair = instantiate(conf);
            result.add(pair);
        }
        return result;
    }

    private Pair<Guard, RuleGroup> instantiate(final Conf conf) {
        final Guard guard = conf.getGuard();
        final RuleGroup ruleGroup = instantiateRuleGroup(conf.getConfId(), findRuleGroupDef(conf.getConfId().getTargetRuleGroupId()));
        return new Pair<Guard, RuleGroup>(guard, ruleGroup);
    }

    private RuleGroup instantiateRuleGroup(final ConfId confId, final RuleGroupSpec ruleGroupSpec) {
        final SimpleRuleGroup result = new SimpleRuleGroup(confId, ruleGroupSpec.getMatchMode(), ruleGroupSpec.getSubscription().getViewType());
        result.inject(context);
        for (final RuleSpec ruleSpec : ruleGroupSpec.getRules()) {
            result.append(instantiateRule(confId, ruleSpec));
        }
        return result;
    }

    private com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.Rule instantiateRule(final ConfId confId, final RuleSpec ruleSpec) {
        BaseJythonRule rule = null;
        switch (ruleSpec.getType()) {
            case STATEFUL:
                rule = new StatefulRule(new RuleId(ruleSpec.getId(), confId), ruleSpec.getName(), ruleSpec.isFlexiJythonNames());
                break;
            case STATELESS:
                rule = new StatelessRule(new RuleId(ruleSpec.getId(), confId), ruleSpec.getName(), ruleSpec.isFlexiJythonNames());
                break;
            default:
                throw new IllegalArgumentException("Unknown rule type '" + ruleSpec.getType() + "'");
        }
        log.info("Created rule '{}' of type '{}' from definition '{}'", name, ruleSpec.getType(), ruleSpec.getReference());
        final String text = Util.readTextFileFully(ruleSpec.getReference());

        final PyCode code = context.scriptCompiler().compile(text);
        rule.setCode(code);
        rule.setEventBinder(context.eventBinder());
        rule.setEnvBinder(context.environmentBinder());
        rule.setForwarder(context.forwarder());
        rule.inject(context);

        for (final Param param : ruleSpec.getParams()) {
            rule.register(param.getName(), param.getValue());
            log.info("Registered parameter '{}' with default value '{}'", param.getName(), param.getValue());
        }
        return rule;
    }

    private RuleGroupSpec findRuleGroupDef(final Id id) {
        final long value = ((SimpleId) id).getId();
        for (final RuleGroupSpec ruleGroupSpec : ruleGroupSpecs) {
            if (value == ((SimpleId) ruleGroupSpec.getId()).getId()) {
                return ruleGroupSpec;
            }
        }
        throw new IllegalArgumentException("Unable to find rule group spec with ID " + id);
    }
}
