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
package com.ericsson.component.aia.services.eps.core.integration.jee.modeledevent;

import java.util.ArrayList;
import java.util.Collection;

import com.ericsson.component.aia.itpf.sdk.eventbus.model.classic.ModeledEventConsumerBean;
import com.ericsson.oss.itpf.sdk.eventbus.classic.EMessageListener;
import com.ericsson.oss.itpf.sdk.eventbus.model.ModeledEvent;

public class TestModeledEventReceiver {
    final ModeledEventConsumerBean modeledConsumerBean = new ModeledEventConsumerBean("MyTestNameSpace", "MyTestOutputEvent", "1.0.0");

    Collection<String> received = new ArrayList<String>();

    /**
     * @return the received
     */
    public Collection<String> getReceived() {
        return received;
    }

    public void subscribeForEvents() {
        modeledConsumerBean.startListening(new TestModeledMessageListener());
    }

    public void unsubscribe() {
        if (modeledConsumerBean.isListeningForMessages()) {
            modeledConsumerBean.stopListening();
        }
    }

    class TestModeledMessageListener implements EMessageListener<ModeledEvent> {
        @Override
        public void onMessage(final ModeledEvent msg) {
            received.add((String) msg.getAttributes().get("myTestValue"));
        }
    }

}
