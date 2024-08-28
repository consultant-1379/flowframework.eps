/*------------------------------------------------------------------------------
 *******************************************************************************
 * © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.io.adapter.modev;

/**
 * This class defines the specialized exception: {@code ModeledEventBusAdapterException}.
 */
public class ModeledEventBusAdapterException extends RuntimeException {

    private static final long serialVersionUID = -6482490915761690803L;

    /**
     * Instantiates a {@code ModeledEventBusAdapterException}.
     */
    public ModeledEventBusAdapterException() {
        super();
    }

    /**
     * Instantiates a {@code ModeledEventBusAdapterException} with specific message and {@link Throwable}.
     *
     * @param msg
     *            the exception message
     * @param exc
     *            the Throwable exception
     */
    public ModeledEventBusAdapterException(final String msg, final Throwable exc) {
        super(msg, exc);
    }

    /**
     * Instantiates a {@code ModeledEventBusAdapterException} with specific message
     *
     * @param msg
     *            the exception message
     */
    public ModeledEventBusAdapterException(final String msg) {
        super(msg);
    }

}
