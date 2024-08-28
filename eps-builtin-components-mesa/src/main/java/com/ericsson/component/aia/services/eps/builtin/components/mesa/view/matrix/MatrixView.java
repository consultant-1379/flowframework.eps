package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix;

import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.ericsson.component.aia.services.eps.mesa.common.resource.ResourceIdAware;

/**
 * Wrapper around matrix of events of the multiple types for single resource ID. Matrix can handle at most 10 different event types.
 */
public interface MatrixView extends View, ResourceIdAware {

    /**
     * Gets the sequence views as list.
     *
     * @return the sequence views
     */
    List<SequenceView> getSequenceViews();

    /**
     * Append the {@link SequenceView} to the list.
     *
     * @param sequenceView
     *            the sequence view
     */
    void append(SequenceView sequenceView);
}
