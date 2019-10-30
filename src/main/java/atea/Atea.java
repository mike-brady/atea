package atea;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
 * Atea - Abbreviated Text Expansion Algorithm
 *
 * Atea takes a String of text and finds all abbreviations used in the text. It determines all the
 * possible expansions of each abbreviation and assigns a confidence level to each expansion based
 * on the context of how the abbreviation was used in the text.
 */
public final class Atea {

  private final Database db;

  public Atea(Connection conn) {
      this.db = new Database(conn);
  }

  /**
   * Find abbreviations used in the text. Get each abbreviations possible expansions and the confidence level for each
   * possible expansion.
   * @param  text The text to look for abbreviations in.
   * @return      Abbreviation objects sorted from first occurrence to last in the text
   */
  public ArrayList<Abbreviation> getAbbreviations(String text) {
    ArrayList<Abbreviation> abbrs = new ArrayList<>();

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
   * Returns the text with the most likely expansion for each abbreviation substituted for the abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      The expanded text.
   */
  public String expand(String text) {
    ArrayList<Abbreviation> abbrs = getAbbreviations(text);
    Strings S = new Strings();
    String[] chunks = S.getFullSplit(text);

    return buildString(chunks, abbrs);
  }

  /**
   * Returns the text with the most likely expansion for each abbreviation put in parenthesis next to the abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      The explained text.
   */
  String explain(String text) {
    ArrayList<Abbreviation> abbrs = getAbbreviations(text);
    Strings S = new Strings();
    String[] chunks = S.getFullSplit(text);

    return buildString(chunks, abbrs, true);
  }

  /**
   * Returns buildString(String[] chunks, ArrayList<Abbreviation> abbrs, boolean explain)
   * passing false for the explain parameter.
   * @param chunks  An ordered array of Strings where the sequence of elements is(delimiter, word, delimiter, word, ...)
   *                Expects the array to always start with and end with a delimiter.
   * @param abbrs   The Abbreviation objects found in the String[] chunks.
   * @return        The built string.
   */
  private String buildString(String[] chunks, ArrayList<Abbreviation> abbrs) {
    return buildString(chunks, abbrs, false);
  }

  /**
   * Builds a String from a String[]. Expects every even element to be a delimiter and every odd element to be a word.
   * Abbreviations passed in the ArrayList should have their index property set to correspond with their index in the
   * set of only words. Given String[] chunks = {"", "An", " ", "abbr", " ", "example", "."}, the Abbreviation abbr has
   * the index 3 in chunks, however in the set of only words ( {"An", "abbr"} ), abbr has the index 1.
   * @param chunks  An ordered array of Strings where the sequence of elements is(delimiter, word, delimiter, word, ...)
   *                Expects the array to always start with and end with a delimiter.
   * @param abbrs   The Abbreviation objects found in the String[] chunks.
   * @param explain On true, will output the expansion of the abbreviation in parenthesis next to the abbreviation.
   *                On false, will substitute the expansion for the abbreviation.
   * @return        The built string.
   */
  private String buildString(String[] chunks, ArrayList<Abbreviation> abbrs, boolean explain) {
    StringBuilder output = new StringBuilder();

    int[] abbr_indexes = getIndexes(abbrs);

    int word_index = 0;
    for(int i=0, a=0; i<chunks.length; i++) {
      // delimiter
      output.append(chunks[i++]);

      if(i >= chunks.length) {
        break;
      }

      // word
      int k = word_index;
      // if word is an abbreviation
      if(Arrays.stream(abbr_indexes).anyMatch(j -> j == k)) {
        if(explain) {
          output.append(chunks[i]);
          output.append(" (").append(abbrs.get(a++).getExpansions().get(0).getValue()).append(")");
        } else {
          output.append(abbrs.get(a++).getExpansions().get(0).getValue());
        }
      } else {
        output.append(chunks[i]);
      }

      word_index++;
    }

    return output.toString();
  }

  /**
   * Gets an array of the index properties of each Abbreviation object.
   * @param abbrs The Abbreviation objects to get the indexes of.
   * @return      An array of the indexes of each Abbreviation object.
   */
  private int[] getIndexes(ArrayList<Abbreviation> abbrs) {
    int[] indexes = new int[abbrs.size()];

    for(int i=0; i<abbrs.size(); i++) {
      indexes[i] = abbrs.get(i).getIndex();
    }

    return indexes;
  }

  /**
   * Returns predictExpansions(String[] text, int index, double threshold) passing .95 for the threshold parameter.
   * @param text      An ordered array of Strings of words.
   * @param index     The index of the word to look for check if it is an abbreviation and to look for expansions of.
   * @return          An ArrayList of Expansion objects. Will be an empty list if the word at index is not an
   *                  abbreviation or none of the possible expansions met the confidence threshold given the context of
   *                  words surrounding the abbreviation.
   */
  public ArrayList<Expansion> predictExpansions(String[] text, int index) {
    return predictExpansions(text, index, .95);
  }

  /**
   * Determines if the word at the specified index of text is an abbreviation and what it's possible expansions are
   * given the context of words surrounding the word in question. Only accepts possible expansions with a confidence
   * score >= the provided threshold.
   * @param text      An ordered array of Strings of words.
   * @param index     The index of the word to look for check if it is an abbreviation and to look for expansions of.
   * @param threshold The minimum confidence level of which to accept a possible expansion.
   * @return          An ArrayList of Expansion objects. Will be an empty list if the word at index is not an
   *                  abbreviation or none of the possible expansions met the confidence threshold given the context of
   *                  words surrounding the abbreviation.
   */
  public ArrayList<Expansion> predictExpansions(String[] text, int index, double threshold) {
    ArrayList<Expansion> expansions = new ArrayList<>();
    String abbr = text[index];

    int id = db.abbreviationExists(abbr);
    if(id != -1) {
      float confidence;
      ArrayList<Expansion> possibleExpansions = db.getExpansions(id);

      for( Expansion expansion: possibleExpansions) {
          confidence = expansionConfidence(text, index, expansion.getId());
          if(confidence >= threshold) {
              expansion.setConfidence(confidence);
              expansions.add( expansion );
          }
      }
      Collections.sort(expansions);
    }

    return expansions;
  }

  /**
   * Determines how likely it is that this expansion is what the abbreviation is meant to stand for. Looks at words on
   * either side of the abbreviation to determine the possibility of the expansion being intended.
   * @param text          An ordered array of Strings of words.
   * @param abbr_index    The index of the abbreviation in text.
   * @param expansion_id  The id of the expansion that is being checked.
   * @return              An confidence score from 0.0 to 1.0, 0 being least confident, 1 being most confident
   */
  private float expansionConfidence(String[] text, int abbr_index, int expansion_id) {
    // How far left and right from the abbr_index to look at the context of its use
    int context_size = 4;

    // Limit the start and end of the context around the abbr to not go out of bounds of text
    int start_index = Math.max(0, abbr_index - context_size);
    int end_index = Math.min(text.length - 1, abbr_index + context_size);
    int abbr_context_index = abbr_index - start_index;
    String[] context = Arrays.copyOfRange(text, start_index, end_index + 1);

    float totalContextScore = 0;
    int totalScores = 0;

    float p;
    // for every word surrounding the abbreviation
    for(int i=0; i<context.length; i++) {
      if(i == abbr_context_index) {
        continue;
      }

      int word_id = db.wordExistsForExpansion(context[i], expansion_id);
      if(word_id != -1) {
        p = contextMatchAt(context, abbr_context_index, expansion_id, word_id, i);
        totalContextScore += p;
        totalScores++;
      }
    }

    return totalContextScore / totalScores;
  }

  /**
   * Determines the likelihood that a word would appear at its current position relative to the abbreviation
   * when the abbreviation is being used to stand for the given expansion.
   * @param context       An ordered array of Strings of words surrounding and including the abbreviation.
   * @param abbr_index    The index of the abbreviation in text.
   * @param expansion_id  The id of the expansion that word is being checked against.
   * @param word_id       The id of the word that is being checked.
   * @param index         The index of the word in text.
   * @return              A context match score from 0.0 to 1.0, 0 being no match, 1 being complete match.
   */
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

      // Do not penalize the context match score if the word is not found in the surrounding columns
      if(probability == 0 && i != index) {
        continue;
      }

      total_score += probability / (Math.abs(distance) + 1);
      total_possible_score += 1.0 / (Math.abs(distance) + 1);
    }

    return total_score / total_possible_score;
  }

  /**
   * Calculates the probability that the given word would appear the the given distance for an expansion
   * @param expansion_id  The id of the word that is being checked.
   * @param distance      The distance of the word that is being checked.
   * @param word_id       The id of the word that is being checked.
   * @return              A probability value from 0.0 to 1.0
   */
  private float probabilityAt(int expansion_id, int distance, int word_id) {
    int occurrences = db.countWordOccurrencesAtDistance(expansion_id, distance, word_id);
    int total = db.countAllOccurrencesAtDistance(expansion_id, distance);

    if(total == 0) {
      return 0;
    }
    return (float) occurrences / total;
  }
}
