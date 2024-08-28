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
package com.ericsson.component.aia.services.eps.core.module.assembler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Used to track statistics for {@link EventInputHandler} instances used by EPS. This delegate exposes all statistics about underlying
 * {@link EventInputHandler}.
 *
 * @author eborziv
 */
public class StatisticsAwareEventInputHandlerDelegate implements EventInputHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EventInputHandler eih;

    private Meter eventMeter;

    /**
     * Instantiates a new statistics aware event input handler delegate.
     *
     * @param eih
     *            the eih
     * @param module
     *            the module
     * @param componentInstanceId
     *            the component instance id
     */
    public StatisticsAwareEventInputHandlerDelegate(final EventInputHandler eih, final EpsModule module, final String componentInstanceId) {
        if (eih == null) {
            throw new IllegalArgumentException("event input handler must not be null");
        }
        if (module == null) {
            throw new IllegalArgumentException("module must not be null");
        }
        if ((componentInstanceId == null) || componentInstanceId.isEmpty()) {
            throw new IllegalArgumentException("component instance id must not be null or empty");
        }
        this.eih = eih;
        initialiseStatistics(module, componentInstanceId);
    }

    @Override
    public void init(final EventHandlerContext ctx) {
        eih.init(ctx);
        log.info("Successfully initialized handler!");
    }

    @Override
    public void destroy() {
        eih.destroy();
    }

    @Override
    public void onEvent(final Object inputEvent) {
        eih.onEvent(inputEvent);
        if (eventMeter != null) {
            eventMeter.mark();
        }
    }

    private void initialiseStatistics(final EpsModule module, final String componentInstanceId) {
        final EpsStatisticsRegister statisticsRegister = EpsInstanceManager.getInstance()
                        .getEpsStatisticsRegister();
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(module.getUniqueModuleIdentifier() + "." + componentInstanceId
                                + ".eventsReceived");
            }
        }
    }
}
