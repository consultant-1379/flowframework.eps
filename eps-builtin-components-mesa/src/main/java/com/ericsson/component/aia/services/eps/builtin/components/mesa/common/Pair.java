package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The Class Pair manages a pair of generic Object.
 *
 * @param <F>
 *            the generic type (First)
 * @param <S>
 *            the generic type (Second)
 */
public final class Pair<F, S> {

    private final F first;

    private final S second;

    /**
     * Instantiates a new pair.
     *
     * @param first
     *            the first
     * @param second
     *            the second
     */
    public Pair(final F first, final S second) {
        super();
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first.
     *
     * @return the first
     */
    public F getFirst() {
        return first;
    }

    /**
     * Gets the second.
     *
     * @return the second
     */
    public S getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(first).append(second).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }

        final Pair<?, ?> rhs = (Pair<?, ?>) obj;
        return new EqualsBuilder().append(first, rhs.first).append(second, rhs.second).isEquals();
    }
}
