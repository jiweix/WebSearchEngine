package edu.nyu.cs.cs2580;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by MacBookAir on 12/7/16.
 */
public class RobotsServer {
  // a map of hostname to set of robot rules
  private HashMap<String, Set<String>> rules = new HashMap<>();
  private final String PATTERNS_DISALLOW = "(?i)Disallow:.*";
  private final String PATTERNS_USERAGENT = "(?i)^User-agent:.*";
  private final int PATTERNS_DISALLOW_LENGTH = 9;
  private final int PATTERNS_USERAGENT_LENGTH = 11;
  private String userAgent = "WSECrawler";

  public boolean allow(String host, String path) throws IOException {
    try {
      if (!rules.containsKey(host)) {
        parseRules(host);
      }
      Set<String> siteRules = rules.get(host);
      for (String r : siteRules) {
        if (r.startsWith("*") || r.startsWith("/*")) {
          if (path.indexOf(r) != -1) return false;
        } else {
          if (path.startsWith(r)) {
            return false;
          }
        }
      }
      return true;
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  private void parseRules(String host) throws IOException {
    URL robotURL = new URL("http://" + host.concat("/robots.txt"));
    HttpURLConnection uc = (HttpURLConnection) robotURL.openConnection();
    // if it's https
    boolean redirect = false;
    int status = uc.getResponseCode();
    if (status != HttpURLConnection.HTTP_OK) {
      if (status == HttpURLConnection.HTTP_MOVED_TEMP
          || status == HttpURLConnection.HTTP_MOVED_PERM
          || status == HttpURLConnection.HTTP_SEE_OTHER)
        redirect = true;
    }
    if (redirect) {
      robotURL = new URL(uc.getHeaderField("Location"));
      uc = (HttpsURLConnection) robotURL.openConnection();
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
    String line;
    Set<String> siteRules = new HashSet<>();

    boolean startRecording = false;
    while ((line = reader.readLine()) != null) {
      if (line.matches(PATTERNS_USERAGENT)) {
        String ua = line.substring(PATTERNS_USERAGENT_LENGTH).trim().toLowerCase();
        startRecording = "*".equals(ua) || ua.contains(userAgent.toLowerCase());
      } else if (line.matches(PATTERNS_DISALLOW)) {
        if (!startRecording) {
          continue;
        } else {
          siteRules.add(line.substring(PATTERNS_DISALLOW_LENGTH).trim());
        }
      }
    }
    rules.put(host, siteRules);
  }

  private void setUserAgent(String agent) {
    userAgent = agent;
  }

  private String getUserAgent() {
    return userAgent;
  }

  public static void main(String[] args) {
    try {
      RobotsServer rs = new RobotsServer();

      URL u = new URL("http://www.bestbuy.com/site/lg-40-class-39-5-diag--led-1080p-hdtv-black/5613404.p?skuId=5613404");
      URL u2 = new URL("http://www.amazon.com/gp/product/B014M8ZO8S/ref=s9_acss_bw_cg_EHD0601B_1b1?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-2&pf_rd_r=JDDGVA1CM5JXWRW7H99B&pf_rd_t=101&pf_rd_p=3598c767-f603-4b07-8a4e-a402bc5e32be&pf_rd_i=15450561011");
      System.out.println(u2.getPath());
      System.out.println(rs.allow(u2.getHost(), u2.getPath()));
    } catch (Exception e) {
      System.out.println("exception in RobotsServer: " + e.getMessage());
    }
  }
}
