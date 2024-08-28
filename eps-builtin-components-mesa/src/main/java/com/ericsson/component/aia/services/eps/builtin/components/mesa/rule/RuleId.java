package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;

/**
 * TODO might require performance optimization for hashcode and equals etc.
 */
public final class RuleId implements Id {

    private final Id ruleId;
    private final ConfId confId;

    /**
     * Instantiates a new rule id.
     *
     * @param ruleId
     *            the rule id
     * @param confId
     *            the conf id
     */
    public RuleId(final Id ruleId, final ConfId confId) {
        super();
        this.ruleId = ruleId;
        this.confId = confId;
    }

    /**
     * Gets the rule id.
     *
     * @return the rule id
     */
    public Id getRuleId() {
        return ruleId;
    }

    /**
     * Gets the conf id.
     *
     * @return the conf id
     */
    public ConfId getConfId() {
        return confId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((confId == null) ? 0 : confId.hashCode());
        result = (prime * result) + ((ruleId == null) ? 0 : ruleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return equalsRuleId((RuleId) obj);
    }

    private boolean equalsRuleId(final RuleId other) {
        if (confId == null) {
            if (other.confId != null) {
                return false;
            }
        } else if (!confId.equals(other.confId)) {
            return false;
        }
        if (ruleId == null) {
            if (other.ruleId != null) {
                return false;
            }
        } else if (!ruleId.equals(other.ruleId)) {
            return false;
        }
        return true;
    }
}
