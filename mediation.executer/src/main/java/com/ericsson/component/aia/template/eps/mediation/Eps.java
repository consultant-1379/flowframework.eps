package com.ericsson.component.aia.template.eps.mediation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.core.module.impl.EpsModulesManagerImpl;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * runs eps flow as a seperate JVM
 *
 */
public class Eps extends Thread {

    private final String epsFlowName;
    private static boolean isKilled = false;
    ModuleManager moduleManager = new EpsModulesManagerImpl();
    private static final Long WAIT_MILLIS = 5000l;

    public Eps(final String flowName) {
        epsFlowName = flowName;
    }

    public void stopFlow() {
        isKilled = true;
    }

    @Override
    public void run() {
        deployModule();
        waitForever();
    }

    private void deployModule() {
        final File flowFile = new File( epsFlowName );
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(flowFile);
        } catch(final FileNotFoundException ex) {
            throw new RuntimeException("Eps flow could not be found.. , Exiting Eps Deployer.");
        }
        if (inputStream == null) {
            throw new RuntimeException("Eps flow could not be found.. , Exiting Eps Deployer.");
        }
        moduleManager.deployModule(inputStream);
        final EpsInstanceManager epsInstance = EpsInstanceManager.getInstance();
        registerShutdownHook(epsInstance);
    }

    private static void registerShutdownHook(final EpsInstanceManager instanceManager) {
        if (instanceManager == null) {
            throw new IllegalArgumentException("instance manager must not be null");
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                isKilled = true;
            }
        });
    }

    private static void waitForever() {
        while (!isKilled) {
            try {
                Thread.sleep(WAIT_MILLIS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
