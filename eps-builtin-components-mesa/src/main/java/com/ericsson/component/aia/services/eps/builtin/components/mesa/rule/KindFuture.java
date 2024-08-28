package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

// leaving this in since it might be needed in the future evolutions of MESA
// Now it's no more profiled out for checkstyle, and renamed in KindFuture
/**
 * The Enum KindFuture. Possible values are: GENERAL, SIMPLE, OCCURRENCES, CONTINUOUS_OCCURRENCES
 */
public enum KindFuture {

    GENERAL, SIMPLE, OCCURRENCES, CONTINUOUS_OCCURRENCES, ;

    /**
     * Parses the KindFuture from string.
     *
     * @param str
     *            the str
     * @return the kindFuture
     */
    public static KindFuture parse(final String str) {
        for (final KindFuture kindFuture : values()) {
            if (kindFuture.name().equalsIgnoreCase(str)) {
                return kindFuture;
            }
        }
        throw new IllegalArgumentException("Unable to parse rule kindFuture from string '" + str + "'");
    }
}