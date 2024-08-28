package com.ericsson.eps.demo.tweeter;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.github.lalyos.jfiglet.FigletFont;
import com.hazelcast.core.*;

public class TwitterMock {

    private static final String[] USERNAMES = { "George", "John", "Thomas", "James", "Andrew", "Martin", "William", "Zachary", "Millard", "Franklin",
        "Abraham", "Ulysses", "Chester", "Grover", "Benjamin" };

    private static final int TWEET_NUMBER = 10000;

    private static final int SLEEP_BETWEEN_TWEETS_MILLIS = 500;

    private static final Random RAND = new Random();

    public static void main(final String[] args) throws Exception {
        subscribeForTweetStats();
        sendTweets();
        getHazelcastInstance().getLifecycleService().shutdown();
    }


    private static void sendTweets() throws Exception {
        for (int i = 0; i < TWEET_NUMBER; i++) {
            final Tweet tw = new Tweet();
            tw.setId(i);
            tw.setRetweetCount(RAND.nextInt(1000));
            tw.setUsername(USERNAMES[RAND.nextInt(USERNAMES.length)]);
            tw.setText("Some tweet text sent by " + tw.getUsername() + " at " + new Date());
            sendTweet(tw);
            if (SLEEP_BETWEEN_TWEETS_MILLIS > 0) {
                TimeUnit.MILLISECONDS.sleep(SLEEP_BETWEEN_TWEETS_MILLIS);
            }
        }
        System.out.println("\n\nIn total sent " + TWEET_NUMBER + " tweets");
    }

    private static void subscribeForTweetStats() {
        getHazelcastInstance().getTopic(TWEET_STATS_TOPIC_NAME).addMessageListener(new MessageListener<Object>() {

            public void onMessage(final Message<Object> message) {
                analyzeAndPrintMessage(message.getMessageObject());
            }
        });
    }

    private static void sendTweet(final Tweet tw) {
        getHazelcastInstance().getTopic(TWEETS_TOPIC_NAME).publish(tw);
    }

    private static void analyzeAndPrintMessage(final Object message) {
        if (message instanceof Map) {
            final Map msg = (Map) message;
            final String type = (String) msg.get("event");
            if (type.equalsIgnoreCase("UP")) {
                final String val = msg.get("username") + "  is  trending  up";
                printTrending(val);
            } else if (type.equalsIgnoreCase("DOWN")) {
                final String val = msg.get("username") + "  is  trending  down!";
                printTrending(val);
            } else if (type.equalsIgnoreCase("cool")) {
                final String val = msg.get("username") + "  is  popular";
                printAscii(val);
            } else {
                System.out.println(message);
            }
        }
    }

    private static void printTrending(final String line) {
        final String border = "\n\n**********************************************************\n\n";
        final String lineToPrint = border + line + border;
        System.out.println(lineToPrint);

    }

    private static void printAscii(final String line) {
        try {
            final String asciiArt = FigletFont.convertOneLine(line);
            System.out.println(asciiArt);
        } catch (final Exception exc) {
            exc.printStackTrace();
        }
    }

    private static final String TWEETS_TOPIC_NAME = "tweets";
    private static final String TWEET_STATS_TOPIC_NAME = "tweet_statistics";

    private static synchronized HazelcastInstance getHazelcastInstance() {
        if (HZ_INSTANCE == null) {
            HZ_INSTANCE = Hazelcast.newHazelcastInstance();
        }
        return HZ_INSTANCE;
    }

    private static HazelcastInstance HZ_INSTANCE;

}
