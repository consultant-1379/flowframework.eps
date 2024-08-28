package com.ericsson.eps.demo.tweeter.handlers;

import com.ericsson.eps.demo.tweeter.Tweet;
import com.ericsson.oss.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;

public class FilterHandler extends AbstractEventHandler implements EventInputHandler {

    private int retweetThreshold;

    @Override
    protected void doInit() {
        retweetThreshold = this.getConfiguration().getIntProperty("threshold");
        if (retweetThreshold < 0) {
            throw new IllegalStateException("Threshold must not be negative integer!");
        }
        log.info("Will filter out all tweets with number of retweets < {}", retweetThreshold);
    }

    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof Tweet) {
            final Tweet tw = (Tweet) inputEvent;
            if (tw.getRetweetCount() >= this.retweetThreshold) {
                this.sendToAllSubscribers(inputEvent);
            }
        }
    }


}

