package edu.nyu.cs.cs2580;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Vector;

/**
 * Product object, used to save all product information
 * Created by Jiwei Xu on 12/8/16.
 */
public class Product implements Serializable {
  private static final long serialVersionUID = -722968119335839882L;

  private final int _id;
  private final String _name;
  private final String _url;
  private final String _seller;
  private Vector<String> _sourceUrl;
  private final String _model;
  private final String _description;
  private HashSet<String> _category;
  private final float _rate;
  private final int _reviewCount;
  private long _price;
  private final long _retrievedTime;

  protected Product(MyBuilder builder) {
    this._id = builder._id;
    this._name = builder._name;
    this._url = builder._url;
    this._seller = builder._seller;
    this._sourceUrl = builder._sourceUrl;
    this._model = builder._model;
    this._description = builder._description;
    this._category = builder._category;
    this._rate = builder._rate;
    this._reviewCount = builder._reviewCount;
    this._price = builder._price;
    this._retrievedTime = builder._retrievedTime;
  }

  public int getId() {
    return this._id;
  }

  public String getName() {
    return this._name;
  }

  public float getRate() {
    return this._rate;
  }

  public int getReviewCount() {
    return this._reviewCount;
  }

  public long getPrice() {
    return this._price;
  }

  public String getDescription() {
    return this._description;
  }

  public HashSet<String> getCategory() {
    return this._category;
  }

  public void setCategory(HashSet<String> categoryList) {
    if (this._category == null) {
      this._category = new HashSet<>();
    }
    this._category.clear();
    this._category.addAll(categoryList);
  }

  public Vector<String> getSourceUrl() {
    return _sourceUrl;
  }

  public void setSourceUrl(Vector<String> sourceUrlList) {
    if (this._sourceUrl == null) {
      this._sourceUrl = new Vector<String>();
    }
    this._sourceUrl.clear();
    this._sourceUrl.addAll(sourceUrlList);
  }

  public boolean newerThan(Product obj) {
    return _retrievedTime > obj._retrievedTime;
  }

  public String toString() {
    return new StringBuffer().append("id: " + _id + "\n")
        .append("retrieved time: " + _retrievedTime + "\n")
        .append("name: " + _name + "\n")
        .append("seller: " + _seller + "\n")
        .append("url: " + _url + "\n")
        .append("model: " + _model + "\n")
        .append("category: " + _category + "\n")
        .append("rate: " + _rate + "\n")
        .append("reviewCount: " + _reviewCount + "\n")
        .append("price: $" + _price / 100.0 + "\n")
        .append("description: " + _description + "\n").toString();
  }

  public String asHtmlText() {
    return new StringBuffer()
        .append("<ul>name: <a href=\'" + _url + "\'>" + _name + "</a></ul>\n")
        .append("<ul>seller: " + _seller + "</ul>\n")
        .append("<ul>category: " + _category + "</ul>\n")
        .append("<ul>rate: " + _rate + "</ul>\n")
        .append("<ul>reviewCount: " + _reviewCount + "</ul>\n")
        .append("<ul>price: $" + _price / 100.0 + "</ul>\n")
        .append("<ul>description: " + _description + "</ul>\n<br>").toString();
  }

  public static class MyBuilder {
    private final int _id;
    private final String _name;
    private final String _url;
    private final String _seller;
    private String _model;
    private String _description = "";
    private HashSet<String> _category = new HashSet<>();
    private Vector<String> _sourceUrl = new Vector<>();
    private float _rate = 0.0f;
    private int _reviewCount = 0;
    private long _price = -1;
    private long _retrievedTime = -1;

    public MyBuilder(int _id, String _name, String _url, String _seller) {
      if (_name == null || _url == null || _seller == null) {
        throw new IllegalArgumentException("Required field can't be null");
      }
      if (_name.isEmpty() || _url.isEmpty() || _seller.isEmpty()) {
        throw new IllegalArgumentException("Required field can't be empty");
      }
      this._id = _id;
      this._name = _name;
      this._url = _url;
      this._seller = _seller;
    }

    public MyBuilder addSourceUrl(String sourceUrl) {
      this._sourceUrl.add(sourceUrl);
      return this;
    }

    public MyBuilder addModel(String model) {
      this._model = model;
      return this;
    }

    public MyBuilder addDescription(String des) {
      this._description = des;
      return this;
    }

    public MyBuilder addCategory(HashSet<String> category) {
      this._category.addAll(category);
      return this;
    }

    public MyBuilder addRate(float _rate) {
      this._rate = _rate;
      return this;
    }

    public MyBuilder addReviewCount(int count) {
      this._reviewCount = count;
      return this;
    }

    public MyBuilder addPrice(long price) {
      this._price = price;
      return this;
    }

    public MyBuilder addRetrievedTime(long retrievedTime) {
      this._retrievedTime = retrievedTime;
      return this;
    }

    public Product build() {
      return new Product(this);
    }
  }
}
