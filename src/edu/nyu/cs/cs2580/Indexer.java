package edu.nyu.cs.cs2580;

import java.io.IOException;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * This is the abstract Indexer class for all concrete Indexer implementations.
 * <p>
 * Use {@link Indexer.Factory} to create concrete Indexer implementation.
 * Do NOT change the interface of this class.

 * @author congyu
 * @author fdiaz
 */
public abstract class Indexer {
    // Options to configure each concrete Indexer, do not serialize.
    protected Options _options = null;

    // In-memory data structures populated once for each server. Those fields
    // are populated during index loading time and must not be modified during
    // serving unless they are made thread-safe. For comments, see APIs below.
    // Subclasses should populate those fields properly.
    protected int _numDocs = 0;
    protected long _totalTermFrequency = 0;

    // Provided for serialization.
    public Indexer() {
    }

    // The real constructor
    public Indexer(Options options) {
        _options = options;
    }

    // APIs for document retrieval.

    /**
     * Random access to documents, used prominently in HW1. In HW2, this interface
     * should only be used to retrieve intrinsic features of the document, not the
     * term features.
     *
     * @param docid
     * @return
     */
    public abstract Document getDoc(int docid);

    /**
     * Iterator access to documents, used in HW2 for retrieving terms features for
     * the query matching the documents.
     *
     * @param query
     * @param docid
     * @return the next Document after {@code docid} satisfying {@code query} or
     * null if no such document exists.
     */
    public abstract Document nextDoc(Query query, int docid);

    // APIs for index construction and loading.

    /**
     * Called when the SearchEngine is in {@code Mode.INDEX} mode. Subclass must
     * construct the index from the provided corpus at {@code corpus_prefix}.
     * <p>
     * Document processing must satisfy the following:
     * 1) Non-visible page content is removed, e.g., those inside <script> tags
     * 2) Tokens are stemmed with Step 1 of the Porter's algorithm
     * 3) No stop word is removed, you need to dynamically determine whether to
     * drop the processing of a certain inverted list.
     * <p>
     * The index must reside at the directory of index_prefix, no other data can
     * be stored (either in a hidden file or in a temporary directory). In serve
     * mode, the constructed index should provide the necessary functionality to
     * support the search tasks.
     */
    public abstract void constructIndex() throws IOException;

    /**
     * Called exactly once when the SearchEngine is in {@code Mode.SERVE} mode.
     * Subclass must load the index at {@code index_prefix} to be ready for
     * serving the search traffic.
     * <p>
     * You must load the index from the constructed index above, do NOT try to
     * reconstruct the index from the corpus. When the search engine is run in
     * serve mode, it will NOT have access to the corpus, all grading for serve
     * mode will be done with the corpus removed from the machine.
     */
    public abstract void loadIndex() throws IOException, ClassNotFoundException;

    // Number of documents in the corpus.
    public final int numDocs() {
        return _numDocs;
    }

    // Number of term occurrences in the corpus. If a term appears 10 times, it
    // will be counted 10 times.
    public final long totalTermFrequency() {
        return _totalTermFrequency;
    }

    // Number of documents in which {@code term} appeared, over the full corpus.
    public abstract int corpusDocFrequencyByTerm(String term);

    // Number of times {@code term} appeared in corpus.
    public abstract int corpusTermFrequency(String term);

    // Number of times {@code term} appeared in the document {@code docid}.
    public abstract int documentTermFrequency(String term, int docid);

    /**
     * All Indexers must be created through this factory class based on the
     * provided {@code options}.
     */
    public static class Factory {
        public static Indexer getIndexerByOption(Options options) {
//      if (options._indexerType.equals("fullscan")) {
//        return new IndexerFullScan(options);
//      } else if (options._indexerType.equals("inverted-doconly")) {
//        return new IndexerInvertedDoconly(options);
//      } else if (options._indexerType.equals("inverted-occurrence")) {
//        return new IndexerInvertedOccurrence(options);
//      } else if (options._indexerType.equals("inverted-compressed")) {
//        return new IndexerInvertedCompressed(options);
//      }
            if (options._indexerType.equals("inverted-compressed")) {
                return new IndexerInvertedCompressed(options);
            }
            return null;
        }
    }
}