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

import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * The Class EpsModuleStepComponent.
 */
public class EpsModuleStepComponent extends EpsModuleComponent {

    private EpsModuleHandlerComponent handler;

    private EpsRulesModuleComponent rule;

    /**
     * Instantiates a new eps module step component.
     *
     * @param type
     *            the type
     * @param instanceId
     *            the instance id
     * @param module
     *            the module
     */
    public EpsModuleStepComponent(final EpsModuleComponentType type, final String instanceId, final EpsModule module) {
        super(type, instanceId, module);
    }

    /**
     * Gets the java handler class name.
     *
     * @return the java handler class name
     */
    public String getJavaHandlerClassName() {
        return handler.getClassName();
    }

    /**
     * Gets the java handler named.
     *
     * @return the java handler named
     */
    public String getJavaHandlerNamed() {
        return handler.getNamed();
    }

    /**
     * Gets the handler.
     *
     * @return the handler
     */
    public EpsModuleHandlerComponent getHandler() {
        return handler;
    }

    /**
     * Sets the handler.
     *
     * @param handler
     *            the handler to set
     */
    public void setHandler(final EpsModuleHandlerComponent handler) {
        this.handler = handler;
    }

    /**
     * Gets the rule.
     *
     * @return the rule
     */
    public EpsRulesModuleComponent getRule() {
        return rule;
    }

    /**
     * Sets the rule.
     *
     * @param rule
     *            the rule to set
     */
    public void setRule(final EpsRulesModuleComponent rule) {
        this.rule = rule;
    }

    /*
     * @see
     * com.ericsson.component.aia.services.eps.component.module.AbstractEpsModuleComponent
     * #getConfiguration()
     */
    @Override
    public Configuration getConfiguration() {
        return handler.getConfiguration();
    }

    /*
     * @see
     * com.ericsson.component.aia.services.eps.component.module.EpsModuleComponent#toString
     * ()
     */
    @Override
    public String toString() {
        return "EpsModuleHandlerComponent [engine=" + handler + ", rule=" + rule + "]";
    }

}
