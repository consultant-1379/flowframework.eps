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

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponentType;
import com.ericsson.component.aia.services.eps.core.EpsComponentConstants;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.eps.pe.core.AbstractComponentsInstaller;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Installer responsible for installing IO adapters.
 *
 * @author eborziv
 *
 */
public class IOAdapterComponentInstaller extends AbstractComponentsInstaller {

    private final EpsProvider epsProvider = EpsProvider.getInstance();

    @Override
    public EventInputHandler getOrInstallComponent(final EpsModuleComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null");
        }
        log.debug("Installing {}", component);
        final EpsModuleComponentType type = component.getComponentType();
        final Configuration componentConfig = component.getConfiguration();
        final String uri = componentConfig.getStringProperty(EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME);
        final String instanceId = component.getInstanceId();
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalStateException("IO adapter with name [" + instanceId + "] does not have associated required "
                    + EpsComponentConstants.ADAPTER_URI_PROPERTY_NAME + " configuration property!");
        }
        EventInputHandler componentHandler = null;
        if (type == EpsModuleComponentType.OUTPUT_ADAPTER) {
            componentHandler = epsProvider.loadOutputAdapter(uri, component.getComponentId());
        } else {
            componentHandler = epsProvider.loadInputAdapter(uri, component.getComponentId());
        }
        addComponentToModule(component.getModule().getUniqueModuleIdentifier(), instanceId, componentHandler);
        log.debug("Registered component [{}] under name [{}]", component, instanceId);
        return componentHandler;
    }

    @Override
    public EpsModuleComponentType[] getSupportedTypes() {
        return new EpsModuleComponentType[] { EpsModuleComponentType.INPUT_ADAPTER, EpsModuleComponentType.OUTPUT_ADAPTER };
    }

}
