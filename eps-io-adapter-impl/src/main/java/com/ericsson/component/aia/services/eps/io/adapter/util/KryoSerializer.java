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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Faster and more space-efficient serializer. Not thread safe. If there are compatibility issues use {@link DefaultJavaSerializer}.
 *
 * @author eborziv
 *
 */
public class KryoSerializer implements ObjectSerializer {

    static final AtomicInteger KRYO_INSTANCE_COUNTER = new AtomicInteger(0);

    private static final int NUM_OF_KRYO_INSTANCES_WARNING_THRESHOLD = 500;

    private static final Logger LOG = LoggerFactory.getLogger(KryoSerializer.class);

    /*
     * Have to do this because Kryo is not thread-safe
     */
    private static final ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            final Kryo kryo = new Kryo();
            final int currentNumberOfKryoInstances = KRYO_INSTANCE_COUNTER.incrementAndGet();
            if (currentNumberOfKryoInstances > NUM_OF_KRYO_INSTANCES_WARNING_THRESHOLD) {
                LOG.warn("Created {} kryo instances!", currentNumberOfKryoInstances);
            }
            return kryo;
        }
    };

    private static Kryo getThreadLocalKryoInstance() {
        return threadLocalKryo.get();
    }

    @Override
    public byte[] objectToBytes(final Object object) {
        if (object == null) {
            return null;
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Output out = new Output(bos);
        try {
            getThreadLocalKryoInstance().writeClassAndObject(out, object);
            out.flush();
        } catch (final Exception exc) {
            if (object instanceof Collection) {
                final Collection coll = (Collection) object;
                final int size = coll.size();
                final Object firstMember = coll.iterator().next();
                LOG.error("Caught exception while serializing collection of {} elements to bytes. First element in collection is {}. Details: {}",
                        new Object[] { size, firstMember, exc.getMessage() });
            } else {
                LOG.error("Caught exception while serializing object {} to bytes. Details: {}", object, exc.getMessage());
            }
            throw exc;
        }
        Resources.safeClose(out);
        Resources.safeClose(bos);
        return bos.toByteArray();
    }

    @Override
    public Object bytesToObject(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            final Input inp = new Input(bis);
            final Object back = getThreadLocalKryoInstance().readClassAndObject(inp);
            Resources.safeClose(inp);
            Resources.safeClose(bis);
            return back;
        } catch (final Exception exc) {
            LOG.error("Caught exception while deserializing bytes to object", exc);
            throw exc;
        }
    }

}
