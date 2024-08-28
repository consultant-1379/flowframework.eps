package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * Policy configuration may specify criteria which enforces that only views with certain field values are passed to policy.
 */
public interface Guard {

    /**
     * The Enum Mode.
     */
    public static enum Mode {
        ALWAYS_PASS, SET, PATTERN, COMPOSITE, ;

        /**
         * Parses the.
         *
         * @param str
         *            the str
         * @return the mode
         */
        public static Mode parse(final String str) {
            for (final Mode kind : values()) {
                if (kind.name().equalsIgnoreCase(str)) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("Unable to parse guard mode from '" + str + "'");
        }
    }

    /**
     * Gets the conf id.
     *
     * @return the conf id
     */
    ConfId getConfId();

    /**
     * Gets the tenant.
     *
     * @return the tenant
     */
    String getTenant();

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    Mode getMode();

    /**
     * May pass.
     *
     * @param view
     *            the view
     * @return true, if successful
     */
    boolean mayPass(View view);
}
