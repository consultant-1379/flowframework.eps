package com.ericsson.component.aia.services.eps.pe.java.util;

import java.util.Iterator;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>EpsCdiInstanceManager</code> contains functionality to initialize and start WeldContainer. This class works for JSE and JEE environment.
 *
 * @author ekalkur
 *
 */
public class EpsCdiInstanceManager {

    private static final EpsCdiInstanceManager INSTANCE = new EpsCdiInstanceManager();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private BeanManager beanManager;

    private EpsCdiInstanceManager() {
    }

    public static EpsCdiInstanceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Create and return the object which have the given EL name and are available for injection.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the EL name
     * @return <T> object of the class type.
     */
    public <T> T getBeanInstanceByName(final String name) {

        if (name == null) {
            throw new IllegalArgumentException("Name nust not be null");
        }
        log.debug("Trying to find CDI component by name [{}]", name);
        if (beanManager == null) {
            log.debug("BeanManager was not set. Might not be able to find CDI handlers!");
            tryLookupBeanManagerInJndi();
            if (beanManager == null) {
                // could not find BeanManager anywhere - throw exception!
                throw new IllegalStateException("Bean manager is not initiated. Unable to find CDI component by name [" + name + "]");
            }
        }
        log.trace("Trying to find bean by name [{}] - using bean manager {}", name, beanManager);
        final Iterator<Bean<?>> iterator = beanManager.getBeans(name).iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("CDI bean manager cannot find a CDI bean by the requested name [" + name
                    + "]. Make sure you are using javax.inject.Named annotation on event handlers and that beans.xml file is present!");
        }
        final Bean<T> bean = (Bean<T>) iterator.next();
        log.trace("Successfully found {} by name {}", bean, name);
        final CreationalContext<T> context = beanManager.createCreationalContext(bean);
        log.trace("Got context {} for bean by name {}", context, name);
        final T object = (T) beanManager.getReference(bean, bean.getBeanClass(), context);
        if (object == null) {
            throw new IllegalStateException("Cannot obtain a contextual reference for the bean: " + bean.getName());
        }
        return object;
    }

    /*
     * In case when BeanManager was not set then we try (as last resort) to look it up in JNDI. This is useful when EPS is bundled inside other JEE
     * application.
     */
    private void tryLookupBeanManagerInJndi() {
        try {
            log.debug("Trying to find BeanManager in JNDI");
            final InitialContext ctx = new InitialContext();
            final BeanManager lookupBeanManager = (BeanManager) ctx.lookup("java:comp/BeanManager");
            if (lookupBeanManager != null) {
                log.debug("Successfully found BeanManager {}", lookupBeanManager);
                beanManager = lookupBeanManager;
            }
        } catch (final NamingException ignored) {
            log.warn("Was not able to find BeanManager in JNDI. Details {}", ignored.getMessage());
            // ignore
        }
    }

    /**
     * Set up BeanManager instance. Invoked from different parts of initialization code for JSE and JEE environments.
     *
     * @param beanManager
     *            object of beanManager
     */
    public void setBeanManager(final BeanManager beanManager) {
        if (beanManager == null) {
            throw new IllegalArgumentException("Bean manager must not be null.");
        }
        log.debug("Setting up bean manager instance: [{}]", beanManager);
        this.beanManager = beanManager;
    }

    /**
     * Stop CDI instance manager.
     */
    public void stop() {
        log.warn("Asked to stop EPS CDI instance!.");
        this.beanManager = null;
        log.debug("Stopped CDI instance Manager. beanManager set to null so it will be fetched from JNDI on restart.");
    }

}
