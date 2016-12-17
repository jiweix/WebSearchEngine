package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * Handles each incoming query, students do not need to change this class except
 * to provide more query time CGI arguments and the HTML output.
 * <p>
 * N.B. This class is not thread-safe.
 *
 * @author congyu
 * @author fdiaz
 */
class QueryHandler implements HttpHandler {
  // For accessing the underlying documents to be used by the Ranker. Since
  // we are not worried about thread-safety here, the Indexer class must take
  // care of thread-safety.
  private Indexer _indexer;
  private TwitterPool _tp;
  private TwitterStreaming _ts = TwitterStreaming.getInstance();
  private final String homeHtmlPath = "./html/home.html";
  private final String partialResultPath = "./html/result.html";
  private String homeHtml;
  private String resultHtmlBefore;
  private String resultHtmlAfter;

  /**
   * CGI arguments provided by the user through the URL. This will determine
   * which Ranker to use and what output format to adopt. For simplicity, all
   * arguments are publicly accessible.
   */
  public static class CgiArguments {
    // The raw user query
    public String _query = "";
    // How many results to return
    private int _numResults = 20;
    private boolean _useTwitter = false;

    // The type of the ranker we will be using.
    public enum RankerType {
      NONE,
      FAVORITE,
      COSINE,
      PHRASE,
      QL,
      LINEAR,
    }

    public RankerType _rankerType = RankerType.FAVORITE;

    // The output format.
    public enum OutputFormat {
      TEXT,
      HTML,
    }

    public OutputFormat _outputFormat = OutputFormat.HTML;

    public CgiArguments(String uriQuery) {
      String[] params = uriQuery.split("&");
      for (String param : params) {
        String[] keyval = param.split("=", 2);
        if (keyval.length < 2) {
          continue;
        }
        String key = keyval[0].toLowerCase();
        String val = keyval[1];
        if (key.equals("query")) {
          _query = val;
        } else if (key.equals("num")) {
          try {
            _numResults = Integer.parseInt(val);
          } catch (NumberFormatException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("ranker")) {
          try {
            _rankerType = RankerType.valueOf(val.toUpperCase());
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("format")) {
          try {
            _outputFormat = OutputFormat.valueOf(val.toUpperCase());
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("numdocs")) {
          try {
            _numResults = Integer.parseInt(val);
          } catch (NumberFormatException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("usetwitter")) {
          try {
            if (val.equals("true")) {
              _useTwitter = true;
            }
          } catch (NumberFormatException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        }
      }  // End of iterating over params
    }
  }

  public QueryHandler(Options options, Indexer indexer) throws IOException, InterruptedException {
    _indexer = indexer;
    _tp = new TwitterPool();
    _ts.setTwitterPool(_tp);
    _ts.start();
    loadHtml();
  }

  private void respondWithMsg(HttpExchange exchange, final String message)
      throws IOException {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", "text/html");
    exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
    OutputStream responseBody = exchange.getResponseBody();
    responseBody.write(message.getBytes());
    responseBody.close();
  }

  private void constructTextOutput(
      final Vector<ScoredDocument> docs, StringBuffer response) {
    for (ScoredDocument doc : docs) {
      response.append(response.length() > 0 ? "\n" : "");
      response.append(doc.asTextResult());
    }
    response.append(response.length() > 0 ? "\n" : "");
  }


  private void constructHTMLOutput(final Vector<ScoredDocument> docs, StringBuffer response) {
    response.append(resultHtmlBefore);
    for (ScoredDocument doc : docs) {
      response.append("\n");
      response.append("<div class=\"result\">");
      response.append(doc.asHtmlResult());
      response.append("</div>");
    }
    response.append(docs.size() > 0 ? "\n" : "No result returned!");
    response.append(resultHtmlAfter);
  }


  public void handle(HttpExchange exchange) throws IOException {
    String requestMethod = exchange.getRequestMethod();
    if (!requestMethod.equalsIgnoreCase("GET")) { // GET requests only.
      return;
    }
    // Print the user request header.
    Headers requestHeaders = exchange.getRequestHeaders();
    System.out.print("Incoming request: ");
    for (String key : requestHeaders.keySet()) {
      System.out.print(key + ":" + requestHeaders.get(key) + "; ");
    }
    System.out.println();
    // Validate the incoming request.
    String uriQuery = exchange.getRequestURI().getQuery();
    String uriPath = exchange.getRequestURI().getPath();
    if (uriPath == null) {
      respondWithMsg(exchange, "Something wrong with the URI!");
    } else if (uriPath.equals("/") || uriQuery == null) {
      // show home page
      respondWithMsg(exchange, homeHtml);
    } else {
      if (!uriPath.equals("/search")) {
        respondWithMsg(exchange, "Only /search is handled!");
      }
      System.out.println("Query: " + uriQuery);
      // Process the CGI arguments.
      CgiArguments cgiArgs = new CgiArguments(uriQuery);
      if (cgiArgs._query.isEmpty()) {
        respondWithMsg(exchange, "No query is given!");
      }
      // Create the ranker.
      Ranker ranker = Ranker.Factory.getRankerByArguments(
          cgiArgs, SearchEngine.OPTIONS, _indexer);
      if (ranker == null) {
        respondWithMsg(exchange,
            "Ranker " + cgiArgs._rankerType.toString() + " is not valid!");
      }
      // set twitter pool for twitter data
      ranker.setTwitterPool(this._tp);
      ranker.setUseTwitter(cgiArgs._useTwitter);
      // Processing the query.
      Query processedQuery = new QueryPhrase(cgiArgs._query);
      processedQuery.processQuery();
      // Ranking.
      Vector<ScoredDocument> scoredDocs = ranker.runQuery(processedQuery, cgiArgs._numResults);
      StringBuffer response = new StringBuffer();
      switch (cgiArgs._outputFormat) {
        case TEXT:
          constructTextOutput(scoredDocs, response);
          break;
        case HTML:
          constructHTMLOutput(scoredDocs, response);
          break;
        default:
          // nothing
      }
      respondWithMsg(exchange, response.toString());
      System.out.println("Finished query: " + cgiArgs._query);
    }
  }

  private void loadHtml() {
    try {
      File file = new File(homeHtmlPath);
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      fis.close();
      homeHtml = new String(data, "UTF-8");
    } catch (IOException e) {
      homeHtml = "can't load home.html";
    }
    try {
      File file = new File(partialResultPath);
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      fis.close();
      String resultHtml = new String(data, "UTF-8");
      int index = resultHtml.indexOf("^^^^");
      resultHtmlBefore = resultHtml.substring(0, index);
      resultHtmlAfter = resultHtml.substring(index + 4, resultHtml.length() - 1);
    } catch (IOException e) {
      resultHtmlBefore = "";
      resultHtmlAfter = "";
    }
  }
}

