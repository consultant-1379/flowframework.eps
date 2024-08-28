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
package com.ericsson.component.aia.services.eps.core.module.impl;

import java.io.InputStream;
import java.util.*;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.FlowDeploymentFailurePolicy;
import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleComponentInstaller;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleInstaller;
import com.ericsson.component.aia.services.eps.core.module.assembler.impl.ComponentContextHandler;
import com.ericsson.component.aia.services.eps.core.module.assembler.impl.DefaultEpsModuleInstallerImpl;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.parsing.EpsModeledFlowParser;
import com.ericsson.component.aia.services.eps.core.parsing.EpsXmlFileParser;
import com.ericsson.component.aia.services.eps.core.statistics.EpsStatisticsRegisterImpl;
import com.ericsson.component.aia.services.eps.core.util.*;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;

/**
 * The Class EpsModulesManagerImpl.
 *
 * @author eborziv
 *
 * @see ModuleManager
 */
public class EpsModulesManagerImpl implements ModuleManager {

    protected Counter flowsDeployed;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final EpsProvider provider = EpsProvider.getInstance();

    private final Map<String, EpsModule> installedModules = new HashMap<>();

    private final EpsModuleInstaller moduleAssembler = new DefaultEpsModuleInstallerImpl();

    private final EpsStatisticsRegisterImpl epsStatisticsRegister = new EpsStatisticsRegisterImpl();

    private final ComponentContextHandler componentContextHandler = ComponentContextHandler.getInstance();

    /**
     * Instantiates a new default EpsModulesManagerImpl.
     */
    public EpsModulesManagerImpl() {
        initialiseStatistics(getEpsStatisticsRegister());
    }

    /**
     * Constructor for unit testing
     *
     * @param epsStatisticsRegister
     *            an EpsStatisticsRegister instance
     */
    protected EpsModulesManagerImpl(final EpsStatisticsRegister epsStatisticsRegister) {
        initialiseStatistics(epsStatisticsRegister);
    }

    /*
     * @Deprecated replaced by {@link #deployModuleFromFile()}
     *
     * @see com.ericsson.component.aia.services.eps.modules.ModuleManager#deployModule(java.io.InputStream)
     */
    @Override
    public synchronized String deployModule(final InputStream moduleResourceInputStream) {
        return this.deployModuleFromFile(moduleResourceInputStream);
    }

    @Override
    public synchronized String deployModuleFromFile(final InputStream moduleResourceInputStream) {
        if (moduleResourceInputStream == null) {
            throw new IllegalArgumentException("input stream must not be null");
        }
        log.debug("Parsing flow descriptor from input stream");
        final EpsXmlFileParser parser = this.getEpsXmlFileParser();
        final EpsModule module = parser.parseModule(moduleResourceInputStream);
        return this.deployModule(module);
    }

    @Override
    public synchronized String deployModuleFromModel(final String modelUrn) {
        if (modelUrn == null) {
            throw new IllegalArgumentException("URN must not be null");
        }
        log.debug("Parsing flow descriptor from input urn {}", modelUrn);
        final ModelInfo modelInfo = ModelInfo.fromUrn(modelUrn);
        final EpsModeledFlowParser parser = this.getModeledFlowParser();
        final EpsModule module = parser.parseModule(modelInfo);
        return this.deployModule(module);
    }

    /**
     * Deploy module.
     *
     * @param module
     *            the module
     * @return the string
     */
    private String deployModule(final EpsModule module) {
        if (module == null) {
            throw new IllegalArgumentException("flow descriptor must not be null");
        }
        log.debug("Trying to deploy flow {}", module);
        final String moduleIdentifier = module.getUniqueModuleIdentifier();
        final EpsModule existingModule = installedModules.get(moduleIdentifier);
        if (existingModule != null) {
            log.warn("Flow {} already deployed! Unable to deploy it again!", existingModule);
            return moduleIdentifier;
        }
        try {
            moduleAssembler.installEpsModuleComponents(module, this);
            log.debug("Successfully deployed all flow components");
        } catch (final Exception exc) {
            log.error("Failed to deploy flow. Will react according to defined user policy!"
                    + " User policy can be configured by specifying system property {}",
                    EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY);
            this.rollback(module);
            final FlowDeploymentFailurePolicy userPolicy = FlowDeploymentFailurePolicyLoader.getFlowDeploymentFailurePolicy();
            log.info("Configured user policy for flow deployment failure is {}", userPolicy);
            if (userPolicy == FlowDeploymentFailurePolicy.CONTINUE) {
                log.info("User policy is to continue execution in case of failed flow deployment. Will propagate exception!");
                throw exc;
            } else if (userPolicy == FlowDeploymentFailurePolicy.STOP_JVM) {
                this.stopJVMOnFlowFailure();
            }
        }
        installedModules.put(moduleIdentifier, module);
        log.debug("Successfully deployed {}", module);
        updateStatisticsWithModulesDeployed();
        log.debug("Unique identifier of deployed flow is {}", moduleIdentifier);
        return moduleIdentifier;
    }

    /**
     * Rollback.
     *
     * @param module
     *            the module
     */
    private void rollback(final EpsModule module) {
        if (module == null) {
            log.info("Unable to undeploy null flow");
            return;
        }
        final String moduleIdentifier = module.getUniqueModuleIdentifier();
        log.debug("Rolling back flow [{}]", moduleIdentifier);

        this.uninstallComponents(module);
        provider.removeIoAdapters(module);
    }

    /**
     * Stop jvm on flow failure.
     */
    private void stopJVMOnFlowFailure() {
        log.error("JVM WILL BE KILLED! Flow failed to deploy correctly and user policy is configured to stop JVM in this case."
                + " I will stop current JVM instance. This can be changed by changing value of system property {}",
                EpsConfigurationConstants.ON_FLOW_DEPLOYMENT_FAILURE_USER_POLICY);
        log.error("Preparing shutdown of current JVM instance...");
        this.undeployAllModules();
        System.err.println("JVM WILL BE KILLED! Flow failed to deploy correctly and user policy is configured to stop JVM in this case."
                + " I will stop current JVM instance. This can be changed by changing value of system property");
        // we do not use System.exit() because of potential to have deadlocks with shutdown hooks
        Runtime.getRuntime().halt(-1);
    }

    @Override
    public boolean undeployModule(final String moduleId) {
        if ((moduleId == null) || moduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("id must not be null or empty");
        }
        log.debug("Removing flow [{}]", moduleId);
        final EpsModule module = installedModules.remove(moduleId);
        if (module == null) {
            log.info("Unable to undeploy flow with id [{}] because it was not deployed", moduleId);
            return false;
        }
        log.debug("Found deployed flow {}. Trying to undeploy it", module);
        this.uninstallComponents(module);
        if (this.epsStatisticsRegister.isStatisticsOn()) {
            flowsDeployed.dec();
        }
        return true;
    }

    /**
     * Uninstall components.
     *
     * @param module
     *            the module
     */
    private void uninstallComponents(final EpsModule module) {
        final String moduleIdentifier = module.getUniqueModuleIdentifier();
        for (final EpsModuleComponent component : module.getModuleComponents().values()) {
            log.debug("Uninstalling component [{}]", component);
            final EpsModuleComponentInstaller installer = provider.loadEpsModuleComponentInstaller(component.getComponentType());
            log.trace("Asking {} to uninstall flow {}", installer, moduleIdentifier);
            installer.uninstallModule(moduleIdentifier);
            log.trace("{} uninstalled {}", installer, moduleIdentifier);
        }
        componentContextHandler.undeployModule(moduleIdentifier);
    }

    @Override
    public int getDeployedModulesCount() {
        return installedModules.size();
    }

    @Override
    public int undeployAllModules() {
        final int numberOfDeployedModules = installedModules.size();
        log.debug("Removing all {} deployed flows [{}]", numberOfDeployedModules, installedModules);
        final Set<String> moduleIds = new HashSet<>();
        moduleIds.addAll(installedModules.keySet());
        int undeployedCount = 0;
        for (final String moduleId : moduleIds) {
            log.debug("Trying to undeploy flow [{}]", moduleId);
            final boolean undeployed = this.undeployModule(moduleId);
            if (undeployed) {
                log.info("Successfully undeployed flow {}", moduleId);
                undeployedCount++;
            } else {
                log.warn("Was not able to successfully undeploy flow with id [{}]", moduleId);
            }
        }
        installedModules.clear();
        log.debug("Undeployed {} flows", undeployedCount);
        return undeployedCount;
    }

    /**
     * Send control event to module.
     *
     * @param module
     *            the module
     * @param controlEvent
     *            the control event
     * @return the int
     */
    private int sendControlEventToModule(final EpsModule module, final ControlEvent controlEvent) {
        int componentReceivedCount = 0;
        for (final Controllable controllable : module.getControllableComponents()) {
            controllable.react(controlEvent);
            componentReceivedCount++;
        }
        if (log.isTraceEnabled()) {
            log.trace("Sent {} to {} components inside {}", new Object[] { controlEvent, componentReceivedCount, module });
        }
        return componentReceivedCount;
    }

    @Override
    public int sendControlEventToAllModules(final ControlEvent controlEvent) {
        if (controlEvent == null) {
            throw new IllegalArgumentException("Control event must not be null");
        }
        log.debug("Sending control event {} to all flows", controlEvent);
        int modulesReceivedTotal = 0;
        for (final EpsModule module : installedModules.values()) {
            this.sendControlEventToModule(module, controlEvent);
            modulesReceivedTotal++;
        }
        log.debug("Sent {} to {} modules", controlEvent, modulesReceivedTotal);
        return modulesReceivedTotal;
    }

    @Override
    public boolean sendControlEventToModuleById(final String moduleIdentifier, final ControlEvent controlEvent) {
        if ((moduleIdentifier == null) || moduleIdentifier.isEmpty()) {
            throw new IllegalArgumentException("flow identifier must not be null or empty");
        }
        if (controlEvent == null) {
            throw new IllegalArgumentException("Control event must not be null");
        }
        log.debug("Trying to send {} to flow with identifier [{}]", controlEvent, moduleIdentifier);
        final EpsModule module = installedModules.get(moduleIdentifier);
        if (module != null) {
            log.debug("Found {} - sending control event {} to it", module, controlEvent);
            this.sendControlEventToModule(module, controlEvent);
            return true;
        } else {
            log.warn("Was not able to find flow with identifier [{}]", moduleIdentifier);
        }
        return false;
    }

    @Override
    public int sendControlEventToEventHandlerById(final String moduleIdentifier, final String eventHandlerId, final ControlEvent controlEvent) {
        if (EpsUtil.isEmpty(moduleIdentifier)) {
            throw new IllegalArgumentException("flow identifier must not be null or empty");
        }
        if (EpsUtil.isEmpty(eventHandlerId)) {
            throw new IllegalArgumentException("event handler identifier must not be null or empty");
        }
        if (controlEvent == null) {
            throw new IllegalArgumentException("Control event must not be null");
        }
        log.debug("Trying to send {} to flow with identifier [{}] and event handler with identifier [{}]", controlEvent, moduleIdentifier,
                eventHandlerId);
        final EpsModule module = installedModules.get(moduleIdentifier);
        if (module != null) {
            log.debug("Found {} - sending control event {} to it", module, controlEvent);
            final Controllable controllable = module.getControllableComponent(eventHandlerId);
            if (controllable != null) {
                controllable.react(controlEvent);
                log.debug("Successfully delivered {} to component with id {}", controlEvent, eventHandlerId);
                return 1;
            } else {
                log.warn("Was not able to find event handler with id {} in flow {}", eventHandlerId, moduleIdentifier);
            }
            return 0;
        } else {
            log.warn("Was not able to find flow with identifier [{}]", moduleIdentifier);
        }
        return 0;
    }

    /**
     * Gets the eps xml file parser.
     *
     * @return the eps xml file parser
     */
    protected EpsXmlFileParser getEpsXmlFileParser() {
        return new EpsXmlFileParser();
    }

    /**
     * Gets the modeled flow parser.
     *
     * @return the modeled flow parser
     */
    protected EpsModeledFlowParser getModeledFlowParser() {
        return new EpsModeledFlowParser();
    }

    /**
     * Initialise statistics.
     *
     * Creates a counter to track deployed flows, if statistics enabled. Counter name will include the EPS instanceId if set, otherwise a generated
     * instance id is used.
     *
     * @param epsStatisticsRegister
     *            the EpsStatisticsRegister instance
     */
    private void initialiseStatistics(final EpsStatisticsRegister epsStatisticsRegister) {
        if (this.epsStatisticsRegister.isStatisticsOn()) {
            final String counterName = getEpsInstanceId() + ".flowsDeployed";
            flowsDeployed = epsStatisticsRegister.createCounter(counterName);
            log.debug("Created counter {}", counterName);
        }
    }

    private String getEpsInstanceId() {
        String epsInstanceId = EpsUtil.getEpsInstanceIdentifier();
        if (EpsUtil.isEmpty(epsInstanceId)) {
            try {
                epsInstanceId = (String) new javax.naming.InitialContext().lookup("java:module/ModuleName");
                log.trace("EPS instance Id not set, using module name {}", epsInstanceId);
            } catch (final NamingException e) {
                epsInstanceId = "eps_instance_" + new Random().nextInt(1000) + "_" + System.currentTimeMillis();
                log.trace("EPS instance Id not set, using random created value {} for counter name.", epsInstanceId);
            }
        }
        return epsInstanceId;
    }

    /**
     * @return an EpsStatisticsRegister instance
     */
    private EpsStatisticsRegister getEpsStatisticsRegister() {
        return epsStatisticsRegister;
    }

    private void updateStatisticsWithModulesDeployed() {
        if (this.epsStatisticsRegister.isStatisticsOn()) {
            flowsDeployed.inc();
        }
    }

}
