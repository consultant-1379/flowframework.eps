package com.ericsson.component.aia.services.eps.builtin.components.mesa.view;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * The Class AbstractView implements {@link View}.
 */
public abstract class AbstractView implements View {

    private final Id policyCoreId;
    private final Id ruleGroupId;

    /**
     * Instantiates a new abstract view.
     *
     * @param policyCoreId
     *            the policy core id
     * @param ruleGroupId
     *            the rule group id
     */
    public AbstractView(final Id policyCoreId, final Id ruleGroupId) {
        super();
        this.policyCoreId = policyCoreId;
        this.ruleGroupId = ruleGroupId;
    }

    @Override
    public final Id getPolicyCoreId() {
        return policyCoreId;
    }

    @Override
    public final Id getRuleGroupId() {
        return ruleGroupId;
    }
}
