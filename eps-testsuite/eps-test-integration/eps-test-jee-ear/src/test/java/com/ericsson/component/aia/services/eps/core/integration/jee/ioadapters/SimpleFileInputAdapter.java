/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jee.ioadapters;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.eps.adapter.InputAdapter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;

/**
 * This class implements EPS {@link InputAdapter}, and will read a file and send its content downstream as an event. There is a configurable
 *
 * The Configurable inputs are defined in the flow.xml file.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class SimpleFileInputAdapter extends AbstractEventHandler implements InputAdapter {

    static final String URI = "fileData:/";
    private static final String ADAPTER_FILE = "files/adapterTestFile.data";
    private final String STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME = "eps_statistics_register";

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final int intervalValue = 1000;
    private boolean destroyed = false;

    private boolean stop = false;

    private EpsStatisticsRegister statisticsRegister;
    private Meter eventMeter;

    @Override
    public boolean understandsURI(final String uri) {
        if (uri == null) {
            return false;
        }
        return uri.equals(URI);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException("Operation not supported. File input adapter is always entry points on event chain!");
    }

    void sendEvent(final Object event) {
        sendToAllSubscribers(event);
        updateStatisticsWithEventsSent(event);
        LOG.debug("\n*\n*\n* event sent to all subscribers. Event : {}\n*\n*", event);
    }

    @Override
    protected void doInit() {
        initialiseStatistics();
        final Thread runnable = new Thread(new Runnable() {

            @Override
            public void run() {

                LOG.info("\n*\n*\n* SimpleFileInputAdapter Start...\n*\n*");

                int count = 0;
                while (!destroyed && !stop && count < 100) {
                    try {
                        Thread.sleep(intervalValue);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    readFile();
                    count++;
                }

                LOG.info("\n*\n*\n* SimpleFileInputAdapter Exit...\n*\n*");
            }
        });

        runnable.start();
    }

    /**
     * This method creates and runs a scheduled task based on the input from the flow.xml
     */
    private void readFile() {

        LOG.debug("\n*\n*\n* Reading IO Adapter file " + ADAPTER_FILE + "\n*\n*");

        InputStream flowIs = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            flowIs = Thread.currentThread().getContextClassLoader().getResourceAsStream(ADAPTER_FILE);

            if (flowIs == null) {
                LOG.error("Unable to get resource {}", ADAPTER_FILE);
                return;
            }
            inputStreamReader = new InputStreamReader(flowIs);
            bufferedReader = new BufferedReader(inputStreamReader);
            final String readLine = bufferedReader.readLine();
            LOG.trace("Read following line for test: " + readLine);
            sendEvent(readLine);
            stop = true;
        } catch (final NullPointerException e) {
            LOG.error("Unable to find resource {}", ADAPTER_FILE, e);
        } catch (final IOException e) {
            LOG.error("Unable to open adapter file", e);
        } finally {
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (flowIs != null) {
                    flowIs.close();
                }
            } catch (final IOException e) {
                LOG.error("Unable to close input stream", e);
                e.printStackTrace();
            }
        }

        LOG.debug("\n*\n*\n* File collection was scheduled for every {} millisecond(s)\n*\n*", intervalValue);
    }

    @Override
    public void destroy() {
        LOG.info("\n*\n*\n* SimpleFileInputAdapter destroy...\n*\n*");
        destroyed = true;
        super.destroy();
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
