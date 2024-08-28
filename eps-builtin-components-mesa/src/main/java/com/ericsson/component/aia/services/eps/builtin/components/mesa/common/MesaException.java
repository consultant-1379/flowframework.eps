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
package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

/**
 * The Class MesaException extends {@link RuntimeException}.
 */
public class MesaException extends RuntimeException {

    private static final long serialVersionUID = -5570669211980013868L;

    /**
     * Instantiates a new mesa exception.
     */
    public MesaException() {
        super();
    }

    /**
     * Instantiates a new mesa exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param enableSuppression
     *            the enable suppression
     * @param writableStackTrace
     *            the writable stack trace
     */
    public MesaException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Instantiates a new mesa exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public MesaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new mesa exception.
     *
     * @param message
     *            the message
     */
    public MesaException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new mesa exception.
     *
     * @param cause
     *            the cause
     */
    public MesaException(final Throwable cause) {
        super(cause);
    }
}
