package com.ericsson.component.aia.services.eps.builtin.components.mesa.integration.esper;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;

/**
 *
 * Used to hold the data passed around during the configuration and creation of the complete EPL files from templates.
 *
 * @author emilawl
 *
 */

public final class MesaEplTemplateConfig {

    private final Map<String, String> eplVariables;
    // TODO consider the use of an ENUM for the template type and move the file
    // paths to this class?
    private String template;
    private String baseDir;

    /**
     * Instantiates a new mesa epl template config.
     */
    public MesaEplTemplateConfig() {
        eplVariables = new HashMap<String, String>();
    }

    /**
     * Put EPL entries in map.
     *
     * @param variableName
     *            the variable name
     * @param variableValue
     *            the variable value
     */
    public void put(final String variableName, final String variableValue) {
        eplVariables.put(variableName, variableValue);
    }

    /**
     * Gets the velocity context.
     *
     * @return the velocity context
     *
     * @see VelocityContext
     *
     */
    public VelocityContext getVelocityContext() {
        final VelocityContext context = new VelocityContext();

        for (final Map.Entry<String, String> variablePair : eplVariables.entrySet()) {
            context.put(variablePair.getKey(), variablePair.getValue());
        }

        return context;
    }

    /**
     * Gets the eplVariable.
     *
     * @param variableName
     *            the variable name
     * @return the string
     */
    public String get(final String variableName) {
        return eplVariables.get(variableName);
    }

    /**
     * Sets the epl template.
     *
     * @param template
     *            the new epl template
     */
    public void setEplTemplate(final String template) {
        this.template = template;
    }

    /**
     * Sets the epl base dir.
     *
     * @param baseDir
     *            the new epl base dir
     */
    public void setEplBaseDir(final String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Gets the template.
     *
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Gets the base dir.
     *
     * @return the base dir
     */
    public String getBaseDir() {
        return baseDir;
    }

}
