/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.parsing;

import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.FlowDefinition;
import com.ericsson.oss.itpf.modeling.schema.util.DtdModelHandlingUtil;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;

/**
 *
 * parser for file system, get the flow description from file system using the {@link InputStream} passed
 *
 * @author epiemir
 *
 */
public class EpsXmlFileParser implements EpsModuleParser<InputStream> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EpsFlowDefinitionParser flowDefinitionParser = new EpsFlowDefinitionParser();

    @Override
    public EpsModule parseModule(final InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("parse inputStream must not be null");
        }
        log.debug("Parsing module from InputStream...");
        final Unmarshaller unmarshaller = DtdModelHandlingUtil.getUnmarshaller(SchemaConstants.FBP_FLOW);
        try {
            final Object root = unmarshaller.unmarshal(new StreamSource(input));
            return flowDefinitionParser.parseFlowDef((FlowDefinition) root);
        } catch (final JAXBException jaxbexc) {
            log.error("JAXB exception caught. Unable to parse model!", jaxbexc);
            throw new IllegalArgumentException("Invalid module - unable to parse it! Details: " + jaxbexc.getMessage());
        }
    }

}
