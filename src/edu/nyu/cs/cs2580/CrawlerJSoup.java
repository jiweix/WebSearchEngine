package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Simple crawler
 * This should be used from SearchEngine
 * Created by MacBookAir on 12/12/16.
 */
public class CrawlerJSoup implements Runnable {

  private Set<String> pagesVisited = new HashSet<>();
  private LinkedList<String> pagesToVisit = new LinkedList<>();
  private RobotsServer rs = new RobotsServer();
  private int docCount = 0;
  private URL startingURL = null;

  public void setStartingURL(URL url) {
    startingURL = url;
  }


  public void run() {
    try {
      String next = startingURL.toString();
      while (next != null) {
        Document p = Jsoup.connect(next)
            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
            .referrer("http://www.google.com")
            .timeout(0).ignoreHttpErrors(true)
            .followRedirects(false)
            .ignoreContentType(true)
            .get();
        getLinks(p);
        pagesVisited.add(next);
        next = nextURL();
      }
    } catch (IOException e) {
      System.out.println("caught exception in crawler.run: " + e.getMessage());
    }
  }

  private void saveProductInfo(Document p) throws IOException {
    String site = new URL(p.baseUri()).getHost().toLowerCase();
    int startIdx = site.indexOf(".") + 1;
    int endIdx = site.indexOf(".", startIdx);
    site = site.substring(startIdx, endIdx);
    switch (site) {
      case "bestbuy":
        Element mainResults = p.getElementById("main-results");
        if (mainResults != null) {
          String prod = BestbuyFileProcessor.extractProductsFromPages(p.html());
          FileGen.generate(prod, "data/bestbuy/pages_tmp/", Integer.toString(docCount++));
        }
        break;
      case "amazon":
        Element atfResults = p.getElementById("atfResults");
        if (atfResults == null)
          System.out.println(atfResults);
        if (atfResults != null) {
          String prod = p.html();
          FileGen.generate(prod, "data/amazon/pages_tmp/", Integer.toString(docCount++));
        }
        break;
      default:
        break;
    }
  }

  private void getLinks(Document page) throws IOException {
    System.out.println("base Uri: " + page.baseUri());
    URL url = new URL(page.baseUri());
    String host = url.getHost();
    String protocol = url.getProtocol();
    Elements linkElems = page.select("a[href]");
    for (Element e : linkElems) {
      String linkStr = e.attr("href");
      URL linkURL = null;
      if (linkStr.startsWith("/")) {
        String completeURL = protocol + "://" + host + linkStr;
        completeURL = completeURL.replace("\n", "");
        linkURL = new URL(completeURL);
      } else if (linkStr.startsWith("http")) {
        linkStr = linkStr.replace("\n", "");
        linkURL = new URL(linkStr);
        // do not run links outside of current host
        if (!linkURL.getHost().equals(url.getHost())) continue;
      }
      if (linkURL != null) {
        int hashTagIdx = linkURL.toString().indexOf("#");
        if (hashTagIdx != -1) {
          String newURL = linkURL.toString().substring(0, hashTagIdx);
          linkURL = new URL(newURL);
        }
        if (rs.allow(linkURL.getHost(), linkURL.getPath()) && !pagesVisited.contains(linkURL.toString()))
          pagesToVisit.add(linkURL.toString());
      }

    }
    saveProductInfo(page);
    System.out.println("pages left: " + pagesToVisit.size() + " pages visited: " + pagesVisited.size());
  }

  private String nextURL() {
    if (this.pagesToVisit.isEmpty()) {
      System.out.println("finished");
      return null;
    }
    String next = this.pagesToVisit.remove();
    while (this.pagesVisited.contains(next)) {
      next = this.pagesToVisit.remove();
    }
    next = next.replace("\n", "");
    if (next.indexOf("\n") != -1) {
      System.out.println("hi");
    }
    return next;
  }

  /* For testing
  public static void main(String[] args) throws Exception {
    CrawlerJSoup cjs = new CrawlerJSoup();
    URL u = new URL("http://www.bestbuy.com/site/searchpage.jsp?cp=1&searchType=search&_dyncharset=UTF-8&id=pcat17071&type=page&ks=960&sc=Global&sp=&list=y&usc=All%20Categories&iht=n&seeAll=&st=computer%20monitors&qp=category_facet%3DComputer%20Accessories%20%26%20Peripherals~abcat0515000");
    URL u2 = new URL("https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Delectronics&field-keywords=computer");
    cjs.setStartingURL(u2);
    cjs.run();
  }
  */
}
