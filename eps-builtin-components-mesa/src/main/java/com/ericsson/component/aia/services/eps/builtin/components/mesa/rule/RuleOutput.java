package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

/**
 * Type of output rule can emit. Either stateless alert or stateful alarm.
 */
public enum RuleOutput {

    ALARM, ALERT, ;

    /**
     * Parses the rules as string.
     *
     * @param str
     *            the str
     * @return the rule output
     */
    public static RuleOutput parse(final String str) {
        for (final RuleOutput output : values()) {
            if (output.name().equalsIgnoreCase(str)) {
                return output;
            }
        }
        throw new IllegalArgumentException("Unable to parse rule output type from string '" + str + "'");
    }
}
