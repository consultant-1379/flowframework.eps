package com.ericsson.component.aia.services.eps.core.integration.jee.cdi;


public interface EventProcessingLocal {
    void listenForCdiEvent(JeeTestCdiEvent cdiEvent);

    int getCdiEventCount();
}
