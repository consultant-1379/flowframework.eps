package com.ericsson.component.aia.services.eps.builtin.components.mesa.manager;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.Conf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.PolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Policy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.RuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * Governs the deployment of policies and ensures that relevant EPL's are deployed and listeners are attached
 *
 * @see Manager
 */
public final class SimpleManager implements Manager {

    private static SimpleManager instance;

    private final Logger log = LoggerFactory.getLogger(getClass());

    // key is name of core policy
    private final Map<Id, Policy> deployedCorePolicies = new HashMap<Id, Policy>();

    // key is ID of core policy
    private final Map<Id, List<PolicyConf>> deployedConfPolicies = new HashMap<Id, List<PolicyConf>>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private Context context;

    @Override
    public void inject(final Context context) {
        this.context = context;
    }

    @Override
    public void deployCore(final Policy policy) throws MesaDeploymentException {
        lock.writeLock().lock();
        try {
            final Policy old = deployedCorePolicies.get(policy.getId());
            if (old != null) {
                throw new IllegalArgumentException("Duplicate core policy '" + policy.getName() + "'");
            }
            context.esperHandler().deployCore(policy);
            deployedCorePolicies.put(policy.getId(), policy);
            context.stats().countIncrement("CorePoliciesDeployed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void undeployCore(final Policy policy) throws MesaDeploymentException {
        lock.writeLock().lock();
        try {
            log.info("Preparing to undeploy core policy {}", policy.getName());
            if (deployedCorePolicies.get(policy.getId()) == null) {
                log.warn("Unknown core policy '{}'", policy.getName());
                return;
            }
            deployedCorePolicies.remove(policy.getId());
            final List<PolicyConf> confs = deployedConfPolicies.remove(policy.getId());
            if (confs != null) {
                log.info("Undeployed {} policy configurations for core policy ", confs.size(), policy.getName());
            }
            context.esperHandler().undeployCore(policy);
            context.stats().countDecrement("CorePoliciesDeployed");
            log.info("Successfully undeployed core policy {}", policy.getName());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deployConf(final PolicyConf conf) {
        lock.writeLock().lock();
        try {
            log.info("Preparing to deploy conf policy {} for core policy {}", conf.getPolicyConfId(), conf.getPolicyCoreId());
            final Policy policy = deployedCorePolicies.get(conf.getPolicyCoreId());
            if (policy == null) {
                throw new IllegalArgumentException("Unknown core policy " + conf.getPolicyCoreId() + "'");
            }
            List<PolicyConf> confs = deployedConfPolicies.get(conf.getPolicyConfId());
            if (confs == null) {
                confs = new ArrayList<PolicyConf>();
                deployedConfPolicies.put(conf.getPolicyConfId(), confs);
            }
            for (final PolicyConf c : confs) {
                if (c.getPolicyConfId().equals(conf.getPolicyConfId())) {
                    throw new IllegalArgumentException("Duplicate conf policy " + conf.getPolicyConfId() + " for core policy "
                            + conf.getPolicyCoreId());
                }
            }
            confs.add(conf);

            final List<Pair<Guard, RuleGroup>> list = policy.instantiate(conf);
            for (final Pair<Guard, RuleGroup> pair : list) {
                context.selector().register(pair.getFirst(), pair.getSecond());
            }
            context.stats().countIncrement("ConfigurationPoliciesDeployed");
            log.info("Successfully deployed conf policy {} for core policy {}", conf.getPolicyConfId(), conf.getPolicyCoreId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void undeployConf(final Id id, final Name name) {
        lock.writeLock().lock();
        try {
            log.info("Preparing to undeploy conf policy {} for core policy {}", id, name);
            final List<PolicyConf> confs = deployedConfPolicies.get(id);
            if ((confs == null) || confs.isEmpty()) {
                log.warn("No conf policy {} found for core policy {}", id, name);
                return;
            }
            final Iterator<PolicyConf> it = confs.iterator();
            while (it.hasNext()) {
                final PolicyConf conf = it.next();
                if (conf.getPolicyConfId().equals(id)) {
                    it.remove();
                    for (final Conf c : conf.getConfs()) {
                        context.selector().unregister(c.getConfId());
                    }
                }
            }
            log.info("Successfully undeployed conf policy {} for core policy {}", id, name);
            context.stats().countDecrement("ConfigurationPoliciesDeployed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void on(final View view) {
        lock.readLock().lock();
        try {
            // TODO synchronization performance etc
            final List<RuleGroup> ruleGroups = context.selector().select(view);
            for (final RuleGroup ruleGroup : ruleGroups) {
                ruleGroup.evaluate(view);
                context.stats().countIncrement("EvaluatedViews");
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    // TODO confirm singleton solution was per JVM over per server/cluster JVM's
    // (might depend on zookeper/storm solution?)
    /**
     * Gets or create {@link SimpleManager}.
     *
     * @return the manager
     */
    public static synchronized SimpleManager getOrCreateManager() {
        if (instance == null) {
            instance = new SimpleManager();
        }
        return instance;
    }
}
