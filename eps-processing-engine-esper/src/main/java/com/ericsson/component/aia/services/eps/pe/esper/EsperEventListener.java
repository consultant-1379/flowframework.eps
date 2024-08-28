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
package com.ericsson.component.aia.services.eps.pe.esper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 *
 * @author eborziv
 *
 */
public class EsperEventListener implements UpdateListener {

    private EpsStatisticsRegister statisticsRegister;

    private Meter eventMeter;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EventHandlerContext eventHandlerContext;

    private int numberOfSubscribers;

    private EventSubscriber singleOptimizedEventSubscriber;

    private final String statementName;

    /**
     * Instantiates a new esper event listener.
     *
     * @param eventHandlerContext
     *            the event handler context
     * @param statementName
     *            the statement name
     * @param module
     *            the module
     */
    public EsperEventListener(final EventHandlerContext eventHandlerContext, final String statementName, final EpsModule module) {
        if (eventHandlerContext == null) {
            throw new IllegalArgumentException("event handler context must not be null");
        }
        if ((statementName == null) || statementName.isEmpty()) {
            throw new IllegalArgumentException("Statement name must not be null or empty");
        }

        this.eventHandlerContext = eventHandlerContext;
        this.statementName = statementName;
        log.debug("Created Esper listener for statement {}", statementName);

        initialiseStatistics(module);

        numberOfSubscribers = 0;
        if (this.eventHandlerContext.getEventSubscribers() != null) {
            numberOfSubscribers = this.eventHandlerContext.getEventSubscribers().size();
        }
        if (numberOfSubscribers == 0) {
            log.warn("Did not find any attached subscribers to Esper listener {}. Output events from Esper will not be processed!", statementName);
        }
        if (numberOfSubscribers == 1) {
            singleOptimizedEventSubscriber = this.eventHandlerContext.getEventSubscribers().iterator().next();
            log.info("There is only one subscriber for Esper listener {}. Optimized access to {}", statementName, singleOptimizedEventSubscriber);
        } else if (numberOfSubscribers > 1) {
            log.info("There are multiple subscribers to Esper listener {}. Will send every event produced by Esper to all of them!",
                    this.eventHandlerContext.getEventSubscribers());
        }
    }

    /**
     * Initialise Statistics
     *
     * @param statementName
     * @param module
     */
    private void initialiseStatistics(final EpsModule module) {
        statisticsRegister = (EpsStatisticsRegister) eventHandlerContext
                        .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);

        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else if (statisticsRegister.isStatisticsOn()) {
            eventMeter = statisticsRegister.createMeter(module.getUniqueModuleIdentifier() + "."
                            + statementName + ".eventReceived");
        }
    }

    @Override
    public void update(final EventBean[] newEvents, final EventBean[] oldEvents) {
        if ((newEvents != null) && (newEvents.length > 0)) {
            int eventCount = 0;
            for (int i = 0; i < newEvents.length; i++) {
                EventBean eventBean = newEvents[i];
                eventCount++;
                final Object obj = eventBean.getUnderlying();
                // help generational GC
                eventBean = null;
                newEvents[i] = null;
                log.trace("Received {} from Esper. Will try to send it downstream!", obj);
                // try to send optimized - to single subscriber
                if (singleOptimizedEventSubscriber != null) {
                    singleOptimizedEventSubscriber.sendEvent(obj);
                    log.trace("Sent {} to single subscriber {}", obj, singleOptimizedEventSubscriber);
                } else {
                    // if not then send to all subscribers, have to access collection
                    for (final EventSubscriber eventSubscriber : eventHandlerContext.getEventSubscribers()) {
                        eventSubscriber.sendEvent(obj);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Successfully sent [{}] to {} subscriber(s)", obj, numberOfSubscribers);
                }
            }
            updateStatistics(eventCount);
        }
    }

    /**
     * @param eventCount
     */
    private void updateStatistics(final int eventCount) {
        if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark(eventCount);
        }
    }

    @Override
    public String toString() {
        return "EsperEventListener [numberOfSubscribers=" + numberOfSubscribers + ", statementName=" + statementName + "]";
    }

}
