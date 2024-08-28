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
package com.ericsson.component.aia.services.eps.io.adapter.util;

/**
 * This class define the specialized exception JavaSerializationException.
 *
 * @see RuntimeException
 */
public class JavaSerializationException extends RuntimeException {

    private static final long serialVersionUID = -4100858333652483984L;

    /**
     * Instantiates a new java serialization exception.
     */
    public JavaSerializationException() {
        super();
    }

    /**
     * Instantiates a new java serialization exception with a specific message.
     *
     * @param message
     *            the message
     */
    public JavaSerializationException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new java serialization exception with a specific message and cause.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public JavaSerializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new java serialization exception with a specific cause.
     *
     * @param cause
     *            the cause
     */
    public JavaSerializationException(final Throwable cause) {
        super(cause);
    }

}
