package com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.InMemoryState;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;

/**
 * The Class SimpleStateManager implements {@link StateManager}.
 */
public final class SimpleStateManager implements StateManager {

    private final Map<Id, State> states;

    private Context context;

    /**
     * Instantiates a new simple state manager.
     */
    public SimpleStateManager() {
        states = new ConcurrentHashMap<Id, State>();
    }

    @Override
    public void inject(final Context context) {
        this.context = context;
    }

    @Override
    public synchronized State get(final Id stateId) {
        State state = states.get(stateId);
        if (state == null) {
            state = new InMemoryState(stateId);
        }
        return state;
    }

    @Override
    public synchronized void put(final State state) {
        // TODO this is basic the state will be overwritten each time
        states.put(state.getId(), state);
        context.stats().countIncrement("StatesStoredIncludingUpdates");
    }
}
