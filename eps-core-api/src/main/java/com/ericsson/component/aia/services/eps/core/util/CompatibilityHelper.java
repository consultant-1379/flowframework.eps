/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.component.aia.services.eps.adapter.InputAdapter;
import com.ericsson.component.aia.services.eps.adapter.OutputAdapter;

/**
 * Class for dynamically proxy instances implementing IO adapter from FF API and EPS API
 */
public class CompatibilityHelper implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(CompatibilityHelper.class);

    private final Object proxied;

    /**
     * {@link CompatibilityHelper} ctor
     *
     * @param proxied
     *            : {@link Object} the proxied object
     */
    public CompatibilityHelper(final Object proxied) {
        this.proxied = proxied;
    }

    /**
     * Build a new proxy instance
     *
     * @param proxied
     *            {@link Object} the proxied object (EPS API IO Adapter implementation)
     * @return {@link Object} proxy instance
     */
    public static Object newInstance(final Object proxied) {

        final Collection<Class<?>> proxiedInterfacesList = new LinkedList<Class<?>>(Arrays.asList(proxied.getClass().getInterfaces()));
        if (proxiedInterfacesList.contains(InputAdapter.class)) {
            proxiedInterfacesList.remove(InputAdapter.class);
            proxiedInterfacesList.add(com.ericsson.component.aia.itpf.common.io.InputAdapter.class);
        } else if (proxiedInterfacesList.contains(OutputAdapter.class)) {
            proxiedInterfacesList.remove(OutputAdapter.class);
            proxiedInterfacesList.add(com.ericsson.component.aia.itpf.common.io.OutputAdapter.class);
        } else {
            return null;
        }

        return java.lang.reflect.Proxy.newProxyInstance(Adapter.class.getClassLoader(), proxiedInterfacesList.toArray(new Class<?>[0]),
                new CompatibilityHelper(proxied));
    }

    /**
     * Invocation proxy on FF API from EPS API
     *
     * @param proxy
     *            : {@link Object} the proxied object
     * @param method
     *            : {@link Method} the proxied invocated method
     * @param args
     *            : {@link Object[]} the proxied method arguments
     * @return {@link Object} : proxied method returned object
     * @throws Throwable
     *             {@link Throwable}
     *
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Object result = null;
        try {
            final Method proxiedMethod = this.proxied.getClass().getMethod(method.getName(), method.getParameterTypes());
            result = proxiedMethod.invoke(this.proxied, args);
        } catch (final Exception e) {
            log.error("CompatibilityHelper::invoke error invoking {} for EPS API IO adapter implementation: {}. {}. Details {}", method,
                    this.proxied, e.getMessage());
            log.error("Was not able to proxy event to deprecated adapter", e);
            throw e;
        }
        return result;
    }

}
