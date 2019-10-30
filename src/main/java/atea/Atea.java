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

  private final Database db;

  public Atea(Connection conn) {
      this.db = new Database(conn);
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

    Strings S = new Strings();
    String[] words = S.getWords(text);

    for(int i=0; i<words.length; i++) {
      ArrayList<Expansion> expansions = predictExpansions(words, i);

      // If expansions are found this word is believed to be an abbreviation
      if(expansions.size() > 0) {
        abbrs.add( new Abbreviation(words[i], i, expansions) );
      }
    }

    Collections.sort(abbrs);
    return abbrs;
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
    Strings S = new Strings();
    String[] chunks = S.getFullSplit(text);

    int word_index = 0;
    for(int i=0, a=0; i<chunks.length; i++) {
      // delimeter
      output += chunks[i++];

      if(i >= chunks.length) {
        break;
      }

      // if word is an abbreviation
      int k = word_index;
      if(Arrays.stream(abbr_indexes).anyMatch(j -> j == k)) {
        output += abbrs.get(a++).getExpansions().get(0).getValue();
      } else {
        output += chunks[i];
      }
      word_index++;
    }

    return output;
  }

  public String explain(String text) {
    String output = "";
    Abbreviation abbr;

    ArrayList<Abbreviation> abbrs = getAbbreviations(text);
    int[] abbr_indexes = getIndexes(abbrs);
    Strings S = new Strings();
    String[] chunks = S.getFullSplit(text);

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
  public ArrayList<Expansion> predictExpansions(String[] text, int abbr_index) {
    return predictExpansions(text, abbr_index, .95);
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
  public ArrayList<Expansion> predictExpansions(String[] text, int abbr_index, double threshold) {
    ArrayList<Expansion> expansions = new ArrayList<Expansion>();
    String abbr = text[abbr_index];

    int id = db.abbreviationExists(abbr);
    if(id != -1) {
      float confidence;
      ArrayList<Expansion> possibleExpansions = db.getExpansions(id);

      for( Expansion expansion: possibleExpansions) {
          confidence = expansionConfidence(text, abbr_index, expansion.getId());
          if(confidence >= threshold) {
              expansion.setConfidence(confidence);
              expansions.add( expansion );
          }
      }
      Collections.sort(expansions);
    }

    return expansions;
  }

  private float expansionConfidence(String[] text, int abbr_index, int expansion_id) {
    // How far left and right from the abbr_index to look at the context of its use
    int context_size = 4;

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

      int word_id = db.wordExistsForExpansion(context[i], expansion_id);
      if(word_id != -1) {
        p = contextMatchAt(context, abbr_context_index, expansion_id, word_id, i);
        contextMatchScore[j++] = p;
        total += p;
      }
    }

    float averageContextMatchScore = total / contextMatchScore.length;

    return averageContextMatchScore;
  }

  private float contextMatchAt(String[] context, int abbr_index, int expansion_id, int word_id, int index) {

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
      probability = probabilityAt(expansion_id, distance, word_id);

      // Do not penalize the conext match score if the word is not found in the sorrounding columns
      if(probability == 0 && i != index) {
        continue;
      }

      total_score += probability / (Math.abs(distance) + 1);
      total_possible_score += 1.0 / (Math.abs(distance) + 1);
    }

    return total_score / total_possible_score;
  }

  private float probabilityAt(int expansion_id, int distance, int word_id) {
    int occurrences = db.countWordOccurrencesAtDistance(expansion_id, distance, word_id);
    int total = db.countAllOccurrencesAtDistance(expansion_id, distance);

    if(total == 0) {
      return 0;
    }
    return (float) occurrences / total;
  }
}
