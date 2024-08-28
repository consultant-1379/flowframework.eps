package com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id;

/**
 * Globally unique ID.
 */
public interface Id {

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @Override
    String toString();
}
