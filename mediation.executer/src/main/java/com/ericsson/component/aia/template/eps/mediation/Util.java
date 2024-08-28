package com.ericsson.component.aia.template.eps.mediation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class Util {

	private static final Logger log = LoggerFactory.getLogger(Util.class);

	/**
	 * Get the property value or return default if not set
	 * @param property - the property
	 * @param defaultValue - default value to return if not set
     * @return - the property
     */
	public static String getStringSysProp(final String property, final String defaultValue) {
        String result = defaultValue;
        final String prop = System.getProperty(property);
        if (prop != null && !prop.isEmpty()) {
            result = prop.trim();
        }
        log.info("Property {} set to {} ", property, result);
        return result;
    }

	/**
	 * Get number of seconds until next minute
	 * @return - the number of seconds
     */
	public static long getSecsUntilNextMin() {
		return ((int) (getMillisecsUntilNextMin() / 1000)) + 1;
	}

	/**
	 * Get number of milliseconds until next minute
	 * @return - the number of milliseconds
     */
	public static long getMillisecsUntilNextMin() {
		final Calendar cal = Calendar.getInstance();
		cal.get(Calendar.MINUTE);
		return 60000 - System.currentTimeMillis() % 60000;
	}

	/**
	 * Sleep current thread for specified number of seconds
	 * @param seconds - number of seconds to sleeo
     */
	public static void sleep(final int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
