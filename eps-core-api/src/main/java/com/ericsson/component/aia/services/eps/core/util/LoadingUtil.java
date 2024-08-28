package com.ericsson.component.aia.services.eps.core.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for loading SPI instances.
 *
 * @author eborziv
 *
 */
public abstract class LoadingUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoadingUtil.class);

    /**
     * Some instances are singletons - so we want to load and initialize them only once per EPS instance.
     */
    private static final Map<Class, Object> singletonInstances = new ConcurrentHashMap<>();

    /**
     * Returns the last loaded implementation of the requested service.
     *
     * @param clazz
     *            The interface or abstract class representing the requested service.
     * @param <T>
     *            the type of the service
     * @return a provider for the requested service. If there is more than one provider available, then the last one loaded is returned.
     * @throws IllegalArgumentException
     *             if clazz is null or the requested service cannot be loaded
     * @see ServiceLoader
     */
    public static <T> T loadOnlyOneInstance(final Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Unable to find implementation for null class");
        }
        logger.debug("Trying to find implementation for {}", clazz);
        final ServiceLoader loader = ServiceLoader.load(clazz);
        final Iterator iter = loader.iterator();
        if (iter == null) {
            throw new IllegalStateException("Was not able to find any implementation for [" + clazz + "]. Please check your packaging!");
        }
        int count = 0;
        Object impl = null;
        while (iter.hasNext()) {
            impl = iter.next();
            count++;
        }
        if (impl == null) {
            throw new IllegalStateException("Was not able to find any implementation for [" + clazz + "]. Please check your packaging!");
        }
        if (count > 1) {
            logger.warn("Found {} implementations of {}. Will use the latest one found {}", new Object[] { count, clazz, impl });
        }
        logger.debug("Successfully found {} implementation of {}", impl, clazz);
        return (T) impl;
    }

    /**
     * Returns a previously loaded implementation if available, otherwise will load the service and return.
     *
     * @param targetClass
     *            The interface or abstract class representing the requested service.
     * @param <T>
     *            the type of the service
     * @return a singleton for the requested service.
     *
     * @throws IllegalArgumentException
     *             if targetClass is null or no service provide is found
     */
    public static <T> T loadSingletonInstance(final Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("target class must not be null");
        }
        logger.debug("Loading singleton instance of {}", targetClass);
        Object instance = singletonInstances.get(targetClass);
        if (instance == null) {
            instance = loadOnlyOneInstance(targetClass);
            singletonInstances.put(targetClass, instance);
            logger.debug("Instance {} created and cached", instance);
        }
        return (T) instance;
    }

}
