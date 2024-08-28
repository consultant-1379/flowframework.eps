package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.IdAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.StateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.common.Lifecycle;

/**
 * All rules with a policy have to be of same type.
 */
public interface Rule extends Lifecycle, IdAware {

    /**
     * The Enum Type.
     */
    public static enum Type {
        STATELESS, STATEFUL, ;

        /**
         * Parses the Type from string.
         *
         * @param str
         *            the str
         * @return the type
         */
        public static Type parseFrom(final String str) {
            for (final Type type : values()) {
                if (type.name().equalsIgnoreCase(str)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unable to parse rule type from '" + str + "'");
        }
    }

    /**
     * Returns {@link RuleId}. This ID is used to manager state within {@link StateManager}.
     *
     * @return the id
     */
    @Override
    Id getId();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the type.
     *
     * @return the type
     */
    Type getType();

    /**
     * Used to register variables.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    void register(String name, Object value);

    /**
     * Evaluate.
     *
     * @param view
     *            the view
     * @param state
     *            the state
     */
    void evaluate(View view, State state);
}
