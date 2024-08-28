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
package com.ericsson.component.aia.services.eps.core.execution;

import java.util.Set;
import java.util.TreeSet;

/**
 * The Class EventFlow.
 */
public class EventFlow {

    /** The input component id. */
    private String inputComponentId;

    /** The output component identifiers. */
    private final Set<String> outputComponentIdentifiers = new TreeSet<>();

    /**
     * Gets the input component id.
     *
     * @return the inputAddress
     */
    public String getInputComponentId() {
        return inputComponentId;
    }

    /**
     * Sets the input component id.
     *
     * @param inputAddress
     *            the inputAddress to set
     */
    public void setInputComponentId(final String inputAddress) {
        if ((inputAddress == null) || inputAddress.isEmpty()) {
            throw new IllegalArgumentException("Input id must not be null or empty");
        }
        inputComponentId = inputAddress;
    }

    /**
     * Gets the output component identifiers.
     *
     * @return the outputAddress
     */
    public Set<String> getOutputComponentIdentifiers() {
        return outputComponentIdentifiers;
    }

    /**
     * Adds the output component identifier.
     *
     * @param outputComponentId
     *            the output component id
     */
    public void addOutputComponentIdentifier(final String outputComponentId) {
        if ((outputComponentId == null) || outputComponentId.trim().isEmpty()) {
            throw new IllegalArgumentException("output component id must not be null or empty");
        }
        outputComponentIdentifiers.add(outputComponentId);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((inputComponentId == null) ? 0 : inputComponentId.hashCode());
        result = (prime * result) + ((outputComponentIdentifiers == null) ? 0 : outputComponentIdentifiers.hashCode());
        return result;
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        return equalsFlow((EventFlow) obj);
    }

    private boolean equalsFlow(final EventFlow other) {
        if (inputComponentId == null) {
            if (other.inputComponentId != null) {
                return false;
            }
        } else if (!inputComponentId.equals(other.inputComponentId)) {
            return false;
        }
        if (outputComponentIdentifiers == null) {
            if (other.outputComponentIdentifiers != null) {
                return false;
            }
        } else if (!outputComponentIdentifiers.equals(other.outputComponentIdentifiers)) {
            return false;
        }
        return true;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "EventFlow [inputComponentId=" + inputComponentId + ", outputComponentIdentifiers=" + outputComponentIdentifiers + "]";
    }

}
