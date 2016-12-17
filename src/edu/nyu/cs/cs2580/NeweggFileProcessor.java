package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public class NeweggFileProcessor {
  private static int productIdCnt = 0;
  private static int extractedProductCnt = 0;

  private static HashMap<String, Product> productCorpus = new HashMap<>();

  public static String extractProductsFromPages(String html) {
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
    return extractProductsPages(jsoupDoc);
  }

  public static String extractProductsFromPages(File file) throws IOException {
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(file, "UTF-8", "http://example.com/");
    return extractProductsPages(jsoupDoc);
  }

  private static String extractProductsPages(Document jsoupDoc) {
    Elements nav = jsoupDoc.select("div#babreadcrumbTop");
    Elements product = jsoupDoc.select("div#bodyArea");
    return nav.outerHtml() + "\n" + product.outerHtml();
  }

  public static void extractInfoFromProducts(File productFile) throws IOException {

  }

  public static HashSet<Product> getUniqueProductSet() {
    HashSet<Product> uniqueProductSet = new HashSet<>();
    uniqueProductSet.addAll(productCorpus.values());
    return uniqueProductSet;
  }

  public static void main(String[] args) throws IOException {
  }
}
