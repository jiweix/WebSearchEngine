package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Jiwei Xu on 12/15/16.
 */
public class AmazonFileProcessor {
    private static String path = "./data/amazon/pages";
    private static List<Product> productList = new ArrayList<Product>();
    private static HashSet<String> productNames = new HashSet<String>();
    private static int productIdCnt = 0;


    public static void setProductId(int id) {
        productIdCnt = id;
    }

    private static float getRating(String html) {
        int index = html.lastIndexOf(" out of 5 stars");
        if (index == -1) {
            return 0.0f;
        }
        String temp = html.substring(0, index);
        int start = temp.lastIndexOf(">");
        String value = temp.substring(start+1);
        //System.out.println(value);
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    private static int getReviewCount(String html) {
        int index = html.lastIndexOf("</a>");
        if (index == -1) {
            return 0;
        }
        String temp = html.substring(0, index);
        int start = temp.lastIndexOf(">");
        String value = temp.substring(start+1);
        //System.out.println(value);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static HashSet<String> extractCategory(String html) {
        int index = html.lastIndexOf("metaKeywords\":\"");
        HashSet<String> res = new HashSet<String>();
        if (index == -1) {
            return res;
        }
        String temp = html.substring(index+15);
        index = temp.indexOf(", Amazon.com\",\"");
        if (index == -1) {
            return res;
        }
        String value = temp.substring(0, index);
        String[] cats = value.split(",");
        for (String s: cats) {
            res.add(processCat(s.trim().toLowerCase()));
        }
        return res;
    }

    public static String processCat(String s) {
        if(s.contains(" ")) {
            String[] tokens = s.split(" ");
            try {
                Integer.parseInt(tokens[tokens.length-1]);
                String padding = "";
                String res = "";
                for (int i = 0; i < tokens.length-1; i++) {
                    res += padding + tokens[i];
                    padding = " ";
                }
                return res;
            } catch (NumberFormatException e){
                return s;
            }
        } else {
            String[] tokens = s.split("\\-");
            try {
                Integer.parseInt(tokens[tokens.length-1]);
                String padding = "";
                String res = "";
                for (int i = 0; i < tokens.length-1; i++) {
                    res += padding + tokens[i];
                    padding = "-";
                }
                return res;
            } catch (NumberFormatException e){
                return s;
            }
        }
    }

    public static void parsePage(File file) throws IOException {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(file, "UTF-8", "http://example.com/");
        HashSet<String> categories = extractCategory(jsoupDoc.html());
        for (int i = 0; i < 25; i++) {
            Elements p = jsoupDoc.select("li#result_"+i);
            if (p == null) {
                return;
            }
            Element product;
            String name;
            String url;
            String priceWhole;
            String priceFractional;
            try {
                product = jsoupDoc.select("li#result_" + i).first();
                //String id = product.attr("data-asin");
                name = product.select("a.s-access-detail-page").text();
                url = product.select("a.s-access-detail-page").attr("abs:href");
                priceWhole = product.select("span.sx-price-whole").text();
                priceFractional = product.select("sup.sx-price-fractional").text();
            } catch (NullPointerException e) {
                continue;
            }
            if (productNames.contains(name)) {
                continue;
            } else {
                productNames.add(name);
            }
            long price;
            try {
                price = Integer.parseInt(priceWhole) * 100 + Integer.parseInt(priceFractional);
            } catch (NumberFormatException e) {
                price = -1;
            }
            String allhtml = product.html();
            float rating = getRating(allhtml);
            int reviewCount = getReviewCount(allhtml);
            //System.out.println(name + ": " + rating);

            Product temp = new Product.MyBuilder(productIdCnt++, name, url, "amazon")
                    .addRate(rating)
                    .addReviewCount(reviewCount)
                    .addPrice(price)
                    .build();
            temp.setCategory(categories);
            productList.add(temp);
        }
    }

    public static List<Product> getProductList() throws IOException {
        File folder = new File(path);
        File[] pages = folder.listFiles();
        File a = new File(path + "/1");
        //parsePage(a);
        for (int i = 0; i < pages.length; i++) {
            System.out.println("Processing No." + i + " File from Amazon");
            parsePage(pages[i]);
        }
        System.out.println(productList.size());
        return productList;
    }
}