package com.ericsson.component.aia.services.eps.builtin.components.mesa.state;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.IdAware;

/**
 * The Interface State extends {@link IdAware}.
 */
public interface State extends IdAware {

    String ALARM_START_KEY = "alarm.start";
    String ALARM_END_KEY = "alarm.end";

    /**
     * If this object is set/exists, it means that given resource ID is in alarming state.
     */
    String MATCHED_FLAG_KEY = "alarm.flag";

    /**
     * Clear.
     */
    void clear();

    /**
     * Checks for key.
     *
     * @param key
     *            the key
     * @return true, if successful
     */
    boolean hasKey(String key);

    /**
     * Gets the Object from the Key.
     *
     * @param key
     *            the key
     * @return the object
     */
    Object get(String key);

    /**
     * Returns old value.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the object
     */
    Object put(String key, Object value);

    /**
     * Removes the.
     *
     * @param key
     *            the key
     * @return the object
     */
    Object remove(String key);
}
