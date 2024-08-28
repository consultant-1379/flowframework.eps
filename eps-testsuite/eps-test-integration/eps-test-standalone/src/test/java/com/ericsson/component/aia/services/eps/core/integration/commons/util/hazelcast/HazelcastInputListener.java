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
package com.ericsson.component.aia.services.eps.core.integration.commons.util.hazelcast;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class HazelcastInputListener implements MessageListener {

    public CountDownLatch cdLatch;

    private final List<Object> receivedMessages = new LinkedList<>();

    public HazelcastInputListener(final int count) {
        cdLatch = new CountDownLatch(count);
    }

    @Override
    public void onMessage(final Message arg) {
        receivedMessages.add(arg.getMessageObject());
        System.out.println("TEST: The message was: " + arg.toString());
        cdLatch.countDown();
    }

    public List<Object> getReceivedMessages() {
        return receivedMessages;
    }

    public void clear(final int count) {
        receivedMessages.clear();
        cdLatch = new CountDownLatch(count);
    }

}
