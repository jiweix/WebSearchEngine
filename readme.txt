Put these library files in lib/ before running
jsoup-1.10.1
twitter4j-4.0.4

-----compile------
javac -cp "lib/*:src" src/edu/nyu/cs/cs2580/*.java

-----crawling------
java -cp "lib/*:src" edu.nyu.cs.cs2580.SearchEngine --mode=crawl  --options=conf/engine.conf

crawled pages are saved to:
    Bestbuy Temp Corpus: ./data/bestbuy/pages_tmp/
    Amazon Temp Corpus: ./data/amazon/pages_tmp/

-----indexing-----
java -cp "lib/*:src" edu.nyu.cs.cs2580.SearchEngine --mode=index  --options=conf/engine.conf

indexing use the following corpus path:
    Bestbuy Corpus: ./data/bestbuy/pages/
    Amazon Corpus: ./data/amazon/pages/

index data is saved in: ./data/index/IndexerInvertedCompressed.idx
NOTE: please create the folder index/ inside ./data/ before running index

-----serve------
java -cp "lib/*:src" edu.nyu.cs.cs2580.SearchEngine --mode=serve --port=25803 --options=conf/engine.conf

Twitter Streaming: ./auth/auth_token
put 4 lines in the file auth_token file
[customer access key]
[customer secret key]
[access key]
[access secret key]

-----website-----
http://localhost:25803/

webpages template are used from: 
    ./html/home.html
    ./html/results.html

This is course project for web search engine. 
In collaberation with Alexia Lou, Yonghong Chen and William Brantley