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
package com.ericsson.component.aia.services.eps.component.module.assembler;

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponentType;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 *
 * @author eborziv
 *
 */
public interface EpsModuleComponentInstaller {

    /**
     *
     * @return an array of the supported {@link EpsModuleComponentType}
     */
    EpsModuleComponentType[] getSupportedTypes();

    /**
     *
     * @param component
     *            the component to install
     * @return the {@link EventInputHandler} for the component
     */
    EventInputHandler getOrInstallComponent(EpsModuleComponent component);

    /**
     *
     * @param component
     *            the component to install
     * @param portName
     *            the port name to use
     * @return the {@link EventInputHandler} for the component
     */
    EventInputHandler getOrInstallComponentAndReferencePort(
            EpsModuleComponent component, String portName);

    /**
     *
     * @param moduleId
     *            the module to uninstall
     */
    void uninstallModule(String moduleId);

}
