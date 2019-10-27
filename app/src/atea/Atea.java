/**
 * Atea - Abbreviated Text Expansion Algorithm
 *
 * Atea takes a String of text and finds all abbreviations used in the text. It determines all the
 * possible expansions of each abbreviation and assigns a confidence level to each expansion based
 * on how well the expansion fits the context of how the abbreviation was used in the text.
 */
package atea;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

public final class Atea {

  private Connection conn;

  public Atea(Connection conn) {
    this.conn = conn;
  }

  /**
   * expand returns
   * @param  text [description]
   * @return      [description]
   */
  public String expand(String text) {
    String output = "";
    Abbreviation abbr;

    ArrayList<Abbreviation> abbrs = getAbbreviations(text);
    int[] abbr_indexes = getIndexes(abbrs);
    String[] chunks = wordSplit(text, 3);

    int word_index = 0;
    for(int i=0, a=0; i<chunks.length; i++) {
      // delimeter
      output += chunks[i++];

      if(i >= chunks.length) {
        break;
      }

      // word
      output += chunks[i];

      // if word is an abbreviation
      int k = word_index;
      if(Arrays.stream(abbr_indexes).anyMatch(j -> j == k)) {
        output += " (" + abbrs.get(a++).getExpansions().get(0).getValue() + ")";
      }
      word_index++;
    }

    return output;

  }

  /**
   * Step through every word in the text and get determine if it is an abbreviation by looking for
   * possible expansions. If no possible expansions are found, it must not be an abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      An array of Abbreviation objects sorted from first occurance to last in the text
   *              The Abbreviation objects will have their possible expansions and the confidence
   *              of each expansion set.
   */
  public ArrayList<Abbreviation> getAbbreviations(String text) {
    ArrayList<Abbreviation> abbrs = new ArrayList<Abbreviation>();

    try ( Statement stmt = this.conn.createStatement(); ) {

      String[] words = getWords(text);

      for(int i=0; i<words.length; i++) {
        ArrayList<Expansion> expansions = getExpansions(words, i);

        // If expansions are found this word is believed to be an abbreviation
        if(expansions.size() > 0) {
          abbrs.add( new Abbreviation(words[i], i, expansions) );
        }
      }

    } catch(SQLException ex) {
       ex.printStackTrace();
    }

    Collections.sort(abbrs);
    return abbrs;
  }

  private String[] getWords(String text) {
    return wordSplit(text, 1);
  }

  private String[] getDelimiters(String text) {
    return wordSplit(text, 2);
  }

  /**
   * Splits text into individual words
   * @param text       The text to be split
   * @param returnType The type of return you would like
   *                   1 - Just the words
   *                   2 - Just the delimiters
   *                   3 - The words and delimiters
   * @return           An array of either just words, delimiters, or both. There will always be a
   *                   delimiter before the first word and after the last word, sometimes an empty
   *                   string.
   */
   private String[] wordSplit(String text, int returnType) {
     String wordPattern = "[A-z_]+";
     String delimiterPattern = "[^A-z_]+";

     String[] words = text.split(delimiterPattern);
     String[] delims = text.split(wordPattern);

     if(words[0].length() == 0) {
       // the string starts with a delimiter and the first element in words will be blank
       // remove that first blank element
       System.out.println("starts with delim");
       words = Arrays.copyOfRange(words, 1, words.length);
     }

     if(delims.length == words.length) {
       // add one more blank element to the end of delims
       delims = Arrays.copyOfRange(delims, 0, words.length + 1);
       delims[delims.length - 1] = "";
     }

     if(returnType == 1) {
       return words;
     } else if(returnType == 2) {
       return delims;
     }

     String[] mixed = new String[delims.length + words.length];
     int j=0;
     for(int i=0; i<words.length; i++) {
       mixed[j++] = delims[i];
       mixed[j++] = words[i];
     }
     mixed[j] = delims[delims.length - 1];

     return mixed;
   }

   private int[] getIndexes(ArrayList<Abbreviation> abbrs) {
     int[] indexes = new int[abbrs.size()];

     for(int i=0; i<abbrs.size(); i++) {
       indexes[i] = abbrs.get(i).getIndex();
     }

     return indexes;
   }

  /**
   * Determines if the string of characters starting at a provided index, of a provided length in
   * the provided text is an abbreviation in Atea's dictionary AND if it's usage in the text
   * indicates it is an abbreviation rather than a word.
   *
   * Example: "am" could be an abbreviation for "ante meridiem" (before noon) as in "9:30 am".
   * Or, it could be the word "am" as in "I am having a good day today".
   * @param  index    The index in the text at which the potential abbreviation starts
   * @param  length   The length of the potential abbreviation
   * @param  text     The text in which the potential abbreviation is found
   * @return          the id of the abbreviation if the potential abbreviation is found in the
   *                  database AND the confidence level is >= .95, otherwise -1
   */
  public ArrayList<Expansion> getExpansions(String[] text, int abbr_index) {
    return getExpansions(text, abbr_index, .95);
  }

  /**
   * Determines if the string of characters starting at a provided index, of a provided length in
   * the provided text is an abbreviation in Atea's dictionary AND if it's usage in the text
   * indicates it is an abbreviation rather than a word.
   *
   * Example: "am" could be an abbreviation for "ante meridiem" (before noon) as in "9:30 am".
   * Or, it could be the word "am" as in "I am having a good day".
   * @param  index     The index in the text at which the potential abbreviation starts
   * @param  length    The length of the potential abbreviation
   * @param  text      The text in which the potential abbreviation is found
   * @param  threshold A value between 0 and 1 inclusive of the minimum confidence level to return
   *                   true on a potential abbreviation
   * @return           the id of the abbreviation if the potential abbreviation is found in the
   *                   database AND the confidence level is >= the provided threshold value,
   *                   otherwise -1
   */
  public ArrayList<Expansion> getExpansions(String[] text, int abbr_index, double threshold) {
    ArrayList<Expansion> expansions = new ArrayList<Expansion>();
    String abbr = text[abbr_index];

    int id = abbreviationExists(abbr);
    if(id != -1) {
      float confidence;
      try ( Statement stmt = this.conn.createStatement(); ) {

        // Loop through all possible expansions for this abbreviation
        String strSelect = "SELECT id, value FROM expansions JOIN abbreviation_expansion ON expansions.id=abbreviation_expansion.expansion_id WHERE abbreviation_expansion.abbreviation_id =" + id;
        ResultSet rset = stmt.executeQuery(strSelect);
        while(rset.next()) {
          confidence = confidence(text, abbr_index, rset.getInt("id"));
          if(confidence >= threshold) {
            expansions.add( new Expansion(rset.getString("value"), confidence) );
          }

        };
      } catch(SQLException ex) {
       ex.printStackTrace();
      }

    }

    Collections.sort(expansions);
    return expansions;
  }

  /**
   * Determines if the provided string of characters is an existing abbreviation in the database
   * @param  chars A string to be checked
   * @return       the id of the abbreviation if chars is found in the database, otherwise -1
   */
  public int abbreviationExists(String chars) {
    try ( Statement stmt = this.conn.createStatement(); ) {
      String strSelect = "SELECT id FROM abbreviations WHERE value='" + chars + "'";
      ResultSet rset = stmt.executeQuery(strSelect);

      if(rset.next()) {
        return rset.getInt("id");
      }
    } catch(SQLException ex) {
       ex.printStackTrace();
    }

    return -1;
  }

  private float confidence(String[] text, int abbr_index, int expansion_id) {
    // How far left and right from the abbr_index to look at the context of its use
    int context_size = 5;

    // Limit the start and end of the context around the abbr to not go out of bounds of text
    int start_index = Math.max(0, abbr_index - context_size);
    int end_index = Math.min(text.length - 1, abbr_index + context_size);
    int abbr_context_index = abbr_index - start_index;
    String[] context = Arrays.copyOfRange(text, start_index, end_index + 1);


    float[] contextMatchScore = new float[end_index - start_index];
    float total = 0;

    float p;
    // for every word surrounding the abbreviation
    for(int i=0, j=0; i<context.length; i++) {
      if(i == abbr_context_index) {
        continue;
      }

      p = contextMatchAt(context, abbr_context_index, expansion_id, i);
      contextMatchScore[j++] = p;
      total += p;
    }

    float averageContextMatchScore = total / contextMatchScore.length;

    return averageContextMatchScore;
  }

  private float contextMatchAt(String[] context, int abbr_index, int expansion_id, int index) {

    // Only look on the left or right side of the abbr
    int start = 0;
    int end = abbr_index;
    if(index > abbr_index) {
      start = abbr_index + 1;
      end = context.length;
    }

    float total_score = 0;
    float total_possible_score = 0;
    float probability;
    for(int i=start; i<end; i++) {
      int distance = i - abbr_index;
      probability = probabilityAt(expansion_id, distance, context[index]);

      // Do not penalize the conext match score if the word is not found in the sorrounding columns
      if(probability == 0 && i != index) {
        continue;
      }

      total_score += probability / (Math.abs(distance) + 1);
      total_possible_score += 1.0 / (Math.abs(distance) + 1);
    }

    return total_score / total_possible_score;
  }

  private float probabilityAt(int expansion_id, int distance, String word) {
    try ( Statement stmt = this.conn.createStatement(); ) {
      int occurances = 0;
      int total = 0;

      String strSelect = "SELECT SUM(count) AS count FROM context JOIN words ON context.word_id = words.id WHERE expansion_id=" + expansion_id + " AND distance=" + distance + " AND words.value='" + word + "'";
      ResultSet rset = stmt.executeQuery(strSelect);
      while(rset.next()) {
        occurances = rset.getInt("count");
      };

      strSelect = "SELECT SUM(count) AS count FROM context WHERE expansion_id=" + expansion_id + " AND distance=" + distance;
      rset = stmt.executeQuery(strSelect);
      while(rset.next()) {
        total = rset.getInt("count");
      };

      if(total == 0) {
        return 0;
      }
      return occurances / total;

    } catch(SQLException ex) {
     ex.printStackTrace();
    }

    return 0;
  }
}
