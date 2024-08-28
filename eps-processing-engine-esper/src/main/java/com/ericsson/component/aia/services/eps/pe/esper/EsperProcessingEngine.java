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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleHandlerType;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsModuleRule;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngineContext;
import com.ericsson.component.aia.services.eps.pe.core.AbstractProcessingEngine;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.espertech.esper.client.*;
import com.espertech.esper.client.deploy.*;

/**
 *
 * @author eborziv
 *
 */
public class EsperProcessingEngine extends AbstractProcessingEngine {

    private static final Set<String> CREATED_ESPER_ENGINE_NAMES = new HashSet<>();

    private final Map<String, String> epsModuleEsperModuleMapping = new HashMap<String, String>();

    private EPServiceProvider epServiceProvider;

    private final Configuration esperConfiguration;

    /**
     * Instantiates a new esper processing engine.
     */
    public EsperProcessingEngine() {
        setEngineType(EpsModuleHandlerType.ESPER_HANDLER.toString());
        esperConfiguration = new Configuration();
    }

    @Override
    public void doStart(final ProcessingEngineContext peContext) {
        if (peContext == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        if (peContext.getConfiguration() == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        log.debug("Creating engine instance of type {} with configuration {}", getEngineType(), peContext.getConfiguration());
        final String configurationFileUrl = peContext.getConfiguration()
                .getStringProperty(EsperEngineConstants.ESPER_CONFIGURATION_FILE_PARAM_NAME);
        Document doc = null;
        if ((configurationFileUrl != null) && !configurationFileUrl.isEmpty()) {
            log.debug("Trying to use file resource [{}] for esper configuration", configurationFileUrl);
            doc = parseConfigurationDocument(configurationFileUrl);
        }
        Configuration finalConfig = esperConfiguration;
        if (doc != null) {
            log.info("Esper engine will use configuration from {}", configurationFileUrl);
            finalConfig = esperConfiguration.configure(doc);
        } else {
            log.warn("Esper will use default configuration since no custom configuration file was provided!");
        }
        log.info("Esper engine with id [{}] configured with {}", getInstanceId(), finalConfig);
        epServiceProvider = EPServiceProviderManager.getProvider(getInstanceId(), finalConfig);
        log.debug("Initializing Esper service provider with name [{}]", getInstanceId());
        epServiceProvider.initialize();
        CREATED_ESPER_ENGINE_NAMES.add(getInstanceId());
        final int totalEnginesCreated = CREATED_ESPER_ENGINE_NAMES.size();
        if (totalEnginesCreated > 1) {
            log.warn("In total created {} Esper engines inside this JVM. Engines are {}", totalEnginesCreated, CREATED_ESPER_ENGINE_NAMES);
        } else {
            log.debug("Created first Esper engine inside this JVM with name {}", getInstanceId());
        }
        log.debug("Successfully started engine of type {} and instance identifier {}", getEngineType(), getInstanceId());
    }

    private Document parseConfigurationDocument(final String configurationUri) {
        final InputStream inputStream = ResourceManagerUtil.loadResourceAsStreamFromURI(configurationUri);
        if (inputStream != null) {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            try {
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(inputStream);
                log.debug("Successfully parsed esper configuration at [{}]", configurationUri);
                return doc;
            } catch (final Exception exc) {
                log.error("Exception while parsing esper configuration [{}]. Details: {}", configurationUri, exc);
            } finally {
                Resources.safeClose(inputStream);
            }
        } else {
            log.warn("Was not able to find resource by name [{}]", configurationUri);
        }
        return null;
    }

    @Override
    public void doStop() {
        log.info("Stopping {}", this);
        epServiceProvider.getEPAdministrator().stopAllStatements();
        epServiceProvider.getEPAdministrator().destroyAllStatements();
        log.info("Successfully stopped {}", this);
    }

    @Override
    public String toString() {
        return "EsperProcessingEngine id = " + getInstanceId() + ", type = " + getEngineType();
    }

    @Override
    public EventInputHandler deployComponent(final EpsModuleComponent epsComponent, final String portName) {
        if (!(epsComponent instanceof EpsRulesModuleComponent)) {
            throw new IllegalArgumentException("Unable to process anything different that type " + EpsRulesModuleComponent.class.getName());
        }
        log.info("Deploying {} to {}", epsComponent, this);
        final EpsRulesModuleComponent epsRulesComp = (EpsRulesModuleComponent) epsComponent;
        final EPAdministrator epAdministrator = epServiceProvider.getEPAdministrator();
        final EPDeploymentAdmin deploymentAdmin = epAdministrator.getDeploymentAdmin();
        deployModules(epsRulesComp, deploymentAdmin);
        final EsperEventInputHandler esperHandler = new EsperEventInputHandler(epsRulesComp, epServiceProvider, portName);
        epAdministrator.startAllStatements();
        return esperHandler;
    }

    private void deployModules(final EpsRulesModuleComponent epsRulesComp, final EPDeploymentAdmin deploymentAdmin) {
        if ((epsRulesComp.getRules() != null) && !epsRulesComp.getRules().isEmpty()) {
            log.debug("Found {} Esper rules to deploy", epsRulesComp.getRules().size());
            for (final EpsModuleRule rule : epsRulesComp.getRules()) {
                deployEsperModule(epsRulesComp, deploymentAdmin, rule);
            }
            log.debug("Successfully deployed {} Esper modules in total", epsRulesComp.getRules().size());
        } else {
            log.info("Did not find any Esper rules to deploy");
        }
    }

    /**
     * @param epsRulesComp
     * @param deploymentAdmin
     * @param rule
     */
    private void deployEsperModule(final EpsRulesModuleComponent epsRulesComp, final EPDeploymentAdmin deploymentAdmin, final EpsModuleRule rule) {
        Module esperModule = null;
        try {
            esperModule = createModuleFromText(rule.getRuleText());
        } catch (final Exception e) {
            throw new IllegalStateException("Exception while parsing module. Details: " + e.getMessage());
        }
        log.debug("Successfully parsed Esper module from text {}. Trying to deploy it...", rule.getRuleText());
        final DeploymentOptions options = new DeploymentOptions();
        options.setCompile(true);
        options.setFailFast(true);
        options.setValidateOnly(false);
        options.setRollbackOnFail(true);
        log.trace("Deploying Esper module {}", esperModule);
        try {
            final DeploymentResult res = deploymentAdmin.deploy(esperModule, options);
            final String deploymentId = res.getDeploymentId();
            final String epsModuleId = epsRulesComp.getModule().getUniqueModuleIdentifier();
            epsModuleEsperModuleMapping.put(epsModuleId, deploymentId);
            log.debug("Successfully deployed module {} - deployment identifier is {}", esperModule, deploymentId);
        } catch (final DeploymentException e) {
            log.error("Exception while deploying module {}. Details: {}", esperModule, e.getMessage());
            throw new IllegalStateException("Exception while deploying rules [" + rule.getRuleText() + "]");
        }
        log.debug("Successfully compiled and deployed an esper module");
    }

    private Module createModuleFromText(final String moduleText) throws IOException, ParseException {
        log.trace("Creating Esper module from {}", moduleText);
        final EPDeploymentAdmin deploymentAdmin = epServiceProvider.getEPAdministrator().getDeploymentAdmin();
        return deploymentAdmin.parse(moduleText);
    }

    @Override
    public Set<String> getDeployedModules() {
        return Collections.unmodifiableSet(epsModuleEsperModuleMapping.keySet());
    }

    @Override
    public boolean undeployModule(final String moduleIdentifier) {
        if ((moduleIdentifier == null) || moduleIdentifier.isEmpty()) {
            throw new IllegalArgumentException("module identifier must not be null or empty");
        }
        final String esperModuleId = epsModuleEsperModuleMapping.get(moduleIdentifier);
        if (esperModuleId != null) {
            log.debug("Found esper module {} for eps module {}", esperModuleId, moduleIdentifier);
            final EPDeploymentAdmin deploymentAdmin = epServiceProvider.getEPAdministrator().getDeploymentAdmin();
            try {
                final UndeploymentResult undeployResult = deploymentAdmin.undeploy(esperModuleId);
                log.info("Successfully undeployed module with id {}. Undeployment result is {}", moduleIdentifier, undeployResult);
                deploymentAdmin.remove(esperModuleId);
                log.debug("Successfully removed esper module with id {}", esperModuleId);
                epsModuleEsperModuleMapping.remove(moduleIdentifier);
                return true;
            } catch (final DeploymentException e) {
                log.error("Was not able to undeploy module with id {}. Details: {}", moduleIdentifier, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        log.debug("Was not able to find module by id {}", moduleIdentifier);
        return false;
    }

    @Override
    public EventInputHandler getDeployedComponent(final EpsModuleComponent epsComponent, final String portName) {
        if (!(epsComponent instanceof EpsRulesModuleComponent)) {
            throw new IllegalArgumentException("Unable to process anything different that type " + EpsRulesModuleComponent.class.getName());
        }
        if (EpsUtil.isEmpty(portName)) {
            throw new IllegalArgumentException("PortName must not be null or empty");
        }
        final EpsRulesModuleComponent epsRulesComp = (EpsRulesModuleComponent) epsComponent;
        log.info("Fetching {} with port {}", epsComponent, portName);
        final EsperEventInputHandler esperHandler = new EsperEventInputHandler(epsRulesComp, epServiceProvider, portName);
        return esperHandler;
    }

}
