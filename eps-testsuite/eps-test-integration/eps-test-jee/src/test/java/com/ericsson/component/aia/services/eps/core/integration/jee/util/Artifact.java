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

package com.ericsson.component.aia.services.eps.core.integration.jee.util;

import static java.nio.file.StandardCopyOption.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.SystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.HazelcastInputListener;
import com.ericsson.component.aia.services.eps.core.integration.commons.util.MessagingTestListener;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.*;
import com.ericsson.oss.itpf.modeling.mdt.impl.app.MdtMain;
import com.ericsson.oss.itpf.modeling.modelservice.meta.ModelRepoBasedModelMetaInformation;
import com.ericsson.oss.itpf.modeling.schema.gen.oss_common.EModelDefinition;
import com.ericsson.oss.itpf.modeling.schema.util.DtdModelHandlingUtil;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
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

    public static final String CLASSPATH_SEPARATOR = SystemUtils.IS_OS_WINDOWS ? ";" : ":";

    public static final String COM_ERICSSON_OSS_ITPF_SERVICES_EPS_DIST_JAR = "com.ericsson.component.aia.services.eps:eps-jboss-module-dist";

    public static final String COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR = "com.ericsson.component.aia.services.eps:eps-jee-war:war";

    public static final String MODEL_DEPLOYMENT_JAR = "com.ericsson.component.aia.itpf.modeling:model-deployment-jar";
    public static final String MODEL_MDT_PLUGIN_JAR = "com.ericsson.component.aia.itpf.modeling:default-mdt-plugins-jar";
    public static final String HORNET_JMS_CLIENT = "org.hornetq:hornetq-jms-client";
    public static final String JBOSS_REMOTE_NAMING = "org.jboss:jboss-remote-naming";
    public static final String JBOSS_XNIO_NIO = "org.jboss.xnio:xnio-nio";

    private static final Logger LOG = LoggerFactory.getLogger(Artifact.class);

    private Artifact() {

    }

    /**
     * Delete .xml files found in the path specified by the property: com.ericsson.component.aia.services.eps.module.deployment.folder.path
     */
    public static void cleanupConfiguredXMLFolder() {
        final String folderName = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);
        final File folder = new File(folderName);
        for (final File file : folder.listFiles()) {
            LOG.trace("Found new file under folder: {}", file.getName());
            if (file.isFile() && file.getName().endsWith(".xml")) {
                LOG.trace("Trying to delete file {} from folder {}", file.getName(), folderName);
                if (!file.delete()) {
                    throw new IllegalStateException("Could not cleanup deployment folder. Failed to delete file " + file.getName());
                }
            }
        }
        LOG.trace("Successfully cleaned up folder {}", folderName);
    }

    /**
     * Copy .xml file provided as input to the path specified by the property: com.ericsson.component.aia.services.eps.module.deployment.folder.path
     */
    public static boolean copyXmlContentToConfiguredFolder(final String file) {
        LOG.trace("Trying to copy file {}", file);
        final int indexOfSlash = file.lastIndexOf("/");
        String simpleFileName = file;
        if (indexOfSlash != -1) {
            simpleFileName = file.substring(indexOfSlash + 1, file.length());
            LOG.trace("Simple file name is {}", simpleFileName);
        }
        simpleFileName = simpleFileName.replace(".xml", "");
        final InputStream inputStream = ResourceManagerUtil.loadResourceAsStreamFromURI("classpath:" + file);
        if (inputStream == null) {
            throw new IllegalArgumentException("Was not able to find " + file + " in classpath!");
        }
        final String folderName = EPSConfigurationLoader
                .getConfigurationProperty(EpsConfigurationConstants.MODULE_DEPLOYMENT_FOLDER_SYS_PROPERTY_NAME);
        final File watchedFolder = new File(folderName);
        if (!watchedFolder.exists() || !watchedFolder.isDirectory()) {
            throw new IllegalStateException(folderName + " is not valid folder!");
        }
        final Random random = new Random();
        final String finalFileNameRoot = simpleFileName + "_eps_module_" + random.nextInt(1000) + "_" + System.currentTimeMillis();
        final String tmpfileName = finalFileNameRoot + ".tmp";
        final String xmlFileName = finalFileNameRoot + ".xml";
        LOG.trace("Trying to create file {} in folder {}", tmpfileName, folderName);
        final File tmpDestinationFile = new File(watchedFolder, tmpfileName);
        final File xmlDestinationFile = new File(watchedFolder, xmlFileName);
        final Path tmpDestinationPath = tmpDestinationFile.toPath();
        final Path xmlDestinationPath = xmlDestinationFile.toPath();
        try {
            final long bytesWritten = Files.copy(inputStream, tmpDestinationPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.trace("Successfully wrote {} bytes into temporary file. Now renaming to {}", bytesWritten, xmlFileName);
            Files.move(tmpDestinationPath, xmlDestinationPath, REPLACE_EXISTING, ATOMIC_MOVE);
            LOG.debug("Moved file to {}", xmlFileName);
            return bytesWritten > 1;
        } catch (final IOException ie) {
            throw new IllegalStateException(ie);
        }
    }

    /**
     * Create a new Hazelcast instance
     * 
     * @return the newly created Hazelcast instance
     */
    public static HazelcastInstance createHazelcastInstance() {
        final Config cfg = new Config();
        final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(cfg);
        return hazelcastInstance;
    }

    public static WebArchive getEpsArchive() {

        final File archiveFile = Maven.resolver().loadPomFromFile("pom.xml").resolve(Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR + ":?")
                .withoutTransitivity().asSingleFile();
        if (archiveFile == null) {
            throw new IllegalStateException("Unable to resolve artifact " + Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR);
        }
        // create arquillian ear archive from file we got from maven repository
        final WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class, archiveFile);
        war.addAsWebInfResource("META-INF/beans.xml");
        war.addClass(Artifact.class);
        war.addClass(HazelcastInputListener.class);
        war.addClass(MessagingTestListener.class);
        return war;
    }

    /**
     * Resolve the eps-jee-war archive to look for models under the specified namespaceUrn
     * 
     * @param namespaceUrn
     *            the namespace of the models eps-jee-war will look for.
     * @return the eps-jee-war archive.
     */
    public static WebArchive getEpsModelServiceArchive(final String namespaceUrn) {

        final WebArchive war = getEpsArchive();

        war.addAsResource("modelsDeploymentEvent_transport.xml", "modelsDeploymentEvent_transport.xml");

        war.addAsResource(new StringAsset(EpsConfigurationConstants.MODULE_DEPLOYMENT_MODEL_SYS_PROPERTY_URN + "=//" + namespaceUrn + "/*/*"),
                "EpsConfiguration.properties");
        LOG.info("initModelServiceProperties SET TO urn={} ", namespaceUrn);

        return war;
    }

    /**
     * Create the command String for launch MDT tool
     * 
     * @param modelFile
     *            the new model to be deployed
     * @return The commmand and option String array suitable for ProcessBuilder
     * 
     * @throws IOException
     */
    public static String[] mdtParameters(final String modelFile) throws IOException {

        final File jarFile = buildModelServiceTestJar(modelFile);

        // find java executable
        final String javaCmd = new StringBuilder().append(java.lang.System.getProperty("java.home")).append(java.io.File.separator).append("bin")
                .append(java.io.File.separator).append("java").toString();

        // build the classpath using MavenResolver 
        final List<File> library = new ArrayList<File>();
        library.addAll(Arrays.asList(resolveArtifactWithDependencies(MODEL_DEPLOYMENT_JAR)));
        library.addAll(Arrays.asList(resolveArtifactWithDependencies(MODEL_MDT_PLUGIN_JAR)));
        library.addAll(Arrays.asList(resolveArtifactWithDependencies(HORNET_JMS_CLIENT)));
        library.addAll(Arrays.asList(resolveArtifactWithDependencies(JBOSS_XNIO_NIO)));
        library.addAll(Arrays.asList(resolveArtifactWithDependencies(JBOSS_REMOTE_NAMING)));
        final StringBuilder classPath = new StringBuilder();

        for (final File libFile : library) {
            if (libFile != null) {
                classPath.append(libFile.getAbsolutePath()).append(CLASSPATH_SEPARATOR);
            }
        }
        classPath.deleteCharAt(classPath.length() - 1);

        final File tempRepo = Files.createTempDirectory(FileSystems.getDefault().getPath(System.getProperty("user.dir"), "target"), "TmpMdtRepo")
                .toFile();
        tempRepo.deleteOnExit();

        final String arg = new StringBuilder().append(MdtMain.class.getCanonicalName()).toString();

        final String res[] = {
                javaCmd,
                "-DMODEL_ROOT=" + tempRepo.getAbsolutePath(),
                "-D" + ModelRepoBasedModelMetaInformation.MODEL_REPO_PATH_PROPERTY + "="
                        + System.getProperty(ModelRepoBasedModelMetaInformation.MODEL_REPO_PATH_PROPERTY), "-cp", classPath.toString(), arg,
                "-modelJars", jarFile.getCanonicalPath() };
        return res;

    }

    public static File[] resolveArtifactWithDependencies(final String artifactCoordinates) {
        final File[] artifacts = Maven.resolver().loadPomFromFile("pom.xml").resolve(artifactCoordinates).withTransitivity().asFile();
        if (artifacts == null) {
            throw new IllegalStateException("Artifact with coordinates " + artifactCoordinates + " was not resolved");
        }

        LOG.debug("Found file artifact {}", (Object[]) artifacts);

        return artifacts;
    }

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

    /**
     * Run the MDT to deploy the new XML model provided as input.
     * 
     * @param modelFile
     *            the new model to be deployed
     * @return true if MDT run successfully
     * @throws IOException
     */
    public static boolean runMdtAndDeployTestModels(final String modelFile) throws IOException {
        final File modelRoot = new File(System.getProperty(ModelRepoBasedModelMetaInformation.MODEL_REPO_PATH_PROPERTY)
                .replace("modelRepo.xml", ""));
        LOG.trace("adding {} to ModelService to {}", modelFile, modelRoot.getAbsolutePath());

        final ProcessBuilder processBuilder = new ProcessBuilder(mdtParameters(modelFile));

        processBuilder.redirectErrorStream(true);

        try {
            LOG.trace("running Mdt command {}", processBuilder.command());
            final Process process = processBuilder.start();
            final InputStream inputStream = process.getInputStream();
            final BufferedReader bufferStream = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferStream.readLine()) != null) {
                LOG.debug("MDT->{}", line);
            }
            process.waitFor();
            while ((line = bufferStream.readLine()) != null) {
                LOG.debug(line);
            }
            inputStream.close();
            LOG.info("MDT finished retvalue {}", process.exitValue());
        } catch (final Exception e) {
            throw new IllegalStateException("Exception caught while running mdt. Was not able to deploy flow! " + e.getMessage());
        }
        return true;
    }

    /**
     * Wait the specified time in milliseconds
     * 
     * @param milliseconds
     *            the time to wait
     * @return true if no interrupt received, false otherwise
     */
    public static boolean wait(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
            return true;
        } catch (final InterruptedException e) {
            LOG.error("Artifact::wait error: ", e);
            return false;
        }
    }

    /**
     * CountDownLatch Usage is required to guarantee model service handling of modeling event sent in test run_mdt_and_deploy_test_models without it
     * we run the risk of perhaps the Jenkins job returning failures from this verify if order of execution is too close.
     */
    public static boolean wait4deploy(final String file, final int count) {
        final ModuleManager modulesManager = LoadingUtil.loadSingletonInstance(ModuleManager.class);
        final int before = modulesManager.getDeployedModulesCount();
        int times = 12; // max wait 1 minute
        LOG.debug("wait4deploy enter for {} deployed {} on {} ", file, before, count);
        while ((count != modulesManager.getDeployedModulesCount()) && ((times--) > 0)) {
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                latch.await(5, TimeUnit.SECONDS);
                LOG.trace("wait4deploy step {} deployed {} on {}", times, modulesManager.getDeployedModulesCount(), count);
            } catch (final InterruptedException e) {
                throw new IllegalStateException("wait4deploy InterruptedException exception " + e.getMessage());
            }
        }
        LOG.debug("wait4deploy exit deployed {} on {} timeout {}", modulesManager.getDeployedModulesCount(), count, (times <= 0));
        return (times > 0);
    }

    /**
     * Build a JAR containing the model definition.
     * 
     * @param modelFile
     *            the model XML file.
     * @return the JAR containing the model definition.
     */
    private static File buildModelServiceTestJar(final String modelFile) {
        LOG.trace("Trying to build jar with {}", modelFile);
        final int indexOfSlash = modelFile.lastIndexOf("/");
        String simpleFileName = modelFile;
        if (indexOfSlash != -1) {
            simpleFileName = modelFile.substring(indexOfSlash + 1, modelFile.length());
            simpleFileName = simpleFileName.replace(".xml", "");
            LOG.debug("Simple file name is {}", simpleFileName);
        }
        final EModelDefinition modelDef = getModelDefinition(modelFile);
        // build a jar suitable for model service deploy
        final String path = "/modeling/etc/model/fbp_flow/" + modelDef.getNs() + "/" + modelDef.getName();
        final String modelName = modelDef.getName() + "-" + modelDef.getVersion() + ".xml";
        final String flow = ResourceManagerUtil.loadResourceAsTextFromURI("classpath:" + modelFile);
        if (flow == null) {
            LOG.error("Was not able to find " + modelFile + " in classpath!");
            throw new IllegalArgumentException("Was not able to find " + modelFile + " in classpath!");
        }
        LOG.info("building jar with flow file {} in {}", modelName, path);
        JavaArchive jar = null;
        try {
            jar = ShrinkWrap.create(JavaArchive.class, simpleFileName + ".jar").add(new StringAsset(flow), path, modelName);
        } catch (final Exception e) {
            LOG.error("Exception caught. Was not able to create jar!", e);
            throw new IllegalArgumentException("Was not able to create jar " + simpleFileName + ".jar. Cause: " + e.getMessage());
        }
        LOG.trace("created jar file {}", jar.getContent());
        File modelServiceTestJarFile;
        try {
            modelServiceTestJarFile = File.createTempFile(simpleFileName, ".jar");
            modelServiceTestJarFile.deleteOnExit();
        } catch (final IOException e) {
            LOG.error("IOException exception caught. Unable to deploy model!", e);
            throw new IllegalArgumentException("Was not able to write " + jar.getName() + " in temporary directory. Cause: {}" + e.getMessage());
        }
        LOG.trace("created jar file {} export to {}", simpleFileName + ".jar", modelServiceTestJarFile.getAbsolutePath());
        new ZipExporterImpl(jar).exportTo(modelServiceTestJarFile, true);
        return modelServiceTestJarFile;
    }

    private static EModelDefinition getModelDefinition(final String testFile) {
        final InputStream inputStream = ResourceManagerUtil.loadResourceAsStreamFromURI("classpath:" + testFile);
        if (inputStream == null) {
            LOG.error("Was not able to find the file " + testFile + " in classpath!");
            throw new IllegalArgumentException("Was not able to find " + testFile + " in classpath!");
        }
        final EModelDefinition modelDef = parseInputStream(inputStream);
        try {
            inputStream.close();
        } catch (final IOException e1) {
            LOG.error("Was not able to close InputStream of: " + testFile, e1);
            throw new IllegalArgumentException("Was not able to close InputStream of " + testFile + " in classpath! Cause: " + e1.getMessage());
        }
        return modelDef;
    }

    private static EModelDefinition parseInputStream(final InputStream inputStream) {
        final Unmarshaller unmarshaller = DtdModelHandlingUtil.getUnmarshaller(SchemaConstants.FBP_FLOW);
        try {
            final Object root = unmarshaller.unmarshal(new StreamSource(inputStream));
            return (EModelDefinition) root;
        } catch (final JAXBException jaxbexc) {
            LOG.error("JAXB exception caught. Unable to parse model!", jaxbexc);
            throw new IllegalArgumentException("Invalid flow - unable to parse it! Details: " + jaxbexc.getMessage());
        }
    }

}
