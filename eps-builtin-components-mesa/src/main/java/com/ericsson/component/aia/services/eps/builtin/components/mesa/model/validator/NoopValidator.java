package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator;

import java.util.Collections;
import java.util.List;

/**
 * The Class NoopValidator, it implements ${@link Validator}.
 */
public final class NoopValidator implements Validator {

    @Override
    public List<Message> validate(final Object model) {
        return Collections.emptyList();
    }
}
