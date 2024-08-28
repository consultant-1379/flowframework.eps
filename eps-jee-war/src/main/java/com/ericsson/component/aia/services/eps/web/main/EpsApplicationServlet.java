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
package com.ericsson.component.aia.services.eps.web.main;

import java.io.IOException;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;
import com.ericsson.component.aia.services.eps.pe.java.util.EpsCdiInstanceManager;

/**
 * The Class EpsApplicationServlet.
 */
public class EpsApplicationServlet implements Servlet {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private EpsInstanceManager epsInstance;
    private EpsCdiInstanceManager cdiInstanceManager;

    @Inject
    private BeanManager beanManager;

    @Override
    public void destroy() {
        log.info("Stopping EPS instance");
        epsInstance.stop();
        cdiInstanceManager.stop();
        log.info("Successfully stopped EPS instance");
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public String getServletInfo() {
        return "EPS Application Servlet";
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        log.info("Starting EPS instance");
        epsInstance = EpsInstanceManager.getInstance();
        epsInstance.start();
        log.info("Successfully started EPS instance");

        log.info("Starting EpsCdiInstanceManager...");
        cdiInstanceManager = EpsCdiInstanceManager.getInstance();
        log.info("Initializing JEE built-in CDI environment... start");
        cdiInstanceManager.setBeanManager(beanManager);
        log.info("Initializing JEE built-in CDI environment... done");
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
    }

}
