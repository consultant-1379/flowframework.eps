package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

/**
 * The Enum Type. Possible values are: BOOLEAN, SHORT, INT, LONG, FLOAT, DOUBLE
 */
public enum Type {

    BOOLEAN, SHORT, INT, LONG, FLOAT, DOUBLE, ;

    /**
     * Parses the enum as string.
     *
     * @param str
     *            the str
     * @return the type
     */
    public static Type parse(final String str) {
        for (final Type type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unable to parse type from '" + str + "'");
    }
}
