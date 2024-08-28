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
package com.ericsson.component.aia.services.eps.pe.esper;

import java.util.*;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngine;
import com.ericsson.component.aia.services.eps.pe.core.AbstractComponentsInstaller;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * The Class EsperComponentsInstaller manages the {@link EpsModuleComponentType.ESPER_COMPONENT}.
 */
public class EsperComponentsInstaller extends AbstractComponentsInstaller {

    protected final EpsProvider epsProvider = EpsProvider.getInstance();

    private final Map<String, List<String>> DEPLOYED_RULES_PER_ESPER_ENGINE = new HashMap<>();

    @Override
    public EpsModuleComponentType[] getSupportedTypes() {
        return new EpsModuleComponentType[] { EpsModuleComponentType.ESPER_COMPONENT };
    }

    @Override
    public EventInputHandler getOrInstallComponent(final EpsModuleComponent component) {
        return getOrInstallComponentAndReferencePort(component, null);
    }

    @Override
    public EventInputHandler getOrInstallComponentAndReferencePort(final EpsModuleComponent component, final String portName) {
        log.trace("asked to install {}", component);
        if (!(component instanceof EpsModuleStepComponent)) {
            throw new IllegalArgumentException("component must not be null and must be of specific type");
        }
        final EpsModuleStepComponent step = (EpsModuleStepComponent) component;
        final EpsModuleHandlerComponent handler = step.getHandler();
        if (handler.getHandlerType() != EpsModuleHandlerType.ESPER_HANDLER) {
            throw new IllegalArgumentException("Only esper components are acceptable by this installer");
        }

        log.debug("Installing {}", component);
        final String handlerId = handler.getInstanceId();
        log.trace("Handler identifier is {}", handlerId);
        final ProcessingEngine processingEngine = epsProvider.loadProcessingEngine(EpsModuleHandlerType.ESPER_HANDLER.toString(), handlerId, handler
                .getConfiguration());
        log.debug("Loaded Esper handler by name {} and with configuration {}", handlerId, handler.getConfiguration());
        final boolean alreadyDeployed = ruleAlreadyDeployedInsideEsper(step.getInstanceId(), handlerId);
        final EpsRulesModuleComponent ruleComponent = step.getRule();
        if (alreadyDeployed) {
            if (!EpsUtil.isEmpty(portName)) {
                log.debug("Component {} already deployed, returning its reference to port {}", component, portName);
                return processingEngine.getDeployedComponent(ruleComponent, portName);
            } else {
                throw new IllegalStateException("Unable to deploy same rule to same Esper instance more than once!");
            }
        } else {
            log.debug("Could not find already deployed rule with id {} in esper {}", step.getInstanceId(), handlerId);
        }
        return processingEngine.deployComponent(ruleComponent, portName);
    }

    private synchronized boolean ruleAlreadyDeployedInsideEsper(final String ruleId, final String esperId) {
        if ((ruleId == null) || (esperId == null)) {
            throw new IllegalArgumentException("rule id and esper engine id must not be null!");
        }
        List<String> deployedRuleIdentifiers = DEPLOYED_RULES_PER_ESPER_ENGINE.get(esperId);
        if (deployedRuleIdentifiers == null) {
            deployedRuleIdentifiers = new LinkedList<>();
        }
        if (deployedRuleIdentifiers.contains(ruleId)) {
            log.error("Rule with id {} has already been deployed inside esper with id {}", ruleId, esperId);
            return true;
        } else {
            deployedRuleIdentifiers.add(ruleId);
            log.debug("Registering that rule with id {} is deployed inside Esper {}", ruleId, esperId);
        }
        DEPLOYED_RULES_PER_ESPER_ENGINE.put(esperId, deployedRuleIdentifiers);
        return false;
    }

    @Override
    public void uninstallModule(final String moduleId) {
        if ((moduleId == null) || moduleId.isEmpty()) {
            throw new IllegalArgumentException("moduleId must not be null");
        }

        final Collection<ProcessingEngine> processingEngines = epsProvider.getAllProcessingEngines();

        for (final ProcessingEngine processingEngine : processingEngines) {
            log.debug("Looping through process engines to find the deployed esper module {} ", moduleId);
            for (final String deployedModule : processingEngine.getDeployedModules()) {
                if (moduleId.equals(deployedModule)) {
                    log.debug("Found matching process engine {} to undeploy esper module {}", processingEngine.getInstanceId(), deployedModule);
                    if (!undeployModule(processingEngine, moduleId)) {
                        log.error("Unable to uninstall the esper module {} from process engine {}", moduleId, processingEngine.getInstanceId());
                    }
                    break;
                }
            }
        }
    }

    private boolean undeployModule(final ProcessingEngine processingEngine, final String moduleId) {
        final boolean undeployed = processingEngine.undeployModule(moduleId);
        log.info("Undeploying Esper module for moduleId {} ", moduleId);
        // if no more deployed modules... shutdown the instance
        if (processingEngine.getDeployedModules().isEmpty()) {
            processingEngine.destroy();
            log.info("Shutting down Esper instance as no other module deployed.");
            DEPLOYED_RULES_PER_ESPER_ENGINE.remove(processingEngine.getInstanceId());
        }

        return undeployed;
    }
}
