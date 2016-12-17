package edu.nyu.cs.cs2580;

import java.io.*;
import java.util.*;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * Indexer for products
 * Only products name and description is used in building the posting list
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable {
  private static final long serialVersionUID = 1077111905740085033L;
  // Maps each term to their integer representation
  private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
  // Term frequency, key is the integer representation of the term and value is
  // the number of times the term appears in the corpus.
  private Map<Integer, Integer> _termCorpusFrequency = new HashMap<Integer, Integer>();
  // Stores all Document in memory.
  private List<DocumentIndexed> _documents = new ArrayList<>();
  // this will be used at run at.
  private Map<Integer, byte[]> _postingLists1 = new HashMap<>();
  private Map<Integer, int[]> _tempPostingList = new HashMap<>();
  // indicate the presaved number of posting lists in terms of the number of query terms
  private int _tempPostingListSize = 1;
  // For fast retrieval of recent query
  private static final int maxPresavedSize = 20;

  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
    System.out.println("Constructing bestbuy product documents");
    BestbuyFileProcessor.constructProductCorpus();
    HashSet<Product> uniqueProductSet = BestbuyFileProcessor.getUniqueProductSet();
    for (Product product : uniqueProductSet) {
      String parsedText = product.getName() + " " + product.getDescription();
      DocumentIndexed doc = new DocumentIndexed(_documents.size());
      doc.setProduct(product);
      _documents.add(doc);
      processDocument(doc._docid, parsedText);
      ++_numDocs;
    }
    System.out.println("Constructing amazon product documents");
    AmazonFileProcessor.setProductId(_numDocs);
    List<Product> amazonProduct = AmazonFileProcessor.getProductList();
    for (Product product : amazonProduct) {
      String parsedText = product.getName();
      DocumentIndexed doc = new DocumentIndexed(_documents.size());
      doc.setProduct(product);
      _documents.add(doc);
      processDocument(doc._docid, parsedText);
      ++_numDocs;
    }
    System.out.println("Indexed " + Integer.toString(_numDocs) + " docs with " + Long.toString(_totalTermFrequency) + " terms.");
    String indexFile = _options._indexPrefix + "/IndexerInvertedCompressed.idx";
    System.out.println("Store index to: " + indexFile);
    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
    writer.writeObject(this);
    writer.reset();
    writer.close();
  }

  private void processDocument(int docid, String content) {
    // get the product name tokens
    String productName = _documents.get(docid).getProduct().getName().toLowerCase();
    Scanner ps = new Scanner(productName).useDelimiter("[^\\w]+"); // remove punctuation & whitespace
    Set<String> nameTokens = new HashSet<>();
    while (ps.hasNext()) {
      String token = ps.next();
      token = Stemmer.stem(token);
      if (token.length() < 1) {
        continue;
      }
      nameTokens.add(token);
    }
    ps.close();
    // parse content, remove punctuation & whitespace
    Scanner s = new Scanner(content).useDelimiter("[^\\w]+");
    HashMap<Integer, Integer> termFreqTable = new HashMap<>();
    Map<Integer, List<Integer>> localList = new HashMap<>();
    int pos = 0;
    while (s.hasNext()) {
      String token = s.next();
      token = Stemmer.stem(token);
      if (token.length() < 1) {
        continue;
      }
      ArrayList<Integer> docOccurList;
      int intToken;
      if (!_dictionary.containsKey(token)) {
        intToken = _dictionary.size();
        _dictionary.put(token, intToken);
      } else {
        intToken = _dictionary.get(token);
      }
      // treat title differently, set title to have more frequency than content
      if (nameTokens.contains(token)) {
        if (termFreqTable.containsKey(intToken))
          termFreqTable.put(intToken, termFreqTable.get(intToken) + 5);
        else
          termFreqTable.put(intToken, 5);
      } else {
        if (termFreqTable.containsKey(intToken))
          termFreqTable.put(intToken, termFreqTable.get(intToken) + 1);
        else
          termFreqTable.put(intToken, 1);
      }
      if (!localList.containsKey(intToken)) {
        docOccurList = new ArrayList<>();
        docOccurList.add(docid);
        docOccurList.add(1);
      } else {
        docOccurList = (ArrayList<Integer>) localList.get(intToken);
        // update number of occurrence
        int occur = (docOccurList.get(1) + 1);
        docOccurList.set(1, occur);
      }
      // add current position
      docOccurList.add(pos);
      localList.put(intToken, docOccurList);
      pos++;
      // update _termCorpusFrequency
      int corpFreq = 1;
      if (_termCorpusFrequency.containsKey(intToken))
        corpFreq = _termCorpusFrequency.get(intToken) + 1;
      _termCorpusFrequency.put(intToken, corpFreq);
      // update _totalTermFrequency
      _totalTermFrequency++;
    }
    _documents.get(docid).setSize((short) pos);
    _documents.get(docid).setTermFrequencyTable(termFreqTable);
    mergePostingList(localList);
    s.close();
  }

  private void mergePostingList(Map<Integer, List<Integer>> docList) {
    for (Map.Entry<Integer, List<Integer>> entry : docList.entrySet()) {
      int intTok = entry.getKey();
      byte[] mergedArr;
      ArrayList<Integer> docOccurList = (ArrayList<Integer>) entry.getValue();
      if (!_postingLists1.containsKey(intTok)) {
        mergedArr = ByteAligned.encoding(docOccurList);
      } else {
        byte[] beforeMerge = _postingLists1.get(intTok);
        byte[] arrToMerge = ByteAligned.encoding(docOccurList);
        mergedArr = new byte[beforeMerge.length + arrToMerge.length];
        System.arraycopy(beforeMerge, 0, mergedArr, 0, beforeMerge.length);
        System.arraycopy(arrToMerge, 0, mergedArr, beforeMerge.length, arrToMerge.length);
      }
      _postingLists1.put(intTok, mergedArr);
    }
  }

  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
    String indexFile = _options._indexPrefix + "/IndexerInvertedCompressed.idx";
    System.out.println("Load index from: " + indexFile);
    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
    IndexerInvertedCompressed loaded = (IndexerInvertedCompressed) reader.readObject();
    this._documents = loaded._documents;
    this._termCorpusFrequency = loaded._termCorpusFrequency;
    // Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
    this._numDocs = _documents.size();
    for (Integer freq : loaded._termCorpusFrequency.values()) {
      this._totalTermFrequency += freq;
    }
    this._postingLists1 = loaded._postingLists1;
    this._dictionary = loaded._dictionary;
    reader.close();
    System.out.println(Integer.toString(_numDocs) + " documents loaded " +
        "with " + Long.toString(_totalTermFrequency) + " terms!");
  }

  @Override
  public Document getDoc(int did) {
    return (did >= _documents.size() || did < 0) ? null : _documents.get(did);
  }


  public int translateToInt(String term) {
    return _dictionary.get(term);
  }

  @Override
  public int corpusTermFrequency(String term) {
    return _dictionary.containsKey(term) ? _termCorpusFrequency.get(_dictionary.get(term)) : 0;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
    if (!_dictionary.containsKey(term)) {
      return 0;
    }
    int intTerm = _dictionary.get(term);
    int[] docOccrArr = getPostingList(intTerm);
    int res = 0;
    int counter = 1;
    while (counter < docOccrArr.length) {
      res++;
      counter = counter + docOccrArr[counter] + 2;
    }
    return res;
  }

  @Override
  public Document nextDoc(Query query, int docid) {
    List<Integer> docids = new ArrayList<>();
    docid++;
    Vector<String> tokens = query._tokens;
    for (String token : tokens) {
      int nextDoc;
      if (token.contains(" ")) {
        nextDoc = nextPhrase(token, docid, 0)[0];
      } else {
        if (_dictionary.containsKey(token)) {
          nextDoc = nextPos(_dictionary.get(token), docid, 0)[0];
        } else {
          nextDoc = -1;
        }
      }
      if (nextDoc == -1) {
        return null;
      }
      docids.add(nextDoc);
    }
    Collections.sort(docids);
    int lastIdx = docids.size() - 1;
    if (docids.get(0).equals(docids.get(lastIdx)))
      return getDoc(docids.get(0));
    return nextDoc(query, docids.get(lastIdx) - 1);
  }

  @Override
  public int documentTermFrequency(String term, int docid) {
    DocumentIndexed curDoc = (DocumentIndexed) getDoc(docid);
    if (!_dictionary.containsKey(term) || curDoc == null) {
      return 0;
    }
    int intTerm = _dictionary.get(term);
    int[] docOccrArr = getPostingList(intTerm);
    int jmpPtr = 0;
    while (jmpPtr < docOccrArr.length) {
      if (docOccrArr[jmpPtr] == docid) {
        return docOccrArr[jmpPtr + 1];
      }
      jmpPtr += docOccrArr[jmpPtr + 1] + 2;
    }
    return 0;
  }

  private int[] nextPos(int token, int docid, int startpos) {
    int[] pos = {-1, -1};
    if (!_postingLists1.containsKey(token)) {
      return pos;
    }
    int[] posting = getPostingList(token);
    int docIndex = 0;
    while (posting[docIndex] < docid) {
      docIndex = posting[docIndex + 1] + docIndex + 2;
      if (docIndex >= posting.length) {
        return pos;
      }
    }
    if (posting[docIndex] > docid) {
      pos[0] = posting[docIndex];
      pos[1] = posting[docIndex + 2];
      return pos;
    }
    int frequency = posting[docIndex + 1];
    int posIndex = docIndex + 2;
    for (int i = 0; i < frequency; i++) {
      if (posting[posIndex] >= startpos) {
        pos[0] = posting[docIndex];
        pos[1] = posting[posIndex];
        return pos;
      }
      posIndex++;
    }
    if (posIndex == posting.length) {
      return pos;
    } else {
      pos[0] = posting[posIndex];
      pos[1] = posting[posIndex + 2];
      return pos;
    }
  }

  // pos = 0 indicate from the beginning of this document.
  // return the end position of a phrase if found
  private int[] nextPhrase(String phrase, int docid, int pos) {
    int[] tokens = getIntegerTermArray(phrase.split(" "));
    _tempPostingListSize = tokens.length > maxPresavedSize ? maxPresavedSize : tokens.length;
    Vector<int[]> positions = new Vector<>();
    int maxIndex = 0;
    int curDocId = -1;
    int[] startPos = nextPos(tokens[0], docid, pos);
    int maxDocId = startPos[0];
    int maxPos = startPos[1];
    if (startPos[0] == -1) return startPos;
    boolean isphrase = true;
    for (int i = 0; i < tokens.length; i++) {
      int[] onePosition = nextPos(tokens[i], startPos[0], startPos[1] + i);
      if (onePosition[0] == -1) {
        return onePosition;
      }
      if (onePosition[0] != startPos[0] || onePosition[1] != (startPos[1] + i)) {
        isphrase = false;
      }
      if (onePosition[0] > maxDocId) {
        maxDocId = onePosition[0];
        maxPos = onePosition[1];
        maxIndex = i;
      } else {
        if (onePosition[1] > maxPos) {
          maxPos = onePosition[1];
          maxIndex = i;
        }
      }
      positions.add(onePosition);
    }
    if (isphrase) {
      return positions.get(positions.size() - 1);
    } else {
      return nextPhrase(phrase, maxDocId, maxPos - maxIndex);
    }
  }

  private int[] getIntegerTermArray(String[] tokens) {
    int[] res = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      res[i] = _dictionary.get(tokens[i]);
    }
    return res;
  }

  private int[] getPostingList(int token) {
    if (_tempPostingList.containsKey(token)) {
      return _tempPostingList.get(token);
    } else {
      int[] res = ByteAligned.decoding(_postingLists1.get(token));
      if (_tempPostingList.keySet().size() > _tempPostingListSize) {
        _tempPostingList = new HashMap<>();
      }
      _tempPostingList.put(token, res);
      return res;
    }
  }
}
