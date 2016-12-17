package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {


  public QueryPhrase(String query) {
    super(query);
  }

  /**
   * Process query string into tokens. Phrase inside "" is considered as one single token
   * token is stored in _tokens.
   * Please call this only once for each query.
   * @Jiwei
   */
  @Override
  public void processQuery() {
    if (_query == null) {
      return;
    }
    // Check is processQuery is called before.
    if (_tokens.size() != 0) {
      _tokens = new Vector<String>();
    }
    String temp = _query.toLowerCase();
    // If phrase presents in the query.
    while (temp.indexOf('\"') != -1) {
      nonPhraseProcess(temp.substring(0, temp.indexOf('\"')));
      temp = temp.substring(temp.indexOf('\"') + 1);
      int end = temp.indexOf('\"');
      if (end == -1) {
        break;
      }
      String phrase = processPhrase(temp.substring(0, end).trim());
      _tokens.add(phrase.toLowerCase());
      temp = temp.substring(end+1);
    }
    nonPhraseProcess(temp);
  }

  private String processPhrase(String s) {
    String res = "";
    String prefix = "";
    if (s.contains(" ")) {
      for (String cur : s.split(" ")) {
        res = res + prefix + Stemmer.stem(cur).toLowerCase();
        prefix = " ";
      }
    }
    if (s.contains("+")) {
      for (String cur : s.split("\\+")) {
        res = res + prefix + Stemmer.stem(cur).toLowerCase();
        prefix = " ";
      }
    }
    return res;
  }

  private void nonPhraseProcess(String s) {
    if (s == null || s.length() == 0) {
      return;
    }
    Scanner scanner = new Scanner(s);
    while (scanner.hasNext()) {
      String temp = scanner.next();
      if (s.contains("+")) {
        for (String cur : s.split("\\+")) {
          if (Stopwords.contains(temp.toLowerCase())) {
            continue;
          }
          _tokens.add(Stemmer.stem(cur).toLowerCase());
        }
      } else {
        if (Stopwords.contains(temp)) {
          continue;
        }
        _tokens.add(Stemmer.stem(temp).toLowerCase());
      }
    }
    scanner.close();
  }
}