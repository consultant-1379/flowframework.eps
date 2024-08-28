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
package com.ericsson.component.aia.services.eps.io.adapter.hc;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.*;

/**
 *
 * @author eborziv
 *
 */
public abstract class HazelcastAdapterUtil {

    public static final String HAZELCAST_URI_SCHEME = "hazelcast:/";

    public static final String CHANNEL_NAME_PROP = "channelName";

    public static final String HAZELCAST_CONFIG_FILE_URI_PROP = "hazelcastConfigFileUrl";

    public static final String HAZELCAST_LISTENER_THREAD_POOL_SIZE = "hazelcastListenerThreadpoolSize";

    public static final String HAZELCAST_LISTENER_THREAD_POOL_THREAD_PRIORITY = "hazelcastListenerThreadpoolThreadPriority";

    private static final String DEFAULT_HAZELCAST_CONFIG_FILE_URI = "__DEFAULT_HAZELCAST_CONFIGURATION__";

    /*
     * Here we cache already created hazelcast instances - mapped to their configuration file, so that we reuse them per JVM
     */
    private static final ConcurrentHashMap<String, SharedHazelcastInstance> CREATED_HAZELCAST_INSTANCES = new ConcurrentHashMap<>();

    private static final int HAZELCAST_WAIT_FOR_SHUTDOWN_BEFORE_TERMINATION_MILLIS = 100;

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastAdapterUtil.class);

    /**
     * Gets the or create hazelcast instance from {@link Configuration}.
     *
     * @param config
     *            the configuration {@link Configuration}
     * @return the or create hazelcast instance
     */
    static synchronized HazelcastInstance getOrCreateHazelcastInstance(final Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        final HazelcastInstance cachedInstance = getCachedHazelcastInstance(config);
        if (cachedInstance != null) {
            LOG.info("Found already cached hazelcast instance for configuration {}. Will reuse it!", config);
            return cachedInstance;
        }
        LOG.trace("Was not able to find cached hazelcast instance. Creating new hazelcast instance for {}", config);
        final String configFileUri = config.getStringProperty(HAZELCAST_CONFIG_FILE_URI_PROP);
        InputStream inputStream = null;
        if ((configFileUri != null) && !configFileUri.isEmpty()) {
            LOG.debug("Trying to use hazelcast configuration file [{}]", configFileUri);
            inputStream = ResourceManagerUtil.loadResourceAsStreamFromURI(configFileUri);
            LOG.debug("Successfully loaded hazelcast configuration file from uri [{}]", configFileUri);
        } else {
            LOG.info("Did not find non-null, non-empty configuration property named [{}]", HAZELCAST_CONFIG_FILE_URI_PROP);
        }
        Config cfg = null;
        if (inputStream != null) {
            LOG.info("Using hazelcast configuration [{}]", configFileUri);
            cfg = new XmlConfigBuilder(inputStream).build();
        } else {
            LOG.info("Using default hazelcast configuration because {} was not specified!", HAZELCAST_CONFIG_FILE_URI_PROP);
            cfg = new Config();
        }
        Resources.safeClose(inputStream);
        final HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(cfg);
        LOG.info("Create hazelcast instance {}", hzInstance);
        cacheHazelcastInstance(config, hzInstance);
        return hzInstance;
    }

    /**
     * Tries to find already cached hazelcast instance for given configuration. Returns null if none can be found.
     *
     * @param config
     *            {@link Configuration}
     * @return the cached hazelcast instance
     */
    private static HazelcastInstance getCachedHazelcastInstance(final Configuration config) {
        final String configFileUri = getCachedHazelcastInstanceIdentifier(config);
        LOG.debug("Trying to find already created hazelcast instance for configuration {}", configFileUri);
        final SharedHazelcastInstance shi = CREATED_HAZELCAST_INSTANCES.get(configFileUri);
        if (shi != null) {
            return shi.getInstance();
        }
        return null;
    }

    private static boolean removeCachedHazelcastInstance(final Configuration config) {
        final String configFileUri = getCachedHazelcastInstanceIdentifier(config);
        LOG.debug("Trying to find already created hazelcast instance for configuration {}", configFileUri);
        final SharedHazelcastInstance shi = CREATED_HAZELCAST_INSTANCES.get(configFileUri);
        if (shi != null) {
            final boolean destroyed = shi.destroy();
            if (destroyed) {
                CREATED_HAZELCAST_INSTANCES.remove(configFileUri);
            }
            return destroyed;
        } else {
            LOG.warn("Was not able to find and remove hazelcast instance for {}", config);
            return false;
        }
    }

    private static String getCachedHazelcastInstanceIdentifier(final Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        String configFileUri = config.getStringProperty(HAZELCAST_CONFIG_FILE_URI_PROP);
        if ((configFileUri == null) || configFileUri.trim().isEmpty()) {
            configFileUri = DEFAULT_HAZELCAST_CONFIG_FILE_URI;
        }
        return configFileUri;
    }

    private static void cacheHazelcastInstance(final Configuration config, final HazelcastInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Unable to cache null hazelcast instance!");
        }
        final String configFileUri = getCachedHazelcastInstanceIdentifier(config);
        LOG.debug("Caching hazelcast instance for configuration {}", configFileUri);
        final SharedHazelcastInstance shi = new SharedHazelcastInstance(instance);
        CREATED_HAZELCAST_INSTANCES.putIfAbsent(configFileUri, shi);
        LOG.debug("Cached hazelcast instance {}. All cached instances are {}", instance, CREATED_HAZELCAST_INSTANCES);
    }

    /**
     * Gets the number of cached hazelcast instances.
     *
     * @return the number of cached hazelcast instances
     */
    static int getNumberOfCachedHazelcastInstances() {
        return CREATED_HAZELCAST_INSTANCES.size();
    }

    /**
     * Shutdown hazelcast instance from {@link Configuration}.
     *
     * @param config
     *            The configuration {@link Configuration}
     * @return true, if successful
     */
    static synchronized boolean shutdownHazelcastInstance(final Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null!");
        }
        final boolean destroyed = removeCachedHazelcastInstance(config);
        if (!destroyed) {
            LOG.error("Was not able to kill hazelcast instance for {}! Instances are {}", config, CREATED_HAZELCAST_INSTANCES);
        }
        return destroyed;
    }

    /**
     * The Class SharedHazelcastInstance.
     */
    static class SharedHazelcastInstance {
        private final HazelcastInstance instance;
        private final AtomicInteger numberOfUses = new AtomicInteger(1);

        /**
         * Instantiates a new shared hazelcast instance from {@link HazelcastInstance}.
         *
         * @param inst
         *            {@link HazelcastInstance}
         */
        public SharedHazelcastInstance(final HazelcastInstance inst) {
            instance = inst;
        }

        /**
         * Destroy the shared Hazelcast Instance if it's no more used by any adapter.
         *
         * @return true, if successful
         */
        public boolean destroy() {
            final int totalUses = numberOfUses.decrementAndGet();
            if (totalUses == 0) {
                LOG.debug("Last use of {}. Will be killed!", instance);
                return killHazelcastInstance();
            } else {
                LOG.debug("Instance {} will not be killed because it is used by {} more adapters", instance, totalUses);
                return false;
            }
        }

        private boolean killHazelcastInstance() {
            LOG.info("Trying to shutdown hazelcast instance {}", instance);
            final LifecycleService lifecycle = instance.getLifecycleService();
            if (lifecycle.isRunning()) {
                LOG.info("Hazelcast instance is running. Will try to shut it down!");
                lifecycle.shutdown();
                for (int i = 0; i < 5; i++) {
                    if (!lifecycle.isRunning()) {
                        LOG.info("Hazelcast gracefully shut down!");
                        break;
                    } else {
                        if (i == 4) {
                            LOG.warn("Even after shutdown() executed hazelcast instance {} is still running. Forcing shutdown!",
                                    HAZELCAST_WAIT_FOR_SHUTDOWN_BEFORE_TERMINATION_MILLIS, instance);
                            lifecycle.shutdown();
                            LOG.info("Executed shutdown() method on {}", instance);
                        } else {
                            try {
                                Thread.sleep(HAZELCAST_WAIT_FOR_SHUTDOWN_BEFORE_TERMINATION_MILLIS);
                            } catch (final Exception ignored) {
                                if (LOG.isTraceEnabled()) {
                                    LOG.trace("Ignored exception: ", ignored);
                                }
                            }
                        }
                    }
                }
                return true;
            } else {
                LOG.warn("Hazelcast instance was already shut down. Nothing to do here!");
                return false;
            }
        }

        /**
         * Gets the single instance of SharedHazelcastInstance.
         *
         * @return single instance of SharedHazelcastInstance
         */
        public HazelcastInstance getInstance() {
            final int totalUses = numberOfUses.incrementAndGet();
            LOG.debug("Registering additional use for {}. In total {} uses", instance, totalUses);
            return instance;
        }

        @Override
        public String toString() {
            return "SharedHazelcastInstance [instance=" + instance + ", numberOfUses=" + numberOfUses + "]";
        }
    }

}
