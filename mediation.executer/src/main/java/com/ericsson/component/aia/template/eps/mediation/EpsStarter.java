package com.ericsson.component.aia.template.eps.mediation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.core.EpsConstants;
import static com.ericsson.component.aia.template.eps.mediation.Util.*;

public class EpsStarter {
    private static String epsFlowName;
    private static final Logger log = LoggerFactory.getLogger(EpsStarter.class);
    private static String flow;

    static {
        epsFlowName = getStringSysProp("epsId", "eps-service" + System.currentTimeMillis());
        System.setProperty(EpsConstants.EPS_INSTANCE_ID_PROP_NAME, epsFlowName);
    }

    public static void main(final String[] args) throws InterruptedException {
        log.info("STARTING EpsFlow");
        flow = getStringSysProp("flow", "csl_flow_generic_record.xml");
        final Eps epsInstance = new Eps(flow);
        epsInstance.start();
        log.info("Eps Flow started");

    }

}
