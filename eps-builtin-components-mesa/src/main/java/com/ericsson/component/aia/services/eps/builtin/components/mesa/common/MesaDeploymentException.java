package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

/**
 * The Class MesaDeploymentException extends {@link RuntimeException}.
 */
public class MesaDeploymentException extends RuntimeException {

    private static final long serialVersionUID = 8445602960681664544L;
    private boolean systemConsistant;

    /**
     * Instantiates a new mesa deployment exception.
     */
    public MesaDeploymentException() {
        super();
    }

    /**
     * Instantiates a new mesa deployment exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param enableSuppression
     *            the enable suppression
     * @param writableStackTrace
     *            the writable stack trace
     * @param systemConsistant
     *            the system consistant
     */
    public MesaDeploymentException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace,
                                   final boolean systemConsistant) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.systemConsistant = systemConsistant;
    }

    /**
     * Instantiates a new mesa deployment exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param systemConsistant
     *            the system consistant
     */
    public MesaDeploymentException(final String message, final Throwable cause, final boolean systemConsistant) {
        super(message, cause);
        this.systemConsistant = systemConsistant;
    }

    /**
     * Instantiates a new mesa deployment exception.
     *
     * @param message
     *            the message
     * @param systemConsistant
     *            the system consistant
     */
    public MesaDeploymentException(final String message, final boolean systemConsistant) {
        super(message);
        this.systemConsistant = systemConsistant;
    }

    /**
     * Instantiates a new mesa deployment exception.
     *
     * @param cause
     *            the cause
     * @param systemConsistant
     *            the system consistant
     */
    public MesaDeploymentException(final Throwable cause, final boolean systemConsistant) {
        super(cause);
        this.systemConsistant = systemConsistant;
    }

    /**
     * Checks if is system consistant.
     *
     * @return true, if is system consistant
     */
    public boolean isSystemConsistant() {
        return systemConsistant;
    }

}
