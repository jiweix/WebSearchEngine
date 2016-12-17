package edu.nyu.cs.cs2580;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jiwei Xu on 10/24/16.
 */
public class Stopwords {
  private static Set<String> stopWord =
      new HashSet<String>(Arrays.asList("sometime","been","mostli","don't","couldn't","your","without",
          "via","these","appreciate","would","because","near","doesn't","you","unlikeli",
          "afterward","sure","thu","go","meanwhile","viz","am","yourselve","an","whose","former",
          "a","contain","at","try","look","i've","much","appropriate","be","anybodi","come",
          "least","consequentli","example","how","see","inward","same","by","whom","indicate",
          "after","shouldn't","you've","we'd","a","wouldn't","b","c","contain","d","thanx","e",
          "f","g","nameli","h","i","j","k","possible","right","co","l","m","n","o","p","the","q",
          "r","","t","u","v","w","x","fifth","thank","y","somewhat","z","under","your","did",
          "novel","nine","sometime","do","down","got","wish","later","beside","seriou","other",
          "need","which","ignor","t'","eg","need","it","thereafter","often","onto","regard",
          "et","gone","never","she","take","ex","aside","immediate","relativeli","therefore",
          "aren't","hardli","useful","little","however","some","rather","downward","for","greet",
          "c'","get","nowhere","perhap","sorri","provide","you're","just","over","we'll","six",
          "thence","go","obviousli","kept","better","with","although","selve","there","well",
          "happen","he","hi","tri","veri","place","therein","soon","thank","tri","else","four",
          "beside","usualli","wherea","ie","per","if","there'","likeli","went","in","consider",
          "noth","anyhow","specifi","i","be","it","forth","somebodi","weren't","ever","we've",
          "even","hello","wherebi","secondli","become","that","other","indicate","against",
          "respectiveli","a'","isn't","whereupon","hadn't","eight","howbeit","known","there",
          "indicate","too","have","hopefulli","everyth","can't","together","know","accord",
          "particularli","thoroughli","mai","seem","within","could","off","awfulli","able",
          "ain't","com","their","presumabli","almost","use","several","upon","while","like",
          "second","latterli","amongst","that","etc","whether","than","me","quite","different",
          "insofar","regardless","all","alwai","new","took","alreadi","below","everyone","didn't",
          "follow","lest","shall","less","seriousli","my","plu","become","nd","were","we're",
          "try","since","became","behind","no","cause","best","around","and","hither","of","oh",
          "somehow","sai","ok","sai","on","allow","brief","certainli","or","whence","exactli",
          "ani","despite","follow","c'mon","concern","until","formerli","gotten","about","what'",
          "anywhere","somewhere","wherein","haven't","wasn't","where'","above","let","welcome",
          "thei","here'","us","qv","contain","old","myself","want","herein","them","then","each",
          "someth","specifi","himself","rd","re","therebi","except","must","sub","nevertheless",
          "hasn't","maybe","probabli","another","believe","two","seen","anywai","seem","sup",
          "into","are","unless","doe","taken","came","where","so","give","apart","ought","think",
          "necessari","though","one","thorough","mani","entireli","actualli","appear","such",
          "definiteli","th","associate","ask","to","they've","but","through","anywai","won't",
          "becom","will","available","seven","cant","had","mainli","either","our","whenever",
          "un","yourself","ha","up","five","they'd","those","u","beforehand","seem","given",
          "last","let'","might","zer","thi","please","reasonabli","look","whatever","especialli",
          "once","everywhere","name","know","overall","v","allow","next","que","do","awai",
          "ask","nearli","change","that'","non","we","anyth","nor","not","now","themselve",
          "throughout","he'","want","hence","wonder","everi","unto","they're","ye","again",
          "wa","yet","indee","i'll","wai","inasmuch","what","furthermore","one","whole","dure",
          "none","beyond","three","when","her","whoever","far","nobodi","truli","between",
          "it'll","okai","still","have","come","they'll","itself","toward","hereupon","among",
          "anyone","follow","uucp","i'd","noone","our","ourselve","i'm","specifi","out","across",
          "se","moreover","cause","get","course","mereli","sensible","wherever","more",
          "unfortunateli","lateli","help","cannot","self","herebi","whereafter","certain",
          "first","thru","before","own","tell","clearli","us","him","look","hi","onli","should",
          "few","from","consider","keep","describ","otherwise","you'd","whither","you'll","like",
          "goe","it'","particular","toward","done","inner","regard","sent","both","most","twice",
          "outside","edu","keep","it'd","herself","seem","thereupon","who","here","everybodi",
          "accord","their","why","elsewhere","her","can","alone","along","who'","said","ltd",
          "value","inc","will","hereafter","saw","also","sai","enough","instead","get","realli",
          "currentli","someone","third","correspond","mean","variou","neither","latter","use",
          "further","tend","normalli"));

  private Stopwords() {
    throw new AssertionError();
  }

  public static boolean contains(String s) {
    return stopWord.contains(s);
  }
}
