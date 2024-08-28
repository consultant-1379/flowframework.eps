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

import com.ericsson.component.aia.services.eps.component.module.EpsModule;

/**
 * @author epiemir
 *
 *         parser for build new {@link EpsModule} instance from various sources
 *
 * @param <T>
 *            Type of data used for accessing the flow
 *
 */
public interface EpsModuleParser<T> {

    /**
     *
     * Read a flow description and produce a new {@link EpsModule} instance
     *
     * @param ref
     *            Parameter used for find and read the flow.
     * @return A new {@link EpsModule} instance
     */
    EpsModule parseModule(final T ref);

}