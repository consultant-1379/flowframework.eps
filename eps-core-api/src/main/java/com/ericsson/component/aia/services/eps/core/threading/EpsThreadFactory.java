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
package com.ericsson.component.aia.services.eps.core.threading;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread factory used by eps built in component for threading
 *
 * @see ThreadManagementComponent
 * @author eborziv
 *
 */
public class EpsThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpsThreadFactory.class);

    public static final int DEFAULT_LOCAL_STACK_SIZE = 512;

    private static final String NAME_PREFIX = "eps-";

    private static final AtomicInteger poolNumber = new AtomicInteger();

    private final ThreadGroup group;

    private final AtomicInteger threadNumber = new AtomicInteger();

    private final String name;

    private final int stack;

    private final int threadPriority;

    /**
     *
     * @param nameLocal
     *            identifier used to create the Thread name
     * @param stackLocal
     *            the desired stack size for the new thread
     */
    public EpsThreadFactory(final String nameLocal, final int stackLocal) {
        this(nameLocal, stackLocal, Thread.NORM_PRIORITY);
    }

    /**
     * Initialize the EpsThreadFactory
     *
     * @param nameLocal
     *            identifier used to create the Thread name
     * @param stackLocal
     *            the desired stack size for the new thread
     * @param threadPriority
     *            priority to set the thread to
     */
    public EpsThreadFactory(final String nameLocal, final int stackLocal, final int threadPriority) {
        final SecurityManager securityManager = System.getSecurityManager();
        group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        name = NAME_PREFIX + nameLocal + poolNumber.getAndIncrement();
        stack = stackLocal;
        this.threadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thisThread = new Thread(group, runnable, name + threadNumber.getAndIncrement(), stack);
        if (thisThread.isDaemon()) {
            thisThread.setDaemon(false);
        }
        thisThread.setPriority(threadPriority);
        thisThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(final Thread threadWithException, final Throwable throwable) {
                LOGGER.error("Exception occured while processing thread : '{}' ", threadWithException.getName(),
                        throwable);
            }
        });
        return thisThread;
    }
}
