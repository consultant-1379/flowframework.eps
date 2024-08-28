package com.ericsson.component.aia.services.eps.builtin.components;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.Deployer;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.SimpleDeployer;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.EpsForwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.MesaEsperHandler;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.manager.Manager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.manager.SimpleManager;
import com.ericsson.component.aia.services.eps.pe.esper.EsperEngineConstants;
import com.ericsson.component.aia.itpf.common.event.ComponentEvent;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Builtin component used for "Monitoring Engine for Smart Analytics", (MESA) will send events to {@link MesaEsperHandler}
 */
public final class MesaEventHandler extends AbstractEventHandler implements EventInputHandler {

    public static final String POLICIES_ROOT_DIRECTORY_KEY = "policies.root.directory";
    public static final String JYTHON_EVENT_NAME_FILE = "jython.event.short.names";

    public static final String TEMPLATE_BASE_DIRECTORY = "epl.template.directory";
    public static final String EVALUATION_TIME_LIMIT = "evaluation.time.limit";
    public static final long EVALUATION_TIME_LIMIT_DEFAULT = 10l;

    private File rootDir;
    private Context context;
    private Manager manager;

    @Override
    protected void doInit() {
        manager = SimpleManager.getOrCreateManager();
        final long evaluationTimeLimit = getEvaluationTimeLimit();

        context = new SimpleContext(
                new SimpleEventBinder(getEventHandlerContext().getEventHandlerConfiguration().getStringProperty(JYTHON_EVENT_NAME_FILE)),
                new EpsForwarder(new ArrayList<>(getEventHandlerContext().getEventSubscribers())),
                manager,
                getEventHandlerContext().getEventHandlerConfiguration().getStringProperty(EsperEngineConstants.ESPER_CONFIGURATION_FILE_PARAM_NAME),
                getEventHandlerContext().getEventHandlerConfiguration().getStringProperty(TEMPLATE_BASE_DIRECTORY), evaluationTimeLimit);
        manager.inject(context);

        final String path = getEventHandlerContext().getEventHandlerConfiguration().getStringProperty(POLICIES_ROOT_DIRECTORY_KEY);
        rootDir = new File(path);

        if (!rootDir.exists()) {
            throw new IllegalArgumentException("Directory '" + rootDir.getAbsolutePath() + "' does not exist");
        }
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Location '" + rootDir.getAbsolutePath() + "' is not a directory");
        }
        log.info("Using '" + rootDir.getAbsolutePath() + "' as root directory of all policies");
        final File[] policyDirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isDirectory();
            }
        });
        log.info("Found following policy directories '" + Arrays.toString(policyDirs) + "'");
        deploy();
    }

    private long getEvaluationTimeLimit() {
        long evaluationTimeLimit;
        if (getEventHandlerContext().getEventHandlerConfiguration().getStringProperty(EVALUATION_TIME_LIMIT) == null) {
            evaluationTimeLimit = EVALUATION_TIME_LIMIT_DEFAULT;
        } else {
            evaluationTimeLimit = Long.parseLong(getEventHandlerContext().getEventHandlerConfiguration().getStringProperty(EVALUATION_TIME_LIMIT));
        }

        return evaluationTimeLimit;
    }

    @Override
    public void onEvent(final Object event) {

        if (!(event instanceof ComponentEvent)) {
            throw new IllegalArgumentException("MESA handle requires all events to inherit from ComponentEvent");
        }

        context.esperHandler().sendEventToEsper(event);
        context.stats().meter(event.getClass().getSimpleName() + ".eventsReceived", this);
    }

    private void deploy() {

        final Deployer deployer = new SimpleDeployer(rootDir, context, manager);
        deployer.start();

    }
}
