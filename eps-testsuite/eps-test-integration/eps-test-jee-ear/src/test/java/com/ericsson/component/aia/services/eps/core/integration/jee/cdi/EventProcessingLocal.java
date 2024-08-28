package com.ericsson.component.aia.services.eps.core.integration.jee.cdi;

import javax.ejb.Local;


@Local
public interface EventProcessingLocal {
    void listenForCdiEvent(JeeTestCdiEvent cdiEvent);

    int getCdiEventCount();
}
