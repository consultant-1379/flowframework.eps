package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart;

import java.util.*;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Pair;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix.MatrixView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix.SimpleMatrixView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.espertech.esper.client.EventBean;

/**
 * Provide {@link MatrixView} for update listeners which assume and understand new data naming conventions.
 */
public final class SmartMatrixUpdateListener extends SmartMesaUpdateListener implements ViewListener {

    int numberOfEventTypes;

    private final Map<Pair<Long, Long>, List<View>> sequences = new HashMap<Pair<Long, Long>, List<View>>();

    /**
     * Instantiates a new smart matrix update listener.
     *
     * @param sink
     *            the sink
     * @param numberOfEventTypes
     *            the number of event types
     * @param coreId
     *            the core id
     * @param ruleId
     *            the rule id
     */
    public SmartMatrixUpdateListener(final ViewListener sink, final int numberOfEventTypes, final Id coreId, final Id ruleId) {
        super(sink, coreId, ruleId);
        this.numberOfEventTypes = numberOfEventTypes;
    }

    @Override
    public void on(final View view) {
        final long resourceId = view.getFirst().getResourceId();
        final long ropId = view.getFirst().getRopId();

        final Pair<Long, Long> key = new Pair<Long, Long>(resourceId, ropId);

        if (sequences.get(key) != null) {
            sequences.get(key).add(view);
        } else {
            final List<View> newList = new ArrayList<View>();
            newList.add(view);
            sequences.put(key, newList);
        }

        if (sequences.get(key).size() == numberOfEventTypes) {
            final MatrixView matrixView = new SimpleMatrixView(getCoreId(), getRuleId());
            for (final View sequenceView : sequences.get(key)) {
                matrixView.append((SequenceView) sequenceView);
            }
            getSink().on(matrixView);
        }

    }

    @Override
    protected void updateNew(final EventBean[] events) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void updateOld(final EventBean[] events) {
        // TODO Auto-generated method stub

    }

}
