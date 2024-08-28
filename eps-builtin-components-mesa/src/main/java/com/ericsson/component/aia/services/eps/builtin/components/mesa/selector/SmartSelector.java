package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Pair;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.group.RuleGroup;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard.Guard;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * TODO Better concurrency.
 *
 * @see Selector
 */
public final class SmartSelector implements Selector {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // first key is core ID; second key is rule group ID;
    // each different configuration will have different rule group(s);
    private final Map<Id, Map<Id, List<Pair<RuleGroup, Guard>>>> uberMap = new ConcurrentHashMap<Id, Map<Id, List<Pair<RuleGroup, Guard>>>>();

    @Override
    public synchronized void unregister(final ConfId confId) {
        log.info("Preparing to unregister {}", confId);

        final Map<Id, List<Pair<RuleGroup, Guard>>> entry = uberMap.get(confId.getPolicyCoreId());
        if ((entry == null) || entry.isEmpty()) {
            return;
        }
        final List<Pair<RuleGroup, Guard>> groups = entry.get(confId.getTargetRuleGroupId());
        if ((groups == null) || groups.isEmpty()) {
            return;
        }

        final Iterator<Pair<RuleGroup, Guard>> itr = groups.iterator();
        while (itr.hasNext()) {
            final RuleGroup group = itr.next().getFirst();
            if (group.getConfId().getPolicyConfId().equals(confId.getPolicyConfId())) {
                itr.remove();
                log.info("Unregistered {} from selector", group.getConfId());
            }
        }
    }

    @Override
    public synchronized void register(final Guard guard, final RuleGroup ruleGroup) {
        if (!guard.getConfId().equals(ruleGroup.getConfId())) {
            throw new IllegalArgumentException("Conf IDs don't match for guard and rule group: " + guard.getConfId() + ", " + ruleGroup.getConfId());
        }
        log.info("Preparing to register {}", guard.getConfId());
        final Id policyCoreId = ruleGroup.getConfId().getPolicyCoreId();
        Map<Id, List<Pair<RuleGroup, Guard>>> entry = uberMap.get(policyCoreId);
        if (entry == null) {
            entry = new HashMap<Id, List<Pair<RuleGroup, Guard>>>();
            final Map<Id, List<Pair<RuleGroup, Guard>>> returned = uberMap.put(policyCoreId, entry);
            if (returned != null) {
                log.error("A previous entry for core ID {} was found, the system may now encounter race conditions", policyCoreId);
                throw new IllegalArgumentException("Core Policies need to have distinct IDs duplicate Id");
            }
        }

        final Id ruleGroupId = ruleGroup.getConfId().getTargetRuleGroupId();
        List<Pair<RuleGroup, Guard>> groups = entry.get(ruleGroupId);
        if (groups == null) {
            groups = new ArrayList<Pair<RuleGroup, Guard>>();
            entry.put(ruleGroupId, groups);
        }

        for (final Pair<RuleGroup, Guard> pair : groups) {
            if (pair.getFirst().getConfId().equals(ruleGroup.getConfId())) {
                throw new IllegalArgumentException("New rule group with '" + ruleGroup.getConfId() + "' conflicts with existing rule group conf ID");
            }
        }
        groups.add(new Pair<RuleGroup, Guard>(ruleGroup, guard));
    }

    @Override
    public synchronized List<RuleGroup> select(final View view) {
        final Id policyCore = view.getPolicyCoreId();
        final Id groupId = view.getRuleGroupId();

        if (log.isTraceEnabled()) {
            log.trace("Preparing to select rule groups for policy core {} and rule group ID {}", policyCore, groupId);
        }

        final Map<Id, List<Pair<RuleGroup, Guard>>> submap = uberMap.get(policyCore);
        if ((submap == null) || submap.isEmpty()) {
            log.warn("No match found for policy core {}", policyCore);
            return Collections.emptyList();
        }
        final List<Pair<RuleGroup, Guard>> list = submap.get(groupId);
        if ((list == null) || list.isEmpty()) {
            log.warn("No match found for group ID {}", groupId);
            return Collections.emptyList();
        }

        final List<RuleGroup> result = new LinkedList<RuleGroup>();
        for (final Pair<RuleGroup, Guard> pair : list) {
            if (pair.getSecond().mayPass(view)) {
                result.add(pair.getFirst());
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Found {} rule groups for policy core {} and rule group ID {}", result.size(), policyCore, groupId);
        }

        return result;
    }
}
