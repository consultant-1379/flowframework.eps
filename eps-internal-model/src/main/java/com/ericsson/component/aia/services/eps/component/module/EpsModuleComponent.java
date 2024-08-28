/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.component.module;

/**
 * The Class EpsModuleComponent.
 */
public class EpsModuleComponent extends AbstractEpsModuleComponent {

    /** The component type. */
    private final EpsModuleComponentType componentType;

    /**
     * Instantiates a new eps module component.
     *
     * @param type
     *            the type
     * @param instanceId
     *            the instance id
     * @param module
     *            the module
     */
    public EpsModuleComponent(final EpsModuleComponentType type, final String instanceId, final EpsModule module) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("Id must not be null or empty");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        componentType = type;
        this.instanceId = instanceId;
        this.module = module;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EpsModuleComponent [id=" + instanceId + ", componentType=" + componentType + ", configuration=" + getConfiguration() + "]";
    }

    /**
     * Gets the component type.
     *
     * @return the componentType
     */
    public EpsModuleComponentType getComponentType() {
        return componentType;
    }

    /*
     * @see
     * com.ericsson.component.aia.services.eps.component.module.AbstractEpsModuleComponent
     * #setInstanceId(java.lang.String)
     */
    @Override
    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

}
