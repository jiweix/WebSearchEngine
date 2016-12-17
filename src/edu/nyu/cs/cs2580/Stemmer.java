package edu.nyu.cs.cs2580;

/**
 * Referenced from stackoverflow
 * To use, call Stemmer.stem(String s), this will return the stemmed String
 */
public class Stemmer {
  public static final Stemmer instance = new Stemmer();

  class NewString {
    public String str;
    NewString() {
      str = "";
    }
  }

  private Stemmer() {

  }

  public static String stem(String s) {
    if (s == null || s.isEmpty()) {
      return "";
    }

    s = s.toLowerCase();
    if (s.endsWith("ingly") || s.endsWith("edly") || s.endsWith("ches")) {
      return instance.step1(s.substring(0, s.length()-2));
    }
    return instance.step1(s);
  }

  private String step1( String str ) {

    NewString stem = new NewString();

    if ( str.charAt( str.length()-1 ) == 's' ) {
      if ( (hasSuffix( str, "sses", stem )) || (hasSuffix( str, "ies", stem)) ){
        String tmp = "";
        for (int i=0; i<str.length()-2; i++)
          tmp += str.charAt(i);
        str = tmp;
      }
      else {
        if ( ( str.length() == 1 ) && ( str.charAt(str.length()-1) == 's' ) ) {
          str = "";
          return str;
        }
        if ( str.charAt( str.length()-2 ) != 's' ) {
          String tmp = "";
          for (int i=0; i<str.length()-1; i++)
            tmp += str.charAt(i);
          str = tmp;
        }
      }
    }

    if ( hasSuffix( str,"eed",stem ) ) {
      if ( measure( stem.str ) > 0 ) {
        String tmp = "";
        for (int i=0; i<str.length()-1; i++)
          tmp += str.charAt( i );
        str = tmp;
      }
    }
    else {
      if (  (hasSuffix( str,"ed",stem )) || (hasSuffix( str,"ing",stem )) ) {
        if (containsVowel( stem.str ))  {

          String tmp = "";
          for ( int i = 0; i < stem.str.length(); i++)
            tmp += str.charAt( i );
          str = tmp;
          if ( str.length() == 1 )
            return str;

          if ( ( hasSuffix( str,"at",stem) ) || ( hasSuffix( str,"bl",stem ) ) || ( hasSuffix( str,"iz",stem) ) ) {
            str += "e";

          }
          else {
            int length = str.length();
            if ( (str.charAt(length-1) == str.charAt(length-2))
                && (str.charAt(length-1) != 'l') && (str.charAt(length-1) != 's') && (str.charAt(length-1) != 'z') ) {

              tmp = "";
              for (int i=0; i<str.length()-1; i++)
                tmp += str.charAt(i);
              str = tmp;
            }
            else
            if ( measure( str ) == 1 ) {
              if ( cvc(str) )
                str += "e";
            }
          }
        }
      }
    }

    if ( hasSuffix(str,"y",stem) )
      if ( containsVowel( stem.str ) ) {
        String tmp = "";
        for (int i=0; i<str.length()-1; i++ )
          tmp += str.charAt(i);
        str = tmp + "i";
      }
    return str;
  }

  private boolean hasSuffix( String word, String suffix, NewString stem ) {

    String tmp = "";

    if ( word.length() <= suffix.length() )
      return false;
    if (suffix.length() > 1)
      if ( word.charAt( word.length()-2 ) != suffix.charAt( suffix.length()-2 ) )
        return false;

    stem.str = "";

    for ( int i=0; i<word.length()-suffix.length(); i++ )
      stem.str += word.charAt( i );
    tmp = stem.str;

    for ( int i=0; i<suffix.length(); i++ )
      tmp += suffix.charAt( i );

    if ( tmp.compareTo( word ) == 0 )
      return true;
    else
      return false;
  }

  private boolean containsVowel( String word ) {

    for (int i=0 ; i < word.length(); i++ )
      if ( i > 0 ) {
        if ( vowel(word.charAt(i),word.charAt(i-1)) )
          return true;
      }
      else {
        if ( vowel(word.charAt(0),'a') )
          return true;
      }

    return false;
  }

  private boolean vowel( char ch, char prev ) {
    switch ( ch ) {

      case 'a': case 'e': case 'i': case 'o': case 'u':
        return true;
      case 'y': {

        switch ( prev ) {
          case 'a': case 'e': case 'i': case 'o': case 'u':

            return false;

          default:
            return true;
        }
      }
      default :

        return false;
    }
  }

  private int measure( String stem ) {

    int i=0, count = 0;
    int length = stem.length();

    while ( i < length ) {
      for ( ; i < length ; i++ ) {
        if ( i > 0 ) {
          if ( vowel(stem.charAt(i),stem.charAt(i-1)) )
            break;
        }
        else {
          if ( vowel(stem.charAt(i),'a') )
            break;
        }
      }

      for ( i++ ; i < length ; i++ ) {
        if ( i > 0 ) {
          if ( !vowel(stem.charAt(i),stem.charAt(i-1)) )
            break;
        }
        else {
          if ( !vowel(stem.charAt(i),'?') )
            break;
        }
      }
      if ( i < length ) {
        count++;
        i++;
      }
    } //while

    return(count);
  }

  private boolean cvc( String str ) {
    int length=str.length();

    if ( length < 3 )
      return false;

    if ( (!vowel(str.charAt(length-1),str.charAt(length-2)) )
        && (str.charAt(length-1) != 'w') && (str.charAt(length-1) != 'x') && (str.charAt(length-1) != 'y')
        && (vowel(str.charAt(length-2),str.charAt(length-3))) ) {

      if (length == 3) {
        if (!vowel(str.charAt(0),'?'))
          return true;
        else
          return false;
      }
      else {
        if (!vowel(str.charAt(length-3),str.charAt(length-4)) )
          return true;
        else
          return false;
      }
    }

    return false;
  }
}
