package com.ericsson.component.aia.services.eps.builtin.components.mesa.context;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source.IdSource;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source.SimpleIdSource;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.JythonScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.ScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.MesaEsperHandler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator.NoopValidator;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator.Validator;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.Selector;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.SmartSelector;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.SimpleStateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.StateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.stats.MesaStatsProxy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;

/**
 * This class impelements and manages all important modules that are shared across policies.
 */
public final class SimpleContext implements Context {

    private final ScriptCompiler scriptCompiler;
    private final StateManager stateManager;
    private final EventBinder eventBinder;
    private final EnvironmentBinder envBinder;
    private final Forwarder forwarder;
    private final IdSource idSource;
    private final Validator validator;
    private final MesaStatsProxy stats;
    private final Selector selector;
    private final MesaEsperHandler esperHandler;
    private final ViewListener viewListener;
    private final long evaluationTimeLimit;

    /**
     * Instantiates a new simple context.
     *
     * @param eventBinder
     *            the event binder
     * @param forwarder
     *            the forwarder
     * @param viewListener
     *            the view listener
     * @param esperConfig
     *            the esper config
     * @param eplBaseDir
     *            the epl base dir
     * @param evaluationTimeLimit
     *            the evaluation time limit
     */
    public SimpleContext(final EventBinder eventBinder, final Forwarder forwarder, final ViewListener viewListener, final String esperConfig,
                         final String eplBaseDir, final long evaluationTimeLimit) {
        super();
        scriptCompiler = new JythonScriptCompiler();
        stateManager = new SimpleStateManager();
        this.eventBinder = eventBinder;
        envBinder = new SimpleEnvironmentBinder();
        this.forwarder = forwarder;
        idSource = new SimpleIdSource();
        validator = new NoopValidator(); // noop for now until we get clarification how EPS globally will do semantic verification
        stats = new MesaStatsProxy();
        selector = new SmartSelector();
        this.viewListener = viewListener;
        esperHandler = new MesaEsperHandler(esperConfig, eplBaseDir);
        this.evaluationTimeLimit = evaluationTimeLimit;

        // new injectables should be injected here...
        stateManager.inject(this);
        esperHandler.inject(this);
        forwarder.inject(this);
    }

    @Override
    public ViewListener viewListener() {
        return viewListener;
    }

    @Override
    public ScriptCompiler scriptCompiler() {
        return scriptCompiler;
    }

    @Override
    public StateManager stateManager() {
        return stateManager;
    }

    @Override
    public EventBinder eventBinder() {
        return eventBinder;
    }

    @Override
    public EnvironmentBinder environmentBinder() {
        return envBinder;
    }

    @Override
    public Forwarder forwarder() {
        return forwarder;
    }

    @Override
    public IdSource idSource() {
        return idSource;
    }

    @Override
    public Validator validator() {
        return validator;
    }

    @Override
    public MesaStatsProxy stats() {
        return stats;
    }

    @Override
    public Selector selector() {
        return selector;
    }

    @Override
    public MesaEsperHandler esperHandler() {
        return esperHandler;
    }

    @Override
    public long getEvaluationTimeLimit() {
        return evaluationTimeLimit;
    }
}
