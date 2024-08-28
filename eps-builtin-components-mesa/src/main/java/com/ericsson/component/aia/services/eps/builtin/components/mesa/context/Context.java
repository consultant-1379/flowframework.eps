package com.ericsson.component.aia.services.eps.builtin.components.mesa.context;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EnvironmentBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.EventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.source.IdSource;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.compiler.ScriptCompiler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.Forwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.MesaEsperHandler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator.Validator;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.Selector;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.manager.StateManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.stats.MesaStatsProxy;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.ViewListener;

/**
 * Holder of all important modules that are shared across policies.
 */
public interface Context {

    /**
     * Script compiler.
     *
     * @return the script compiler
     */
    ScriptCompiler scriptCompiler();

    /**
     * State manager.
     *
     * @return the state manager
     */
    StateManager stateManager();

    /**
     * Event binder.
     *
     * @return the event binder
     */
    EventBinder eventBinder();

    /**
     * Environment binder.
     *
     * @return the environment binder
     */
    EnvironmentBinder environmentBinder();

    /**
     * Forwarder.
     *
     * @return the forwarder
     */
    Forwarder forwarder();

    /**
     * Id source.
     *
     * @return the id source
     */
    IdSource idSource();

    /**
     * Validator.
     *
     * @return the validator
     */
    Validator validator();

    /**
     * Stats.
     *
     * @return the mesa stats proxy
     */
    MesaStatsProxy stats();

    /**
     * Selector.
     *
     * @return the selector
     */
    Selector selector();

    /**
     * Esper handler.
     *
     * @return the mesa esper handler
     */
    MesaEsperHandler esperHandler();

    /**
     * View listener.
     *
     * @return the view listener
     */
    ViewListener viewListener();

    /**
     * Gets the evaluation time limit.
     *
     * @return the evaluation time limit
     */
    long getEvaluationTimeLimit();
}
