package com.ericsson.component.aia.services.eps.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.core.modules.ModuleListener;
import com.ericsson.component.aia.services.eps.core.modules.impl.DummyModuleListenerImpl2;
import com.ericsson.component.aia.services.eps.core.util.LoadingUtil;

public class LoadingUtilTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_singleton_null() {
        LoadingUtil.loadSingletonInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_only_one_null() {
        LoadingUtil.loadOnlyOneInstance(null);
    }

    @Test
    public void test_load_instance_not_null() {
        final ModuleListener moduleListener = LoadingUtil.loadSingletonInstance(ModuleListener.class);
        assertNotNull(moduleListener);
        assertTrue(moduleListener instanceof DummyModuleListenerImpl2);
    }
}
