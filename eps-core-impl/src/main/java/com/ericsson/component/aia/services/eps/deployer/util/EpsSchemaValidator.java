package com.ericsson.component.aia.services.eps.deployer.util;

import java.io.*;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaUtil;
import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * The Class EpsSchemaValidator reads and validate the configured schema models.
 */
public class EpsSchemaValidator {

    private static final Logger logger = LoggerFactory.getLogger(EpsSchemaValidator.class);

    private static final Validator validator;
    private static final String modelsDir = "/schemata/";

    private static final String fbpFlowDir = "/schemata/skip_interning/";

    private final String resourceURI;

    /**
     * Instantiates a new eps schema validator.
     *
     * @param resourceURI
     *            the resource uri
     */
    public EpsSchemaValidator(final String resourceURI) {
        if ((resourceURI == null) || resourceURI.isEmpty()) {
            throw new IllegalArgumentException("Flow xml must not be null.");
        }
        this.resourceURI = resourceURI;
    }

    static {
        validator = getSchemavalidator(SchemaConstants.FBP_FLOW);
    }

    /**
     * Checks if the Resource specified by the configured URI is compliant with schema.
     *
     * @return true, if is compliant with schema
     */
    public boolean isCompliantWithSchema() {

        final InputStream inputStream = ResourceManagerUtil.loadResourceAsStreamFromURI(resourceURI);
        try {
            validator.validate(new StreamSource(inputStream));
            return true;
        } catch (SAXException | IOException e) {
            logger.error("Xml {} is not compliant with schema. Details: {}", resourceURI, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Gets the schema validator for the specified model Type if available, otherwise return null..
     *
     * @param modelType
     *            the model type
     * @return the schema validator
     *
     * @see Validator
     */
    private static Validator getSchemavalidator(final String modelType) {
        final Collection<String> dependencies = SchemaUtil.getDependenciesFor(modelType);
        if ((dependencies == null) || dependencies.isEmpty()) {
            throw new IllegalArgumentException("Unable to create Validator for model type " + modelType);
        }

        final List<StreamSource> sources = new ArrayList<StreamSource>();

        final StreamSource metaModelSource = new StreamSource(Resources.getClasspathResource(fbpFlowDir + modelType + ".xsd").getInputStream());
        sources.add(metaModelSource);

        for (final String dependency : dependencies) {
            final StreamSource dependencySource = new StreamSource(Resources.getClasspathResource(modelsDir + dependency + ".xsd").getInputStream());
            sources.add(dependencySource);
        }

        final StreamSource[] streamSources = sources.toArray(new StreamSource[sources.size()]);

        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(new LSResourceResolver() {
                @Override
                public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId,
                                               final String baseURI) {
                    final String schemaName = SchemaUtil.getSchemaNameFromSystemId(systemId);
                    final InputStream resourceAsStream = Resources.getClasspathResource(modelsDir + schemaName + ".xsd").getInputStream();
                    final LSInputHelper helper = new LSInputHelper(publicId, systemId, resourceAsStream);
                    return helper;
                }
            });
            final Schema schema = schemaFactory.newSchema(streamSources);

            final Validator validator = schema.newValidator();
            return validator;
        } catch (final Exception exception) {
            logger.error("Exception while creating Validator. Details: {}", exception.getMessage());
        }
        return null;
    }

    private static class LSInputHelper implements LSInput {

        private String systemId;
        private String publicId;
        private final BufferedInputStream inputStream;

        LSInputHelper(final String publicId, final String systemId, final InputStream resourceAsStream) {
            this.publicId = publicId;
            this.systemId = systemId;
            inputStream = new BufferedInputStream(resourceAsStream);
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(final String systemId) {
            this.systemId = systemId;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public void setPublicId(final String publicId) {
            this.publicId = publicId;
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public InputStream getByteStream() {
            return null;
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public Reader getCharacterStream() {
            return null;
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public String getStringData() {
            synchronized (inputStream) {
                try {
                    final byte[] input = new byte[inputStream.available()];
                    inputStream.read(input);
                    final String contents = new String(input);
                    return contents;
                } catch (final IOException ex) {
                    throw new IllegalStateException("There has been a problem reading the XSD.", ex);
                }
            }
        }

        @Override
        public void setBaseURI(final String baseURI) {
        }

        @Override
        public void setByteStream(final InputStream byteStream) {
        }

        @Override
        public void setCertifiedText(final boolean certifiedText) {
        }

        @Override
        public void setCharacterStream(final Reader characterStream) {
        }

        @Override
        public void setEncoding(final String encoding) {
        }

        @Override
        public void setStringData(final String stringData) {
        }
    }
}
