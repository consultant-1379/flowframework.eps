package com.ericsson.component.aia.services.eps.builtin.components.mesa.view;

import java.util.Iterator;
import java.util.List;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.id.Id;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Snapshot of events that is passed from streaming domain (Esper) into imperative domain (Jython) for IF-THEN-ELSE rules.
 */
public interface View {

    /**
     * The Enum ViewType.
     *
     * Possible values are: SINGLETON, SEQUENCE, MATRIX, CUBE, BAG
     */
    public static enum ViewType {
        SINGLETON, // one event
        SEQUENCE, // events of one type, for one logical resource
        MATRIX, // events of multiple types, for one logical resource
        CUBE, // events of multiple types, for multiple logical resources
        BAG, ;

        /**
         * Parses the ViewType by String.
         *
         * @param str
         *            the str
         * @return the view type
         */
        public static ViewType parse(final String str) {
            for (final ViewType viewType : values()) {
                if (viewType.name().equalsIgnoreCase(str)) {
                    return viewType;
                }
            }
            throw new IllegalArgumentException("Unable to parse view type from string '" + str + "'");
        }
    }

    /**
     * ID of the core this view is destined for. Used for internal routing.
     *
     * @return the policy core id
     */
    Id getPolicyCoreId();

    /**
     * ID of the rule group this view is destined for. Used for internal routing.
     *
     * @return the rule group id
     */
    Id getRuleGroupId();

    /**
     * Gets the view type.
     *
     * @return the view type
     */
    ViewType getViewType();

    /**
     * Event Iterator.
     *
     * @return the iterator
     */
    Iterator<Event> iterator();

    /**
     * Gets the first Event.
     *
     * @return the first
     */
    Event getFirst();

    /**
     * Gets all the Events as List.
     *
     * @return the all
     */
    List<Event> getAll();
}
