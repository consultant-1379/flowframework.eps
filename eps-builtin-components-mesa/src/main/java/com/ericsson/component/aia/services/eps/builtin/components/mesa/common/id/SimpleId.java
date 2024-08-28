package com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id;

/**
 * The Class SimpleId manages the Globally Unique Id.
 *
 * @see Id
 */
public final class SimpleId implements Id {

    private final long simpleId;

    /**
     * Instantiates a new simple id.
     *
     * @param sId
     *            the i
     */
    public SimpleId(final long sId) {
        simpleId = sId;
    }

    public long getId() {
        return simpleId;
    }

    @Override
    public String toString() {
        return "Id [id=" + simpleId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (simpleId ^ (simpleId >>> 32));
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
        final SimpleId other = (SimpleId) obj;
        if (simpleId != other.simpleId) {
            return false;
        }
        return true;
    }
}
