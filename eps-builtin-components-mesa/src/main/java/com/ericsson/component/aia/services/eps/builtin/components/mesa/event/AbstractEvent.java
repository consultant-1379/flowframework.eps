package com.ericsson.component.aia.services.eps.builtin.components.mesa.event;

import java.io.Serializable;
import java.util.Map;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.NotYetImplementedException;
import com.ericsson.component.aia.services.eps.mesa.common.rop.RopType;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * This class represents the AbstractEvent.
 *
 * It implements {@link Event}, {@link Serializable}
 *
 */
public abstract class AbstractEvent implements Event, Serializable {

    private static final long serialVersionUID = -3392431111542293984L;

    private long typeId;
    private long resourceId = NULL_RESOURCE_ID;
    private long ropId = NULL_ROP_ID;
    private RopType ropType;
    private long timestamp = System.currentTimeMillis();

    /**
     * Sets the event type id.
     *
     * @param typeId
     *            the new event type id
     */
    public final void setEventTypeId(final long typeId) {
        this.typeId = typeId;
    }

    /**
     * Sets the rop type.
     *
     * @param ropType
     *            the new rop type
     */
    public final void setRopType(final RopType ropType) {
        this.ropType = ropType;
    }

    /**
     * Sets the rop type.
     *
     * @param eventName
     *            the new rop type from eventName
     */
    public final void setRopType(final String eventName) {
        final String[] quallifiedNameParts = eventName.split("\\.");
        final String[] eventNameParts = (quallifiedNameParts[(quallifiedNameParts.length - 1)]).split("_");

        switch (eventNameParts[eventNameParts.length - 1]) {
            case "60MIN":
                this.setRopType(RopType.ROP_60MIN);
                break;
            case "5MIN":
                this.setRopType(RopType.ROP_5MIN);
                break;
            case "15MIN":
                this.setRopType(RopType.ROP_15MIN);
                break;
            default:
                this.setRopType(RopType.ROP_RAW);
        }
    }

    /**
     * Sets the resource id.
     *
     * @param resourceId
     *            the new resource id
     */
    public final void setResourceId(final long resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Sets the rop id.
     *
     * @param ropId
     *            the new rop id
     */
    public final void setRopId(final long ropId) {
        this.ropId = ropId;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp
     *            the new timestamp
     */
    public final void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public final long getResourceId() {
        return resourceId;
    }

    @Override
    public final boolean hasResourceId() {
        return resourceId != NULL_RESOURCE_ID;
    }

    @Override
    public final RopType getRopType() {
        return ropType;
    }

    @Override
    public final long getRopId() {
        return ropId;
    }

    @Override
    public final boolean hasRopId() {
        return ropId != NULL_ROP_ID;
    }

    @Override
    public final long getTimestamp() {
        return timestamp;
    }

    @Override
    public final long eventTypeId() {
        return typeId;
    }

    @Override
    public final Map<String, Object> getHeaders() {
        throw new NotYetImplementedException("The getHeaders() method has not yet been implemented for the abstractEvent");
    }

    @Override
    public final Object getPayload() {
        throw new NotYetImplementedException("The getPayload() method has not yet been implemented for the abstractEvent");
    }

    @Override
    public final String getNamespace() {
        throw new NotYetImplementedException("The getNamespace() method has not yet been implemented for the abstractEvent");
    }

    @Override
    public final String getName() {
        throw new NotYetImplementedException("The getName() method has not yet been implemented for the abstractEvent");
    }

    @Override
    public final String getVersion() {
        throw new NotYetImplementedException("The getVersion() method has not yet been implemented for the abstractEvent");
    }
}
