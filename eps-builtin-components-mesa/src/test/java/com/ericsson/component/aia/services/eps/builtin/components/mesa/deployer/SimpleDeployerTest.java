package com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.UUID;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.binder.SimpleEventBinder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.SimpleContext;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.Deployer;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.deployer.SimpleDeployer;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.manager.Manager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.manager.SimpleManager;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.rule.PrintingForwarder;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.PrintingViewListener;

public final class SimpleDeployerTest {

    private int counter;
    private File root;
    private HelperDeploymentAware aware;

    public void setUp() throws Exception {
        final UUID randomName = new UUID(System.currentTimeMillis(), System.currentTimeMillis());
        final String tempDirPath = System.getProperty("java.io.tmpdir") + File.separator + "mesa_test" + randomName;

        root = new File(tempDirPath);
        deleteDirectory(root);
        Files.createDirectory(root.toPath(), new FileAttribute<?>[] {});
        System.out.println("Using '" + root.getAbsolutePath() + "' as root directory for policies");

        final Context context = new SimpleContext(new SimpleEventBinder(null), new PrintingForwarder(), new PrintingViewListener(),
                "src/test/resources/config/esper-test-config.xml", "src/test/resources/template/", 1000l);

        final Manager manager = new SimpleManager();
        manager.inject(context);
        aware = new HelperDeploymentAware(manager);

        final Deployer deployer = new SimpleDeployer(root.getAbsolutePath(), context, aware);
        deployer.start();
    }

    @Test
    public void testDeployFiles() throws Exception {

        setUp();

        copyPolicy(new File("src/test/resources/deployer/deployer_extra/com.ericsson.component.aia.wcdma_policy-2_1.0"));

        copyPolicy(new File("src/test/resources/deployer/deployer_extra/com.ericsson.component.aia.wcdma_policy-3_1.0"));

        copyPolicy(new File("src/test/resources/deployer/deployer/com.ericsson.component.aia.wcdma_policy-1_1.0"));
        aware.waitForCoreDeployment("policy-1");
        aware.waitForConfDeployments("2", "1");
        aware.waitForConfDeployments("1", "2");

        final File thirdConf = new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "conf_3.xml");

        final File fileAtPolicyLevel = new File(root, "fileforTesting");
        fileAtPolicyLevel.createNewFile();
        final File hiddenFileAtPolicyLevel = new File(root, ".fileforTesting");
        hiddenFileAtPolicyLevel.createNewFile();
        final File underscoreFileAtPolicyLevel = new File(root, "_fileforTesting");
        underscoreFileAtPolicyLevel.createNewFile();

        final File badConfDirectorySyntax = new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "dirForTest");
        badConfDirectorySyntax.mkdir();

        final File badConfSyntax = new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "c0nf_3.xml");

        Files.copy(new File("src/test/resources/deployer/deployer_extra/c0nf_3.xml").toPath(), badConfSyntax.toPath());

        final File badConfExtensionSyntax = new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "conf_3.txt");

        Files.copy(new File("src/test/resources/deployer/deployer_extra/conf_3.txt").toPath(), badConfExtensionSyntax.toPath(), new CopyOption[] {});

        Files.copy(new File("src/test/resources/deployer/deployer_extra/conf_3.xml").toPath(), thirdConf.toPath(), new CopyOption[] {});
        aware.waitForConfDeployment("7");

        badConfDirectorySyntax.delete();
        Files.delete(badConfSyntax.toPath());
        Files.delete(badConfExtensionSyntax.toPath());
        Files.delete(fileAtPolicyLevel.toPath());
        Files.delete(hiddenFileAtPolicyLevel.toPath());
        Files.delete(underscoreFileAtPolicyLevel.toPath());
        Files.delete(thirdConf.toPath());
        aware.waitForConfUnDeployment("7");

        Files.delete(new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "conf_2.xml").toPath());
        aware.waitForConfUnDeployment("2");

        Files.delete(new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "conf_1.xml").toPath());
        aware.waitForConfUnDeployment("1");

        Files.delete(new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0" + File.separator + "core.xml").toPath());
        Files.delete(new File(root, "com.ericsson.component.aia.wcdma_policy-1_1.0").toPath());
        aware.waitForCoreUnDeployment("policy-1");
    }

    @Test
    public void testDelpoy() throws Exception {
        boolean check1 = false, check2 = false;
        final UUID randomName = new UUID(System.currentTimeMillis(), System.currentTimeMillis());
        final String tempFolderPath = System.getProperty("java.io.tmpdir") + "/mesa_test_deployer" + randomName;

        final File workDirectory = new File(tempFolderPath);

        if (!workDirectory.exists()) {
            try {
                workDirectory.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        final File tempFile = new File(tempFolderPath + "tempFile");
        try {
            tempFile.createNewFile();
        } catch (final IOException e1) {
            e1.printStackTrace();
        }

        try {
            new SimpleDeployer(tempFile, null, null);
        } catch (final IllegalArgumentException e) {
            workDirectory.delete();
            tempFile.delete();
            check1 = true;
        }

        try {
            new SimpleDeployer(workDirectory, null, null);
        } catch (final IllegalArgumentException e) {
            workDirectory.delete();
            check2 = true;
        }

        assertTrue(check1 && check2);
    }

    private void copyPolicy(final File sourceDir) throws Exception {
        final File tempPolicyDir = new File(root, "_temp_dir_" + counter++);
        Files.createDirectory(tempPolicyDir.toPath(), new FileAttribute<?>[] {});
        copyFiles(sourceDir, tempPolicyDir);
        Files.move(tempPolicyDir.toPath(), new File(root, sourceDir.getName()).toPath(), new CopyOption[] { StandardCopyOption.ATOMIC_MOVE });
    }

    private void copyFiles(final File sourceDir, final File targetDir) throws Exception {
        for (final File file : sourceDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isFile();
            }
        })) {
            Files.copy(file.toPath(), new File(targetDir, file.getName()).toPath(), new CopyOption[] {});
        }
    }

    private void deleteDirectory(final File dir) throws Exception {
        if ((dir == null) || !dir.exists()) {
            return;
        }
        for (final File file : dir.listFiles()) {
            if (file.isFile()) {
                Files.delete(file.toPath());
            } else {
                deleteDirectory(file);
            }
        }
        Files.delete(dir.toPath());
    }
}
