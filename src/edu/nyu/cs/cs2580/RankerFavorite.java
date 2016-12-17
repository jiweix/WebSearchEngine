package edu.nyu.cs.cs2580;

import java.util.*;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import javafx.scene.paint.Stop;

public class RankerFavorite extends Ranker {
    // a share hashmap that maps a query to [category, their distance]
    private static HashMap<String, HashMap<String, Integer>> categoryQueryDistances = new HashMap<>();

    private double ratingWeight = 0.5;
    private double tweetWeight = 0.5;
    private double categoryWeight = 1;

    private TwitterPool _tp;

    public RankerFavorite(Options options,
                          CgiArguments arguments, Indexer indexer) {
        super(options, arguments, indexer);
        System.out.println("Using Ranker: " + this.getClass().getSimpleName());
    }

    public void setTwitterPool(TwitterPool twitterPool) {
        this._tp = twitterPool;
    }
    public void setUseTwitter(Boolean flag) {
        if (!flag) {
            tweetWeight = 0;
        }
    }

    @Override
    public Vector<ScoredDocument> runQuery(Query query, int numResults) {
        Vector<ScoredDocument> all = new Vector<ScoredDocument>();
        DocumentIndexed curDoc = (DocumentIndexed) _indexer.nextDoc(query, -1);
        try {
            while (curDoc != null) {
                all.add(scoreDocument(query, curDoc));
                curDoc = (DocumentIndexed) _indexer.nextDoc(query, curDoc._docid);
            }
            Collections.sort(all, Collections.reverseOrder());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        Vector<ScoredDocument> results = new Vector<ScoredDocument>();
        for (int i = 0; i < all.size() && i < numResults; ++i) {
            results.add(all.get(i));
        }

        // test results
        if (categoryQueryDistances.containsKey(query._query)) {
            for (String category : categoryQueryDistances.get(query._query).keySet()) {
                int dist = categoryQueryDistances.get(query._query).get(category);
                if (dist >= 0) {
                    System.out.println(query._tokens + " | " + Arrays.asList(category.split("-")) + " | " + dist);
                }
            }
        }
        return results;
    }

    protected ScoredDocument scoreDocument(Query query, DocumentIndexed doc) {
        double score = 0.0;

        // standard cosine distance score
        double cosineScore = getCosineDistanceScore(query, doc);

        // Adjust by tweets
        double twitterScore = getTwitterPopularityScore(doc) * tweetWeight;

//        // Adjust by rating
        double rateScore = getDocRatingScore(doc) * ratingWeight;

//        // adjust by product category - query similarity
        double categoryDistScore = getCategoryEditDistanceScore(query, doc) * categoryWeight;

        score = cosineScore + twitterScore + rateScore + categoryDistScore;

        return new ScoredDocument(doc, score).addSubScores(cosineScore, twitterScore, rateScore, categoryDistScore);
    }

    public double getCosineDistanceScore(Query query, DocumentIndexed doc) {
        IndexerInvertedCompressed translator = (IndexerInvertedCompressed)_indexer;

        HashMap<Integer, Integer> termFreqTable = doc.getTermFrequencyTable();

        //construct query vector
        Vector<String> queryTokens = flatToken(query._tokens);
        HashMap<Integer, Double> queryVec = new HashMap<>();

        for(String queryToken : queryTokens){
            int translated = translator.translateToInt(queryToken);

            if(!queryVec.containsKey(translated)){
                double freq = Collections.frequency(queryTokens, queryToken);
                queryVec.put(translated,freq/(double)queryTokens.size());
            }
        }

        // calculate the cosine dist
        double nominator = 0.0;
        double l2NormQ = 0.0;
        for(String queryToken : queryTokens){
            double dij = 0.0;
            int intTok = translator.translateToInt(queryToken);
            if(termFreqTable.containsKey(intTok)){
                dij = termFreqTable.get(intTok);
            }

            double qj = queryVec.get(intTok);
            nominator += dij*qj;
            l2NormQ += qj*qj;
        }
        double dijSquares = 0.0;
        for(Map.Entry<Integer,Integer> entry: termFreqTable.entrySet()){
            dijSquares+=Math.pow(entry.getValue(),2);
        }
        double denominator = Math.sqrt(dijSquares*l2NormQ);
        double score = nominator/denominator;
        return score;
    }

    public double getTwitterPopularityScore(DocumentIndexed doc) {
        String productName = doc.getProduct().getName();
        int tweetsCount = _tp.getProductCount(productName);
        double tweetScore = (double) tweetsCount/10;
        if (tweetScore > 1) {
            tweetScore = 1.0;
        }

        return  tweetScore;
    }

    public double getDocRatingScore(DocumentIndexed doc) {
        // Adjust by rating
        double ratingScore = doc.getProduct().getRate()/5.0;
        if (doc.getProduct().getReviewCount() < 50) {
            ratingScore *= (float) doc.getProduct().getReviewCount()/50.0;
        }
        return ratingScore;
    }

    public double getCategoryEditDistanceScore(Query query, DocumentIndexed doc) {
        // initiate with query
        if (!categoryQueryDistances.containsKey(query._query)) {
            categoryQueryDistances.put(query._query, new HashMap<>());
        }

        HashSet<String> categoryList = doc.getProduct().getCategory();

        // get the minimum of all distances between the query and all categories
        int minDist = -1;
        for (String category : categoryList) {
            String[] words = category.split("-");
            HashSet<String> categoryWords = new HashSet<>();
            for (int i = 0; i < words.length; i++) {
                words[i] = Stemmer.stem(words[i]);
                if (!Stopwords.contains(words[i])) {
                    categoryWords.add(words[i]);
                }
            }

            // check if a category contains every word of the query
            int numMatch = 0;
            for (String token : query._tokens) {
                if (categoryWords.contains("canon") && categoryWords.contains("dslr")){
                    int a = 0;
                }
                if (categoryWords.contains(token)) {
                    numMatch++;
                }
            }
            // if not contains every word. TODO: maybe have a certain amount of word matches is enough
            if (numMatch < query._tokens.size()) {
                continue;
            }

            int dist;
            if (categoryQueryDistances.get(query._query).containsKey(category)) {
                dist = categoryQueryDistances.get(query._query).get(category);
            } else {
                dist = editDistance(new Vector<>(categoryWords), query._tokens);
                categoryQueryDistances.get(query._query).put(category, dist);
            }

            if (minDist == -1) {
                minDist = dist;
            } else {
                minDist = minDist < dist? minDist : dist;
            }
        }

        double score = 0.0;
        // indicate relevant
        if (minDist >= 0) {
            score = 1.0/(minDist + 1);
        }

        return score;
    }

    private int editDistance(Vector<String> s1, Vector<String> s2) {
        int m = s1.size();
        int n = s2.size();
        int[][] dp = new int[m + 1][n + 1];

        //caculate
        for (int i = 0; i < m + 1; i++) {
            for (int j = 0; j < n + 1; j++) {
                if (i == 0)
                    dp[i][j] = j;
                else if (j == 0)
                    dp[i][j] = i;
                else if (s1.get(i - 1).equals(s2.get(j - 1)))
                    dp[i][j] = dp[i - 1][j - 1];
                else
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
            }
        }

        return dp[m][n];
    }

    private Vector<String> flatToken(Vector<String> queryTokens) {
        Vector<String> res = new Vector<String>();
        for (String s : queryTokens) {
            if (s.contains(" ")) {
                for (String inner : s.split(" ")) {
                    res.add(inner);
                }
            } else {
                res.add(s);
            }
        }
        return res;
    }
}
