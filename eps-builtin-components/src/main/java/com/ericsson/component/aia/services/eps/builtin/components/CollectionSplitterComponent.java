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
package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.Collection;
import java.util.Iterator;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Builtin component used for splitting collections and arrays of events. In
 * case when received events is not collection or array it is simply delivered
 * downstream.
 *
 * @author eborziv
 *
 */
public class CollectionSplitterComponent extends AbstractEventHandler implements
        EventInputHandler {

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof Collection) {
            Collection coll = (Collection) inputEvent;
            final int count = coll.size();
            Iterator iter = coll.iterator();
            while (iter.hasNext()) {
                final Object evt = iter.next();
                sendToAllSubscribers(evt);
                // let GC clean up ASAP
                iter.remove();
            }
            iter = null;
            coll = null;
            log.debug(
                    "Split event into {} individual events and sent them to subscribers",
                    count);
        } else if (inputEvent instanceof Object[]) {
            Object[] objArr = (Object[]) inputEvent;
            final int count = objArr.length;
            for (int i = 0; i < objArr.length; i++) {
                Object obj = objArr[i];
                sendToAllSubscribers(obj);
                objArr[i] = null;
                obj = null;
            }
            log.debug(
                    "Split event into {} individual events and sent them to subscribers",
                    count);
            objArr = null;
        } else {
            log.debug("Event is not splittable.. sending it as it is {}",
                    inputEvent);
            sendToAllSubscribers(inputEvent);
        }
    }

    @Override
    protected void doInit() {

    }

}