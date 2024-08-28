package com.ericsson.component.aia.services.eps.coordination;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandler;

/**
 * A {@link Serialisable} implementation of {@link Configuration}.
 * <p>
 * Represents configuration properties for {@link EventHandler} components which can be provided <b>after</b> the
 * component is initialised.
 * <p>
 * 
 * @see EventHandler
 * @see Configuration
 * @since 1.1.102
 */

@XmlRootElement
public class EpsAdaptiveConfiguration implements Configuration, Serializable {
	
	private static final long serialVersionUID = 6146160701393848281L;
	private static final String KEY_VALUE_DELIM = ":";
	private static final String KEY_VALUE_PAIR_DELIM = ",";
	
	private Map<String, Object> configurationAttributes;
	
	@Override
	public Map<String, Object> getAllProperties() {
		return configurationAttributes;
	}
	
	/**
	 * @param propertyName
	 *            The requested property.
	 * 
	 * @return String the requested property if it exists and is a {@link Boolean} otherwise returns null.
	 */
	@Override
	public Boolean getBooleanProperty(final String propertyName) {
		if (configurationAttributes == null) {
			return null;
		}
		final Object attribute = configurationAttributes.get(propertyName);
		if ((attribute != null) && (attribute instanceof Boolean)) {
			return (Boolean) attribute;
		}
		return null;
	}
	
	/**
	 * @return {@link Map} containing the configuration properties.
	 */
	public Map<String, Object> getConfiguration() {
		return configurationAttributes;
	}
	
	/**
	 * @param propertyName
	 *            The requested property.
	 * 
	 * @return String the requested property if it exists and is a {@link Integer} otherwise returns null.
	 */
	@Override
	public Integer getIntProperty(final String propertyName) {
		if (configurationAttributes == null) {
			return null;
		}
		final Object attribute = configurationAttributes.get(propertyName);
		if ((attribute != null) && (attribute instanceof Integer)) {
			return (Integer) attribute;
		}
		return null;
	}
	
	/**
	 * @param propertyName
	 *            The requested property.
	 * 
	 * @return String the requested property if it exists and is a {@link String} otherwise returns null.
	 */
	@Override
	public String getStringProperty(final String propertyName) {
		if (configurationAttributes == null) {
			return null;
		}
		final Object attribute = configurationAttributes.get(propertyName);
		if ((attribute != null) && (attribute instanceof String)) {
			return (String) attribute;
		}
		return null;
	}
	
	/**
	 * Sets a Map of configuration properties.
	 * 
	 * @param configuration
	 *            a Map of configuration properties. Must not be null or empty.
	 */
	public void setConfiguration(final Map<String, Object> configuration) {
		ArgumentChecker.verifyArgumentNotNull("configuration properties", configuration);
		ArgumentChecker.verifyMapNotEmpty("configuration properties", configuration);
		
		configurationAttributes = configuration;
	}
	
	/**
	 * Constructs a string representation of the configuration properties. Key Value pairs are separated by ":" and
	 * followed by ","
	 * 
	 * @return {@link String} A String representation of the configuration properties.
	 */
	@Override
	public String toString() {
		if ((configurationAttributes == null) || configurationAttributes.isEmpty()) {
			return "";
		}
		
		String configurationAttributeString = "";
		for (final Map.Entry<String, Object> entry : configurationAttributes.entrySet()) {
			if ("".equalsIgnoreCase(configurationAttributeString)) {
				configurationAttributeString += entry.getKey() + KEY_VALUE_DELIM + entry.getValue();
			} else {
				configurationAttributeString += KEY_VALUE_PAIR_DELIM + entry.getKey() + KEY_VALUE_DELIM
						+ entry.getValue();
			}
		}
		return configurationAttributeString;
	}
}
