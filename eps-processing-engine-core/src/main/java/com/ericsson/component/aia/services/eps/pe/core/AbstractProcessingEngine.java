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
package com.ericsson.component.aia.services.eps.pe.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.pe.ProcessingEngine;
import com.ericsson.component.aia.services.eps.pe.ProcessingEngineContext;

/**
 *
 * @author eborziv
 *
 */
public abstract class AbstractProcessingEngine implements ProcessingEngine {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ENGINE_STATE state = ENGINE_STATE.STOPPED;

    private String instanceId;
    private String engineTypeId;

    /**
     * The Enum ENGINE_STATE.
     *
     * The possible values are: STARTED, STOPPED.
     *
     */
    static enum ENGINE_STATE {
        STARTED, STOPPED
    }

    /**
     * @param engineTypeId
     *            the engineTypeId to set
     */
    public void setEngineType(final String engineTypeId) {
        if ((engineTypeId == null) || engineTypeId.isEmpty()) {
            throw new IllegalArgumentException("Engine type id must not be null or empty");
        }
        this.engineTypeId = engineTypeId;
    }

    /**
     * @param instanceId
     *            the instanceId to set
     */
    @Override
    public void setInstanceId(final String instanceId) {
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("Instance id must not be null or empty");
        }
        this.instanceId = instanceId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getEngineType() {
        return engineTypeId;
    }

    @Override
    public void start(final ProcessingEngineContext peContext) {
        if (peContext == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (state == ENGINE_STATE.STARTED) {
            log.warn("Engine {} already started");
        } else {
            doStart(peContext);
            state = ENGINE_STATE.STARTED;
        }
    }

    /**
     * Do start, abstract.
     *
     * @param ctx
     *            the context {@link ProcessingEngineContext}
     */
    protected abstract void doStart(ProcessingEngineContext ctx);

    @Override
    public void destroy() {
        if (state == ENGINE_STATE.STOPPED) {
            log.warn("Engine already stopped");
        } else {
            doStop();
            state = ENGINE_STATE.STOPPED;
        }
    }

    /**
     * Do stop, abstract.
     */
    protected abstract void doStop();

}
