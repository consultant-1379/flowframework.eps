package com.ericsson.component.aia.services.eps.core.module.impl;

import static org.mockito.Mockito.*;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Counter;
import com.ericsson.component.aia.services.eps.core.module.impl.EpsModulesManagerImpl;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.sdk.resources.Resources;

public class EpsModulesManagerImplTest {

    private final EpsStatisticsRegister mockEpsStatisticsRegister = mock(EpsStatisticsRegister.class);
    private EpsModulesManagerImpl manager;

    @Before
    public void setUp() {
        when(mockEpsStatisticsRegister.isStatisticsOn()).thenReturn(false);
        when(mockEpsStatisticsRegister.createCounter(anyString())).thenReturn(new Counter());
        manager = new EpsModulesManagerImpl(mockEpsStatisticsRegister);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_control_event() {
        manager.sendControlEventToAllModules(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_control_event_to_module() {
        manager.sendControlEventToModuleById("abc", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_control_event_to_null_module() {
        final ControlEvent controlEvent = new ControlEvent(1);
        manager.sendControlEventToModuleById(null, controlEvent);
    }

    @Test
    public void test_control_event_no_receiver() {
        final ControlEvent controlEvent = new ControlEvent(1);
        final int received = manager.sendControlEventToAllModules(controlEvent);
        Assert.assertEquals(0, received);
        final boolean moduleFound = manager.sendControlEventToModuleById("abc", controlEvent);
        Assert.assertFalse(moduleFound);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_filesystem_deploy_null_module() {
        manager.deployModuleFromFile(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_filesystem_deploy_invalid_module() {
        final InputStream inputStream = Resources.getClasspathResource("/not_xml.txt").getInputStream();
        Assert.assertNotNull(inputStream);
        manager.deployModuleFromFile(inputStream);
    }

    @Test
    public void test_filesystem_deploy_undeploy_valid_module() {
        final InputStream inputStream = Resources.getClasspathResource("/flow_01.xml").getInputStream();
        Assert.assertNotNull(inputStream);
        Assert.assertEquals(0, manager.getDeployedModulesCount());
        final String moduleIdentifier = manager.deployModuleFromFile(inputStream);
        Assert.assertNotNull(moduleIdentifier);
        Assert.assertEquals(1, manager.getDeployedModulesCount());
        Assert.assertEquals(1, manager.sendControlEventToAllModules(new ControlEvent(10)));
        Assert.assertTrue(manager.sendControlEventToModuleById(moduleIdentifier, new ControlEvent(22)));
        final boolean undeployed = manager.undeployModule(moduleIdentifier);
        Assert.assertTrue(undeployed);
        Assert.assertEquals(0, manager.getDeployedModulesCount());
        Assert.assertEquals(0, manager.sendControlEventToAllModules(new ControlEvent(10)));
        Assert.assertFalse(manager.sendControlEventToModuleById(moduleIdentifier, new ControlEvent(22)));
    }

    @Test
    public void test_undeploy_modules() {
        final int undeployed = manager.undeployAllModules();
        Assert.assertEquals(0, undeployed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_undeploy_null_module() {
        manager.undeployModule(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_undeploy_empty_module() {
        manager.undeployModule(" ");
    }

    @Test
    public void test_undeploy_non_existing_module() {
        final boolean undeployed = manager.undeployModule("does not exist");
        Assert.assertFalse(undeployed);
    }

}
