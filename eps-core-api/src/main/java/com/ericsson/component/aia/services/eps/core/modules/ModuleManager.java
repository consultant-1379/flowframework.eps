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
package com.ericsson.component.aia.services.eps.core.modules;

import java.io.InputStream;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;

/**
 * EPS module manager interface. All operations on modules deployed inside EPS are exposed via this interface.
 *
 * @see EpsInstanceManager#getModuleManager()
 *
 * @author eborziv
 *
 */
public interface ModuleManager {

    /**
     * Returns number of currently deployed modules in the current EPS instance.
     *
     * @return the number of currently deployed modules.
     */
    int getDeployedModulesCount();

    /**
     * @Deprecated replaced by {@link #deployModuleFromFile()}
     *
     * @param moduleResourceInputStream
     *            {@link InputStream} pointing to content with textual representation of module flow descriptor xml file. Must not be null. Must point
     *            to valid flow descriptor.
     * @return unique identifier of module
     */
    @Deprecated
    String deployModule(InputStream moduleResourceInputStream);

    /**
     * Deploy specified module.
     *
     * @param moduleResourceInputStream
     *            {@link InputStream} pointing to content with textual representation of module flow descriptor xml file. Must not be null. Must point
     *            to valid flow descriptor.
     * @return unique identifier of module
     */
    String deployModuleFromFile(InputStream moduleResourceInputStream);

    /**
     * Deploy specified modeled module.
     *
     * @param modelUrn
     *            Urn pointing to flow descriptor stored in ModelService. Must not be null. Must point to valid flow descriptor in fb_flow name space.
     * @return unique identifier of module
     */
    String deployModuleFromModel(String modelUrn);

    /**
     * Undeploy module with unique identifier provided if it can be found in the current EPS instance.
     *
     * @param moduleId
     *            unique identifier of module. Must not be null or empty string
     * @return true if module was successfully undeployed or false otherwise
     */
    boolean undeployModule(String moduleId);

    /**
     * Undeploy all currently deployed modules in the current EPS instance.
     *
     * @return number of successfully undeployed modules
     */
    int undeployAllModules();

    /**
     * Sends control event to all currently deployed modules in the current EPS instance.
     *
     * @param controlEvent
     *            the control event. Must not be null.
     * @return number of modules which received this control event.
     */
    int sendControlEventToAllModules(ControlEvent controlEvent);

    /**
     * Sends control event to event handler identified by <tt>eventHandlerId</tt> which exists inside module identified by <tt>moduleIdentifier</tt>.
     *
     * @param moduleIdentifier
     *            identifier of module where to look for event handler
     * @param eventHandlerId
     *            identifier of event handler within module
     * @param controlEvent
     *            the control event to be sent. Must not be null
     *
     * @return number of event handlers notified.
     */
    int sendControlEventToEventHandlerById(String moduleIdentifier, String eventHandlerId, ControlEvent controlEvent);

    /**
     * Sends control event to module identified by <tt>moduleIdentifier</tt>.
     *
     * @param moduleIdentifier
     *            unique identifier of module as returned by {@link #deployModuleFromFile(InputStream)} or
     *            {@link #deployModuleFromModel(String modelUrn)}. Must not be null or empty string.
     * @param controlEvent
     *            the control event. Must not be null
     * @return true if module was found and control event was successfully delivered to it, false otherwise.
     */
    boolean sendControlEventToModuleById(String moduleIdentifier, ControlEvent controlEvent);

}
