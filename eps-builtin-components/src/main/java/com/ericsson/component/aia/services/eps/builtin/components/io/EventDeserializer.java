package com.ericsson.component.aia.services.eps.builtin.components.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.io.adapter.util.ObjectSerializer;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Component capable of deserializing serialized events. Serialization and
 * deserialization algorithms must match in order for process to be successful.
 * In case when received event is not of type <code>byte[]</code>
 * deserialization will not even be attempted and raw event will be sent
 * downstream.
 *
 * @see EventSerializer
 * @author eborziv
 *
 */
public class EventDeserializer extends AbstractEventHandler implements
        EventInputHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectSerializer serializer;

    @Override
    public void onEvent(Object inputEvent) {
        if (inputEvent instanceof byte[]) {
            byte[] serializedBytes = (byte[]) inputEvent;
            try {
                final long start = System.currentTimeMillis();
                final Object deserialized = serializer
                        .bytesToObject(serializedBytes);
                if (log.isDebugEnabled()) {
                    final long total = System.currentTimeMillis() - start;
                    log.debug(
                            "In total it took {}ms to deserialize byte array of size {} into object",
                            total, serializedBytes.length);
                }
                serializedBytes = null;
                // set to null because next operation might be long running,
                // allow GC to clean up ASAP
                inputEvent = null;
                if (deserialized != null) {
                    sendToAllSubscribers(deserialized);
                } else {
                    log.warn(
                            "{} deserialized to null. Will not send it downstream!",
                            inputEvent);
                }
            } catch (final Exception exc) {
                log.error("Problem deserializing event {}. Details {}",
                        inputEvent, exc.getMessage());
            }
        } else {
            // if not serialized just send it downstream
            sendToAllSubscribers(inputEvent);
        }
    }

    @Override
    protected void doInit() {
        serializer = IOComponentUtil.determineSerializer(getConfiguration());
    }

}
