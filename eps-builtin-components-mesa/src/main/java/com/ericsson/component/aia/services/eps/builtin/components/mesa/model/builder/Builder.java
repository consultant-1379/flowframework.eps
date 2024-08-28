package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder;

import java.net.URI;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.Model;

/**
 * Builds java representation of model from given input, which is most likely XML.
 */
public interface Builder {

    /**
     * Builds the ${@link Model}.
     *
     * @param model
     *            the model
     * @return the model
     */
    Model build(URI model);
}
