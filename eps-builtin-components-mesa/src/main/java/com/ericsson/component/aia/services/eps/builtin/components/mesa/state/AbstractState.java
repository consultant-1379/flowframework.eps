package com.ericsson.component.aia.services.eps.builtin.components.mesa.state;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * The Abstarct Class AbstractState implements {@link State}.
 */
public abstract class AbstractState implements State {

    private final Id identifier;

    /**
     * Instantiates a new abstract state.
     *
     * @param identifier
     *            the id
     */
    public AbstractState(final Id identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public final Id getId() {
        return identifier;
    }

    @Override
    public abstract String toString();
}
