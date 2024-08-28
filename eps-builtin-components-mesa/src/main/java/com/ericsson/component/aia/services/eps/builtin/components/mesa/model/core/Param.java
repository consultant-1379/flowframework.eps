package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Type;

/**
 * Two params are equal if they have the same name.
 */
public interface Param extends Comparable<Param> {

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the value.
     *
     * @return the value
     */
    Object getValue();

    /**
     * Gets the type.
     *
     * @return the type
     */
    Type getType();
}
