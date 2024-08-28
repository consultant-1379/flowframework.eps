package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.stateless;

import org.python.core.PyException;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.alert.SimpleStandardAlert;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.BaseJythonRule;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.common.resource.ResourceIdAware;

/**
 * The Class StatelessRule extends {@link BaseJythonRule}.
 */
public final class StatelessRule extends BaseJythonRule {

    /**
     * Instantiates a new stateless rule.
     *
     * @param identifier
     *            the id
     * @param name
     *            the name
     * @param flexiableBinding
     *            the flexiable binding
     */
    public StatelessRule(final Id identifier, final String name, final boolean flexiableBinding) {
        super(identifier, name, flexiableBinding);
    }

    @Override
    public Type getType() {
        return Type.STATELESS;
    }

    @Override
    public synchronized void evaluate(final View view, final State state) {
        final long start = System.currentTimeMillis();
        final SimpleStandardAlert alert = new SimpleStandardAlert();

        // bind the java objects to the variable names which will be used to
        // access the object in Jython. The same object can be bound to more
        // then one variable name.
        getInterpreter().set("a", alert);
        getInterpreter().set("alert", alert);

        getEventBinder().bind(view, getInterpreter());

        try {
            getInterpreter().exec(getCode());

            final boolean matched = hasMatched();
            if (matched) {
                if (view instanceof ResourceIdAware) {
                    alert.setResourceId(((ResourceIdAware) view).getResourceId());
                }
                // place here all setters that should be common instead of being
                // done within the rules themselves...

                getForwarder().on(alert);
                if (log.isTraceEnabled()) {
                    log.trace("Sent new alert {}", alert);
                }
            }
            setDuration(System.currentTimeMillis() - start);
            if (log.isTraceEnabled()) {
                log.trace("Evaluation of stateless rule {} with name {} for view {} and state {} took {} millis with matched status {}", getId(),
                        getName(), view, state, getDuration(), matched);
            }

        } catch (final PyException pyEx) {
            super.handelPythonException(pyEx);
        }
    }
}
