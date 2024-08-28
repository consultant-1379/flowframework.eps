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

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;

/**
 *
 * @author eborziv
 *
 */
public interface EpsModuleInstaller {

    /**
     * loads and installs the components for the specified EpsModule
     *
     * @param module
     *            the {@link EpsModule}
     * @param moduleManager
     *            a {@link ModuleManager}
     */
    void installEpsModuleComponents(final EpsModule module,
            ModuleManager moduleManager);

}