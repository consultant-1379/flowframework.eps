package com.ericsson.component.aia.services.eps.builtin.components.mesa.model;

// TODO use model service POJOs etc...
/**
 * The Class EventRef.
 */
public final class EventRef {

    private final String name;
    private final String namespace;
    private final String version;

    /**
     * Instantiates a new event ref.
     *
     * @param name
     *            the name
     * @param namespace
     *            the namespace
     * @param version
     *            the version
     */
    public EventRef(final String name, final String namespace, final String version) {
        super();
        this.name = name;
        this.namespace = namespace;
        this.version = version;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the namespace.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + ((namespace == null) ? 0 : namespace.hashCode());
        result = (prime * result) + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EventRef other = (EventRef) obj;
        if (!isNameEqual(other)) {
            return false;
        }
        if (!isNamespaceEqual(other)) {
            return false;
        }
        if (!isVersionEqual(other)) {
            return false;
        }

        return true;
    }

    private boolean isNameEqual(final EventRef other) {
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        return true;
    }

    private boolean isNamespaceEqual(final EventRef other) {
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }

        return true;
    }

    private boolean isVersionEqual(final EventRef other) {
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }

        return true;
    }
}
