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

import java.io.Serializable;

import org.junit.Test;

import com.ericsson.component.aia.services.eps.coordination.EpsMarshallException;
import com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer;

public class XMLDeSerializerTest {

    XMLDeSerializer xmlDeSerializer = new XMLDeSerializer();

    TestSerialisableIsXmlRoot itemIsXmlRoot = new TestSerialisableIsXmlRoot("stringArg1", 11, false);
    TestSerialisableIsNotXmlRoot itemIsNotXmlRoot = new TestSerialisableIsNotXmlRoot("stringArg2", 22, true);

    TestSerialisableIsXmlRoot itemIsXmlRoot2 = new TestSerialisableIsXmlRoot();
    TestSerialisableIsNotXmlRoot itemIsNotXmlRoot2 = new TestSerialisableIsNotXmlRoot();

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#deserialize(byte[])}.
     */
    @Test
    public final void deserialize_nullInputGiven_shouldReturnNull() {
        // when
        final Serializable result = xmlDeSerializer.deserialize(null);

        //then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#serialize(java.io.Serializable)}.
     */
    @Test
    public final void serialize_inputIsNotXmlRoot_inputIsSuccesfullyDeserialised() {
        // when
        final byte[] serialisedInput = xmlDeSerializer.serialize(itemIsNotXmlRoot2);
        final String deserialisedInput = (String) xmlDeSerializer.deserialize(serialisedInput);

        //then
        assertTrue(deserialisedInput.startsWith(TestSerialisableIsNotXmlRoot.class.getName()));
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#serialize(java.io.Serializable)}.
     */
    @Test
    public final void serialize_inputIsXmlRoot_inputIsSuccesfullyUnmarshalled() {
        // when
        final byte[] serialisedInput = xmlDeSerializer.serialize(itemIsXmlRoot2);
        final Serializable deserialisedInput = xmlDeSerializer.deserialize(serialisedInput);
        final TestSerialisableIsXmlRoot deserialisedTestSerialisableIsXmlRoot = XMLDeSerializer.unmarshal((String) deserialisedInput,
                TestSerialisableIsXmlRoot.class);

        //then
        assertEquals(itemIsXmlRoot2, deserialisedTestSerialisableIsXmlRoot);
    }

    @Test
    public final void unmarshal_xmlInputIsNull_throwsEpsMarshallException() {
        // given
        final TestSerialisableIsXmlRoot result = XMLDeSerializer.unmarshal(null, TestSerialisableIsXmlRoot.class);
        assertNull(result);
    }

    @Test
    public final void unmarshal_classTypeIsNull_returnsNull() {
        // given
        final byte[] serialisedInput = xmlDeSerializer.serialize(itemIsXmlRoot2);
        final Serializable deserialisedInput = xmlDeSerializer.deserialize(serialisedInput);

        final TestSerialisableIsXmlRoot result = XMLDeSerializer.unmarshal((String) deserialisedInput, null);
        assertNull(result);
    }

    @Test
    public final void unmarshal_stringIsNotXml_throwsEpsMarshallException() {
        // given

        final TestSerialisableIsXmlRoot result = XMLDeSerializer.unmarshal("not XML", TestSerialisableIsXmlRoot.class);
        assertNull(result);

    }

    @Test(expected = EpsMarshallException.class)
    public final void marshal_inputIsNull_EpsMarshallExceptionThrown() {
        // given
        final byte[] serialisedInput = XMLDeSerializer.marshal(null);
        fail("expected EpsMarshallException ");
    }

    @Test(expected = EpsMarshallException.class)
    public final void marshal_inputIsNotXmlRoot_EpsMarshallExceptionThrown() {
        // given
        final byte[] serialisedInput = XMLDeSerializer.marshal(itemIsNotXmlRoot);
        fail("expected EpsMarshallException ");
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#serialize(java.io.Serializable)}.
     */
    @Test
    public final void serialize_inputIsNull_returnsNull() {
        // when
        final Serializable result = xmlDeSerializer.serialize(null);

        //then
        assertNull(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXMLRoot(java.io.Serializable)}.
     */
    @Test
    public final void isXMLRoot_argIsXmlRoot_returnsTrue() {
        // when
        final boolean result = XMLDeSerializer.isXMLRoot(itemIsXmlRoot);
        assertTrue(result);
        //then
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXMLRoot(java.io.Serializable)}.
     */
    @Test
    public final void isXMLRoot_argIsNotXmlRoot_returnsFalse() {
        // when
        final boolean result = XMLDeSerializer.isXMLRoot(itemIsNotXmlRoot);
        assertFalse(result);
        //then
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXMLRoot(java.io.Serializable)}.
     */
    @Test
    public final void isXMLRoot_argIsNull_returnsFalse() {
        // when
        final boolean result = XMLDeSerializer.isXMLRoot(null);
        assertFalse(result);
        //then
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXML(java.lang.String)}.
     */
    @Test
    public final void isXML_inputStringIsXml_returnsTrue() {
        // when
        final boolean result = XMLDeSerializer.isXML("<?xml");

        // then
        assertTrue(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXML(java.lang.String)}.
     */
    @Test
    public final void isXML_inputStringIsNotXml_returnsFalse() {
        // when
        final boolean result = XMLDeSerializer.isXML("<?xm");

        // then
        assertFalse(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXML(java.lang.String)}.
     */
    @Test
    public final void isXML_inputStringEmpty_returnsFalse() {
        // when
        final boolean result = XMLDeSerializer.isXML("");

        // then
        assertFalse(result);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer#isXML(java.lang.String)}.
     */
    @Test
    public final void isXML_inputStringNull_returnsFalse() {
        // when
        final boolean result = XMLDeSerializer.isXML(null);

        // then
        assertFalse(result);
    }

}
