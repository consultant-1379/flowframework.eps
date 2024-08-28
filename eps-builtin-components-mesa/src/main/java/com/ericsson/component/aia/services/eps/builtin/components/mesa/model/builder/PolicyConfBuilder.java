package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder;

import java.net.URI;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.NotYetImplementedException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Model;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.jaxb.conf.generated.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard.Mode;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.jaxb.conf.generated.Properties;

/**
 * This class manages and builds java representation of model from given input, which is most likely XML.
 */
public final class PolicyConfBuilder extends AbstractBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Id policyCoreId;

    /**
     * Instantiates a new policy conf builder.
     *
     * @param context
     *            the context
     * @param policyCoreId
     *            the policy core id
     */
    public PolicyConfBuilder(final Context context, final Id policyCoreId) {
        super(context);
        this.policyCoreId = policyCoreId;
    }

    @Override
    public Model build(final URI model) {
        log.info("Preparing to JAXB-parse {}", model);
        final PolicyConfiguration policyConf = (PolicyConfiguration) buildJaxbModel(model, PolicyConfiguration.class);
        validate(policyConf);

        final Id policyConfId = new SimpleId(policyConf.getId().longValue());

        final SimplePolicyConf result = new SimplePolicyConf(policyCoreId, policyConfId);
        for (final Conf conf : buildConfs(policyConf, policyConfId)) {
            result.append(conf);
        }
        return result;
    }

    private List<Conf> buildConfs(final PolicyConfiguration policyConf, final Id policyConfId) {
        final List<Conf> confs = new ArrayList<Conf>();
        for (final Configuration configuration : policyConf.getConfigurations().getConfiguration()) {
            confs.add(buildConf(policyCoreId, policyConfId, configuration));
        }
        return confs;
    }

    private Conf buildConf(final Id policyCoreId, final Id policyConfId, final Configuration configuration) {

        final ConfId confId = new ConfId(policyCoreId,
                new SimpleId(Long.valueOf(getPropValue("target.rule.group", configuration.getProperties()))), policyConfId, new SimpleId(
                        configuration.getId().longValue()));

        final Guard guard = buildGuard(confId, configuration);

        return new SimpleConf(confId, configuration.getTenant(), guard);
    }

    private Guard buildGuard(final ConfId confId, final Configuration configuration) {
        final String tenant = configuration.getTenant();
        final Mode mode = Mode.parse(getPropValue("resources.mode", configuration.getProperties()));
        if (mode != Mode.SET) {
            throw new NotYetImplementedException("Only set guard mode is supported so far: " + mode);
        }

        final Set<?> set = buildSet(configuration.getProperties());

        final String ontology = (getPropValue("ontology.field", configuration.getProperties())).toString();
        final boolean allowed = Boolean.parseBoolean(getPropValue("resources.allowed", configuration.getProperties()));

        if ("none".equalsIgnoreCase(ontology)) {
            return new AlwaysPositiveGuard(confId, tenant);
        } else {
            return new ReflectionGuard<>(confId, tenant, allowed, set, ontology);
        }
    }

    private Set<?> buildSet(final Properties props) {

        final String line = getPropValue("resources.spec", props);
        final String[] parts = line.split(",");
        final String regex = "[0-9]+";

        if (parts[0].matches(regex)) {

            final Set<Long> result = new HashSet<Long>();
            for (int i = 0; i < parts.length; i++) {
                result.add(Long.parseLong(parts[i]));
            }
            return result;

        } else {
            final Set<String> result = new HashSet<String>();
            for (int i = 0; i < parts.length; i++) {
                result.add((parts[i]).toString());
            }
            return result;
        }
    }

    private String getPropValue(final String name, final Properties props) {
        for (final Property prop : props.getProperty()) {
            if (prop.getKey().equals(name)) {
                return prop.getValue();
            }
        }
        throw new IllegalStateException("Unable to find property '" + name + "'");
    }
}
