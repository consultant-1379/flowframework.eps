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
package com.ericsson.component.aia.services.eps.core.integration.jee;

import static java.nio.file.StandardCopyOption.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.*;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EPSConfigurationLoader;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Maven artifact constants
 * 
 * @author emaomic
 * 
 */
public class Artifact {

    /**
     *
     */
    private static final String JBOSS_DEPLOYMENT_STRUCTURE_XML_NAME = "jboss-deployment-structure.xml";
    /**
     *
     */
    public static final String SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME = "ServiceFrameworkConfiguration.properties";
    /**
     *
     */
    private static final String BEANS_XML_NAME = "beans.xml";
    /**
     *
     */
    private static final String TEST_JAR_NAME = "test.jar";
    private static final Logger LOG = LoggerFactory.getLogger(Artifact.class);
    public static final String COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR = "com.ericsson.component.aia.services.eps:eps-jee-war:war";
    public static final String COM_ERICSSON_OSS_SERVICES_EPS_WAR_OLD = "com.ericsson.component.aia.services:eps-jee-war:war";
    public static final String CONFIGURATION_FILE_NAME = "EpsConfiguration.properties";
    public static final String PERSISTENCE_FILE_NAME = "persistence.xml";

    public static File resolveArtifactWithoutDependencies(final String artifactCoordinates) {
        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml").resolve(artifactCoordinates).withoutTransitivity().asFile();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates " + artifactCoordinates + " was not resolved");
        }

        if (artifacts.length != 1) {

            throw new IllegalStateException("Resolved more then one artifact with coordinates " + artifactCoordinates);
        }
        LOG.debug("Found file artifact {}", artifacts[0]);

        return artifacts[0];
    }

    public static File[] resolveArtifactWithDependencies(final String artifactCoordinates) {
        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml").resolve(artifactCoordinates).withTransitivity().asFile();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates " + artifactCoordinates + " was not resolved");
        }

        LOG.debug("Found file artifact {}", artifacts);

        return artifacts;
    }

    public static Properties loadProperties() {

        final Properties prop = new Properties();

        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("projectProperties.properties");
        try {
            prop.load(inputStream);
        } catch (final IOException e) {
            LOG.error("Cannot load properties: Cause: " + e.getMessage(), e);
        }

        return prop;
    }

    public static String readOldProjectVersion() {
        return loadProperties().getProperty("version.old.eps");
    }

    public static HazelcastInstance createHazelcastInstance() {
        final Config cfg = new Config();
        cfg.setClassLoader(Thread.currentThread().getContextClassLoader());
        final HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(cfg);
        return hzInstance;
    }

    public static boolean copyFlowFile(final String testFile, final InputStream inputStream, final String folderName) throws IOException {
        final int indexOfSlash = testFile.lastIndexOf("/");
        String simpleFileName = testFile;
        if (indexOfSlash != -1) {
            simpleFileName = testFile.substring(indexOfSlash + 1, testFile.length());
            LOG.debug("Simple file name is {}", simpleFileName);
        }
        simpleFileName = simpleFileName.replace(".xml", "");

        if (inputStream == null) {
            throw new IllegalArgumentException("Was not able to find " + testFile + " in classpath!");
        }
        final File watchedFolder = new File(folderName);
        if (!watchedFolder.exists() || !watchedFolder.isDirectory()) {
            throw new IllegalStateException(folderName + " is not valid folder!");
        }
        final Random random = new Random();
        final String finalFileNameRoot = simpleFileName + "_eps_module_" + random.nextInt(1000) + "_" + System.currentTimeMillis();
        final String tmpfileName = finalFileNameRoot + ".tmp";
        final String xmlFileName = finalFileNameRoot + ".xml";
        LOG.debug("Trying to create file {} in folder {}", tmpfileName, folderName);
        final File tmpDestinationFile = new File(watchedFolder, tmpfileName);
        final File xmlDestinationFile = new File(watchedFolder, xmlFileName);
        final Path tmpDestinationPath = tmpDestinationFile.toPath();
        final Path xmlDestinationPath = xmlDestinationFile.toPath();
        try {
            final long bytesWritten = Files.copy(inputStream, tmpDestinationPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.debug("Successfully wrote {} bytes into temporary file. Now renaming to {}", bytesWritten, xmlFileName);
            Files.move(tmpDestinationPath, xmlDestinationPath, REPLACE_EXISTING, ATOMIC_MOVE);
            LOG.debug("Moved file to {}", xmlFileName);
            return bytesWritten > 1;
        } catch (final IOException ie) {
            LOG.error("Failed to rename file to {}", xmlFileName);
            throw ie;
        } finally {
            inputStream.close();
        }
    }

    public static boolean wait(final int milliseconds) {
        LOG.debug("Waiting " + milliseconds + " ms");
        try {
            Thread.sleep(milliseconds);
            return true;
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean waitForModuleDeploy(final int moduleCount, final int retry) throws InterruptedException {
        LOG.info("\n*\n*\n* Artifact::waitForModuleDeploy: ModuleCount " + moduleCount + " Retry: " + retry + "\n*\n*");

        ModuleManager modulesManager = null;
        try {
            modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        } catch (final Exception ex) {
            LOG.error("Cannot load ModuleManager: Cause: " + ex.getMessage(), ex);
            return false;
        }

        boolean isModuleDeployed = false;
        int iterations = 0;
        while (iterations < retry) {

            final int count = modulesManager.getDeployedModulesCount();

            LOG.info("\n*\n*\n* Artifact::ModuleManager: " + modulesManager + " Count: " + count + "\n*\n*");

            iterations++;

            if (count == moduleCount) {
                isModuleDeployed = true;
                break;
            }
            Thread.sleep(5000);
        }

        return isModuleDeployed;
    }

    public static void deployFlowFile(final String flowFile, final String configurationFileName) throws IOException {
        final Properties properties = new Properties();
        InputStream propertiesIs = null;
        InputStream flowIs = null;
        boolean copied = false;
        try {
            LOG.debug("Loading properties from {}", configurationFileName);
            propertiesIs = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath", configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }

            final String folderName = properties.getProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);
            flowIs = Thread.currentThread().getContextClassLoader().getResourceAsStream(flowFile);
            copied = Artifact.copyFlowFile(flowFile, flowIs, folderName);
        } catch (final IOException ie) {
            LOG.error("loadProperties IOexception on loading file {} from classpath", ie);
            throw ie;
        } finally {
            Assert.assertNotNull("Configuration file stream is null ", propertiesIs);
            flowIs.close();
            propertiesIs.close();
            Assert.assertTrue("File has not been copied ", copied);
        }
    }

    public static int waitForCsvFiles(final int fileCount, final int retry) throws InterruptedException {

        int iterations = 0;
        int foundFiles = 0;
        while (iterations < retry) {

            final List<File> csvFiles = getCsvFiles();
            foundFiles = csvFiles.size();

            LOG.info("\n*\n*\n* Number of CSV files found: " + foundFiles + "\n*\n*");

            iterations++;

            if (foundFiles == fileCount) {
                break;
            }
            Thread.sleep(5000);
        }

        return foundFiles;
    }

    public static List<File> getCsvFiles() {

        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (name.endsWith(".csv")) {
                    LOG.debug("csv file name {}", name);
                    return true;
                }
                return false;
            }
        };
        final String csvDir = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME);
        LOG.debug("Getting csv files in {}", csvDir);
        final File[] listFiles = new File(csvDir).listFiles(filter);
        LOG.debug("Found {} csv files.", listFiles.length);
        return Arrays.asList(listFiles);
    }

    public static void createCsvOutputLocation(final String configurationFileName) {
        LOG.debug("Loading properties from {}", configurationFileName);
        final Properties properties = new Properties();
        InputStream propertiesIs = null;

        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            propertiesIs = classLoader.getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath", configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }
        } catch (final IOException ie) {
            LOG.error("loadProperties IOexception on loading file {} from classpath", configurationFileName);
            ie.printStackTrace();

        } finally {
            Assert.assertNotNull("Configuration file stream is null ", propertiesIs);
            try {
                propertiesIs.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        final String csvLocationPath = properties.getProperty(EpsConfigurationConstants.STATISTICS_REPORTING_CSV_OUTPUT_LOCATION_PARAM_NAME);

        Assert.assertNotNull("CSV files location directory is null ", csvLocationPath);

        final File csvFolder = new File(csvLocationPath);
        if (!csvFolder.exists()) {
            LOG.debug("Trying to create directory {}", csvLocationPath);
            Assert.assertTrue(csvFolder.mkdirs());
        } else {
            LOG.debug("Directory exist, deleting .cvs files in {}", csvLocationPath);
            final File folder = new File(csvLocationPath);
            for (final File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".csv")) {
                    Assert.assertTrue(file.delete());
                }
            }
        }
    }

    public static void createModuleOutputLocation(final String configurationFileName) {
        LOG.debug("Loading properties from {}", configurationFileName);
        final Properties properties = new Properties();
        InputStream propertiesIs = null;

        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            propertiesIs = classLoader.getResourceAsStream(configurationFileName);
            if (propertiesIs != null) {
                properties.load(propertiesIs);
                LOG.debug("Successfully loaded file {} from classpath", configurationFileName);
            } else {
                LOG.warn("Was not able to find file {}", configurationFileName);
            }
        } catch (final IOException ie) {
            LOG.error("loadProperties IOexception on loading file {} from classpath", configurationFileName);
            ie.printStackTrace();

        } finally {
            Assert.assertNotNull("Configuration file stream is null ", propertiesIs);
            try {
                propertiesIs.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        final String moduleDeploymentFolder = properties.getProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);

        Assert.assertNotNull("Module Deployment files location directory is null ", moduleDeploymentFolder);

        LOG.debug("Trying to create directory {}", moduleDeploymentFolder);
        final File moduleFolder = new File(moduleDeploymentFolder);
        if (!moduleFolder.exists()) {
            Assert.assertTrue(moduleFolder.mkdirs());
        }
    }

    static EnterpriseArchive buildEar(final String earName, final String epsWarCoordinates, final String flowFileName,
    		final String sfWkConfigFileName, final String epsConfigFileName, final String manifestFile, final String jbossDeploymentFile,
            final Class<?> testClass, final List<Class<?>> handlerClasses, final List<Class<?>> ejbJarClasses) {
        LOG.info("Deploying: " + earName);

        final File archiveFile = resolveArtifactWithoutDependencies(epsWarCoordinates);

        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact " + epsWarCoordinates);
        }

        final WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class, archiveFile);
        war.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);

        final JavaArchive jarLib = ShrinkWrap.create(JavaArchive.class, TEST_JAR_NAME).addClass(testClass);
        jarLib.addClass(Artifact.class);
        if (handlerClasses != null && !handlerClasses.isEmpty()) {
            jarLib.addClasses(handlerClasses.toArray(new Class<?>[handlerClasses.size()]));
        }
        jarLib.addAsResource(flowFileName, "flow.xml");
        jarLib.addAsResource(epsConfigFileName, CONFIGURATION_FILE_NAME);
        jarLib.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);

        final JavaArchive jarEjb = ShrinkWrap.create(JavaArchive.class, "test-ejb.jar");

        if (ejbJarClasses != null && !ejbJarClasses.isEmpty()) {
            jarEjb.addClasses(ejbJarClasses.toArray(new Class<?>[ejbJarClasses.size()]));
        }

        jarEjb.addAsResource(sfWkConfigFileName, SERVICE_FRAMEWORK_CONFIGURATION_PROPERTIES_NAME);
        jarEjb.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);
        jarEjb.addAsManifestResource("persistence.xml", PERSISTENCE_FILE_NAME);

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, earName);

        ear.addAsManifestResource(manifestFile, "MANIFEST.MF");
        ear.addAsManifestResource("META-INF/beans.xml", BEANS_XML_NAME);
        ear.addAsModules(jarEjb);
        ear.addAsModules(war);
        ear.addAsLibraries(jarLib);
        ear.addAsManifestResource(jbossDeploymentFile, JBOSS_DEPLOYMENT_STRUCTURE_XML_NAME);

        return ear;
    }
}
