package com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix;

import java.util.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.NotYetImplementedException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.AbstractView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 *
 * provides access to multiple event types from the same resource ID as a Matrix view of the data. This is done by using a list of multiple sequences.
 *
 * @author emilawl
 *
 */
public final class SimpleMatrixView extends AbstractView implements MatrixView {

    private final List<SequenceView> sequences = new ArrayList<SequenceView>();

    /**
     * Instantiates a new simple matrix view.
     *
     * @param policyCoreId
     *            the policy core id
     * @param ruleGroupId
     *            the rule group id
     */
    public SimpleMatrixView(final Id policyCoreId, final Id ruleGroupId) {
        super(policyCoreId, ruleGroupId);
    }

    @Override
    public void append(final SequenceView sequenceView) {
        sequences.add(sequenceView);
    }

    @Override
    public List<Event> getAll() {
        throw new NotYetImplementedException();
    }

    /**
     * The matrix view is for a single resource Id so we can take any events resource Id for this matrix. so we will take the first sequences first
     * events resource id.
     *
     * @return the resource id
     */
    @Override
    public long getResourceId() {
        return sequences.get(0).get(0).getResourceId();
    }

    @Override
    public boolean hasResourceId() {
        throw new NotYetImplementedException();
    }

    @Override
    public ViewType getViewType() {
        return ViewType.MATRIX;
    }

    @Override
    public Iterator<Event> iterator() {
        final List<Event> allEvents = new ArrayList<Event>();
        for (final SequenceView sequence : sequences) {
            allEvents.addAll(sequence.getAll());
        }
        return allEvents.iterator();
    }

    @Override
    public Event getFirst() {
        throw new NotYetImplementedException();
    }

    @Override
    public List<SequenceView> getSequenceViews() {
        return sequences;
    }

    @Override
    public String toString() {
        return "MatrixView[" + sequences + "]";
    }
}
