package edu.nyu.cs.cs2580;

import twitter4j.FilterQuery;
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
 * Get tweets that match a list of keywords
 * The current issue is what if a hot product is not in the list?
 * Use another twitter random streaming to solve this
 * Created by Jiwei Xu on 12/12/16.
 */
public class TwitterStreaming {
  private static final TwitterStreaming ts = new TwitterStreaming();
  private static TwitterPool tp = new TwitterPool();
  private static TwitterStream twitterStream;
  private static Thread _t;
  // Streaming Configuration
  private static final String authFile = "./auth/auth_yonghong";
  // The time interval for switch data
  private static final int TIMEOUT = 30000;
  private String keywords[] = {"iphone", "laptop", "tablet", "headphone",
      "Alexa", "logitech", "lenovo", "fitbit", "kindle", "xbox", "palystation",
      "nikon", "canon", "sony", "beats", "Sennheiser", "Yamaha", "Bose", "Samsung",
      "Huawei", "dell", "philips", "Insignia", "itunes gift card", "VIZIO", "lg",
      "Fire TV Stick", "DUALSHOCK 4 Wireless Controller", "Star Wars: The Force Awakens",
      "Powerbeats2 Wireless Earbud", "M325 Wireless Optical Mouse", "Charge HR",
      "Apple Watch Sport", "PlayStation Network Card", "Jurassic World", "Guardians of the Galaxy",
      "Deadpool", "Flex Wireless", "Chromecast", "MacBook Pro", "Days of Future Past",
      "Skullcandy", "SoundLink", "Office 365", "Captain America: Civil War", "Amazon Fire",
      "Dawn of Justice", "Age of Ultron", "Steam Wallet Card", "NETGEAR", "Maleficent",
      "microsoft", "Apple TV", "Inside Out", "Seagate", "SanDisk", "Captain America",
      "The Hunger Games", "hp", "jvc", "Transformers", "Spectre", "Nintendo", "The Martian",
      "NETGEAR", "Dynex", "WD", "OtterBox", "American Sniper", "Zootopia", "ZAGG", "Call of Duty",
      "Big Hero 6", "Rocketfish", "Fujifilm"
  };
  private String _consumerKey;
  private String _consumerSecret;
  private String _accessToken;
  private String _accessTokenSecret;

  private TwitterStreaming() {
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

  public static TwitterStreaming getInstance() {
    return ts;
  }

  public void start() throws IOException, InterruptedException {
    MyStatusListener listener = new MyStatusListener();
    FilterQuery fq = new FilterQuery();
    fq.track(keywords);
    twitterStream.addListener(listener);
    twitterStream.filter(fq);
    Thread.sleep(TIMEOUT);
    tp.startNewBatch();
    _t = new Thread(new MyRunnable());
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
    } catch (FileNotFoundException e1) {
      System.out.println("auth file not found");
    } catch (IOException e2) {
    }
  }

  private class MyStatusListener implements StatusListener {
    public void onStatus(Status status) {
      if (status != null) {
        String text = status.getText();
        tp.addTweet(text);
      }
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

  private class MyRunnable implements Runnable {
    public void run() {
      for (int i = 0; i < 500; i++) {
        try {
          Thread.sleep(30 * 1000);
          tp.startNewBatch();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}