package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public class BestbuyFileProcessor {
    private static int productIdCnt = 0;
    private static int extractedProductCnt = 0;
    private static String corpusPath="./data/bestbuy/pages";

    private static HashMap<String, Product> productCorpus = new HashMap<>();

    private BestbuyFileProcessor() {
    }

    public static String extractProductsFromPages(String html) {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        return extractProductsPages(jsoupDoc);
    }

    public static String extractProductsFromPages(File file) throws IOException {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(file, "UTF-8", "http://example.com/");
        return extractProductsPages(jsoupDoc);
    }

    private static String extractProductsPages(Document jsoupDoc) {
        Elements products = jsoupDoc.select("div.list-item");
        StringBuffer results = new StringBuffer();

        String[] tags = {"div.sku-title", "div.sku-model", "div.short-description", "div.customer-rating",
                "div.pucks-and-price"};
        for (Element product : products) {
            Elements temp;
            results.append("<div class=\"list-item\">\n");
            // add time stamp to each product
            results.append("<div class=\"retrieved-time\">" + System.currentTimeMillis() + "</div>\n");
            for (String tag : tags) {
                temp = product.select(tag);
                results.append(temp.outerHtml() + "\n");
            }
            results.append("</div>\n");
            results.append("\n\n");
        }

        return results.toString();
    }

    public static void extractInfoFromProducts(File productFile) throws IOException {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(productFile, "UTF-8", "http://example.com/");

        String sourceUrl = jsoupDoc.select("div.url").text();
        HashSet<String> categoryList = extractCategoryFromUrl(productFile.getName(), sourceUrl);

        Elements products = jsoupDoc.select("div.list-item");
        extractedProductCnt += products.size();

        for (Element product : products) {
            String name = product.select("div.sku-title").text();
            String url = "http://www.bestbuy.com" + product.select("div.sku-title").select("a[href]").attr("href");
            String seller = "bestbuy";
            String model = product.select("div.sku-model").select("span[itemprop='model']").text();
            String description = product.select("div.short-description").html();
            String rate = product.select("div.customer-rating").select("span.average-score").text();
            String reviewCount = product.select("div.customer-rating")
                    .select("meta[itemprop='reviewCount']").attr("content");
            String price = product.select("div.pucks-and-price").select("div.medium-item-price").text();
            String time = product.select("div.retrieved-time").text();

            float custRate = 0.0f;
            int custReviewCnt = 0;
            long rprice = -1;
            if (!rate.isEmpty()) {
                custRate = Float.parseFloat(rate);
            }
            if (!reviewCount.isEmpty()) {
                custReviewCnt = Integer.parseInt(reviewCount);
            }
            if (!price.isEmpty()) {
                try {
                    rprice = (long) (NumberFormat.getCurrencyInstance().parse(price).floatValue()*100);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }

            long retrievedTime = Long.parseLong(time);

            Product temp = new Product.MyBuilder(productIdCnt++, name, url, seller)
                    .addSourceUrl(sourceUrl)
                    .addDescription(description)
                    .addCategory(categoryList)
                    .addModel(model)
                    .addRate(custRate)
                    .addReviewCount(custReviewCnt)
                    .addPrice(rprice)
                    .addRetrievedTime(retrievedTime)
                    .build();

            String skuid = product.select("span[itemprop='productID'").text();
            // check duplicated and record in files
            if (productCorpus.containsKey(skuid)) {
                // update the corpus with updated products
                if (temp.newerThan(productCorpus.get(skuid))) {
//                    FileGen.appendDataToFile(productCorpus.get(skuid).toString() + "--updated--" + temp.toString() + "\n",
//                            "data/", "duplicatedProducts");
                    temp.getCategory().addAll(productCorpus.get(skuid).getCategory());
                    temp.getSourceUrl().addAll(productCorpus.get(skuid).getSourceUrl());
                    productCorpus.put(skuid, temp);
                } else {
                    productCorpus.get(skuid).getCategory().addAll(temp.getCategory());
                    productCorpus.get(skuid).getSourceUrl().addAll(temp.getSourceUrl());
                }

            } else {
                productCorpus.put(skuid, temp);
            }
        }
    }

    public static HashSet<String> extractCategoryFromUrl(String filename, String url) {
        url = url.toLowerCase();
        String category = "";
        String domain = "http://www.bestbuy.com/site/";

        if (url.startsWith(domain)) {
            int start = url.indexOf(domain) + domain.length();
            category = url.substring(start);
            domain = "promo/";
            // promo category, ignore it for now
            if (category.startsWith(domain)) {
                category = "";
            } else {
                int end = category.lastIndexOf('?');
                category = category.substring(0, end);
                if (category.endsWith(".jsp")) {
                    // skip jsp file
                    category = "";
                } else {
                    category = category.substring(0, category.lastIndexOf('/'));
//                    String[] temp = category.split("/");
//                    String results = "";
//                    for (int i = 0; i < temp.length - 1; i++) {
//                        results += String.format("%-35s ", temp[i]);
//                    }
//                    FileGen.appendDataToFile(String.format("%-8s ", filename) + results + "\n",
//                            "data", "usual-category");
                }
            }
        }
        HashSet<String> categoryList = new HashSet<>();
        String[] categories = category.split("/");
        categoryList.addAll(Arrays.asList(categories));

        return categoryList;
    }

    public static HashSet<Product> getUniqueProductSet() {
        HashSet<Product> uniqueProductSet = new HashSet<>();
        uniqueProductSet.addAll(productCorpus.values());
        return uniqueProductSet;
    }

    public static void constructProductCorpus() throws IOException {
        File folder = new File(corpusPath);
        File[] productFiles = folder.listFiles();
        int cnt = 0;
        for (File file : productFiles) {
            System.out.println("Processing No." + cnt++ + " File: " + file.getName());
            extractInfoFromProducts(file);
        }
    }
    public static void main(String[] args) throws IOException{
        File folder = new File("data/pages");
        File[] pages = folder.listFiles();
      for (int i = 0; i < pages.length; i++) {
            File f = pages[i];
            try {
                System.out.println("Extracting the No." + i + " file: " + f.getName());
                extractInfoFromProducts(f);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        // generate product list
        StringBuffer results = new StringBuffer();
        for (String key : productCorpus.keySet()) {
            results.append(productCorpus.get(key).toString() + "\n");
        }
        FileGen.generate(results.toString(), "data", "product-list");

        System.out.println("In total we retrieved " + extractedProductCnt + " products");
        System.out.println("And we have " + productCorpus.size() + " unique products");
    }
}
