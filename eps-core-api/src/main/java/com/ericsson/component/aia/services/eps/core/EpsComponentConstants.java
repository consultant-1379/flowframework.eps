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
package com.ericsson.component.aia.services.eps.core;

/**
 * Constants used for managing EPS components
 */
public interface EpsComponentConstants {

    /**
     * Parameter name used for passing adapter URI property (both for
     * input/output adapters)
     */
    String ADAPTER_URI_PROPERTY_NAME = "uri";

    /**
     * Prefix used for local (in-EPS instance) adapters. This is used to connect
     * output of one flow to the input of another flow inside same EPS instance
     * - without any need for remoting and serialization.
     */
    String LOCAL_IO_ADAPTER_PROTOCOL = "local:/";

    String LOCAL_IO_ADAPTER_CHANNEL_ID_PARAM_NAME = "channelId";

}
