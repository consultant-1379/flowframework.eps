/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.deployer.file;

import java.io.File;
import java.io.InputStream;

import javax.naming.directory.SchemaViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.*;
import com.ericsson.component.aia.services.eps.deployer.util.EpsSchemaValidator;
import com.ericsson.component.aia.services.eps.modules.FlowRepositoryListener;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * @author eborziv Manage the listener for dynamic deploy of flows on file system.
 */
public class FileSystemListenerImpl implements FlowRepositoryListener {

    static final String DEFAULT_MODULE_DEPLOYMENT_FOLDER = "/var/ericsson/eps/flows";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);

    private volatile boolean startedListening;

    private final String watchedFolderUri;

    private FlowFileListener fileListener;

    /**
     * Instantiates a new file system listener impl.
     *
     */
    public FileSystemListenerImpl() {
        watchedFolderUri = findFirstApplicableUri();
        final boolean tryDeployingExistingModulesOnStartup = shouldDeployExistingModulesOnStartup();
        if (tryDeployingExistingModulesOnStartup) {
            findAndDeployExistingModules();
        } else {
            logger.info("Will not consider deploying existing modules found in [{}]", watchedFolderUri);
        }
    }

    private void startListeningForFolderChanges() {
        logger.debug("Subscribing for new deployments...");
        fileListener = new FlowFileListener(this);
        final boolean registered = Resources.registerListener(fileListener);
        logger.debug("Successfully registered to listen for new modules = {}", registered);
    }

    @Override
    public synchronized void startListeningForDeployments() {
        logger.info("Starting listening for new deployments...");
        if (!startedListening) {
            startListeningForFolderChanges();
            startedListening = true;
        }
    }

    @Override
    public synchronized void stopListeningForDeployments() {
        if (startedListening) {
            startedListening = false;
            if (fileListener != null) {
                Resources.unregisterListener(fileListener);
            }
            logger.info("Stopped listening for new deployments");
        }
    }

    public String getURI() {
        return watchedFolderUri;
    }

    private String findFirstApplicableUri() {
        logger.info("Only NEW files will be considered - modified files are not used by EPS");
        logger.debug("Deciding where to look for deployments...");
        final String configuredSysPropFolder = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);
        if ((configuredSysPropFolder != null) && !configuredSysPropFolder.isEmpty()) {
            logger.info("Watching path [{}] for new eps module deployments", configuredSysPropFolder);
            return configuredSysPropFolder;
        }
        logger.debug("Configuration property [{}] not defined. Watching default location [{}] for eps module deployments",
                EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME, FileSystemListenerImpl.DEFAULT_MODULE_DEPLOYMENT_FOLDER);
        return FileSystemListenerImpl.DEFAULT_MODULE_DEPLOYMENT_FOLDER;
    }

    /**
     * On startup will try to find all files (in watched folder) that could possibly be deployable modules into EPS and then try to deploy them into
     * EPS.
     */
    private void findAndDeployExistingModules() {
        logger.info("Trying to find already existing event-flow deployments in [{}]", watchedFolderUri);
        final File watchedFolder = new File(watchedFolderUri);
        if (watchedFolder.canRead() && watchedFolder.isDirectory()) {
            logger.trace("Found readable folder [{}]. Trying to find all files in it", watchedFolderUri);
            final File[] files = watchedFolder.listFiles();
            if ((files != null) && (files.length > 0)) {
                int autoDeployedModules = 0;
                for (final File possibleModuleFile : files) {
                    final String fullPath = possibleModuleFile.getAbsolutePath();
                    if (possibleModuleFile.isFile() && possibleModuleFile.canRead()) {
                        logger.debug("Found readable file [{}]. Will try to deploy it as module", possibleModuleFile);
                        final boolean parsedAndDeployed = parseModuleFromFilePath(fullPath);
                        if (parsedAndDeployed) {
                            autoDeployedModules++;
                        } else {
                            logger.debug("Path [{}] does not point to valid event-flow file", fullPath);
                        }
                    } else {
                        logger.debug("[{}] is not readable file and will not be considered for deployment", fullPath);
                    }
                }
                logger.info("Successfully auto-deployed {} event-flows on startup", autoDeployedModules);
            } else {
                logger.debug("Did not find any files in {}", watchedFolderUri);
            }
        } else {
            logger.error("Path [{}] does not point to readable folder. Will not be able to deploy any modules!", watchedFolderUri);
        }
    }

    /**
     * Parses the module from file path.
     *
     * @param modifiedResourceURI
     *            the modified resource uri
     * @return true, if successful
     */
    public final boolean parseModuleFromFilePath(String modifiedResourceURI) {
        if ((modifiedResourceURI != null) && modifiedResourceURI.endsWith(".xml")) {
            logger.debug("Found event-flow xml file [{}]", modifiedResourceURI);
            modifiedResourceURI = ResourceManagerUtil.FILE_PREFIX + modifiedResourceURI;

            InputStream resource = null;
            try {
                final EpsSchemaValidator schemaValidator = new EpsSchemaValidator(modifiedResourceURI);
                if (!schemaValidator.isCompliantWithSchema()) {
                    logger.error("Schema validation failed for event-flow {} ", modifiedResourceURI);
                    throw new SchemaViolationException("Schema validation failed for eps flow  " + modifiedResourceURI);
                }
                resource = ResourceManagerUtil.loadResourceAsStreamFromURI(modifiedResourceURI);
                if (resource == null) {
                    logger.error("Was not able to load resource [{}]. Unable to deploy new module!", modifiedResourceURI);
                }
                logger.debug("Successfully loaded [{}] as input stream", modifiedResourceURI);
                logger.debug("Trying to deploy module from [{}]", modifiedResourceURI);
                final String moduleId = modulesManager.deployModuleFromFile(resource);
                logger.info("Successfully deployed module from [{}] - module identifier is {}", modifiedResourceURI, moduleId);
                return true;
            } catch (final Throwable exc) {
                logger.error("Caught exception while processing event-flow xml [{}]. Details {}", modifiedResourceURI, exc);
            } finally {
                Resources.safeClose(resource);
            }
        }
        return false;
    }

    private boolean shouldDeployExistingModulesOnStartup() {
        final String deployModulesSysPropValue = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER);
        logger.debug("Configuration property [{}] has value [{}]", EpsConfigurationConstants.DEPLOY_MODULES_EXISTING_IN_WATCHED_DEPLOYMENT_FOLDER,
                deployModulesSysPropValue);
        final boolean deployModulesOnStartup = !("false".equals(deployModulesSysPropValue));
        return deployModulesOnStartup;
    }
}
