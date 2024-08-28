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
package com.ericsson.component.aia.services.eps.io.adapter.jms;

/**
 *
 * @author eborziv
 *
 */
public class JmsAdapterException extends RuntimeException {

    private static final long serialVersionUID = -5934930026538299168L;

    /**
     * Instantiates a new jms adapter exception.
     */
    public JmsAdapterException() {
        super();
    }

    /**
     * Instantiates a new jms adapter exception with specific message and {@link Throwable}.
     *
     * @param msg
     *            the exception message
     * @param exc
     *            the Throwable exception
     */
    public JmsAdapterException(final String msg, final Throwable exc) {
        super(msg, exc);
    }

}
