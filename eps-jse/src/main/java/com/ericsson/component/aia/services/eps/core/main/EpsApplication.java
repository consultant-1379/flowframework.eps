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
package com.ericsson.component.aia.services.eps.core.main;

import java.io.PrintStream;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.pe.java.util.EpsCdiInstanceManager;

/**
 * Used for starting up EPS in JSE environment.
 *
 * @author eborziv
 *
 */
public class EpsApplication {

    private static final long WAIT_MILLIS = 1000 * 5;

    private static final Logger LOG = LoggerFactory.getLogger(EpsApplication.class);

    private static volatile boolean isKilled;

    private static volatile boolean isCdiEnabled;

    private EpsApplication() {
        // no constructor
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        LOG.info("Starting EPS instance");
        checkAndPrintInstanceId();

        final EpsInstanceManager epsInstance = EpsInstanceManager.getInstance();
        registerShutdownHook(epsInstance);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        LOG.info("Added uncaught exception handler");
        // if cdi.enabled system property is true, start weld container for JSE
        manageCDIEnvironment();
        epsInstance.start();
        waitForever();
    }

    private static void manageCDIEnvironment() {
        final String cdiEnabled = System.getProperty(EpsConfigurationConstants.EVENT_INPUT_HANDLER_CDI_ENABLED);
        if ("true".equalsIgnoreCase(cdiEnabled)) {
            LOG.info("CDI environment is enabled, starting CDI environment...");
            isCdiEnabled = true;
            startWeldContainer();
            LOG.info("Successfully started CDI environment.");
        } else {
            LOG.info("CDI environment is disabled! To enable CDI use -D{}=true", EpsConfigurationConstants.EVENT_INPUT_HANDLER_CDI_ENABLED);
        }
    }

    private static void startWeldContainer() {
        LOG.info("Booting weld and creating weld container instance (Weld SE).");
        final WeldContainer weldContainer = new Weld().initialize();
        final BeanManager beanManager = weldContainer.getBeanManager();
        LOG.info("Starting EpsCdiInstanceManager...");
        final EpsCdiInstanceManager cdiInstanceManager = EpsCdiInstanceManager.getInstance();
        LOG.info("Initializing Weld SE CDI environment...");
        cdiInstanceManager.setBeanManager(beanManager);
    }

    private static void checkAndPrintInstanceId() {
        final String instanceId = EpsUtil.getEpsInstanceIdentifier();
        LOG.info("EPS instance identifier is [{}]", instanceId);
    }

    private static void registerShutdownHook(final EpsInstanceManager instanceManager) {
        if (instanceManager == null) {
            throw new IllegalArgumentException("instance manager must not be null");
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                LOG.info("Executing shutdown actions...");
                instanceManager.stop();
                isKilled = true;
                LOG.info("Successfully stopped EPS instance");
                if (isCdiEnabled) {
                    EpsCdiInstanceManager.getInstance().stop();
                    LOG.info("Successfully stopped EPS CDI instance");
                }
            }
        });
        LOG.info("added shutdown hook");
    }

    private static void waitForever() {
        while (!isKilled) {
            try {
                Thread.sleep(WAIT_MILLIS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
                LOG.error("Interrupted application thread!");
            }
        }
    }

    /**
     *
     * Default Unmanaged exceptions handler
     *
     */
    private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        /**
         * manages uncaught exceptions, if any other handler takes care of them
         *
         * @param thread
         *            thread who finished because of the uncaught exception
         * @param exc
         *            throwable uncaught
         */
        @Override
        public void uncaughtException(final Thread thread, final Throwable exc) {
            final String errorMsg = String.format("Uncaught exception %s from thread %s with message: %s", exc.getClass().getName(), thread
                    .getName(), exc.getMessage());
            LOG.error(errorMsg, exc);
            printUncaughtErrors(errorMsg, System.out, exc);
            printUncaughtErrors(errorMsg, System.err, exc);
            LOG.error("Shutting down this EPS instance!");
            //finish JVM...
            System.exit(-1);
        }

        //print error to desired stream
        private void printUncaughtErrors(final String errorMsg, final PrintStream stream, final Throwable exc) {
            stream.println(errorMsg);
            exc.printStackTrace(stream);
        }
    }

}