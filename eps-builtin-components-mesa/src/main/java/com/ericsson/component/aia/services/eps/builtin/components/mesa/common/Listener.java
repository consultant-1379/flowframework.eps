package com.ericsson.component.aia.services.eps.builtin.components.mesa.common;

/**
 * The Interface Listener.
 *
 * @param <T>
 *            the generic type
 */
public interface Listener<T> {

    /**
     * On.
     *
     * @param genT
     *            the generic type
     */
    void on(T genT);
}
