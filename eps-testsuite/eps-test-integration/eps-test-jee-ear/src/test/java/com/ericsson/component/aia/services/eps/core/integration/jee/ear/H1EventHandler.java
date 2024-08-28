/**
 *
 */
package com.ericsson.component.aia.services.eps.core.integration.jee.ear;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.*;

/**
 * H1EventHandler
 *
 * @author esarlag
 *
 */
@Named("h1")
public class H1EventHandler extends AbstractEventHandler implements EventInputHandler {

    private final String STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME = "eps_statistics_register";
    private boolean destroyed = false;
    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @EJB
    SomeStatelessSessionBean bean;

    @Inject
    SomeCDIBean cBean;

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    public void onEvent(final Object inputEvent) {
        sendToAllSubscribers(bean.testInjection(inputEvent, cBean.toString()));
        updateStatisticsWithEventsSent(inputEvent);
    }

    @Override
    protected void doInit() {
        initialiseStatistics();
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                this.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter("sampleMeterForduplicator", this);
            }
        }
    }

    private void updateStatisticsWithEventsSent(final Object inputEvent) {
        if (!(statisticsRegister == null) && (statisticsRegister.isStatisticsOn())) {
            eventMeter.mark();
        }
    }
}
