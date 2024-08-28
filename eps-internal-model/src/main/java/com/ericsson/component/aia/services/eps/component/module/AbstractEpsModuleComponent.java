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
package com.ericsson.component.aia.services.eps.component.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.Identifiable;
import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * The Class AbstractEpsModuleComponent.
 *
 * @author enmadmin
 */
public abstract class AbstractEpsModuleComponent implements Identifiable {

    protected String instanceId;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected EpsModule module;

    private Configuration configuration;

    /*
     * @return the unique component ID
     */
    public String getComponentId() {
        return getModule().getUniqueModuleIdentifier() + "_" + this.instanceId;
    }

    /**
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /*
     * @see com.ericsson.oss.itpf.common.Identifiable#getInstanceId()
     */
    @Override
    public String getInstanceId() {
        return this.instanceId;
    }

    /**
     * @return the module
     */
    public EpsModule getModule() {
        return module;
    }

    /**
     * @param configuration
     *            the configuration to set
     */
    public void setConfiguration(final Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        this.configuration = configuration;
    }

    /*
     * @see com.ericsson.oss.itpf.common.Identifiable#setInstanceId(java.lang.String)
     */
    @Override
    public void setInstanceId(final String instanceId) {
        throw new IllegalStateException("not supported");
    }

}
