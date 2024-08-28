package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Injectable;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.DeploymentAware;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper.smart.*;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.EventRef;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.PolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.*;
import com.espertech.esper.client.*;
import com.espertech.esper.client.deploy.*;

/**
 *
 * Provides the MESA component access to an Esper instance to attach event types and rules with associated listeners to programmatically.
 *
 * @author emilawl
 *
 */

public class MesaEsperHandler implements DeploymentAware, Injectable {

    private static final String NAME = "name";

    private static final String EVENT_NAME = "eventName";

    private static final String ESPER_RUNTIME_NAME = "mesa-esper-runtime";

    private static final String SINGLETON_EPL_TEMPLATE = "template_sliding_window.epl";
    private static final String SEQUENCE_EPL_TEMPLATE = "template_sliding_window.epl";

    private static final int MAX_MATRIX_EVENT_TYPES = 10;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String baseDir;

    private final Map<Name, Policy> deployedPolicy = new HashMap<Name, Policy>();
    // Note map of coreId to pair of ruleGroupId-ruleId to the deployedConfig
    private final Map<Id, HashMap<Pair<Id, Id>, DeploymentConfig>> deployed = new HashMap<Id, HashMap<Pair<Id, Id>, DeploymentConfig>>();

    private final EPServiceProvider epServiceProvider;
    private final List<String> loadedEvents;
    private final Map<String, Module> eplModules;
    private final Map<String, String> eplModuleIdToWindowName;
    private final Map<String, String> eplWindowNameToModuleId;
    private final Configuration esperConfiguration;
    private Context context;

    /**
     * Instantiates a new mesa esper handler.
     *
     * @param esperConfig
     *            the esper config
     * @param baseDir
     *            the base dir
     */
    public MesaEsperHandler(final String esperConfig, final String baseDir) {
        esperConfiguration = new Configuration();
        esperConfiguration.configure(new File(esperConfig));
        epServiceProvider = EPServiceProviderManager.getProvider(ESPER_RUNTIME_NAME + esperConfig, esperConfiguration);
        loadedEvents = new ArrayList<String>();
        eplModules = new HashMap<String, Module>();
        eplModuleIdToWindowName = new HashMap<String, String>();
        eplWindowNameToModuleId = new HashMap<String, String>();
        this.baseDir = baseDir;
    }

    @Override
    public void inject(final Context context) {
        this.context = context;
    }

    /**
     * Send event to esper.
     *
     * @param object
     *            {@link Object}
     */
    public void sendEventToEsper(final Object object) {
        epServiceProvider.getEPRuntime().sendEvent(object);
    }

    @Override
    public void deployCore(final Policy policy) throws MesaDeploymentException {
        for (final RuleGroupSpec ruleGroupSpec : policy.getRuleGroupSpecs()) {
            final List<String> eventList = new ArrayList<String>();
            for (final EventRef eventRef : ruleGroupSpec.getSubscription().get()) {
                addEventTypeToEsper(eventRef.getName(), eventRef.getNamespace() + "." + eventRef.getName());
                eventList.add(eventRef.getName());
            }
            for (final RuleSpec rule : ruleGroupSpec.getRules()) {
                final DeploymentConfig deploymentConfig = new DeploymentConfig(eventList, rule, ruleGroupSpec, policy);
                selectConfigurationToLoad(deploymentConfig);
            }
        }
        deployedPolicy.put(policy.getName(), policy);
    }

    @Override
    public void undeployCore(final Policy policy) throws MesaDeploymentException {
        log.info("Preparing to undeploy the core policy {}", policy.getName().toString());
        if (deployedPolicy.get(policy.getName()) == null) {
            log.error("The policy {} has not been deployed, no undeployment action will be taken", policy.getName().toString());
        } else {
            for (final DeploymentConfig deploymentConfig : deployed.get(policy.getId()).values()) {
                for (final Entry<String, MesaUpdateListener> set : deploymentConfig.getListeners().entrySet()) {
                    if ("matrix".equalsIgnoreCase(set.getKey())) {
                        log.debug("A matrix listener has been removed");
                        continue;
                    } else {
                        removeListener(set.getKey(), set.getValue());
                        if (checkForListener(set.getKey()) == null) {
                            removeEsperModule(set.getKey());
                        }
                    }
                }
                //deploymentConfig = null;
            }
            deployed.remove(policy.getId());
        }
    }

    private void removeEsperModule(final String key) {
        try {
            epServiceProvider.getEPAdministrator().getDeploymentAdmin().remove(eplModuleIdToWindowName.get(key));
            context.stats().countDecrement("EplModulesDeployed");
        } catch (EPServiceDestroyedException | DeploymentException e) {
            throw new MesaDeploymentException("Unable to undeploy the module " + eplModuleIdToWindowName.get(key)
                    + ", system is now in an inconsistant state", false);
        }
    }

    private Iterator<UpdateListener> checkForListener(final String key) {
        return epServiceProvider.getEPAdministrator().getStatement(eplModuleIdToWindowName.get(key)).getUpdateListeners();
    }

    private void removeListener(final String key, final MesaUpdateListener listener) {
        epServiceProvider.getEPAdministrator().getStatement(eplModuleIdToWindowName.get(key)).removeListener(listener);
    }

    @Override
    public void deployConf(final PolicyConf policyConf) {
        throw new NotYetImplementedException();
    }

    @Override
    public void undeployConf(final Id id, final Name name) {
        throw new NotYetImplementedException();
    }

    /**
     * Adds the event type to esper.
     *
     * @param eventName
     *            the event name
     * @param eventFullyQualifiedName
     *            the event fully qualified name
     */
    public void addEventTypeToEsper(final String eventName, final String eventFullyQualifiedName) {
        if (!loadedEvents.contains(eventFullyQualifiedName)) {
            epServiceProvider.getEPAdministrator().getConfiguration().addEventType(eventName, eventFullyQualifiedName);
            loadedEvents.add(eventFullyQualifiedName);
            log.debug("Event Type {} added to Esper", eventFullyQualifiedName);
            context.stats().countIncrement("EventTypesDeployedToEsper");
        } else {
            log.debug("Event Type already configured in Esper {}", eventFullyQualifiedName);
        }
    }

    /**
     * Creates the epl from {@link MesaEplTemplateConfig} and {@link DeploymentConfig}.
     *
     * @param config
     *            the config
     * @param deploymentConfig
     *            the deployment config
     * @return the string
     * @throws MesaDeploymentException
     *             the mesa deployment exception
     */
    public String createEplFrom(final MesaEplTemplateConfig config, final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        final String fileName = config.get(EVENT_NAME) + "_sliding_window.epl";
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            log.debug("Creating EPL {}, using the template {}", fileName, (config.getTemplate()));
            reader = new BufferedReader(new FileReader(config.getTemplate()));
            writer = new BufferedWriter(new FileWriter(config.getBaseDir() + fileName));

            final VelocityContext context = config.getVelocityContext();
            Velocity.evaluate(context, writer, fileName, reader);

        } catch (final Exception e) {
            rollBack("Failed to create an EPL for deployment from the template " + fileName, deploymentConfig, true);
        } finally {
            try {
                writer.close();
                reader.close();
            } catch (final IOException e) {
                log.error("A Problem occourd whilst trying to close a file reader/writer");
            }
        }

        return config.getBaseDir() + fileName;
    }

    /**
     * loads an epl into a module and deploys it, if a module has already been deployed the additional listener will be attached to the previously
     * deployed module.
     *
     * @param eplFullPath
     *            the epl full path
     * @param windowName
     *            the window name
     * @param listener
     *            the listener
     * @param deploymentConfig
     *            the deployment config
     * @return returns the moduleId of the module deployed
     * @throws MesaDeploymentException
     *             the mesa deployment exception
     */
    public String loadEpl(final String eplFullPath, final String windowName, final MesaUpdateListener listener,
                          final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        final EPDeploymentAdmin deployAdmin = epServiceProvider.getEPAdministrator().getDeploymentAdmin();
        Module module;
        String moduleId = null;
        if (eplModules.get(windowName) == null) {
            try {

                final File eplFile = new File(eplFullPath);
                module = deployAdmin.read(eplFile);
                final DeploymentResult res = deployAdmin.deploy(module, null);
                moduleId = res.getDeploymentId();
                eplModules.put(windowName, module);
                eplModuleIdToWindowName.put(moduleId, windowName);
                eplWindowNameToModuleId.put(windowName, moduleId);
                addListenerToEpl(windowName, listener);
                log.debug("EPL {}, deployed and Listener attached", eplFullPath);
                context.stats().countIncrement("EplModulesDeployed");

            } catch (final Exception e) {
                rollBack("Failed to read the epl file: " + eplFullPath + e.getMessage(), deploymentConfig, true);
            }

            return moduleId;

        } else {
            log.debug("EPL for this window {} previously deployed, attaching addtional listener to the window", windowName);
            addListenerToEpl(windowName, listener);
            return eplWindowNameToModuleId.get(windowName);
        }

    }

    private void addListenerToEpl(final String windowName, final MesaUpdateListener listener) {
        epServiceProvider.getEPAdministrator().getStatement(windowName).addListener(listener);
    }

    /**
     * Gets the ep service provider.
     *
     * @return the ep service provider
     *
     * @see EPServiceProvider
     */
    public EPServiceProvider getEpServiceProvider() {
        return epServiceProvider;
    }

    private void selectConfigurationToLoad(final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        final int numberOfEvents = deploymentConfig.getEventList().size();
        if (deployed.get(deploymentConfig.getPolicy().getId()) == null) {
            final HashMap<Pair<Id, Id>, DeploymentConfig> configs = new HashMap<Pair<Id, Id>, DeploymentConfig>();
            deployed.put(deploymentConfig.getPolicy().getId(), configs);
        }
        switch (deploymentConfig.getRuleGroup().getSubscription().getViewType()) {
            case SINGLETON:
                if (numberOfEvents == 1) {
                    createSingletonEPLAndListener(deploymentConfig);
                } else {
                    rollBack("Rule groups with Rule type SINGLETON do not yet support multiple event definitions", deploymentConfig, true);
                }
                break;
            case SEQUENCE:
                if (numberOfEvents == 1) {
                    createSequenceEPLAndListener(deploymentConfig);
                } else {
                    rollBack("Rule groups with Rule type SEQUENCE do not yet support multiple event deffinitions", deploymentConfig, true);
                }
                break;
            case MATRIX:
                if (numberOfEvents > MAX_MATRIX_EVENT_TYPES) {
                    rollBack("Support for matrix with more then " + MAX_MATRIX_EVENT_TYPES + " event types is currently not supported",
                            deploymentConfig, true);
                } else {
                    createMatrixEPLAndListener(deploymentConfig);
                }
                break;
            // case BAG:
            // rollBack("No listener available yet for the viewType: bag",
            // deploymentConfig, true);
            // throw new NotYetImplementedException();
            // case CUBE:
            // rollBack("No listener available yet for the viewType: cube",
            // deploymentConfig, true);
            // throw new NotYetImplementedException();
            default:
                rollBack("No default available for the viewType, please check a valid viewType has been defined in the policy", deploymentConfig,
                        true);
        }
    }

    private MesaUpdateListener createSingletonEPLAndListener(final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        final String eventName = deploymentConfig.getEventList().get(0);
        final String windowName = "sliding_window_" + eventName;

        final MesaEplTemplateConfig config = new MesaEplTemplateConfig();
        final MesaUpdateListener listener = new SmartSingletonUpdateListener(context.viewListener(), deploymentConfig.getPolicy().getId(),
                deploymentConfig.getRuleGroup().getId());

        config.put(NAME, windowName);
        config.put(EVENT_NAME, eventName);
        config.setEplTemplate(baseDir + SINGLETON_EPL_TEMPLATE);
        config.setEplBaseDir(baseDir);

        final String moduleId = configureEPLAndAttachListener(listener, config, deploymentConfig);
        deploymentConfig.update(moduleId, listener);
        deployed.get(deploymentConfig.getPolicy().getId()).put(deploymentConfig.getKey(), deploymentConfig);
        return listener;
    }

    private MesaUpdateListener createSequenceEPLAndListener(final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        final String eventName = deploymentConfig.getEventList().get(0);
        final String windowName = "sliding_window_" + eventName;

        final MesaEplTemplateConfig config = new MesaEplTemplateConfig();
        final MesaUpdateListener listener = new SmartSequenceUpdateListener(context.viewListener(), deploymentConfig.getPolicy().getId(),
                deploymentConfig.getRuleGroup().getId());

        config.put(NAME, windowName);
        config.put(EVENT_NAME, eventName);
        config.setEplTemplate(baseDir + SEQUENCE_EPL_TEMPLATE);
        config.setEplBaseDir(baseDir);

        final String moduleId = configureEPLAndAttachListener(listener, config, deploymentConfig);
        deploymentConfig.update(moduleId, listener);
        deployed.get(deploymentConfig.getPolicy().getId()).put(deploymentConfig.getKey(), deploymentConfig);
        return listener;
    }

    /**
     *
     * Sequence Matrix is made up of sequences, a new sequence listener and epl will be deployed where required, if an epl is already in place a
     * second sequence listener will be attached and used to create the matrix.
     *
     * @param name
     * @param id
     * @param eventList
     * @throws Exception
     *
     */
    private void createMatrixEPLAndListener(final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        // TODO use new template at some point
        // String windowName = "sliding_window_" + name;
        //
        // //TODO populate this properly with eventTypeIds
        // String tempStringForEventTypeIds = "emptyString";
        // MesaEplTemplateConfig config = new MesaEplTemplateConfig();
        // config.put("varWindowLength", "4");
        // config.put("name", windowName);
        // config.put("eventName", ComponentEvent.class.getName());
        // config.put("eventTypeIds", tempStringForEventTypeIds);
        // config.setEplTemplate(baseDir + MATRIX_EPL_TEMPLATE);
        // config.setEplBaseDir(baseDir);
        //
        final SmartMatrixUpdateListener matrixListener = new SmartMatrixUpdateListener(context.viewListener(), deploymentConfig.getEventList()
                .size(), deploymentConfig.getPolicy().getId(), deploymentConfig.getRuleGroup().getId());

        for (final String eventName : deploymentConfig.getEventList()) {
            final String windowName = "sliding_window_" + eventName;
            final MesaEplTemplateConfig config = new MesaEplTemplateConfig();
            final MesaUpdateListener listener = new SmartSequenceUpdateListener(matrixListener, deploymentConfig.getPolicy().getId(),
                    deploymentConfig.getRuleGroup().getId());

            config.put(NAME, windowName);
            config.put(EVENT_NAME, eventName);
            config.setEplTemplate(baseDir + SEQUENCE_EPL_TEMPLATE);
            config.setEplBaseDir(baseDir);

            final String moduleId = configureEPLAndAttachListener(listener, config, deploymentConfig);
            deploymentConfig.update(moduleId, listener);
            ;
        }
        deploymentConfig.update("matrix", matrixListener);
        deployed.get(deploymentConfig.getPolicy().getId()).put(deploymentConfig.getKey(), deploymentConfig);
    }

    private String configureEPLAndAttachListener(final MesaUpdateListener listener, final MesaEplTemplateConfig config,
                                                 final DeploymentConfig deploymentConfig) throws MesaDeploymentException {
        final String eplLocation = createEplFrom(config, deploymentConfig);
        final String moduleId = loadEpl(eplLocation, config.get(NAME), listener, deploymentConfig);
        return moduleId;
    }

    private void rollBack(final String message, final DeploymentConfig config, final boolean systemConsistant) throws MesaDeploymentException {
        // Haven't done anything yet but we could have deployed other rules just
        // call undeploy to clean up
        undeployCore(config.getPolicy());
        throw new MesaDeploymentException(message, systemConsistant);
    }

}
