package com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Name;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.SimpleId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyConfBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder.PolicyCoreBuilder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.SimplePolicyConf;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core.Policy;

/**
 * Assumes that movement of directories is atomic.
 */
public final class SimpleDeployer implements Deployer {

    // time unit is seconds...
    private static final int DEFAULT_INITIAL_DELAY = 5;
    private static final int DEFAULT_SCAN_DELAY = 5;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final int initialDelay = DEFAULT_INITIAL_DELAY; // TODO make these
                                                            // configurable
    private final int scanDelay = DEFAULT_SCAN_DELAY; // TODO make these
                                                      // configurable

    private final File rootDir;
    private final Context context;
    private final DeploymentAware manager;

    // key is name of policy, value is its generated core policy ID
    private final Map<Name, Policy> deployedCorePolicies = new HashMap<Name, Policy>();
    // key is name of policy, value is set of conf policy IDs (which are equal
    // to IDs in file name)
    private final Map<Name, Set<Id>> deployedConfPolicies = new HashMap<Name, Set<Id>>();

    /**
     * Instantiates a new simple deployer.
     *
     * @param rootDirName
     *            the root dir name
     * @param context
     *            the context
     * @param manager
     *            the manager
     */
    public SimpleDeployer(final String rootDirName, final Context context, final DeploymentAware manager) {
        this(new File(rootDirName), context, manager);
    }

    /**
     * Instantiates a new simple deployer.
     *
     * @param rootDir
     *            the root dir
     * @param context
     *            the context
     * @param manager
     *            the manager
     */
    public SimpleDeployer(final File rootDir, final Context context, final DeploymentAware manager) {
        super();
        this.context = context;
        this.manager = manager;
        this.rootDir = rootDir;

        if (!rootDir.exists()) {
            throw new IllegalArgumentException("Directory '" + rootDir.getAbsolutePath() + " does not exist");
        }
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Path '" + rootDir.getAbsolutePath() + " is not a directory");
        }
        log.info("Using following policy deployment directory '{}'", rootDir.getAbsolutePath());
    }

    @Override
    public synchronized void start() {
        // TODO check if EPS has shared executor service, maybe ServiceFramework
        // should be used here?
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new ScannerTask(), initialDelay, scanDelay, TimeUnit.SECONDS);
    }

    private final class ScannerTask implements Runnable {
        @Override
        public void run() {
            try {
                runInternal();
            } catch (final Exception t) {
                log.error("Exception within deployer scanner task", t);
            }
        }

        private void runInternal() throws MesaException {
            final File[] policyDirs = rootDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return file.isDirectory() && !file.isHidden() && !file.getName().startsWith("_");
                }
            });
            log.info("Detected {} policy directories", policyDirs.length);
            final Set<Name> encountered = new HashSet<Name>();
            for (final File dir : policyDirs) {
                final Name name = Name.parseFrom(dir.getName());
                encountered.add(name);
                if (!deployedCorePolicies.containsKey(name)) {
                    final Policy policy = deployNewPolicy(dir, name);
                    if (policy != null) {
                        deployedCorePolicies.put(name, policy);
                    }
                } else {
                    processExistingPolicy(dir, name, deployedCorePolicies.get(name));
                }
            }
            // check for deleted ones
            for (final Name name : deployedCorePolicies.keySet()) {
                if (!encountered.contains(name)) {
                    processDeletedPolicy(deployedCorePolicies.get(name));
                }
            }
        }

        // deploy new policy
        private Policy deployNewPolicy(final File policyDir, final Name policyName) {
            log.info("Deploying new policy {}", policyName);
            // deploy core first
            Policy policy = null;
            try {
                final PolicyCoreBuilder coreBuilder = new PolicyCoreBuilder(context);
                policy = (Policy) coreBuilder.build(new File(policyDir, "core.xml").toURI());
                policy.inject(context);
                manager.deployCore(policy);
            } catch (final Exception e) {
                log.error("Unable to deploy new core policy {}, gonna delete entire policy directory", policyName, e);
                deletePolicyDir(policyDir);
                return null;
            }
            // now try to deploy all confs
            final File[] confFiles = policyDir.listFiles(new ConfFileFilter());
            deployNewConfFiles(policyDir, confFiles, policyName, policy);
            return policy;
        }

        // check if new confs have been deployed
        private void processExistingPolicy(final File policyDir, final Name policyName, final Policy policy) {
            final Set<Id> encountered = new HashSet<Id>();
            final Set<Id> ids = deployedConfPolicies.get(policyName);
            final File[] confFiles = policyDir.listFiles(new ConfFileFilter());
            for (final File confFile : confFiles) {
                final Id confId = getConfId(confFile.getName());
                if (!ids.contains(confId)) {
                    deployNewConfFiles(policyDir, new File[] { confFile }, policyName, policy);
                }
                encountered.add(confId);
            }
            // check for deleted ones, removing the Ids from
            // deployedConfPolicies in this loop led to the removal of them from
            // ids, this in turn lead to iterator issues
            final Set<Id> removed = new HashSet<Id>();
            for (final Id itemId : ids) {
                if (!encountered.contains(itemId)) {
                    manager.undeployConf(itemId, policyName);
                    removed.add(itemId);
                    deleteConfPolicyFile(policyDir, new File(policyDir, getFileNameConfId(itemId)));
                }
            }

            for (final Id iditemId : removed) {
                deployedConfPolicies.get(policyName).remove(iditemId);
            }
        }

        private void processDeletedPolicy(final Policy policy) throws MesaException {
            log.info("Removing core policy {}", policy.getName());
            deployedCorePolicies.remove(policy.getName());
            deployedConfPolicies.remove(policy.getName());
            manager.undeployCore(policy);

        }

        private void deployNewConfFiles(final File policyDir, final File[] confFiles, final Name policyName, final Policy policy) {
            for (final File confFile : confFiles) {
                try {
                    final PolicyConfBuilder confBuilder = new PolicyConfBuilder(context, policy.getId());
                    final SimplePolicyConf conf = (SimplePolicyConf) confBuilder.build(confFile.toURI());
                    manager.deployConf(conf);
                    if (deployedConfPolicies.get(policyName) == null) {
                        final Set<Id> ids = new HashSet<Id>();
                        ids.add(conf.getPolicyConfId());
                        deployedConfPolicies.put(policyName, ids);
                    } else {
                        deployedConfPolicies.get(policyName).add(conf.getPolicyConfId());
                    }
                } catch (final Exception t) {
                    log.error("Unable to deploy new conf policy '{}' for core policy {}, going to delete this conf policy", confFile.getName(),
                            policyName, t);
                    deleteConfPolicyFile(policyDir, confFile);
                }
            }
        }

        private void deletePolicyDir(final File policyDir) {
            log.info("Preparing to delete policy directory '{}'", policyDir.getAbsolutePath());
            // delete all files under policy dir, then delete policy dir
            final File[] files = policyDir.listFiles();
            for (final File file : files) {
                if (file.exists()) {
                    file.delete();
                }
            }
            log.info("Deleted {} files", files.length);
            policyDir.delete();
            if (policyDir.exists()) {
                throw new IllegalStateException("Deployer scanner task was unable to delete policy dir '" + policyDir.getAbsolutePath()
                        + "'. System may end up corrupted. Make sure this process can delete files and directories" + " under '"
                        + rootDir.getAbsolutePath() + "'");
            }
            log.info("Done deleting policy directory '{}'", policyDir.getAbsolutePath());
        }

        private void deleteConfPolicyFile(final File policyDir, final File policyConfFile) {
            policyConfFile.delete();
            if (policyConfFile.exists()) {
                throw new IllegalStateException("Deployer scanner task was unable to delete policy conf file '" + policyConfFile.getAbsolutePath()
                        + "'. System may end up corrupted. Make sure this process can delete files and directories" + " under '"
                        + policyDir.getAbsolutePath() + "'");
            }
        }

        private Id getConfId(final String fileName) {
            final int underscore = fileName.indexOf('_');
            final int dot = fileName.indexOf('.');
            return new SimpleId(Long.valueOf(fileName.substring(underscore + 1, dot)));
        }

        private String getFileNameConfId(final Id confId) {
            return "conf_" + ((SimpleId) confId).getId() + ".xml";
        }
    }

    private static final class ConfFileFilter implements FileFilter {

        @Override
        public boolean accept(final File file) {
            return file.isFile() && file.getName().startsWith("conf_") && file.getName().endsWith(".xml");
        }
    }
}
