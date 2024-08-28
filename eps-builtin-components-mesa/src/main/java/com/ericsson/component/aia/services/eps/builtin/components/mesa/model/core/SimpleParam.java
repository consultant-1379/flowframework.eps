package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.core;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Type;

/**
 * The Class SimpleParam.
 *
 * @see Param
 */
public final class SimpleParam implements Param {

    private final String name;
    private final Object value;
    private final Type type;

    /**
     * Instantiates a new simple param.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param type
     *            the type
     */
    public SimpleParam(final String name, final Object value, final Type type) {
        super();
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public int compareTo(final Param rhs) {
        return name.compareTo(rhs.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
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
        final SimpleParam other = (SimpleParam) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
