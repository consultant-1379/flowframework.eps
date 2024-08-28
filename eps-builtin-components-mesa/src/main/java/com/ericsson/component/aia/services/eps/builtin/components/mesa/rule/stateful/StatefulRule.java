package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateful;

import org.python.core.PyException;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.alarm.SimpleStandardAlarm;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.BaseJythonRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.common.resource.ResourceIdAware;

/**
 * The Class StatefulRule extends {@link BaseJythonRule}.
 */
public final class StatefulRule extends BaseJythonRule {

    /**
     * Instantiates a new stateful rule.
     *
     * @param id
     *            the id
     * @param name
     *            the name
     * @param flexiableBinding
     *            the flexiable binding
     */
    public StatefulRule(final Id id, final String name, final boolean flexiableBinding) {
        super(id, name, flexiableBinding);
    }

    @Override
    public Type getType() {
        return Type.STATEFUL;
    }

    @Override
    public synchronized void evaluate(final View view, final State state) {
        final long start = System.currentTimeMillis();
        final boolean isAlarmed = state.hasKey(State.MATCHED_FLAG_KEY);

        SimpleStandardAlarm alarmStart = new SimpleStandardAlarm();
        final SimpleStandardAlarm alarmEnd = new SimpleStandardAlarm();

        if (isAlarmed) {
            // current resource ID is alreay in alarming state
            alarmStart = (SimpleStandardAlarm) state.get(State.ALARM_START_KEY);
        }

        // bind the java objects to the variable names which will be used to
        // access the object in Jython. The same object can be bound to more
        // then one variable name.
        getInterpreter().set("s", state);
        getInterpreter().set("state", state);

        // getInterpreter().set("as", alarmStart); // as is reserver word
        getInterpreter().set("alarmStart", alarmStart);

        getInterpreter().set("ae", alarmEnd);
        getInterpreter().set("alarmEnd", alarmEnd);

        getEventBinder().bind(view, getInterpreter());

        try {
            getInterpreter().exec(getCode());

            final boolean matched = hasMatched();
            if (matched) {
                if (isAlarmed) {
                    // we aint alarmed any more
                    if (view instanceof ResourceIdAware) {
                        alarmEnd.setResourceId(((ResourceIdAware) view).getResourceId());
                    }
                    state.clear();
                    getForwarder().on(alarmEnd);
                    if (log.isTraceEnabled()) {
                        log.trace("Sent alarm end {}", alarmEnd);
                    }
                } else {
                    state.put(State.ALARM_START_KEY, alarmStart);
                    state.put(State.MATCHED_FLAG_KEY, Boolean.TRUE);

                    if (view instanceof ResourceIdAware) {
                        alarmStart.setResourceId(((ResourceIdAware) view).getResourceId());
                    }
                    getForwarder().on(alarmStart);
                    if (log.isTraceEnabled()) {
                        log.trace("Sent alarm start {}", alarmStart);
                    }
                }
            }

            setDuration(System.currentTimeMillis() - start);
            if (log.isTraceEnabled()) {
                log.trace("Evaluation of stateful rule {} with name {} for view {} and state {} took {} millis with"
                        + "matched status {} and with isAlarmed status {} ", getId(), getName(), view, state, getDuration(), matched, isAlarmed);
            }

        } catch (final PyException pyEx) {
            super.handelPythonException(pyEx);
        }
    }
}
