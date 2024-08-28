package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.cube;

import java.util.SortedSet;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;

/**
 * Multiple resource IDs.
 */
public interface CubeView extends View {

    /**
     * Gets the resource ids.
     *
     * @return the resource ids (sorted)
     */
    SortedSet<Long> getResourceIds();

    /**
     * Gets the View.
     *
     * @param <T>
     *            the generic type
     * @param resourceId
     *            the resource id
     * @return the View
     */
    <T extends View> T get(long resourceId);
}
