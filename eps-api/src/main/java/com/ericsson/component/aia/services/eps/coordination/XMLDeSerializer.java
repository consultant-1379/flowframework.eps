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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.cluster.coordination.DeSerializer;

/**
 * Implementation of {@code DeSerializer} to serialize/deserialize objects to/from xml.
 * 
 * @see DeSerializer
 * @since 1.1.108
 */
public class XMLDeSerializer implements DeSerializer {
	
	static final Logger LOGGER = LoggerFactory.getLogger(XMLDeSerializer.class);
	private static final String EMPTY_STRING = "";
	
	/**
	 * Tests if a String is an XML document
	 * 
	 * @param xmlCandidate
	 *            The {@link String} to test
	 * @return true if the xmlCandidate starts with the xml declaration: {@literal<?xml}
	 */
	static boolean isXML(final String xmlCandidate) {
		if ((xmlCandidate == null) || EMPTY_STRING.equalsIgnoreCase(xmlCandidate)) {
			return false;
		}
		return xmlCandidate.startsWith("<?xml");
	}
	
	private static boolean isXMLRoot(final Class<? extends Serializable> clazz) {
		if (clazz == null) {
			return false;
		}
		return clazz.isAnnotationPresent(XmlRootElement.class);
	}
	
	/**
	 * Tests if an object is annotated with {@code XmlRootElement}
	 * 
	 * @param object
	 *            the Object to test, can be null.
	 * @return true if the object's class is annotated with {@link XmlRootElement}, false otherwise
	 */
	static boolean isXMLRoot(final Serializable object) {
		if (object == null) {
			return false;
		}
		return isXMLRoot(object.getClass());
	}
	
	/**
	 * Marshal the specified object and return as a byte array.
	 * 
	 * @param object
	 *            The object to marshal. Must not be null.
	 * @return the marshalled object, as a byte array.
	 */
	public static byte[] marshal(final Serializable object) {
		if (object == null) {
			throw new EpsMarshallException("marshal failed. Input Serializable object is null.");
		}
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			
			final Marshaller marshaller = jaxbContext.createMarshaller();
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			marshaller.marshal(object, outputStream);
			return outputStream.toByteArray();
		} catch (final Exception exception) {
			LOGGER.error("Marshal failed. Details : ", exception.getMessage());
			throw new EpsMarshallException("Marshal failed. Details : " + exception.getMessage());
		}
	}
	
	/**
	 * Unmarshal the specified XML string and return an instance of the specified type.
	 * 
	 * @param xml
	 *            a {@String} which contains the XML to be unmarshalled
	 * @param type
	 *            the class of the object which is unmarshalled
	 * @param <T>
	 *            the type of the object which is unmarshalled
	 * @return the object which has been unmarshalled or null if the xml or type is null or if xml is not valid xml
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(final String xml, final Class<T> type) {
		if (type == null) {
			LOGGER.error("unmarshal failed. Class type to unmarshal is null");
			return null;
		}
		if (xml == null) {
			LOGGER.error("unmarshal failed. XML is null");
			return null;
		}
		if (!isXML(xml)) {
			LOGGER.error("Invalid XML  : [{}] ", xml);
			return null;
		}
		
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { type });
			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			final T configuration = (T) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
			return configuration;
		} catch (final Exception exception) {
			LOGGER.error("Unmarshal failed for [{}]. Details : ", type.getName(), exception.getMessage());
			throw new EpsMarshallException(
					"Unmarshal failed for " + type.getName() + ". Details : " + exception.getMessage());
		}
	}
	
	@Override
	public Serializable deserialize(final byte[] object) {
		if (object == null) {
			return null;
		}
		return new String(object);
	}
	
	@Override
	public byte[] serialize(final Serializable object) {
		if (object == null) {
			return null;
		}
		if (isXMLRoot(object)) {
			return marshal(object);
		} else {
			return object.toString().getBytes();
		}
	}
}
