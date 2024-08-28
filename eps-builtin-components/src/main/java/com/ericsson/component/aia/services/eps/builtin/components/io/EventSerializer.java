package com.ericsson.component.aia.services.eps.builtin.components.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.io.adapter.util.ObjectSerializer;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Component capable of serializing every received event and sending serialized
 * version downstream. Useful for avoiding messaging system to do all
 * serialization and also able to do more efficient
 * serialization than built-in Java serialization.
 *
 * @author eborziv
 *
 */
public class EventSerializer extends AbstractEventHandler implements
        EventInputHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectSerializer serializer;

    @Override
    public void onEvent(Object inputEvent) {
        final long start = System.currentTimeMillis();
        final boolean inputEventWasNull = (inputEvent == null);
        byte[] serializedBytes = null;
        try {
            serializedBytes = serializer.objectToBytes(inputEvent);
        } catch (final Exception exc) {
            log.error(
                    "Exception caught while serializing event. Will not send it downstream!",
                    exc);
            inputEvent = null;
            return;
        }
        // set to null because sending downstream might take some time, allow GC
        // to clean up ASAP
        inputEvent = null;
        if (log.isDebugEnabled()) {
            final long total = System.currentTimeMillis() - start;
            int byteArraySize = 0;
            if (serializedBytes != null) {
                byteArraySize = serializedBytes.length;
            }
            log.debug(
                    "In total it took {}ms to serialize object into byte array of size {}",
                    total, byteArraySize);
        }
        final boolean shouldSend = (serializedBytes != null)
                || inputEventWasNull;
        if (shouldSend) {
            sendToAllSubscribers(serializedBytes);
        } else {
            log.error("{} serialized to null. Will not send it downstream!");
        }
    }

    @Override
    protected void doInit() {
        serializer = IOComponentUtil.determineSerializer(getConfiguration());
    }

}
