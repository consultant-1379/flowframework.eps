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
package com.ericsson.component.aia.services.eps.builtin.components.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.io.adapter.util.*;
import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 *
 * @author eborziv
 *
 */
public abstract class IOComponentUtil {

    private static final Logger LOG = LoggerFactory
            .getLogger(IOComponentUtil.class);

    private static final String SERIALIZATION_MODE_PROP_NAME = "serializationMode";

    private static final String COMPATIBILITY_MODE = "compatibility";

    private static final String KRYO_MODE = "kryo";

    /**
     * Checks the value of the configuration property
     * {@value #SERIALIZATION_MODE_PROP_NAME} and returns the required
     * serializer:
     * <ul>
     * <li>{@link DefaultJavaSerializer} if {@value #COMPATIBILITY_MODE}
     * configured</li>
     * <li>{@link KryoSerializer} if {@value #KRYO_MODE} configured, or no
     * configuration is set</li>
     * </ul>
     *
     * @param config
     *            the {@link Configuration} properties, must not be null
     * @return the configured {@link ObjectSerializer} implementation
     * @throws IllegalArgumentException
     *             if config is null
     */
    public static final ObjectSerializer determineSerializer(
            final Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        final String compatMode = config
                .getStringProperty(SERIALIZATION_MODE_PROP_NAME);
        if (COMPATIBILITY_MODE.equalsIgnoreCase(compatMode)) {
            LOG.info("Configured to use compatibility serialization!");
            return new DefaultJavaSerializer();
        } else if (KRYO_MODE.equalsIgnoreCase(compatMode)) {
            LOG.info("Configured to use kryo serialization!");
            return new KryoSerializer();
        }
        LOG.info("By default using kryo serialization");
        return new KryoSerializer();
    }

}
