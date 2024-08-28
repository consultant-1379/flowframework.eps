/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.util;

import java.util.regex.Pattern;

import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;

/**
 *
 * Match a {@link ModelInfo} or Urn string against a ModelInfo set previously,
 * manage wildcards.
 *
 * @author epiemir
 *
 */
public class UrnMatcher {

    private Pattern schPattern;
    private Pattern nsPattern;
    private Pattern namePattern;
    private Pattern versionPattern;

    /**
     *
     * @param watchedFlows
     *            the configured EPS modeled flows to monitor
     */
    public void set(final ModelInfo watchedFlows) {

        final String WILDCARD = "*";

        if (!watchedFlows.getSchema().equals(WILDCARD)) {
            schPattern = Pattern.compile(convertToRegex(watchedFlows
                    .getSchema()));
        }
        if (!watchedFlows.getNamespace().equals(WILDCARD)) {
            nsPattern = Pattern.compile(convertToRegex(watchedFlows
                    .getNamespace()));
        }
        if (!watchedFlows.getName().equals(WILDCARD)) {
            namePattern = Pattern
                    .compile(convertToRegex(watchedFlows.getName()));
        }
        if (!watchedFlows.getVersion().toString().equals(WILDCARD)) {
            versionPattern = Pattern.compile(watchedFlows.getVersion()
                    .toString());
        }
    }

    private String convertToRegex(final String rawString) {
        final String ASTERIX = "*";
        final String REGEX_WILDCARD = ".*";

        final String[] tokens = rawString.split("\\*", -1);
        final StringBuilder stringBuilder = new StringBuilder();

        if (rawString.equals(ASTERIX)) {
            return REGEX_WILDCARD;
        }

        for (int i = 0; i < (tokens.length - 1); i++) {
            final String patternQuote = Pattern.quote(tokens[i]);
            stringBuilder.append(patternQuote);
            stringBuilder.append(".*");
        }
        final String patternQuote = Pattern.quote(tokens[tokens.length - 1]);
        stringBuilder.append(patternQuote);

        return stringBuilder.toString();
    }

    /**
     *
     * @param urn
     *            a String of the urn to match
     * @return true if urn matches
     */
    public boolean match(final String urn) {
        return match(ModelInfo.fromUrn(urn));
    }

    /**
     *
     * @param flowUrn
     *            the {@link ModelInfo} to test for a match
     * @return true if the provided flowUrn matches the {@link #set(ModelInfo)}
     */
    public boolean match(final ModelInfo flowUrn) {
        if ((schPattern != null)
                && !schPattern.matcher(flowUrn.getSchema()).matches()) {
            return false;
        }
        if ((nsPattern != null)
                && !nsPattern.matcher(flowUrn.getNamespace()).matches()) {
            return false;
        }
        if ((namePattern != null)
                && !namePattern.matcher(flowUrn.getName()).matches()) {
            return false;
        }
        if ((versionPattern != null)
                && !versionPattern.matcher(flowUrn.getVersion().toString())
                        .matches()) {
            return false;
        }
        return true;
    }

}
