package com.ericsson.component.aia.services.eps.core.integration.jee.ear;

import javax.ejb.Stateless;

@Stateless
public class SomeStatelessSessionBean {

    public String testInjection(final Object inputEvent, final String appName) {
        return inputEvent + appName;

    }

}
