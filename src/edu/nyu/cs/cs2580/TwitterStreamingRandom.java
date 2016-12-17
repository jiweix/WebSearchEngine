package edu.nyu.cs.cs2580;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Get random tweets stream
 * Need to setup twitterpool before call start
 * Use another twitter random streaming to solve this
 */
public class TwitterStreamingRandom {
    private static TwitterStreamingRandom ts = new TwitterStreamingRandom();
    private static TwitterPool tp = null;
    private static TwitterStream twitterStream;
    private static Thread _t;
    // Streaming Configuration
    private static final String authFile = "./auth/auth_yonghong";
    // The time interval for switch data
    private static final int TIMEOUT = 60000;
    private String _consumerKey;
    private String _consumerSecret;
    private String _accessToken;
    private String _accessTokenSecret;

    private TwitterStreamingRandom() {
        readAuth();
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(_consumerKey)
                .setOAuthConsumerSecret(_consumerSecret)
                .setOAuthAccessToken(_accessToken)
                .setOAuthAccessTokenSecret(_accessTokenSecret);
        twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
    }

    public void setTwitterPool(TwitterPool tp) {
        this.tp = tp;
    }

    public static TwitterStreamingRandom getInstance() {
        return ts;
    }
    public void start() throws IOException, InterruptedException{
        MyStatusListener listener = new MyStatusListener();
        twitterStream.addListener(listener);
        twitterStream.sample();
        Thread.sleep(TIMEOUT);
        tp.startNewBatch();
        _t = new Thread(new MyRunnableRandom());
        _t.start();
    }

    public void stop() {
        _t = null;
        twitterStream.shutdown();
    }

    private void readAuth() {
        try (BufferedReader br = new BufferedReader(new FileReader(authFile))) {
            _consumerKey = br.readLine();
            _consumerSecret = br.readLine();
            _accessToken = br.readLine();
            _accessTokenSecret = br.readLine();
        }
        catch (FileNotFoundException e1) {
        } catch (IOException e2) {
        }
    }

    private class MyRunnableRandom implements Runnable {
        public void run() {
            for(int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(TIMEOUT);
                    tp.startNewBatch();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyStatusListener implements StatusListener {
        public void onStatus(Status status) {
            String text = status.getText();
            tp.addTweet(text);
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
        }
        public void onScrubGeo(long userId, long upToStatusId) {
            //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }
        public void onException(Exception ex) {
            ex.printStackTrace();
        }
        public void onStallWarning(StallWarning arg0) {
            System.out.println("Warning: " + arg0);
        }
    }
}