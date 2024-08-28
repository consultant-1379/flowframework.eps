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
 * The Class EpsModuleUtil.
 */
public abstract class EpsModuleUtil {

    private static final String ESPER_HANDLER = "EsperHandler";

    private static final String JVM_SCRIPTING_HANDLER = "JvmScriptingHandler";

    /**
     * Determine handler type.
     *
     * @param className
     *            the class name
     * @param named
     *            the named
     * @return the eps module handler type
     */
    public static EpsModuleHandlerType determineHandlerType(final String className, final String named) {
        if ((className != null) && (named != null)) {
            throw new IllegalArgumentException("a handler cannot contain both className [" + className + "] and named [" + named
                    + "]. remove one of them");
        }

        EpsModuleHandlerType type = null;

        if (className != null) {
            type = EpsModuleHandlerType.JAVA_HANDLER;
            return type;
        }

        if (named != null) {
            switch (named) {
                case ESPER_HANDLER:
                    type = EpsModuleHandlerType.ESPER_HANDLER;
                    break;
                case JVM_SCRIPTING_HANDLER:
                    type = EpsModuleHandlerType.JVM_SCRIPTING_HANDLER;
                    break;
                default:
                    type = EpsModuleHandlerType.JAVA_HANDLER;
                    break;
            }
            return type;
        }

        throw new IllegalArgumentException("a handler node must contain either a className or named node");
    }

    /**
     * Determine component type based on handler type.
     *
     * @param handlerType
     *            the handler type
     * @return the eps module component type
     */
    public static EpsModuleComponentType determineComponentTypeBasedOnHandlerType(final EpsModuleHandlerType handlerType) {
        if (handlerType == null) {
            throw new IllegalArgumentException("handler type must not be null");
        }

        EpsModuleComponentType componentType = null;

        switch (handlerType) {
            case ESPER_HANDLER:
                componentType = EpsModuleComponentType.ESPER_COMPONENT;
                break;
            case JVM_SCRIPTING_HANDLER:
                componentType = EpsModuleComponentType.JVM_SCRIPTING_COMPONENT;
                break;
            default:
                componentType = EpsModuleComponentType.JAVA_COMPONENT;
                break;
        }

        return componentType;
    }

    /**
     * Determine handler type based on component type.
     *
     * @param compType
     *            the comp type
     * @return the eps module handler type
     */
    public static EpsModuleHandlerType determineHandlerTypeBasedOnComponentType(final EpsModuleComponentType compType) {
        if (compType == null) {
            throw new IllegalArgumentException("component type must not be null");
        }

        EpsModuleHandlerType handlerType = null;

        switch (compType) {
            case ESPER_COMPONENT:
                handlerType = EpsModuleHandlerType.ESPER_HANDLER;
                break;
            case JVM_SCRIPTING_COMPONENT:
                handlerType = EpsModuleHandlerType.JVM_SCRIPTING_HANDLER;
                break;
            default:
                handlerType = EpsModuleHandlerType.JAVA_HANDLER;
                break;
        }

        return handlerType;
    }

    /**
     * Checks if is port reference component identifier.
     *
     * @param componentId
     *            the component id
     * @return true, if is port reference component identifier
     */
    public static boolean isPortReferenceComponentIdentifier(final String componentId) {
        final int indexOfDot = componentId.indexOf('.');
        if (indexOfDot != -1) {
            final String componentName = getComponentName(componentId);
            final String portName = getPortName(componentId);
            return !isEmpty(portName) && !isEmpty(componentName);
        }
        return false;
    }

    /**
     * Checks if is empty.
     *
     * @param str
     *            the str
     * @return true, if is empty
     */
    private static boolean isEmpty(final String str) {
        return (str == null) || str.trim().isEmpty();
    }

    /**
     * Gets the component name.
     *
     * @param componentId
     *            the component id
     * @return the component name
     */
    public static String getComponentName(final String componentId) {
        final int indexOfDot = componentId.indexOf('.');
        if (indexOfDot != -1) {
            final String componentName = componentId.substring(0, indexOfDot);
            return componentName;
        }
        return componentId;
    }

    /**
     * Gets the port name.
     *
     * @param componentId
     *            the component id
     * @return the port name
     */
    public static String getPortName(final String componentId) {
        final int indexOfDot = componentId.indexOf('.');
        if (indexOfDot != -1) {
            final String portName = componentId.substring(indexOfDot + 1);
            return portName;
        }
        return null;
    }

}
