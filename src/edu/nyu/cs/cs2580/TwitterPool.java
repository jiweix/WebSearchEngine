package edu.nyu.cs.cs2580;

import java.util.*;

/**
 * Call getInstance to get the TwitterPool object before calling any method
 * There is only one static TwitterPool, this is NOT a thread safe object.
 * <p>
 * Created by Jiwei Xu on 12/12/16.
 */
public class TwitterPool {
  private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
  private Map<Integer, int[]> _postingList = new HashMap<Integer, int[]>();
  private Vector<String> _terms = new Vector<>();
  // add new data to this set of data structure
  private Map<String, Integer> _dictionaryB = new HashMap<String, Integer>();
  private Map<Integer, int[]> _postingListB = new HashMap<Integer, int[]>();
  private Vector<String> _termsB = new Vector<>();
  private boolean ready = false;
  private int tweetNumber = 0;

  public TwitterPool() {

  }

  public int getProductCount(String product) {
    int res = 0;
    Vector<String> strings = tokenize(product);
    for (String s : strings) {
      res += getCount(s);
    }
    res = res / strings.size();
    System.out.print(product + ", " + res + ", ");
    return res;
  }

  public int getSimilarCount() {
    return getSimilar();
  }

  public boolean isReady() {
    return ready;
  }

  public void startNewBatch() {
    ready = false;
    this._dictionary = this._dictionaryB;
    this._postingList = this._postingListB;
    this._terms = this._termsB;
    _dictionaryB = new HashMap<String, Integer>();
    _postingListB = new HashMap<Integer, int[]>();
    _termsB = new Vector<>();
    tweetNumber = 0;
    ready = true;
  }

  public void addTweet(String tweets) {
    Scanner s = new Scanner(tweets).useDelimiter("[^\\w]+");  // remove punctuation & whitespace
    while (s.hasNext()) {
      String token = s.next();
      token = Stemmer.stem(token).toLowerCase();
      if (token.length() < 1) {
        continue;
      }
      int intToken;
      if (!_dictionaryB.containsKey(token)) {
        // new token
        intToken = _dictionaryB.size();
        _termsB.add(token);
        _dictionaryB.put(token, intToken);
        int[] posting = new int[1];
        posting[0] = tweetNumber;
        _postingListB.put(intToken, posting);
      } else {
        intToken = _dictionaryB.get(token);
        int[] oldPosting = _postingListB.get(intToken);
        int[] newPosting = new int[oldPosting.length + 1];
        for (int i = 0; i < oldPosting.length; i++) {
          newPosting[i] = oldPosting[i];
        }
        newPosting[oldPosting.length] = tweetNumber;
        _postingListB.put(intToken, newPosting);
      }
    }
    s.close();
    tweetNumber++;
  }

  private int getCount(String p) {
    if (!ready) {
      return -1;
    }
    Scanner s = new Scanner(p).useDelimiter("[^\\w]+");
    Set<Integer> res = null;
    while (s.hasNext()) {
      String token = Stemmer.stem(s.next()).toLowerCase();
      if (token.length() >= 1) {
        Integer key = _terms.indexOf(token);
        if (key == -1) {
        } else {
          int[] posting = _postingList.get(key);
          Set<Integer> tempSet = new HashSet<Integer>();
          for (int i : posting) {
            tempSet.add(i);
          }
          if (res == null) {
            res = tempSet;
          } else {
            res.retainAll(tempSet);
          }
        }
      }
    }
    s.close();
    if (res == null) {
      return 0;
    }
    return res.size();
  }

  private int getSimilar() {
    return 0;
  }

  private static Vector<String> tokenize(String line) {
    Vector<String> res = new Vector<String>();
    String a = line;
    if (a.startsWith("New!")) {
      a = a.substring(5);
    }
    int index1 = a.indexOf(" - ");
    int index2 = a.lastIndexOf(" - ");
    if (index1 == -1) {
      res.add(process(a));
      return res;
    }
    if (index1 == index2) {
      res.add(a.substring(0, index1));
      res.add(a.substring(index1 + 3));
      return res;
    }
    String brand = a.substring(0, index1);
    res.add(brand);
    a = a.substring(index1 + 3);
    index2 = a.indexOf(" - ");
    a = a.substring(0, index2);
    res.add(process(a));
    return res;
  }

  private static String process(String s) {
    String a = s.toLowerCase();
    int subEnd;
    if (a.contains("[")) {
      subEnd = Math.max(a.indexOf("[") - 1, 0);
      a = a.substring(0, subEnd);
    }
    if (a.contains("(")) {
      subEnd = Math.max(a.indexOf("(") - 1, 0);
      a = a.substring(0, subEnd);
    }
    if (a.toLowerCase().contains("for")) {
      subEnd = Math.max(a.toLowerCase().indexOf("for") - 1, 0);
      a = a.substring(0, subEnd);
    }
    if (a.contains(":")) {
      subEnd = Math.max(a.indexOf(":") - 1, 0);
      a = a.substring(0, subEnd);
    }
    if (a.toLowerCase().contains("with")) {
      subEnd = Math.max(a.toLowerCase().indexOf("with") - 1, 0);
      a = a.substring(0, subEnd);
    }
    return a;
  }
}