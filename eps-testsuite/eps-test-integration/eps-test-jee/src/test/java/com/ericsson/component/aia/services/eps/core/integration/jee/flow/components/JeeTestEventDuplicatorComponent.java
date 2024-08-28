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
package com.ericsson.component.aia.services.eps.core.integration.jee.flow.components;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.oss.itpf.common.event.handler.*;

public class JeeTestEventDuplicatorComponent extends AbstractEventHandler
		implements EventInputHandler {

	private boolean destroyed = false;
	private int duplicationCount;
	private EpsStatisticsRegister statisticsRegister;
	private Meter eventMeter;

	@Override
	public void destroy() {
		destroyed = true;
	}

	@Override
	protected void doInit() {
		duplicationCount = this.getConfiguration().getIntProperty(
				"duplicationCount");
		initialiseStatistics();
	}

	@Override
	public void onEvent(final Object inputEvent) {
		if (destroyed) {
			throw new IllegalStateException(
					"Component was already destroyed - should not be invoked again");
		}
		log.debug("Received event {}", inputEvent);
		for (final EventSubscriber eih : this.getEventHandlerContext()
				.getEventSubscribers()) {
			for (int i = 0; i < duplicationCount; i++) {
				eih.sendEvent(inputEvent);
				updateStatisticsWithEventsSent(inputEvent);
			}
			log.debug("Duplicated event {} - {} times", inputEvent,
					duplicationCount);
		}
	}

    /**
     * Initialise statistics.
     */
	protected void initialiseStatistics() {
		statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                    EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
		if (statisticsRegister == null) {
			log.error("statisticsRegister should not be null");
		} else {
			if (statisticsRegister.isStatisticsOn()) {
				eventMeter = statisticsRegister.createMeter("passThroughCount", this);
			}
		}
	}

	private void updateStatisticsWithEventsSent(final Object inputEvent) {
	    if ((statisticsRegister != null) && (statisticsRegister.isStatisticsOn())) {
		eventMeter.mark();
            }
	}
}
