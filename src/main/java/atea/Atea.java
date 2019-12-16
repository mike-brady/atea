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
  private int context_width = 8;

  /**
   * @param db_credentials    {"host", "username", "password"}
   */
  public Atea(String[] db_credentials) throws SQLException {
    this.db = new Database(db_credentials);
  }

  public ArrayList<Abbreviation> getAbbreviations(String text) {
    return getAbbreviations(text, true);
  }

  /**
   * Find abbreviations used in the text. Get each abbreviations possible expansions and the confidence level for each
   * possible expansion.
   * @param  text The text to look for abbreviations in.
   * @return      Abbreviation objects sorted from first occurrence to last in the text
   */
  public ArrayList<Abbreviation> getAbbreviations(String text, boolean predict) {
    ArrayList<Abbreviation> abbrs = new ArrayList<>();

    String[] words = StringSplitter.getWords(text);
    String[] delims = StringSplitter.getDelimiters(text);

    for(int i=0; i<words.length; i++) {
      Context context = StringSplitter.getContext(words, delims, i, context_width);
      Abbreviation abbr = new Abbreviation(words[i], i, context);
      if(predict) {
        ArrayList<Expansion> expansions = predictExpansions(abbr);

        // If expansions are found this word is believed to be an abbreviation
        if(expansions.size() > 0) {
          abbr.setExpansions(expansions);
          abbrs.add( abbr );
        }
      } else {
        if(db.abbreviationExists(words[i]) != -1) {
          abbrs.add( abbr );
        }
      }

    }

    Collections.sort(abbrs);
    return abbrs;
  }

  /**
   * Adds an example of an abbreviation being used to the database.
   * @param context       An ordered array of words from a sentence where an abbreviation is used.
   * @param abbr_index    The index in context where the abbreviation is.
   * @param expansion_id  The id of the expansion for this abbreviation.
   * @return              True on success, false on failure.
   */
  public boolean addAbbreviationExample(String[] context, int abbr_index, int expansion_id) {
    return db.insertAbbreviationExample(context, abbr_index, expansion_id);
  }

  /**
   * Adds an example of an abbreviation being used to the database.
   * @param context       An ordered array of words from a sentence where an abbreviation is used.
   * @param abbr_index    The index in context where the abbreviation is.
   * @param expansion     The expansion for this abbreviation.
   * @return              True on success, false on failure.
   */
  public boolean addAbbreviationExample(String[] context, int abbr_index, String expansion) {
    return db.insertAbbreviationExample(context, abbr_index, expansion);
  }

  /**
   * Returns the text with the most likely expansion for each abbreviation substituted for the abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      The expanded text.
   */
  public String expand(String text) {
    ArrayList<Abbreviation> abbrs = getAbbreviations(text);
    String[] chunks = StringSplitter.getFullSplit(text);

    return buildString(chunks, abbrs);
  }

  /**
   * Returns the text with the most likely expansion for each abbreviation put in parenthesis next to the abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      The explained text.
   */
  String explain(String text) {
    ArrayList<Abbreviation> abbrs = getAbbreviations(text);
    String[] chunks = StringSplitter.getFullSplit(text);

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
   * @return          An ArrayList of Expansion objects. Will be an empty list if the word at index is not an
   *                  abbreviation or none of the possible expansions met the confidence threshold given the context of
   *                  words surrounding the abbreviation.
   */
  public ArrayList<Expansion> predictExpansions(Abbreviation abbr) {
    return predictExpansions(abbr, .95);
  }

  /**
   * Determines if the word at the specified index of text is an abbreviation and what it's possible expansions are
   * given the context of words surrounding the word in question. Only accepts possible expansions with a confidence
   * score >= the provided threshold.
   * @param threshold The minimum confidence level of which to accept a possible expansion.
   * @return          An ArrayList of Expansion objects. Will be an empty list if the word at index is not an
   *                  abbreviation or none of the possible expansions met the confidence threshold given the context of
   *                  words surrounding the abbreviation.
   */
  public ArrayList<Expansion> predictExpansions(Abbreviation abbr, double threshold) {
    ArrayList<Expansion> expansions = new ArrayList<>();

    int id = db.abbreviationExists(abbr.getValue());
    if(id != -1) {
      boolean is_always_abbreviation = db.isAlwaysAbbreviation(id);
      float confidence;
      ArrayList<Expansion> possibleExpansions = db.getExpansions(id);
      float total_confidence_score = 0;
      for(Expansion expansion: possibleExpansions) {
        confidence = expansionConfidence(abbr, expansion.getId());

        total_confidence_score += confidence;
        if(is_always_abbreviation || confidence >= threshold) {
          expansion.setConfidence(confidence);
          expansions.add( expansion );
        }
      }

      // adjust confidence scores relative to each other
      if(is_always_abbreviation) {

        // if we have 0 confidence for all expansions, distribute the confidence equally
        if(total_confidence_score == 0) {
          total_confidence_score = expansions.size();
          for(Expansion expansion: expansions) {
            expansion.setConfidence(1 / total_confidence_score);
          }
        }

        else {
          for (Expansion expansion : expansions) {
            expansion.setConfidence(expansion.getConfidence() / total_confidence_score);
          }
        }
      }
      Collections.sort(expansions);
    }

    return expansions;
  }

  /**
   * Determines how likely it is that this expansion is what the abbreviation is meant to stand for. Looks at words on
   * either side of the abbreviation to determine the possibility of the expansion being intended.
   * @param expansion_id  The id of the expansion that is being checked.
   * @return              An confidence score from 0.0 to 1.0, 0 being least confident, 1 being most confident
   */
  private float expansionConfidence(Abbreviation abbr, int expansion_id) {
    Context context = abbr.getContext();
    String[] words = context.getWords();
    int abbr_context_index = context.getWord_index();

    float totalContextScore = 0;
    int totalScores = 0;

    float p;
    // for every word surrounding the abbreviation
    for(int i=0; i<words.length; i++) {
      if(i == abbr_context_index) {
        continue;
      }

      int word_id = db.wordExistsForExpansion(context.getWords()[i], expansion_id);
      if(word_id != -1) {
        p = contextMatchAt(context, expansion_id, word_id, i);
        totalContextScore += p;
        totalScores++;
      }
    }

    if(totalScores == 0) {
      return 0;
    }

    return totalContextScore / totalScores;
  }

  /**
   * Determines the likelihood that a word would appear at its current position relative to the abbreviation
   * when the abbreviation is being used to stand for the given expansion.
   * @param context       An ordered array of Strings of words surrounding and including the abbreviation.
   * @param expansion_id  The id of the expansion that word is being checked against.
   * @param word_id       The id of the word that is being checked.
   * @param index         The index of the word in text.
   * @return              A context match score from 0.0 to 1.0, 0 being no match, 1 being complete match.
   */
  private float contextMatchAt(Context context, int expansion_id, int word_id, int index) {

    // Only look on the left or right side of the abbr
    int abbr_index = context.getWord_index();
    String[] words = context.getWords();
    int start = 0;
    int end = abbr_index;
    if(index > abbr_index) {
      start = abbr_index + 1;
      end = words.length;
    }

    float probability_sum = 0;
    float total_possible_score = 0;
    float probability;
    for(int i=start; i<end; i++) {
      int distance = i - abbr_index;
      probability = probabilityAt(expansion_id, distance, word_id);

      // Do not penalize the context match score if the word is not found in the surrounding columns
      if(probability == 0 && i != index) {
        continue;
      }

      float weight = 1f / (Math.abs(distance) + 1);
      probability_sum += probability * weight;
      total_possible_score += weight;
    }

    return probability_sum / total_possible_score;
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
