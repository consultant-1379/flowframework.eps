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
 * The Class EpsModuleHandlerComponent.
 */
public class EpsModuleHandlerComponent extends AbstractEpsModuleComponent {

    private final EpsModuleHandlerType handlerType;

    private final String className;

    private final String named;

    /**
     * Instantiates a new eps module handler component.
     *
     * @param type
     *            the type
     * @param instanceId
     *            the instance id
     * @param module
     *            the module
     */
    public EpsModuleHandlerComponent(final EpsModuleHandlerType type, final String instanceId, final EpsModule module) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("Id must not be null or empty");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }

        this.instanceId = instanceId;
        this.module = module;
        handlerType = type;
        className = null;
        named = null;
    }

    /**
     * Instantiates a new eps module handler component.
     *
     * @param type
     *            the type
     * @param instanceId
     *            the instance id
     * @param module
     *            the module
     * @param className
     *            the class name
     * @param named
     *            the named
     */
    public EpsModuleHandlerComponent(final EpsModuleHandlerType type, final String instanceId, final EpsModule module, final String className,
                                     final String named) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("Id must not be null or empty");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }

        this.instanceId = instanceId;
        this.module = module;
        handlerType = type;
        this.className = className;
        this.named = named;
    }

    /**
     * Gets the handler type.
     *
     * @return the handlerType
     */
    public EpsModuleHandlerType getHandlerType() {
        return handlerType;
    }

    /**
     * Gets the class name.
     *
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the named.
     *
     * @return the named
     */
    public String getNamed() {
        return named;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "EpsModuleHandlerComponent [handlerType=" + handlerType + ", id=" + instanceId + "]";
    }

}
