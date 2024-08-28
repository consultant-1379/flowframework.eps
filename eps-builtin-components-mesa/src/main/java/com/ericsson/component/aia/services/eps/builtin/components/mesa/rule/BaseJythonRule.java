package com.ericsson.component.aia.services.eps.builtin.components.mesa.rule;

import java.util.Properties;

import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EnvironmentBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.stats.MesaGauge;

/**
 * The Class BaseJythonRule manages Jython rules.
 */
public abstract class BaseJythonRule extends AbstractRule {

    public boolean flexiableBinding;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final PythonInterpreter interpreter;

    private EventBinder eventBinder;
    private EnvironmentBinder envBinder;
    private Context context;
    private PyCode code;
    private Forwarder forwarder;
    private Long duration = 0l;
    private final MesaGauge gauge;

    /**
     * Instantiates a new base jython rule.
     *
     * @param valId
     *            the id
     * @param name
     *            the name
     * @param flexiableBinding
     *            the flexiable binding
     */
    public BaseJythonRule(final Id valId, final String name, final boolean flexiableBinding) {
        super(valId, name);
        this.flexiableBinding = flexiableBinding;
        final Properties props = new Properties();
        props.setProperty("python.security.respectJavaAccessibility", "false");
        PythonInterpreter.initialize(props, props, null);
        interpreter = new PythonInterpreter();
        gauge = new MesaGauge();
    }

    /**
     * Sets the event binder.
     *
     * @param eventBinder
     *            the new event binder
     */
    public final void setEventBinder(final EventBinder eventBinder) {
        this.eventBinder = eventBinder;
    }

    /**
     * Sets the env binder.
     *
     * @param envBinder
     *            the new env binder
     */
    public final void setEnvBinder(final EnvironmentBinder envBinder) {
        this.envBinder = envBinder;
    }

    /**
     * Sets the code.
     *
     * @param code
     *            the new code
     */
    public final void setCode(final PyCode code) {
        this.code = code;
    }

    /**
     * Sets the forwarder.
     *
     * @param forwarder
     *            the new forwarder
     */
    public final void setForwarder(final Forwarder forwarder) {
        this.forwarder = forwarder;
    }

    /**
     * Inject the context.
     *
     * @param context
     *            the context
     */
    public final void inject(final Context context) {
        this.setContext(context);
        context.stats().registerGauge("rule." + super.getName() + ".evaluationTime", gauge);
    }

    @Override
    public void init() {
        super.init();
        getEnvironmentBinder().bind(getInterpreter());
    }

    @Override
    public final void register(final String name, final Object value) {
        getInterpreter().set(name, value);
    }

    /**
     * Gets the event binder.
     *
     * @return the event binder
     */
    protected final EventBinder getEventBinder() {
        return eventBinder;
    }

    /**
     * Gets the environment binder.
     *
     * @return the environment binder
     */
    protected final EnvironmentBinder getEnvironmentBinder() {
        return envBinder;
    }

    /**
     * Gets the interpreter.
     *
     * @return the interpreter
     */
    protected final PythonInterpreter getInterpreter() {
        return interpreter;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    protected final PyCode getCode() {
        return code;
    }

    /**
     * Gets the forwarder.
     *
     * @return the forwarder
     */
    protected final Forwarder getForwarder() {
        return forwarder;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Sets the duration.
     *
     * @param duration
     *            the new duration
     */
    public void setDuration(final Long duration) {
        this.duration = duration;
        if (duration > getContext().getEvaluationTimeLimit()) {
            log.warn("The rule {} is exceeding the current evaluation time limit of {}, the evaluation duration was ", getName(), getContext()
                    .getEvaluationTimeLimit(), duration);
        }
        getGauge().setDuration(duration);
    }

    /**
     * Gets the gauge.
     *
     * @see MesaGauge
     *
     * @return the gauge
     */
    public MesaGauge getGauge() {
        return gauge;
    }

    /**
     * Checks for matched.
     *
     * @return true, if successful
     */
    protected final boolean hasMatched() {
        return (boolean) getInterpreter().get("matched").__tojava__(Boolean.class);
    }

    /**
     * Handel python exception.
     *
     * @param pyEx
     *            the py ex
     */
    public void handelPythonException(final PyException pyEx) {
        final String whatsUp = "" + pyEx.value;

        if (whatsUp.contains("is not defined")) {
            if (flexiableBinding) {
                log.warn("Python was unable to find an object bound to the following resource: {} {} for rule: {}", whatsUp, pyEx, this.getName());
            } else {
                log.error("Python was unable to find an object bound to the following resource: {} {} for rule: {}", whatsUp, pyEx, this.getName());
            }
        } else {
            log.error("Encountered an error whilst trying to execuate the rule {}. The python error was: {} {} for rule: {}", super.getName(),
                    whatsUp, pyEx, this.getName());
        }
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the context.
     *
     * @param context
     *            the new context
     */
    public void setContext(final Context context) {
        this.context = context;
    }
}
