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
package com.ericsson.component.aia.services.eps.coordination;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;

public class EpsAdaptiveConfigurationTest {

    private static EpsAdaptiveConfiguration epsAdaptiveConfiguration;
    private static final String STR_PROP_NAME = "aStringAttribName";
    private static final String STR_PROP_VALUE = "aStringAttribValue";
    private static final String INT_PROP_NAME = "anIntAttribName";
    private static final Integer INT_PROP_VALUE = new Integer(14);
    private static final String BOOL_PROP_NAME = "aBoolAttribName";
    private static final Boolean BOOL_PROP_VALUE = new Boolean(true);

    private static final Map<String, Object> CONFIG_ATTRIBS_NOT_EMPTY = new HashMap<String, Object>();
    private static final Map<String, Object> CONFIG_ATTRIBS_EMPTY = new HashMap<String, Object>();

    private static final String EXPECTED_TOSTRING_NOT_EMPTY = BOOL_PROP_NAME + ":" + BOOL_PROP_VALUE + "," + INT_PROP_NAME + ":" + INT_PROP_VALUE
            + "," + STR_PROP_NAME + ":" + STR_PROP_VALUE ;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpOnce() {
        CONFIG_ATTRIBS_NOT_EMPTY.put(STR_PROP_NAME, STR_PROP_VALUE);
        CONFIG_ATTRIBS_NOT_EMPTY.put(INT_PROP_NAME, INT_PROP_VALUE);
        CONFIG_ATTRIBS_NOT_EMPTY.put(BOOL_PROP_NAME, BOOL_PROP_VALUE);
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // given
        epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(CONFIG_ATTRIBS_NOT_EMPTY);

    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#setConfiguration(java.util.Map)}. Test method for
     * {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getConfiguration()}.
     */
    @Test
    public final void getSetConfiguration_validConfig_IsEqual() {

        // when
        final Map<String, Object> configAttribsReturned = epsAdaptiveConfiguration.getConfiguration();

        // then
        assertEquals(configAttribsReturned, CONFIG_ATTRIBS_NOT_EMPTY);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#setConfiguration(java.util.Map)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void setConfiguration_configAttribNull_throwIllegalArgumentException() {
        // when
        epsAdaptiveConfiguration.setConfiguration(null);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#setConfiguration(java.util.Map)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void setConfiguration_configAttribEmpty_throwIllegalArgumentException() {
        // when
        epsAdaptiveConfiguration.setConfiguration(CONFIG_ATTRIBS_EMPTY);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#toString()}.
     */
    @Test
    public final void toString_configAttribValid_returnsExpectedString() {

        // when
        final String returnedString = epsAdaptiveConfiguration.toString();

        // then
        assertEquals(EXPECTED_TOSTRING_NOT_EMPTY, returnedString);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#toString()}.
     */
    @Test
    public final void toString_configAttribNotSet_returnsEmptyString() {
        // given
        final EpsAdaptiveConfiguration epsAdaptiveConfigurationNotSet = new EpsAdaptiveConfiguration();

        // when
        final String returnedString = epsAdaptiveConfigurationNotSet.toString();

        // then
        assertEquals("", returnedString);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getIntProperty(String )}.
     */
    @Test
    public final void getIntProperty_validInteger_returnsExpectedProperty() {
        // when
        final Integer result = epsAdaptiveConfiguration.getIntProperty(INT_PROP_NAME);

        // then
        assertEquals(INT_PROP_VALUE, result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getIntProperty(String )}.
     */
    @Test
    public final void getIntProperty_propUnknown_returnsNull() {
        // when
        final Integer result = epsAdaptiveConfiguration.getIntProperty("unknownPropName");

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getIntProperty(String )}.
     */
    @Test
    public final void getIntProperty_propNotInteger_returnsNull() {
        // when
        final Integer result = epsAdaptiveConfiguration.getIntProperty(STR_PROP_NAME);

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getIntProperty(String )}.
     */
    @Test
    public final void getIntProperty_configNotSet_returnsNull() {
        // given
        final EpsAdaptiveConfiguration epsAdaptiveConfigurationNotSet = new EpsAdaptiveConfiguration();

        // when
        final Integer result = epsAdaptiveConfigurationNotSet.getIntProperty("INT_PROP_NAME");

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getStringProperty(String )}.
     */
    @Test
    public final void getStringProperty_validString_returnsExpectedProperty() {
        // when
        final String result = epsAdaptiveConfiguration.getStringProperty(STR_PROP_NAME);

        // then
        assertEquals(STR_PROP_VALUE, result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getStringProperty(String )}.
     */
    @Test
    public final void getStringProperty_propUnknown_returnsNull() {
        // when
        final String result = epsAdaptiveConfiguration.getStringProperty("unknownPropName");

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getStringProperty(String )}.
     */
    @Test
    public final void getStringProperty_propNotString_returnsNull() {
        // when
        final String result = epsAdaptiveConfiguration.getStringProperty(INT_PROP_NAME);

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getStringProperty(String )}.
     */
    @Test
    public final void getStringProperty_configNotSet_returnsNull() {
        // given
        final EpsAdaptiveConfiguration epsAdaptiveConfigurationNotSet = new EpsAdaptiveConfiguration();

        // when
        final String result = epsAdaptiveConfigurationNotSet.getStringProperty("STR_PROP_NAME");

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getBooleanProperty(String )}.
     */
    @Test
    public final void getBooleanProperty_validString_returnsExpectedProperty() {
        // when
        final Boolean result = epsAdaptiveConfiguration.getBooleanProperty(BOOL_PROP_NAME);

        // then
        assertEquals(BOOL_PROP_VALUE, result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getBooleanProperty(String )}.
     */
    @Test
    public final void getBooleanProperty_propUnknown_returnsNull() {
        // when
        final Boolean result = epsAdaptiveConfiguration.getBooleanProperty("unknownPropName");

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getBooleanProperty(String )}.
     */
    @Test
    public final void getBooleanProperty_propNotString_returnsNull() {
        // when
        final Boolean result = epsAdaptiveConfiguration.getBooleanProperty(STR_PROP_NAME);

        // then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration#getBooleanProperty(String )}.
     */
    @Test
    public final void getBooleanProperty_configNotSet_returnsNull() {
        // given
        final EpsAdaptiveConfiguration epsAdaptiveConfigurationNotSet = new EpsAdaptiveConfiguration();

        // when
        final Boolean result = epsAdaptiveConfigurationNotSet.getBooleanProperty("BOOL_PROP_NAME");

        // then
        assertNull(result);
    }
}