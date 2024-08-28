package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

/**
 * The Enum MatchMode. possible values are: ALL, STOP_ON_FIRST_MATCH.
 */
public enum MatchMode {

    ALL, STOP_ON_FIRST_MATCH, ;

    /**
     * Parses the match mode from string.
     *
     * @param str
     *            the str
     * @return the match mode
     */
    public static MatchMode parse(final String str) {
        for (final MatchMode mode : values()) {
            if (mode.name().equalsIgnoreCase(str)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unable to parse match mode from string '" + str + "'");
    }
}
