package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator;

/**
 * Raised by {@link Validator} about semantics of model.
 */
public final class Message {

    /**
     * The Enum Kind.
     *
     * Possible values are: WARN, ERROR
     */
    public static enum Kind {
        WARN, ERROR
    }

    private final Kind kind;
    private final String message;

    /**
     * Instantiates a new message.
     *
     * @param kind
     *            the kind
     * @param message
     *            the message
     */
    public Message(final Kind kind, final String message) {
        super();
        this.kind = kind;
        this.message = message;
    }

    /**
     * Gets the kind.
     *
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
