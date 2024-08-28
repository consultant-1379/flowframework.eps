package com.ericsson.component.aia.services.eps.mesa.common.resource;

/**
 * Each resource/entity, whether physical (i.e. cell) or virtual (i.e. a bearer), has globally unique ID. All events which are aware of resource that
 * generated them, should implement this marker interface.
 * <p>
 *
 * See Identity Service in YMER's SAD document.
 */
public interface ResourceIdAware {

    long NULL_RESOURCE_ID = -1;

    /**
     * Gets the resource id.
     *
     * @return the resource id
     */
    long getResourceId();

    /**
     * Checks for resource id.
     *
     * @return true, if successful
     */
    boolean hasResourceId();
}
