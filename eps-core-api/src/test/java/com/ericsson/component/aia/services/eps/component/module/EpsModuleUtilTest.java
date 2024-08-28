package com.ericsson.component.aia.services.eps.component.module;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponentType;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleHandlerType;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleUtil;

public class EpsModuleUtilTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_null_empty() {
        EpsModuleUtil.determineHandlerType(null, null);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_illegal_argument() {
        EpsModuleUtil.determineHandlerType("EsperHandler", "cdiEventHandler");
        fail();
    }

    @Test
    public void test_valid_type() {
        assertEquals(EpsModuleHandlerType.ESPER_HANDLER, EpsModuleUtil.determineHandlerType(null, "EsperHandler"));
        assertEquals(EpsModuleHandlerType.JAVA_HANDLER, EpsModuleUtil.determineHandlerType("com.ericsson.component.aia.services.eps.core.integration.jse.TestEventDuplicatorComponent", null));
        assertEquals(EpsModuleHandlerType.JVM_SCRIPTING_HANDLER, EpsModuleUtil.determineHandlerType(null, "JvmScriptingHandler"));
        assertEquals(EpsModuleHandlerType.JAVA_HANDLER, EpsModuleUtil.determineHandlerType(null, "cdiEventHandler"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_componentTypeBasedOnHandlerType() {
        EpsModuleUtil.determineComponentTypeBasedOnHandlerType(null);
        fail();
    }

    @Test
    public void test_valid_componentTypeBasedOnHandlerType() {
        assertEquals(EpsModuleComponentType.ESPER_COMPONENT, EpsModuleUtil.determineComponentTypeBasedOnHandlerType(EpsModuleHandlerType.ESPER_HANDLER));
        assertEquals(EpsModuleComponentType.JAVA_COMPONENT, EpsModuleUtil.determineComponentTypeBasedOnHandlerType(EpsModuleHandlerType.JAVA_HANDLER));
        assertEquals(EpsModuleComponentType.JVM_SCRIPTING_COMPONENT, EpsModuleUtil.determineComponentTypeBasedOnHandlerType(EpsModuleHandlerType.JVM_SCRIPTING_HANDLER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_handlerTypeBasedOnComponentType() {
        EpsModuleUtil.determineHandlerTypeBasedOnComponentType(null);
        fail();
    }

    public void test_valid_handlerTypeBasedOnComponentType() {
        assertEquals(EpsModuleHandlerType.ESPER_HANDLER, EpsModuleUtil.determineHandlerTypeBasedOnComponentType(EpsModuleComponentType.ESPER_COMPONENT));
        assertEquals(EpsModuleHandlerType.JAVA_HANDLER, EpsModuleUtil.determineHandlerTypeBasedOnComponentType(EpsModuleComponentType.JAVA_COMPONENT));
        assertEquals(EpsModuleHandlerType.JVM_SCRIPTING_HANDLER, EpsModuleUtil.determineHandlerTypeBasedOnComponentType(EpsModuleComponentType.JVM_SCRIPTING_COMPONENT));
    }
}
