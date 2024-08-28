package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator;

import java.util.List;

/**
 * Used to enforce semantics of given models.
 */
public interface Validator {

    /**
     * Validate the model.
     *
     * @param model
     *            the model
     * @return the list
     */
    List<Message> validate(Object model);
}
