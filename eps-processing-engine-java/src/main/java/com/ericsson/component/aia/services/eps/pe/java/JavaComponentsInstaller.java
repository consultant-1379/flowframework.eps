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
package com.ericsson.component.aia.services.eps.pe.java;

import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerService;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.component.aia.services.eps.component.module.*;
import com.ericsson.component.aia.services.eps.pe.core.AbstractComponentsInstaller;
import com.ericsson.component.aia.services.eps.pe.java.util.EpsCdiInstanceManager;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Component responsible for installing (creating, starting) Java event handlers.
 *
 * @author eborziv
 *
 */
public class JavaComponentsInstaller extends AbstractComponentsInstaller {

    @Override
    public EpsModuleComponentType[] getSupportedTypes() {
        return new EpsModuleComponentType[] { EpsModuleComponentType.JAVA_COMPONENT };
    }

    @Override
    public synchronized EventInputHandler getOrInstallComponent(final EpsModuleComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null");
        }

        final EventInputHandler previouslyLoadedComponent = getEventInputHandlerIfPreviouslyLoaded(component);
        if (previouslyLoadedComponent != null) {
            return previouslyLoadedComponent;
        }
        log.debug("Installing {}", component);

        if (component instanceof EpsModuleStepComponent) {
            final EpsModuleStepComponent epsModuleHandler = (EpsModuleStepComponent) component;

            final String componentId = component.getInstanceId();
            final String moduleId = component.getModule().getUniqueModuleIdentifier();
            String uri = null;
            if (null != epsModuleHandler.getHandler()) {
                uri = epsModuleHandler.getHandler().getConfiguration().getStringProperty("uri");
            }
            EventInputHandler newlyLoadedComponent;
            // Load from uri if attribute provided, otherwise load from class definition in flow
            if (null == uri || uri.length() == 0) {
                newlyLoadedComponent = installJavaComponent(epsModuleHandler);
            } else {
                newlyLoadedComponent = loadAdapter(uri, moduleId);
            }
            addComponentToModule(moduleId, componentId, newlyLoadedComponent);
            log.debug("Successfully created and added {}", newlyLoadedComponent);
            return newlyLoadedComponent;
        }
        throw new IllegalArgumentException("Unsupported component " + component + " for this installer");
    }

    /**
     * For a given uri tries to load EventHandlerService that understands that particular URI.
     *
     * @param uri - the uri
     * @param instanceId - the instanceId
     * @return initialized instance of the adapter or null of none could be found for specified uri.
     */
    public Adapter loadAdapter(final String uri, final String instanceId) {
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalArgumentException("Unable to load output adapter for null or empty URI");
        }
        if ((instanceId == null) || instanceId.isEmpty()) {
            throw new IllegalArgumentException("instance identifier must not be null or empty");
        }
        log.debug("Trying to find adapter for URI [{}]", uri);
        final ServiceLoader loader = ServiceLoader.load(EventHandlerService.class);
        final Iterator iter = loader.iterator();
        while (iter.hasNext()) {
            final Adapter adapter = (EventHandlerService) iter.next();
            log.trace("Found adapter {}", adapter);
            if (adapter.understandsURI(uri)) {
                log.debug("Found {} that understands [{}]", adapter, uri);
                return adapter;
            }
        }

        log.warn("Was not able to find adapter that understands [{}]. Check validity of URI and packaging of application!", uri);
        return null;
    }

    private EventInputHandler installJavaComponent(final EpsModuleStepComponent javaComponent) {
        final String className = javaComponent.getJavaHandlerClassName();
        final String named = javaComponent.getJavaHandlerNamed();
        if ((className != null) && (named != null)) {
            throw new IllegalArgumentException("java component [" + javaComponent + "] cannot contain both className [" + className
                    + "] and named [" + named + "]. remove one of them");
        }
        if (className != null) {
            return installNonCdiJavaComponent(className);
        }
        if (named != null) {
            return installCdiJavaComponent(named);
        }
        throw new IllegalArgumentException("java component must contain a className or named");
    }

    private EventInputHandler installCdiJavaComponent(final String componentId) {
        final Object instance = EpsCdiInstanceManager.getInstance().getBeanInstanceByName(componentId);
        if (instance instanceof EventInputHandler) {
            final EventInputHandler eih = (EventInputHandler) instance;
            log.debug("Java cdi component {} installed for componentId {}", eih, componentId);
            return eih;
        } else {
            throw new IllegalArgumentException("Instance " + instance + " is not of required type " + EventInputHandler.class.getName());
        }
    }

    @SuppressWarnings("rawtypes")
    private EventInputHandler installNonCdiJavaComponent(final String className) {
        try {
            log.debug("Trying to load class by name [{}]", className);
            final Class clazz = Class.forName(className);
            log.debug("Successfully loaded class [{}]. Creating instance of it ...", className);
            final Object instance = clazz.newInstance();
            log.trace("Successfully created instance {} of class {}", instance, className);
            if (instance instanceof EventInputHandler) {
                final EventInputHandler eih = (EventInputHandler) instance;
                log.debug("Found {}", eih);
                return eih;
            } else {
                throw new IllegalArgumentException("Instance " + instance + " is not of required type " + EventInputHandler.class.getName());
            }
        } catch (final ClassNotFoundException e) {
            log.error("Was not able to find class by name [{}]", className);
        } catch (final InstantiationException e) {
            log.error("Unable to create instance of class [{}]. Details {}", className, e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error("Exception while trying to create instance of class [{}]. Details {}", className, e.getMessage());
        }
        throw new IllegalArgumentException("Cannot install a component for the requested className: " + className);
    }
}
