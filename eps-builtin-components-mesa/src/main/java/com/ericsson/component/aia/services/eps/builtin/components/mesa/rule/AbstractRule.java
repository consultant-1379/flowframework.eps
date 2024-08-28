package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * The Class AbstractRule.
 */
public abstract class AbstractRule implements Rule {

    private final Id identifier;
    private final String name;

    /**
     * Instantiates a new abstract rule.
     *
     * @param identifier
     *            the id
     * @param name
     *            the name
     */
    public AbstractRule(final Id identifier, final String name) {
        this.identifier = identifier;
        this.name = name;
    }

    @Override
    public final Id getId() {
        return identifier;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }
}
