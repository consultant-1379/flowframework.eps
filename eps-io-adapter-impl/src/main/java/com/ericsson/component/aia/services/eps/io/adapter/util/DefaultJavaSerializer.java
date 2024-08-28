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

import java.io.*;

import com.ericsson.oss.itpf.sdk.resources.Resources;

/**
 * Object serializer that uses default Java serialization.
 *
 * @author eborziv
 *
 * @see ObjectSerializer
 *
 */
public class DefaultJavaSerializer implements ObjectSerializer {

    @Override
    public byte[] objectToBytes(final Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(object);
                final byte[] bytes = bos.toByteArray();
                return bytes;
            } catch (final Exception exc) {
                throw new JavaSerializationException(exc);
            }
        } finally {
            safeClose(out);
            Resources.safeClose(bos);
        }
    }

    @Override
    public Object bytesToObject(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput inObj = null;
        try {
            try {
                inObj = new ObjectInputStream(bis);
                final Object obj = inObj.readObject();
                return obj;
            } catch (final Exception exc) {
                throw new JavaSerializationException(exc);
            }
        } finally {
            Resources.safeClose(bis);
            safeClose(inObj);
        }
    }

    private static void safeClose(final ObjectOutput resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (final IOException ignored) {
                return;
            }
        }
    }

    private static void safeClose(final ObjectInput resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (final IOException ignored) {
                return;
            }
        }
    }

}
