package com.ericsson.component.aia.services.eps.builtin.components.mesa.state;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * State not persisted anywhere.
 */
public final class InMemoryState extends AbstractState {

    private final Map<String, Object> map;

    /**
     * Instantiates a new in memory state.
     *
     * @param ruleId
     *            the rule id
     */
    public InMemoryState(final Id ruleId) {
        super(ruleId);
        map = new HashMap<String, Object>();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean hasKey(final String key) {
        return map.containsKey(key);
    }

    @Override
    public Object get(final String key) {
        return map.get(key);
    }

    @Override
    public Object put(final String key, final Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(final String key) {
        return map.remove(key);
    }

    @Override
    public String toString() {
        return "InMemoryState [map=" + map + ", ruleId=" + getId() + "]";
    }
}
