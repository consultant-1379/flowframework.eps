package com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;

/**
 * The Interface IdSource, provide a new {@link Id}.
 */
public interface IdSource {

    /**
     * New id.
     *
     * @return the id
     */
    Id newId();
}
