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
package com.ericsson.component.aia.services.eps.io.adapter.hornetq;

/**
 * This class defines the specialized exception: HornetQAdapterException.
 */
public class HornetQAdapterException extends RuntimeException {

    private static final long serialVersionUID = 4717641577168303297L;

    /**
     * Instantiates a new hornetQ adapter exception.
     */
    public HornetQAdapterException() {
        super();
    }

    /**
     * Instantiates a new hornetQ adapter exception with specific message.
     *
     * @param message
     *            The exception message
     */
    public HornetQAdapterException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new hornetQ adapter exception with specific message and cause.
     *
     * @param message
     *            The exception message
     * @param cause
     *            The exception cause
     */
    public HornetQAdapterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new hornetQ adapter exception with specific cause.
     *
     * @param cause
     *            The exception cause
     */
    public HornetQAdapterException(final Throwable cause) {
        super(cause);
    }

}
