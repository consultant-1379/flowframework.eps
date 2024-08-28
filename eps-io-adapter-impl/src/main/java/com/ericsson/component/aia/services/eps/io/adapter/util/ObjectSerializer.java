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
package com.ericsson.component.aia.services.eps.io.adapter.util;

import java.io.Serializable;

/**
 * Object serialization.
 *
 * @author eborziv
 *
 */
public interface ObjectSerializer {

    /**
     * Serializes object to byte array. Object must be {@link Serializable}.
     *
     * @param object
     *            the object to be serialized.
     * @return the byte[]
     */
    byte[] objectToBytes(final Object object);

    /**
     * Converts byte[] created by {@link ObjectSerializer#objectToBytes(Object)} back to Object.
     *
     * @param bytes
     *            the bytes to be converted.
     * @return the object
     */
    Object bytesToObject(final byte[] bytes);

}
