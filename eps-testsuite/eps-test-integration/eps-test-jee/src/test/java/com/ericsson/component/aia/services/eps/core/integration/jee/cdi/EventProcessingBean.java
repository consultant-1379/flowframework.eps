package com.ericsson.component.aia.services.eps.core.integration.jee.cdi;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(EventProcessingLocal.class)
public class EventProcessingBean implements EventProcessingLocal {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static int cdiEventCount = 0;

    @Override
    public void listenForCdiEvent(@Observes final JeeTestCdiEvent cdiEvent) {
        if (cdiEvent == null) {
            throw new IllegalArgumentException("Event send by handler is null.");
        }

        cdiEventCount++;
        log.debug("CDI event processed by EventProcessingBean {}", cdiEvent);
    }

    @Override
    public int getCdiEventCount() {
        return cdiEventCount;
    }

}
