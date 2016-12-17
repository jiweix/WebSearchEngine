package edu.nyu.cs.cs2580;

import java.util.HashMap;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
  private static final long serialVersionUID = -4093365505663362578L;

  private HashMap<Integer, Integer> _documentTermFrequency = new HashMap<>();
  private short _size;

  public DocumentIndexed(int docid) {
    super(docid);
  }

  public HashMap<Integer, Integer> getTermFrequencyTable() {
    return _documentTermFrequency;
  }

  public void setTermFrequencyTable(HashMap<Integer, Integer> termFreq) {
    this._documentTermFrequency = termFreq;
  }


  public void setSize(short size) {
    this._size = size;
  }

  public int getDocSize() {
    return this._size;

  }
}