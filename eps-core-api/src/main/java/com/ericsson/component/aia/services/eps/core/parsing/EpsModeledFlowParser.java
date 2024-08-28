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
package com.ericsson.component.aia.services.eps.core.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.util.ModelServiceUtil;
import com.ericsson.oss.itpf.common.flow.modeling.schema.gen.fbp_flow.FlowDefinition;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.direct.DirectModelAccess;
import com.ericsson.oss.itpf.modeling.modelservice.exception.ModelProcessingException;
import com.ericsson.oss.itpf.modeling.modelservice.exception.UnknownModelException;

/**
 *
 * Parser for ModelService, the flow is located using the URN in the {@link ModelInfo} class passed.
 *
 * @author epiemir
 *
 */
public class EpsModeledFlowParser implements EpsModuleParser<ModelInfo> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EpsFlowDefinitionParser flowDefinitionParser = new EpsFlowDefinitionParser();

    @Override
    public EpsModule parseModule(final ModelInfo input) {
        if (input == null) {
            throw new IllegalArgumentException("ModelInfo value must not be null");
        }
        return parseFlowUrn(input);
    }

    private EpsModule parseFlowUrn(final ModelInfo modelUrn) {
        log.debug("The flowUrn received is: {}", modelUrn.toUrn());
        final DirectModelAccess dma = getModelService().getDirectAccess();
        try {
            final FlowDefinition flwDef = dma.getAsJavaTree(modelUrn, FlowDefinition.class);
            log.debug("got FlowDefinition [{}]", flwDef);
            return flowDefinitionParser.parseFlowDef(flwDef);
        } catch (final UnknownModelException ex) {
            log.error("UnknownModelException {}", ex.getMessage());
            throw new IllegalArgumentException("invalid model URN " + ex.getMessage());
        } catch (final ModelProcessingException ex) {
            log.error("ModelProcessingException {}", ex.getMessage());
            throw new IllegalArgumentException("Invalid flow syntax " + ex.getMessage());
        }
    }

    protected ModelService getModelService() {
        return ModelServiceUtil.getModelService();
    }

}
