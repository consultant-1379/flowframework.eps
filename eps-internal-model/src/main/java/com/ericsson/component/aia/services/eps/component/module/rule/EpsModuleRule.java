/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.component.module.rule;

/**
 * Represents rule resource or inline rule.
 *
 * @author eborziv
 * @see EpsRulesModuleComponent
 *
 */
public class EpsModuleRule {

    private final String ruleText;

    /**
     * Instantiates a new eps module rule.
     *
     * @param ruleText
     *            the rule text
     */
    public EpsModuleRule(final String ruleText) {
        if ((ruleText == null) || ruleText.isEmpty()) {
            throw new IllegalArgumentException("Rule text must not be null or empty");
        }
        this.ruleText = ruleText;
    }

    /**
     * Gets the rule text.
     *
     * @return the ruleText
     */
    public String getRuleText() {
        return ruleText;
    }

}
