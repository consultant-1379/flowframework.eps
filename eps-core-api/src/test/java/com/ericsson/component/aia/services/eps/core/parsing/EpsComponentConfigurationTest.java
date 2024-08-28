package com.ericsson.component.aia.services.eps.core.parsing;

import java.util.Properties;

import org.junit.*;

import com.ericsson.component.aia.services.eps.core.parsing.EpsComponentConfiguration;

public class EpsComponentConfigurationTest {

    private EpsComponentConfiguration compConfig;

    @Before
    public void setup() {
        final Properties props = new Properties();
        props.put("a", "b");
        props.put("one", "1");
        props.put("two", "two");
        props.put("true", "true");
        compConfig = new EpsComponentConfiguration(props);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_empty_prop_name() {
        compConfig.getIntProperty("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_empty_prop_name2() {
        compConfig.getIntProperty("  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_prop_name() {
        compConfig.getIntProperty(null);
    }

    @Test
    public void testValidInteger() {
        final Integer val = compConfig.getIntProperty("one");
        Assert.assertEquals(1, val.intValue());
        Assert.assertNull(compConfig.getIntProperty("does_not_exist_prop"));
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidInteger() {
        compConfig.getIntProperty("two");
    }

    @Test
    public void testValidBoolean() {
        Assert.assertTrue(compConfig.getBooleanProperty("true"));
        Assert.assertFalse(compConfig.getBooleanProperty("one"));
        Assert.assertNull(compConfig.getBooleanProperty("does_not_exist_boolean_prop"));
    }

}
