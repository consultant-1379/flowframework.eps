package com.ericsson.component.aia.services.eps.mesa.locator;

import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Locator using new, smart & YMER aligned data naming and identification conventions. This locator would transitively depend on all analytical data
 * stores we will have in our data layer (BDS, ADS, IMKVS etc).
 */
public interface SmartLocator extends Locator {

    /**
     * Returns true if this locator can locate events of given type that appeared rops count of ROPs into the past or future compare to ROP event
     * belongs to. Negative values of rops paramter refer to past, while positive values refer to future, relative to event.
     *
     * @param event
     *            the event
     * @param ropCount
     *            the rop count
     * @return true, if successful
     */
    boolean canLocate(Event event, int ropCount);

    /**
     * Returns true if this locator can locate events of given type that appeared at given ROP ID for given resource ID.
     *
     * @param eventTypeName
     *            the event type name
     * @param resourceId
     *            the resource id
     * @param ropId
     *            the rop id
     * @return true, if successful
     */
    boolean canLocate(String eventTypeName, long resourceId, long ropId);

    /**
     * Locate.
     *
     * @param source
     *            the source
     * @param ropCount
     *            the rop count
     * @return the event
     */
    Event locate(Event source, int ropCount);

    /**
     * Locate.
     *
     * @param eventTypeName
     *            the event type name
     * @param resourceId
     *            the resource id
     * @param ropId
     *            the rop id
     * @return the event
     */
    Event locate(String eventTypeName, long resourceId, long ropId);
}
