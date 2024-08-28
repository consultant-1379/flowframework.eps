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
package com.ericsson.component.aia.services.eps.core.util;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.component.module.assembler.EpsModuleComponentInstaller;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngine;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngineContext;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;

/**
 *
 * This class is responsible for loading SPI instances of different parts of EPS. This is an entry point to finding any implementation class inside
 * EPS.
 *
 * @author eborziv
 *
 */
public class EpsProvider {

    private static final Collection<ProcessingEngine> loadedProcessingEngines = new LinkedList<>();

    private static final Map<EpsModuleComponentType, EpsModuleComponentInstaller> loadedModuleComponentInstallers = new HashMap<>();

    private static final Map<String, InputAdapter> loadedInputAdapters = new HashMap<String, InputAdapter>();

    private static final Map<String, OutputAdapter> loadedOutputAdapters = new HashMap<String, OutputAdapter>();

    private static EpsProvider instance;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        instance = new EpsProvider();
    }

    private EpsProvider() {
        // not exposed - this class is supposed to be singleton
    }

    public static EpsProvider getInstance() {
        return instance;
    }

    /**
     * For a given uri tries to load {@link InputAdapter} that understands that particular URI. During initialization of input adapter it will use
     * given instance identifier. {@link InputAdapter} instances are not singletones, new instance is created and initialized whenever this method is
     * invoked.
     *
     * @param uri
     *            the uri for which we are trying to find input adapter. Must not be null or empty string.
     * @param instanceId
     *            the unique instance identifier to be used when initializing input adapter instance. This will become unique id of
     *            {@link InputAdapter} instance. Must not be null or empty string.
     * @return initialized instance of input adapter or null if none could be found for specified uri
     */
    public InputAdapter loadInputAdapter(final String uri, final String instanceId) {
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalArgumentException("Unable to load input adapter for null or empty URI");
        }
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("instance identifier must not be null or empty");
        }
        logger.debug("Trying to find input adapter with id {}", instanceId);
        final InputAdapter inputAdapter = loadedInputAdapters.get(instanceId);
        if (inputAdapter != null) {
            logger.debug("Found existing input adapter by id {}", instanceId);
            if (!inputAdapter.understandsURI(uri)) {
                throw new IllegalArgumentException("Existing input adapter by id " + instanceId + " does not understand uri " + uri);
            }
            logger.warn("Found existing input adapter by name {}. Input adapters are reused inside JVM, across all modules! Adapter details are {}",
                    instanceId, inputAdapter);
            return inputAdapter;
        }

        logger.debug("Trying to find FF input adapter for URI [{}]", uri);
        final ServiceLoader loader = ServiceLoader.load(InputAdapter.class);

        final Iterator iter = loader.iterator();
        while (iter.hasNext()) {
            final InputAdapter adapter = (InputAdapter) iter.next();
            logger.debug("Found FF input adapter {}. Checking if it understands uri {}", adapter, uri);
            if (adapter.understandsURI(uri)) {
                logger.debug("Found {} that understands [{}]. Full class name is {}", adapter, uri, adapter.getClass().getName());
                loadedInputAdapters.put(instanceId, adapter);
                logger.debug("Cached input adapter {}", adapter);
                return adapter;
            }
        }

        /* ************************************************************************************* */
        /* can be removed when backward compatibility with EPS InputAdapter is no longer needed. */
        final InputAdapter epsInputAdapter = loadEpsInputAdapter(uri, instanceId);

        if (epsInputAdapter != null) {
            return epsInputAdapter;
        }
        /* ************************************************************************************** */

        logger.warn("Was not able to find input adapter that understands [{}]. Check validity of URI and packaging of application!", uri);
        return null;
    }

    /**
     * Look for deprecated {@link com.ericsson.component.aia.services.eps.adapter.InputAdapter}. Added for backward compatibility with
     * {@link com.ericsson.component.aia.services.eps.adapter.InputAdapter} Interface
     *
     * @param uri
     *            the uri for which we are trying to find input adapter. Must not be null or empty string.
     * @param instanceId
     *            the unique instance identifier to be used when initializing input adapter instance. This will become unique id of
     *            {@link InputAdapter} instance. Must not be null or empty string.
     * @return initialized instance of input adapter or null if none could be found for specified uri.
     */
    private InputAdapter loadEpsInputAdapter(final String uri, final String instanceId) {
        logger.debug("Trying to find deprecated EPS input adapter for URI [{}]", uri);
        ServiceLoader loader = ServiceLoader.load(InputAdapter.class);

        Iterator iter = loader.iterator();
        loader = ServiceLoader.load(com.ericsson.component.aia.services.eps.adapter.InputAdapter.class);

        iter = loader.iterator();
        while (iter.hasNext()) {
            final com.ericsson.component.aia.services.eps.adapter.InputAdapter legacyAdapter = (com.ericsson.component.aia.services.eps.adapter.InputAdapter) iter.next();
            logger.debug("Found deprecated EPS input adapter {}. Checking if it understands uri {}", legacyAdapter, uri);
            if (legacyAdapter.understandsURI(uri)) {
                logger.debug("Found {} that understands [{}]. Full class name is {}", legacyAdapter, uri, legacyAdapter.getClass().getName());
                logger.trace("Compatibility adaptation: from class {} to class {}", legacyAdapter.getClass().getName(), InputAdapter.class.getName());
                logger.warn("Found deprecated EPS input adapter {} that understands uri {}. You should consider to implement: {} class from FF APIs",
                        legacyAdapter, uri, OutputAdapter.class.getName());
                final InputAdapter ffInputAdapter = (InputAdapter) CompatibilityHelper.newInstance(legacyAdapter);
                loadedInputAdapters.put(instanceId, ffInputAdapter);
                logger.debug("Cached input adapter {}", ffInputAdapter);
                return ffInputAdapter;
            }
        }

        return null;
    }

    /**
     * For a given uri tries to load {@link OutputAdapter} that understands that particular URI. During initialization of output adapter it will use
     * given instance identifier. {@link OutputAdapter} instances are not singletones, new instance is created and initialized whenever this method is
     * invoked.
     *
     * @param uri
     *            the uri for which we are trying to find output adapter. Must not be null or empty string.
     * @param instanceId
     *            the unique instance identifier to be used when initializing output adapter instance. This will become unique id of
     *            {@link OutputAdapter} instance. Must not be null or empty string.
     * @return initialized instance of output adapter or null of none could be found for specified uri.
     */
    public OutputAdapter loadOutputAdapter(final String uri, final String instanceId) {
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalArgumentException("Unable to load output adapter for null or empty URI");
        }
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("instance identifier must not be null or empty");
        }
        logger.debug("Trying to find output adapter with id {}", instanceId);
        final OutputAdapter outputAdapter = loadedOutputAdapters.get(instanceId);
        if (outputAdapter != null) {
            logger.debug("Found existing output adapter by id {}", instanceId);
            if (!outputAdapter.understandsURI(uri)) {
                throw new IllegalArgumentException("Existing output adapter by id " + instanceId + " does not understand uri " + uri);
            }
            logger.warn(
                    "Found existing output adapter by name {}. Output adapters are reused inside JVM, across all modules! Adapter details are {}",
                    instanceId, outputAdapter);
            return outputAdapter;
        }
        logger.debug("Trying to find output adapter for URI [{}]", uri);
        final ServiceLoader loader = ServiceLoader.load(OutputAdapter.class);
        final Iterator iter = loader.iterator();
        while (iter.hasNext()) {
            final OutputAdapter adapter = (OutputAdapter) iter.next();
            logger.trace("Found output adapter {}", adapter);
            if (adapter.understandsURI(uri)) {
                logger.debug("Found {} that understands [{}]", adapter, uri);
                loadedOutputAdapters.put(instanceId, adapter);
                logger.debug("Cached output adapter {}", adapter);
                return adapter;
            }
        }

        /* ************************************************************************************** */
        /* can be removed when backward compatibility with EPS OutputAdapter is no longer needed. */
        final OutputAdapter epsOutputAdapter = loadEpsOutputAdapter(uri, instanceId);

        if (epsOutputAdapter != null) {
            return epsOutputAdapter;
        }
        /* ************************************************************************************** */

        logger.warn("Was not able to find output adapter that understands [{}]. Check validity of URI and packaging of application!", uri);
        return null;
    }

    /**
     * Look for deprecated {@link com.ericsson.component.aia.services.eps.adapter.OutputAdapter}. Added for backward compatibility with
     * {@link com.ericsson.component.aia.services.eps.adapter.OutputAdapter} Interface.
     *
     * @param uri
     *            the uri for which we are trying to find output adapter. Must not be null or empty string.
     * @param instanceId
     *            the unique instance identifier to be used when initializing output adapter instance. This will become unique id of
     *            {@link OutputAdapter} instance. Must not be null or empty string.
     * @return initialized instance of output adapter or null of none could be found for specified uri.
     */
    private OutputAdapter loadEpsOutputAdapter(final String uri, final String instanceId) {
        // Added for compatibility with EPS OutputAdapter Interface
        logger.debug("Trying to find deprecated EPS output adapter for URI [{}]", uri);
        final ServiceLoader loader = ServiceLoader.load(com.ericsson.component.aia.services.eps.adapter.OutputAdapter.class);

        final Iterator iter = loader.iterator();
        while (iter.hasNext()) {
            final com.ericsson.component.aia.services.eps.adapter.OutputAdapter legacyAdapter = (com.ericsson.component.aia.services.eps.adapter.OutputAdapter) iter
                    .next();
            logger.debug("Found deprecated EPS output adapter {}. Checking if it understands uri {}", legacyAdapter, uri);
            if (legacyAdapter.understandsURI(uri)) {
                logger.debug("Found {} that understands [{}]. Full class name is {}", legacyAdapter, uri, legacyAdapter.getClass().getName());
                logger.trace("Compatibility adaptation: from class {} to class {}", legacyAdapter.getClass().getName(),
                        OutputAdapter.class.getName());
                logger.warn(
                        "Found deprecated EPS output adapter {} that understands uri {}. You should consider to implement: {} class from FF APIs",
                        legacyAdapter, uri, OutputAdapter.class.getName());
                final OutputAdapter ffOutputAdapter = (OutputAdapter) CompatibilityHelper.newInstance(legacyAdapter);
                loadedOutputAdapters.put(instanceId, ffOutputAdapter);
                logger.debug("Cached output adapter {}", ffOutputAdapter);
                return ffOutputAdapter;
            }
        }

        return null;
    }

    /**
     * Tries to find {@link EpsModuleComponentInstaller} for given type. For every given type there will be one singleton instance of
     * {@link EpsModuleComponentInstaller}.
     *
     * @param type
     *            the type for which installer is needed
     * @return installer if can be found (or loaded) or null otherwise
     */
    public synchronized EpsModuleComponentInstaller loadEpsModuleComponentInstaller(final EpsModuleComponentType type) {
        logger.debug("Trying to find component installer for [{}]", type);
        final EpsModuleComponentInstaller componentInstaller = loadedModuleComponentInstallers.get(type);
        if (componentInstaller == null) {
            final ServiceLoader loader = ServiceLoader.load(EpsModuleComponentInstaller.class);
            final Iterator iter = loader.iterator();
            while (iter.hasNext()) {
                final EpsModuleComponentInstaller installer = (EpsModuleComponentInstaller) iter.next();
                logger.trace("Found installer {}", installer);
                if ((installer.getSupportedTypes() == null) || (installer.getSupportedTypes().length == 0)) {
                    throw new IllegalStateException("Installer " + installer + " must return supported types!");
                }
                for (final EpsModuleComponentType epsModuleComponentType : installer.getSupportedTypes()) {
                    if (epsModuleComponentType == type) {
                        logger.debug("Found matching installer for type {}", type);
                        loadedModuleComponentInstallers.put(type, installer);
                        return installer;
                    }
                }
            }
        }
        if (componentInstaller == null) {
            logger.error("Was not able to find component installer for type {}", type);
        } else {
            logger.info("Found cached matching component installer for type {} - {}", type, componentInstaller);
        }
        return componentInstaller;
    }

    /**
     * Tries to find already initialized processing engine with specified unique identifier. Will not try to create processing engine in case it can
     * not find existing one.
     *
     * @param engineUniqueIdentifier
     *            identifier of engine to be found. Must not be null or empty string
     * @return engine matching given identifier or null otherwise
     */
    public synchronized ProcessingEngine getCreatedProcessingEngine(final String engineUniqueIdentifier) {
        if ((engineUniqueIdentifier == null) || engineUniqueIdentifier.isEmpty()) {
            throw new IllegalArgumentException("Engine unique identifer must not be null or empty");
        }
        logger.debug("Trying to find previously created PE with identifier {}", engineUniqueIdentifier);
        for (final ProcessingEngine cachedPE : loadedProcessingEngines) {
            if (cachedPE.getInstanceId().equals(engineUniqueIdentifier)) {
                logger.debug("Found matching processing engine {}", cachedPE);
                return cachedPE;
            }
        }
        return null;
    }

    /**
     * Return unmodifiable view of all existing processing engines which are in STARTED state.
     *
     * @return unmodifiable view of all existing processing engines.
     */
    public synchronized Collection<ProcessingEngine> getAllProcessingEngines() {
        return Collections.unmodifiableCollection(loadedProcessingEngines);
    }

    /**
     * Loads processing engine by specified engine type, unique identifier and configuration. It first tries to find already started processing engine
     * with given unique identifier. If there is one immediately returns that processing engine instance. If there is no already created processing
     * engine instance with given identifier it creates new one using provided parameters.
     *
     * @param processingEngineType
     *            the type of ProcessingEngine to load
     * @param engineUniqueIdentifier
     *            the unique identifier for the ProcessingEngine
     * @param config
     *            the {@link Configuration} for the ProcessingEngine
     * @return the loaded {@link ProcessingEngine}
     * @throws IllegalArgumentException
     *             if the engineUniqueIdentifier is not unique or if the ProcessingEngine could not be found
     */
    public synchronized ProcessingEngine loadProcessingEngine(final String processingEngineType, final String engineUniqueIdentifier,
                                                              final Configuration config) {
        final ProcessingEngine cachedProcessingEngine = getCreatedProcessingEngine(engineUniqueIdentifier);
        if (cachedProcessingEngine != null) {
            if (!cachedProcessingEngine.getEngineType().equals(processingEngineType)) {
                logger.error("Engine {} found with identifier {} but is not of requested type {}", cachedProcessingEngine, engineUniqueIdentifier,
                        processingEngineType);
                throw new IllegalArgumentException("Engine identifier " + engineUniqueIdentifier + " is not unique");
            }
            return cachedProcessingEngine;
        }
        logger.info("Was not able to find cached PE by type {} and identifier {}. Creating it...", processingEngineType, engineUniqueIdentifier);
        final ServiceLoader loader = ServiceLoader.load(ProcessingEngine.class);
        final Iterator iter = loader.iterator();
        if (iter == null) {
            throw new IllegalStateException("Was not able to find any implementation for [" + ProcessingEngine.class
                    + "]. Please check classpath (packaging) of your application!");
        }
        ProcessingEngine impl = null;
        while (iter.hasNext()) {
            impl = (ProcessingEngine) iter.next();
            logger.debug("Found {}. Checking if engine type matches", impl);
            if (processingEngineType.equals(impl.getEngineType())) {
                logger.debug("Found {} matching type {}. Initiating...", impl, processingEngineType);
            }
            final ProcessingEngineContext ctx = new ProcessingEngineContext(config);
            impl.setInstanceId(engineUniqueIdentifier);
            logger.debug("Successfully created {}. Caching it for future use", impl);
            loadedProcessingEngines.add(impl);
            logger.debug("Cached {}. Trying to start it...", impl);
            impl.start(ctx);
            logger.debug("Started {}", impl);
            return impl;
        }
        logger.info("Was not able to find processing engine of matching type = {}. Check your packaging.", processingEngineType);
        return null;
    }

    /**
     * Used to clean all previously loaded components.
     */
    public void clean() {
        loadedInputAdapters.clear();
        loadedOutputAdapters.clear();
        loadedProcessingEngines.clear();
    }

    /**
     * Removes the IO adapters for the specified {@code EpsModule}
     *
     * @param module
     *            the {@link EpsModule} to remove IO adapters from
     */
    public void removeIoAdapters(final EpsModule module) {
        for (final EpsModuleComponent component : module.getModuleComponents().values()) {
            loadedInputAdapters.remove(component.getComponentId());
            loadedOutputAdapters.remove(component.getComponentId());
        }
    }

}
