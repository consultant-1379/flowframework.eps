package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

/**
 * The Class Name managed by name, namespace and version.
 */
public final class Name {

    private final String name;
    private final String namespace;
    private final String version;

    /**
     * Instantiates a new name.
     *
     * @param name
     *            the name
     * @param namespace
     *            the namespace
     * @param version
     *            the version
     */
    public Name(final String name, final String namespace, final String version) {
        super();
        this.name = name;
        this.namespace = namespace;
        this.version = version;
    }

    /**
     * Parses the from as string.
     *
     * @param str
     *            the str
     * @return the name
     */
    public static Name parseFrom(final String str) {
        final int first = str.indexOf('_');
        final int last = str.lastIndexOf('_');

        final String namespace = str.substring(0, first);
        final String name = str.substring(first + 1, last);
        final String version = str.substring(last + 1);

        return new Name(name, namespace, version);
    }

    /**
     * Serialize the name.
     *
     * @param name
     *            the name
     * @return the string
     */
    public static String serializeTo(final Name name) {
        return name.getNamespace() + '_' + name.getName() + '_' + name.getVersion();
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
        final Name other = (Name) obj;
        if (!equalsName(other)) {
            return false;
        } else if (!equalsNamespace(other)) {
            return false;
        } else if (!equalsVersion(other)) {
            return false;
        }

        return true;
    }

    private boolean equalsName(final Name other) {
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    private boolean equalsNamespace(final Name other) {
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        return true;
    }

    private boolean equalsVersion(final Name other) {
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Name[name=" + name + ", namespace=" + namespace + ", version=" + version + "]";
    }
}
