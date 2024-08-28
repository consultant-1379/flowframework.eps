package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * Links core to conf. Has core ID, target rule group ID, policy conf ID and conf ID.
 */
public final class ConfId implements Id {

    private final Id policyCoreId;
    private final Id targetRuleGroupId;
    private final Id policyConfId;
    private final Id confId;

    /**
     * Instantiates a new conf id.
     *
     * @param policyCoreId
     *            the policy core id
     * @param targetRuleGroupId
     *            the target rule group id
     * @param policyConfId
     *            the policy conf id
     * @param confId
     *            the conf id
     */
    public ConfId(final Id policyCoreId, final Id targetRuleGroupId, final Id policyConfId, final Id confId) {
        super();
        this.policyCoreId = policyCoreId;
        this.targetRuleGroupId = targetRuleGroupId;
        this.policyConfId = policyConfId;
        this.confId = confId;
    }

    /**
     * Gets the policy core id.
     *
     * @return the policy core id
     */
    public Id getPolicyCoreId() {
        return policyCoreId;
    }

    /**
     * Gets the target rule group id.
     *
     * @return the target rule group id
     */
    public Id getTargetRuleGroupId() {
        return targetRuleGroupId;
    }

    /**
     * Gets the policy conf id.
     *
     * @return the policy conf id
     */
    public Id getPolicyConfId() {
        return policyConfId;
    }

    /**
     * Gets the conf id.
     *
     * @return the conf id
     */
    public Id getConfId() {
        return confId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((confId == null) ? 0 : confId.hashCode());
        result = (prime * result) + ((policyConfId == null) ? 0 : policyConfId.hashCode());
        result = (prime * result) + ((policyCoreId == null) ? 0 : policyCoreId.hashCode());
        result = (prime * result) + ((targetRuleGroupId == null) ? 0 : targetRuleGroupId.hashCode());
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
        final ConfId other = (ConfId) obj;
        if (confId == null) {
            if (other.confId != null) {
                return false;
            }
        } else if (!confId.equals(other.confId)) {
            return false;
        }
        if (policyConfId == null) {
            if (other.policyConfId != null) {
                return false;
            }
        } else if (!policyConfId.equals(other.policyConfId)) {
            return false;
        }
        if (policyCoreId == null) {
            if (other.policyCoreId != null) {
                return false;
            }
        } else if (!policyCoreId.equals(other.policyCoreId)) {
            return false;
        }
        if (targetRuleGroupId == null) {
            if (other.targetRuleGroupId != null) {
                return false;
            }
        } else if (!targetRuleGroupId.equals(other.targetRuleGroupId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConfId [policyCoreId=" + policyCoreId + ", targetRuleGroupId=" + targetRuleGroupId + ", policyConfId=" + policyConfId + ", confId="
                + confId + "]";
    }
}
