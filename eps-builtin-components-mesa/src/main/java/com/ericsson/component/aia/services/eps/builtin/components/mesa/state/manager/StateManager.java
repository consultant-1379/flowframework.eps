package com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager;

import java.util.ServiceLoader;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Injectable;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;

/**
 * Implementations of state manager should be loaded via {@link ServiceLoader} API.
 */
public interface StateManager extends Injectable {

    /**
     * Create new state if it is missing.
     *
     * @param stateId
     *            the state id
     * @return the state
     */
    State get(Id stateId);

    /**
     * Stores new or updates existing saved state.
     *
     * @param state
     *            the state
     */
    void put(State state);
}
