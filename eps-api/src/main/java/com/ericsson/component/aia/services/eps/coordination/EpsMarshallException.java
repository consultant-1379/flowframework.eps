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
package com.ericsson.component.aia.services.eps.coordination;

/**
 * Thrown to indicate failure to marshal or un-marshal an object.
 * 
 * @since 1.1.108
 */
public class EpsMarshallException extends RuntimeException {
	
	private static final long serialVersionUID = 5967378741485344441L;
	
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public EpsMarshallException() {
	}
	
	/**
	 * Constructs a new exception with the specified cause.
	 * 
	 * @param exception
	 *            the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is
	 *            permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public EpsMarshallException(final Exception exception) {
		super(exception);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param exceptionMessage
	 *            the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
	 */
	public EpsMarshallException(final String exceptionMessage) {
		super(exceptionMessage);
	}
	
	/**
	 * Constructs a new runtime exception with the specified detail message and cause.
	 * 
	 * @param exceptionMessage
	 *            the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
	 * @param exception
	 *            the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is
	 *            permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public EpsMarshallException(final String exceptionMessage, final Exception exception) {
		super(exceptionMessage, exception);
	}
	
}
