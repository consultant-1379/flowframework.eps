package com.ericsson.component.aia.services.eps.core.integration.jee.bundle;

import java.io.IOException;

import javax.servlet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.EpsInstanceManager;

public class EpsBundleServlet implements Servlet {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private EpsInstanceManager lifeCycleHandler;

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        log.info("Starting EPS instance");
        lifeCycleHandler = EpsInstanceManager.getInstance();
        lifeCycleHandler.start();
        log.info("Successfully started EPS instance");
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {

    }

    @Override
    public void destroy() {
        log.info("Stopping EPS instance");
        lifeCycleHandler.stop();
        log.info("Successfully stopped EPS instance");
    }

    @Override
    public String getServletInfo() {
        return "EPS Bundle Servlet";
    }

}
