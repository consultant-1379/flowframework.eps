/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.builtin.components.mesa.esper.listeners;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample.INTERNAL_SYSTEM_UTILIZATION_60MIN;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart.SmartMatrixUpdateListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix.MatrixView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SimpleSequenceView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

public class MatrixListenerTest {

    @Test
    public void testMatrixListener() {
        final int numberOfEventTypes = 2;
        final SimpleId coreId = new SimpleId(0);
        final SimpleId ruleId = new SimpleId(0);

        final SmartMatrixUpdateListener listener = new SmartMatrixUpdateListener(new SimpleViewListener(), numberOfEventTypes, coreId, ruleId);

        final INTERNAL_SYSTEM_UTILIZATION eventISU_0 = new INTERNAL_SYSTEM_UTILIZATION();
        eventISU_0.setRopId(10);
        eventISU_0.setResourceId(2);
        final INTERNAL_SYSTEM_UTILIZATION eventISU_1 = new INTERNAL_SYSTEM_UTILIZATION();
        eventISU_1.setRopId(15);
        eventISU_1.setResourceId(2);
        final INTERNAL_SYSTEM_UTILIZATION eventISU_2 = new INTERNAL_SYSTEM_UTILIZATION();
        eventISU_2.setRopId(20);
        eventISU_2.setResourceId(2);
        final INTERNAL_SYSTEM_UTILIZATION eventISU_3 = new INTERNAL_SYSTEM_UTILIZATION();
        eventISU_3.setRopId(25);
        eventISU_3.setResourceId(2);

        final INTERNAL_SYSTEM_UTILIZATION_60MIN eventISU60_0 = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
        eventISU60_0.setRopId(10);
        eventISU60_0.setResourceId(2);
        final INTERNAL_SYSTEM_UTILIZATION_60MIN eventISU60_1 = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
        eventISU60_1.setRopId(15);
        eventISU60_1.setResourceId(2);
        final INTERNAL_SYSTEM_UTILIZATION_60MIN eventISU60_2 = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
        eventISU60_2.setRopId(20);
        eventISU60_2.setResourceId(2);
        final INTERNAL_SYSTEM_UTILIZATION_60MIN eventISU60_3 = new INTERNAL_SYSTEM_UTILIZATION_60MIN();
        eventISU60_3.setRopId(25);
        eventISU60_3.setResourceId(2);

        final SimpleSequenceView view = new SimpleSequenceView(coreId, ruleId);
        final SimpleSequenceView view2 = new SimpleSequenceView(coreId, ruleId);

        view.append(eventISU_0);
        view2.append(eventISU60_0);

        listener.on(view);
        listener.on(view2);

        view.append(eventISU_1);
        view2.append(eventISU60_1);

        listener.on(view);
        listener.on(view2);

        view.append(eventISU_2);
        view2.append(eventISU60_2);

        listener.on(view);
        listener.on(view2);

        view.append(eventISU_3);
        view2.append(eventISU60_3);

        listener.on(view);
        listener.on(view2);
    }

    private final class SimpleViewListener implements ViewListener {

        int[] expected = { 10, 15, 20, 25 };

        public SimpleViewListener() {
        }

        @Override
        public void on(final View view) {
            if (view instanceof MatrixView) {
                final MatrixView matrixView = (MatrixView) view;
                for (final SequenceView seqView : matrixView.getSequenceViews()) {
                    int idx = 0;
                    for (final Event event : seqView.getAll()) {
                        assertTrue(event.getRopId() == expected[idx]);
                        idx++;
                    }
                }
            }
        }
    }
}
