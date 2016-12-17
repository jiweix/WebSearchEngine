package edu.nyu.cs.cs2580;

/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
class ScoredDocument implements Comparable<ScoredDocument> {
  private Document _doc;
  private double _score;
  private double _cosineScore = -1;
  private double _twitterScore = -1;
  private double _rateScore = -1;
  private double _categoryDistScore = -1;

  public ScoredDocument(Document doc, double score) {
    _doc = doc;
    _score = score;
  }

  public String asTextResult() {
    return _doc.getProduct().toString() + "\n";
  }

  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */
  public String asHtmlResult() {
    StringBuffer results = new StringBuffer();
    results.append(String.format("consineScore: %-10.2f\t", _cosineScore))
            .append(String.format("_twitterScore: %-10.2f\t", _twitterScore))
            .append(String.format("_rateScore: %-10.2f\t", _rateScore))
            .append(String.format("_categoryDistScore: %-10.2f\n", _categoryDistScore))
            .append(String.format("Total score: %-10.2f", _score));
    return results.toString() + "\n" + _doc.getProduct().asHtmlText();
  }

  public ScoredDocument addSubScores(double _cosineScore, double _twitterScore,
                                     double _rateScore, double _categoryDistScore) {
    this._cosineScore = _cosineScore;
    this._twitterScore = _twitterScore;
    this._rateScore = _rateScore;
    this._categoryDistScore = _categoryDistScore;
    return this;
  }

  @Override
  public int compareTo(ScoredDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }
}
