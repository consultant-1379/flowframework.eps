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
package com.ericsson.component.aia.services.eps.pe.jvm.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleComponentType;
import com.ericsson.component.aia.services.eps.component.module.EpsModuleStepComponent;
import com.ericsson.component.aia.services.eps.pe.core.AbstractComponentsInstaller;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * The Class JvmScriptingComponentsInstaller extends {@link AbstractComponentsInstaller}.
 */
public class JvmScriptingComponentsInstaller extends AbstractComponentsInstaller {

    private static final String REQUIRED_HANDLER_PROPERTY_NAME = "script_handler_name";

    private static final String REQUIRED_VARIABLE_NAME = "eventHandler";

    @Override
    public EpsModuleComponentType[] getSupportedTypes() {
        return new EpsModuleComponentType[] { EpsModuleComponentType.JVM_SCRIPTING_COMPONENT };
    }

    @Override
    public EventInputHandler getOrInstallComponent(final EpsModuleComponent component) {
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
            final EventInputHandler newlyLoadedComponent = createScriptComponent(epsModuleHandler);
            final String componentId = component.getInstanceId();
            final String moduleId = component.getModule().getUniqueModuleIdentifier();
            addComponentToModule(moduleId, componentId, newlyLoadedComponent);
            log.debug("Successfully created and added {}", newlyLoadedComponent);
            return newlyLoadedComponent;
        }
        throw new IllegalArgumentException("Unsupported component " + component + " for this installer");
    }

    private EventInputHandler createScriptComponent(final EpsModuleStepComponent epsModuleHandler) {
        final String engineName = epsModuleHandler.getConfiguration().getStringProperty(REQUIRED_HANDLER_PROPERTY_NAME);
        if ((engineName == null) || engineName.isEmpty()) {
            throw new IllegalArgumentException("For JVM scripting components configuration property [" + REQUIRED_HANDLER_PROPERTY_NAME
                    + "] must be specified!");
        }
        log.debug("Trying to load scripting engine by name [{}]", engineName);
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName(engineName);
        if (engine == null) {
            throw new IllegalStateException("Was not able to find scripting engine by name [" + engineName + "]");
        }
        log.debug("Successfully found engine by name {}", engineName);
        final String ruleText = epsModuleHandler.getRule().getRules().get(0).getRuleText();
        log.trace("Trying to execute {} inside engine {}", ruleText, engineName);
        try {
            engine.eval(ruleText);
        } catch (final ScriptException e) {
            log.error("Exception while executing script {} in script engine [{}]", ruleText, engineName);
            log.error("Exception details: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
        log.debug("Successfully executed script inside engine {}. Trying to find value of variable {}", engineName, REQUIRED_VARIABLE_NAME);
        final Object eventHandler = engine.get(REQUIRED_VARIABLE_NAME);
        if (eventHandler == null) {
            throw new IllegalStateException("Was not able to find required variable named [" + REQUIRED_VARIABLE_NAME
                    + "]. Every script must create one!");
        }
        if (eventHandler instanceof EventInputHandler) {
            log.debug("Object found as result of script {} implements valid interface", eventHandler);
            final EventInputHandler eih = (EventInputHandler) eventHandler;
            return eih;
        } else {
            throw new IllegalStateException("Value of variable [" + REQUIRED_VARIABLE_NAME + "] does not implement required interface "
                    + EventInputHandler.class);
        }
    }

}
