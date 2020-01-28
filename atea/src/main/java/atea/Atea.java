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
  private final String[] commonWords;

  /**
   *
   * @param host        The hostname of the database
   * @param username    The username of the database user
   * @param password    The password of the database user
   * @throws SQLException
   */
  public Atea(String host, String username, String password) throws SQLException {
    this.db = new Database(host, username, password);
    commonWords = db.getCommonWords();
  }

  /**
   * Finds all words in a String of text whose characters match an abbreviation in the database.
   * The returned items may or may not actually be abbreviations. This method makes no predictions
   * on whether the words are being used as abbreviations or not in the text.
   * @param text      The text to look for potential abbreviations in
   * @return          An ArrayList of Abbreviation objects whose expansions property is empty
   */
  public ArrayList<Abbreviation> findPotentialAbbreviations(String text) {
    ArrayList<Abbreviation> abbrs = new ArrayList<>();
    SplitString ss = new SplitString(text);

    String[] words = ss.getWords();
    for(int i=0; i<words.length; i++) {

      try {
        int id = db.abbreviationExists(words[i]);
        if(id != -1 ) {
          abbrs.add(new Abbreviation(id, words[i], ss, i));
        }
      }
      catch(SQLException ex) {
        ex.printStackTrace();
        return new ArrayList<>();
      }
    }

    Collections.sort(abbrs);
    return abbrs;
  }

  /**
   * Finds all words in a String of text that ATEA believes to be an abbreviation.
   * @param text      The text to look for potential abbreviations in
   * @return          An ArrayList of Abbreviation objects whose expansions property contains a list
   *                  of Expansion objects sorted from most likely to least likely expansion
   */
  public ArrayList<Abbreviation> predictAbbreviations(String text) {
    ArrayList<Abbreviation> potentialAbbrs = findPotentialAbbreviations(text);
    ArrayList<Abbreviation> abbrs = new ArrayList<>();

    for (Abbreviation abbr : potentialAbbrs) {
      ArrayList<Expansion> expansions = predictExpansions(abbr);

      // If expansions are found this word is believed to be an abbreviation
      if(expansions.size() > 0) {
        abbr.setExpansions(expansions);
        abbrs.add( abbr );
      }
    }

    Collections.sort(abbrs);
    return abbrs;
  }

  /**
   * Adds an example of an abbreviation being used to the database. This method also accepts examples
   * of when an abbreviation is NOT being used as an abbreviation. Example: "it" could stand for
   * "information technology" or could be the word "it" (It is sunny.)
   * @param abbr        An Abbreviation object with its value property set to the abbreviation
   * @param expansion   An Expansion object with its value property set to what the abbreviation stands
   *                    for. For an example when a word is not an abbreviation set the Expansion object's
   *                    FILL IN HERE property to FILL IN HERE
   * @return            true on success, false on failure
   */
  // TODO - How to pass not expansion example. Should expansion object have id set to -1, value to ''?
  public boolean addExample(Abbreviation abbr, Expansion expansion) {
    try {
      db.insertExample(abbr, expansion.getValue());
    }
    catch(SQLException ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Returns the text with the most likely expansion for each abbreviation substituted for the
   * abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      The expanded text.
   */
  public String expand(String text) {
    ArrayList<Abbreviation> abbrs = predictAbbreviations(text);
    SplitString ss = new SplitString(text);
    String[] chunks = ss.getFullSplit();

    return buildString(chunks, abbrs);
  }

  /**
   * Returns the text with the most likely expansion for each abbreviation put in parenthesis next to
   * the abbreviation.
   * @param  text The text to look for abbreviations in.
   * @return      The explained text.
   */
  public String explain(String text) {
    ArrayList<Abbreviation> abbrs = predictAbbreviations(text);
    SplitString ss = new SplitString(text);
    String[] chunks = ss.getFullSplit();

    return buildString(chunks, abbrs, true);
  }

  /**
   * Returns buildString(String[] chunks, ArrayList<Abbreviation> abbrs, boolean explain)
   * passing false for the explain parameter.
   * @param chunks  An ordered array of Strings where the sequence of elements is(delimiter, word,
   *                delimiter, word, ...)
   *                Expects the array to always start with and end with a delimiter.
   * @param abbrs   The Abbreviation objects found in the String[] chunks.
   * @return        The built string.
   */
  private String buildString(String[] chunks, ArrayList<Abbreviation> abbrs) {
    return buildString(chunks, abbrs, false);
  }

  /**
   * Builds a String from a String[]. Expects every even element to be a delimiter and every odd element
   * to be a word. Abbreviations passed in the ArrayList should have their index property set to
   * correspond with their index in the set of only words. Given String[] chunks = {"", "An", " ",
   * "abbr", " ", "example", "."}, the Abbreviation abbr has the index 3 in chunks, however in the set
   * of only words ( {"An", "abbr"} ), abbr has the index 1.
   * @param chunks  An ordered array of Strings where the sequence of elements is(delimiter, word,
   *                delimiter, word, ...)
   *                Expects the array to always start with and end with a delimiter.
   * @param abbrs   The Abbreviation objects found in the String[] chunks.
   * @param explain On true, will output the expansion of the abbreviation in parenthesis next to the abbreviation.
   *                On false, will substitute the expansion for the abbreviation.
   * @return        The built string.
   */
  // TODO - Should this be removed and replaced with a SplitString method?
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
   * Predicts what an Abbreviation might stand for.
   * @param abbr  An Abbreviation object to predict expansions for.
   * @return      An ArrayList of Expansion objects
   */
  private ArrayList<Expansion> predictExpansions(Abbreviation abbr) {
    ArrayList<Expansion> expansions = new ArrayList<>();

    try {
      int abbr_id = abbr.getId();
      if(abbr_id != -1) {
        expansions = db.getExpansions(abbr_id);
        expansions.add(new Expansion(-1, ""));

        double totalKeywordScore = 0;
        double[] scores = new double[expansions.size()];
        double[] weights = new double[expansions.size()];
        int i=0;
        for( Expansion expansion : expansions) {
          try {
            scores[i] = db.getExpansionBaseProbability(abbr_id, expansion.getId());
            weights[i] = getKeywordScore(abbr, expansion);
            i++;
          }
          catch(SQLException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
          }
        }

        double[] weightedScores = weightScores(scores, weights);
        for(i=0; i<expansions.size(); i++) {
          expansions.get(i).setConfidence(weightedScores[i]);
        }
      }
    }
    catch(SQLException ex) {
      ex.printStackTrace();
      return new ArrayList<>();
    }

    return expansions;
  }

  /**
   * Adjust scores according to given weights. Weighted scores will sum to the same total as
   * the original scores.
   * Example:     Scores    Weights     Weighted Scores
   *               .21      360          .603...
   *               .64      61           .311...
   *               .15      72           .086...
   *      TOTAL:  1.00                  1.00
   *
   * @param scores    The scores to be weighted
   * @param weights   The weights to apply to the scores
   * @return          The weighted scores
   * @throws NumberFormatException
   */
  private double[] weightScores(double[] scores, double[] weights) throws NumberFormatException {
    if(scores.length != weights.length) {
      throw new NumberFormatException("Length of scores and weights do not match.");
    }

    float weightsTotal = 0;
    for(double weight : weights) { weightsTotal += weight; }

    if(weightsTotal == 0) {
      return weights;
    }

    double[] weightedScores = new double[scores.length];
    float weightedScoresTotal = 0;
    for(int i=0; i<weights.length; i++) {
      weightedScores[i] = scores[i] * weights[i] / weightsTotal;
      weightedScoresTotal += weightedScores[i];
    }

    for(int i=0; i<weightedScores.length; i++) {
      weightedScores[i] /= weightedScoresTotal;
    }

    return weightedScores;

  }

  /**
   * Determines if a word is a keyword. To be considered a keyword it must not be found in the
   * commonWords array, which contains the n-most commonly used words in the English language.
   * @param word     The word to check
   * @return         True if the word is a keyword, False otherwise
   */
  private boolean isKeyword(String word) {
    word = word.toLowerCase();

    // If word is found in commonWords, it is not a keyword.
    for(int i=0; i<commonWords.length; i++) {
      if(commonWords[i].toLowerCase() == word) {
        return false;
      }
    }

    // Else, it is a keyword.
    return true;
  }

  /**
   * Scores an abbreviation/expansion combo based on the number of keywords used within the context
   * of this abbreviation that match keywords found in examples in ATEA's database.
   * @param abbr        The abbreviation to get a keyword score for
   * @param expansion   The expansion to get a keyword score for
   * @return
   */
  private float getKeywordScore(Abbreviation abbr, Expansion expansion) {
    String[] words = abbr.getText().getWords();
    float keywordTotalScore = 0;
    for(int i=0; i<words.length; i++) {
      if(i == abbr.getIndex() || !isKeyword(words[i])) {
        continue;
      }
      float thisKeywordScore;
      try {
        thisKeywordScore = db.getExpansionKeywordScore(abbr.getId(), expansion.getId(), words[i]);
        // TODO - split this into multiple db method calls to the math/logic is happening inside
        // this class instead of inside the db class
      }
      catch(SQLException ex) {
        ex.printStackTrace();
        return -1;
      }
      keywordTotalScore += thisKeywordScore;
    }

    return keywordTotalScore;
  }
}
