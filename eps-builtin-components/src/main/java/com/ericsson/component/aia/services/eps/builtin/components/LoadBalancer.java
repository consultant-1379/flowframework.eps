package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Handler that is able to load-balance events based on specified strategy.
 * This handler is applicable only where there are two or more subscribers
 * downstream.
 * <p>
 * The load balancing strategy is configured using the property:
 * {@value #STRATEGY_CONFIG_PROPERTY_NAME} <br>
 * There are two strategies supported:
 * <ul>
 * <li> {@value RANDOM_STRATEGY_CONFIG}<br>
 * the subscriber to send events to is selected randomly
 * <li> {@value ROUND_ROBIN_STRATEGY_CONFIG}<br>
 * the subscriber to send events to is selected in equal turns.
 * </ul>
 * <p>
 * If no strategy is specified then the default load balancing strategy is
 * {@value ROUND_ROBIN_STRATEGY_CONFIG}.
 *
 * @author eborziv
 *
 */
public class LoadBalancer extends AbstractEventHandler implements
        EventInputHandler {

    protected static final String STRATEGY_CONFIG_PROPERTY_NAME = "strategy";

    protected static final String RANDOM_STRATEGY_CONFIG = "random";

    protected static final String ROUND_ROBIN_STRATEGY_CONFIG = "roundRobin";

    private LoadBalancingStrategy strategy;

    @Override
    public void onEvent(final Object inputEvent) {
        final int subscriberToSendEventTo = this.strategy.getTargetSubscriber();
        this.sendEvent(inputEvent, subscriberToSendEventTo);
    }

    @Override
    protected void doInit() {
        final int numOfSubscribers = this.getNumberOfSubscribers();
        if (numOfSubscribers < 2) {
            log.warn(
                    "Load balancer does not make sense unless there are at least 2 subscribers. Currently there are {} subscribers attached",
                    numOfSubscribers);
        }
        String strategyConfig = this.getConfiguration().getStringProperty(
                STRATEGY_CONFIG_PROPERTY_NAME);
        if (EpsUtil.isEmpty(strategyConfig)) {
            log.info(
                    "Load balancing strategy not configured. Will use {} by default",
                    ROUND_ROBIN_STRATEGY_CONFIG);
            strategyConfig = ROUND_ROBIN_STRATEGY_CONFIG;
            strategy = new RoundRobinLoadBalancingStrategy(numOfSubscribers);
        } else if (ROUND_ROBIN_STRATEGY_CONFIG.equalsIgnoreCase(strategyConfig)) {
            log.info("Will use {} load-balancing strategy",
                    ROUND_ROBIN_STRATEGY_CONFIG);
            strategyConfig = ROUND_ROBIN_STRATEGY_CONFIG;
            strategy = new RoundRobinLoadBalancingStrategy(numOfSubscribers);
        } else if (RANDOM_STRATEGY_CONFIG.equalsIgnoreCase(strategyConfig)) {
            log.info("Will use {} load-balancing strategy",
                    RANDOM_STRATEGY_CONFIG);
            strategyConfig = RANDOM_STRATEGY_CONFIG;
            strategy = new RandomLoadBalancingStrategy(numOfSubscribers);
        } else {
            throw new IllegalArgumentException(
                    "Invalid load-balancing strategy " + strategyConfig);
        }
    }

    /**
     * A load balancing strategy which selects the target subscriber to send
     * events to.
     */
    interface LoadBalancingStrategy {

        /**
         *
         * @return the selected target subscriber
         */
        int getTargetSubscriber();

    }

    /**
     * A load balancing strategy which selects the target subscriber
     * pseudorandomly.
     */
    static class RandomLoadBalancingStrategy implements LoadBalancingStrategy {

        private final Random rand;
        private final int numberOfSubscribers;

        /**
         * @param numSubscribers
         *            the number of subscribers to choose from. Cannot be less
         *            than 2.
         */
        public RandomLoadBalancingStrategy(final int numSubscribers) {
            if (numSubscribers < 2) {
                throw new IllegalArgumentException(
                        "Number of subscribers must be >= 2");
            }
            this.numberOfSubscribers = numSubscribers;
            rand = new Random();
        }

        @Override
        public int getTargetSubscriber() {
            return rand.nextInt(numberOfSubscribers);
        }

    }

    /**
     * A load balancing strategy which selects the target subscribers equally in
     * turn, without priority.
     */
    static class RoundRobinLoadBalancingStrategy implements
            LoadBalancingStrategy {

        private final int numberOfSubscribers;
        private final AtomicInteger usageCounter = new AtomicInteger();

        /**
         * @param numSubscribers
         *            the number of subscribers to choose from. Cannot be less
         *            than 2.
         */
        public RoundRobinLoadBalancingStrategy(final int numSubscribers) {
            if (numSubscribers < 2) {
                throw new IllegalArgumentException(
                        "Number of subscribers must be >= 2");
            }
            this.numberOfSubscribers = numSubscribers;
        }

        @Override
        public int getTargetSubscriber() {
            if (usageCounter.get() == Integer.MAX_VALUE) {
                usageCounter.set(0);
            }
            final int val = usageCounter.getAndIncrement();
            final int sendToSubscriber = val % this.numberOfSubscribers;
            return sendToSubscriber;
        }

    }

}
