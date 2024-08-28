package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

/**
 * Xml utility class for validation.
 */
public abstract class XmlUtil {

    /**
     * Validate.
     *
     * @param xml
     *            the xml
     * @param xsd
     *            the xsd
     */
    public static void validate(final File xml, final File xsd) {
        try {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = factory.newSchema(new StreamSource(xsd));
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
        } catch (final Exception e) {
            throw new MesaException(e);
        }
    }
}
