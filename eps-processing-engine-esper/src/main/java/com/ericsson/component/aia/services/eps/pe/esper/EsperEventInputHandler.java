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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.component.module.rule.EpsRulesModuleComponent;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.eps.pe.esper.util.EventTypeStatisticsHandlersProxy;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.espertech.esper.client.*;

/**
 *
 * @author eborziv
 *
 */
public class EsperEventInputHandler implements EventInputHandler {

    private static final Map<String, Meter> knownEventTypes = new HashMap<String, Meter>();

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final EpsRulesModuleComponent epsRulesComponent;
    private final String specificOutputPortName;
    private final String esperEventName;
    private final EPServiceProvider epServiceProvider;
    private final EPRuntime epRuntime;
    private final EpsStatisticsRegister statisticsRegister = new EventTypeStatisticsHandlersProxy();

    /**
     * Instantiates a new esper event input handler.
     *
     * @param epsRulesComponent
     *            the eps rules component
     * @param epServiceProvider
     *            the ep service provider
     * @param specificOutputPortName
     *            the specific output port name
     */
    public EsperEventInputHandler(final EpsRulesModuleComponent epsRulesComponent, final EPServiceProvider epServiceProvider,
                                  final String specificOutputPortName) {
        if (epsRulesComponent == null) {
            throw new IllegalArgumentException("EpsRulesComponent must not be null");
        }
        esperEventName = epsRulesComponent.getInputRuleName();
        if ((esperEventName == null) || esperEventName.isEmpty()) {
            log.warn("esperEventName not provided. You will not be able to use java.util.Map as event format for Esper!");
        } else {
            log.info("esperEventName={}, all Map instances will be sent to Esper under this name", esperEventName);
        }
        if (epServiceProvider == null) {
            throw new IllegalArgumentException("EPServiceProvider must not be null");
        }
        this.epServiceProvider = epServiceProvider;
        epRuntime = this.epServiceProvider.getEPRuntime();
        this.specificOutputPortName = specificOutputPortName;
        this.epsRulesComponent = epsRulesComponent;
    }

    @Override
    public void init(final EventHandlerContext ctx) {
        subscribeForOutputEvents(epsRulesComponent, epServiceProvider.getEPAdministrator(), ctx);
    }

    private boolean shouldAttachAnySubscribersToEsperStatements(final EventHandlerContext ctx) {
        final int numOfSubscribers = ctx.getEventSubscribers().size();
        return numOfSubscribers > 0;
    }

    private void subscribeForOutputEvents(final EpsRulesModuleComponent epsRulesComp, final EPAdministrator epAdmin,
                                          final EventHandlerContext ctxWithEsperSubscribers) {
        log.debug("Trying to subscribe for output events for {}", epsRulesComp);
        final boolean shouldAttachSubscribers = shouldAttachAnySubscribersToEsperStatements(ctxWithEsperSubscribers);
        if (!shouldAttachSubscribers) {
            log.debug("Will not subscribe to any Esper statements for {}", epsRulesComp);
            return;
        }

        final Set<EPStatement> availableEPStatements = new HashSet<EPStatement>();
        subscribeEsperStatement(epsRulesComp, epAdmin, availableEPStatements);

        for (final EPStatement epStatement : availableEPStatements) {
            final String epStatementName = epStatement.getName();
            final UpdateListener listener = new EsperEventListener(ctxWithEsperSubscribers, epStatementName, epsRulesComp.getModule());
            epStatement.addListener(listener);
            if (log.isDebugEnabled()) {
                log.debug("Attached listener {} to Esper statement {}", listener, epStatement.getText());
                int listenerCount = 0;
                final Iterator<UpdateListener> updateListeners = epStatement.getUpdateListeners();
                if (updateListeners != null) {
                    while (updateListeners.hasNext()) {
                        final UpdateListener lst = updateListeners.next();
                        log.debug("Found listener {}", lst);
                        listenerCount++;
                    }
                }
                log.debug("Currently Esper statement {} has {} listeners attached", epStatement.getText(), listenerCount);
            }
        }
        log.debug("Successfully subscribed for output events in {}", epsRulesComp);
    }

    private void subscribeEsperStatement(final EpsRulesModuleComponent epsRulesComp, final EPAdministrator epAdmin,
                                         final Set<EPStatement> availableEPStatements) {
        log.debug("Will subscribe to Esper statements...");
        if (!EpsUtil.isEmpty(specificOutputPortName)) {
            log.debug("Subscribing only for specific output port {}", specificOutputPortName);
            boolean foundPort = false;
            for (final String epsOutputRuleName : epsRulesComp.getOutputRuleNames()) {
                if (specificOutputPortName.equals(epsOutputRuleName)) {
                    subscribeForSingleEsperStatement(epAdmin, availableEPStatements, epsOutputRuleName);
                    foundPort = true;
                }
            }
            if (!foundPort) {
                throw new IllegalStateException("Was not able to find port with name " + specificOutputPortName);
            }
        } else {
            log.debug("Subscribing for all output ports.");
            for (final String epsOutputRuleName : epsRulesComp.getOutputRuleNames()) {
                subscribeForSingleEsperStatement(epAdmin, availableEPStatements, epsOutputRuleName);
            }
        }
    }

    /**
     * @param epAdmin
     * @param availableEPStatements
     * @param epsOutputRuleName
     */
    private void subscribeForSingleEsperStatement(final EPAdministrator epAdmin, final Set<EPStatement> availableEPStatements,
                                                  final String epsOutputRuleName) {
        log.debug("Subscribing for output events from Esper statement [{}]", epsOutputRuleName);
        final EPStatement epStatement = epAdmin.getStatement(epsOutputRuleName);
        if (epStatement == null) {
            throw new IllegalStateException("Was not able to find Esper statement by name " + epsOutputRuleName
                    + "! Check your EPL file. Did you use @Name annotation where necessary?");
        } else {
            log.debug("Found Esper statement {} by name {}", epStatement, epsOutputRuleName);
            availableEPStatements.add(epStatement);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof Map) {
            final Map eventMap = (Map) inputEvent;
            epRuntime.sendEvent(eventMap, esperEventName);
        } else if (inputEvent != null) {
            epRuntime.sendEvent(inputEvent);
            updateStatistics(inputEvent);
        }
    }

    /**
     * @param inputEvent
     */
    private void updateStatistics(final Object inputEvent) {
        // needed for counting specific event types
        if (shouldCountTypes()) {
            final String meterName = inputEvent.getClass().getSimpleName() + ".eventsReceived";
            final Meter meter = getMeterForEvent(meterName);
            meter.mark();
        }
    }

    private boolean shouldCountTypes() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn() && log.isDebugEnabled();
    }

    /**
     * @param meterName
     * @return
     */
    private Meter getMeterForEvent(final String meterName) {
        Meter meter = null;
        if (!knownEventTypes.containsKey(meterName)) {
            meter = statisticsRegister.createMeter(meterName);
            knownEventTypes.put(meterName, meter);
        } else {
            meter = knownEventTypes.get(meterName);
        }
        return meter;
    }

}
