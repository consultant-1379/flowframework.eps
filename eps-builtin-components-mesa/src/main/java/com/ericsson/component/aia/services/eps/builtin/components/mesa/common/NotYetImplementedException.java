package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

/**
 * The Class NotYetImplementedException extends {@link RuntimeException}.
 */
public final class NotYetImplementedException extends RuntimeException {

    private static final long serialVersionUID = 8445602960681664544L;

    /**
     * Instantiates a new not yet implemented exception.
     */
    public NotYetImplementedException() {
        super();
    }

    /**
     * Instantiates a new not yet implemented exception.
     *
     * @param msg
     *            the msg
     * @param cause
     *            the cause
     * @param enableSuppression
     *            the enable suppression
     * @param writableStackTrace
     *            the writable stack trace
     */
    public NotYetImplementedException(final String msg, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(msg, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Instantiates a new not yet implemented exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public NotYetImplementedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new not yet implemented exception.
     *
     * @param message
     *            the message
     */
    public NotYetImplementedException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new not yet implemented exception.
     *
     * @param cause
     *            the cause
     */
    public NotYetImplementedException(final Throwable cause) {
        super(cause);
    }
}
