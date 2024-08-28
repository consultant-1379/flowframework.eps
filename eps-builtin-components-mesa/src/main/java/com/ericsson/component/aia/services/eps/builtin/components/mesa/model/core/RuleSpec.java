package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import java.util.SortedSet;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.RuleOutput;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.Rule.Type;

/**
 * The Interface RuleSpec.
 */
public interface RuleSpec {

    /**
     * Gets the id.
     *
     * @return the id
     */
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
     * Gets the output.
     *
     * @return the output {@link RuleOutput}
     */
    RuleOutput getOutput();

    /**
     * Checks if is flexi jython names.
     *
     * @return true, if is flexi jython names
     */
    boolean isFlexiJythonNames();

    /**
     * Gets the reference.
     *
     * @return the reference
     */
    String getReference();

    /**
     * Gets the params.
     *
     * @return the params
     */
    SortedSet<Param> getParams();
}
