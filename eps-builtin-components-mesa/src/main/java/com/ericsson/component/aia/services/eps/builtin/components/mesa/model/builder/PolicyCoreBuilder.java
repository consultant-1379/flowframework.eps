package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder;

import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Name;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Type;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.EventRef;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Model;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Subscription;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.jaxb.core.generated.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.MatchMode;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.RuleOutput;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View.ViewType;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Param;
/**
 * The Class PolicyCoreBuilder extends {@link AbstractBuilder}.
 */
public final class PolicyCoreBuilder extends AbstractBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Instantiates a new policy core builder.
     *
     * @param context
     *            the context
     */
    public PolicyCoreBuilder(final Context context) {
        super(context);
    }

    @Override
    public Model build(final URI model) {
        log.info("Preparing to JAXB-parse {}", model);

        final PolicySpecification policySpec = (PolicySpecification) buildJaxbModel(model, PolicySpecification.class);
        log.info("Processing policy '{}' from namespace '{}' with version '{}'", policySpec.getGeneral().getName(), policySpec.getGeneral()
                .getNamespace(), policySpec.getGeneral().getVersion());
        validate(policySpec);

        final Name name = new Name(policySpec.getGeneral().getName(), policySpec.getGeneral().getNamespace(), policySpec.getGeneral().getVersion());

        final SimplePolicy result = new SimplePolicy(getContext().idSource().newId(), name);
        for (final RuleGroup group : policySpec.getRules().getRuleGroup()) {
            result.append(buildRuleGroupSpec(group));
        }
        return result;
    }

    private RuleGroupSpec buildRuleGroupSpec(final RuleGroup ruleGroup) {
        final SimpleRuleGroupSpec result = new SimpleRuleGroupSpec(new SimpleId(ruleGroup.getId().longValue()), MatchMode.parse(ruleGroup
                .getMatchMode()), buildSubscription(ruleGroup.getInput()));
        for (final Rule rule : ruleGroup.getRule()) {
            final SimpleRuleSpec ruleSpec = new SimpleRuleSpec(new SimpleId(rule.getId().longValue()), rule.getName(),
                    com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.Rule.Type.parseFrom(rule.getType()), RuleOutput.parse(rule
                            .getOutput()), rule.isFlexiableJythonNameBinding(), rule.getReference());
            for (final com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Param param : buildParams(rule.getParams())) {
                ruleSpec.append(param);
            }
            result.append(ruleSpec);
        }
        return result;
    }

    private Subscription buildSubscription(final Input input) {
        final Subscription result = new Subscription(ViewType.parse(input.getEvents().getView()));
        for (final Event event : input.getEvents().getEvent()) {
            result.append(new EventRef(event.getName(), event.getNamespace(), event.getVersion()));
        }
        return result;
    }

    private SortedSet<com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Param> buildParams(final Params params) {
        final SortedSet<Param> set = new TreeSet<com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Param>();
        for (final com.ericsson.component.aia.services.eps.builtin.components.mesa.model.jaxb.core.generated.Param param : params.getParam()) {
            set.add(new SimpleParam(param.getName(), decodeDefaultValue(param), Type.parse(param.getType())));
        }
        return set;
    }

    private Object decodeDefaultValue(final com.ericsson.component.aia.services.eps.builtin.components.mesa.model.jaxb.core.generated.Param param) {
        final Type type = Type.parse(param.getType());
        Object value = null;
        switch (type) {
            case BOOLEAN:
                value = Boolean.parseBoolean(param.getDefault());
                break;
            case SHORT:
                value = Short.parseShort(param.getDefault());
                break;
            case INT:
                value = Integer.parseInt(param.getDefault());
                break;
            case LONG:
                value = Long.parseLong(param.getDefault());
                break;
            case FLOAT:
                value = Float.parseFloat(param.getDefault());
                break;
            case DOUBLE:
                value = Double.parseDouble(param.getDefault());
                break;
            default:
                throw new IllegalArgumentException("Unknown parameter type " + type + "'");
        }
        return value;
    }
}
