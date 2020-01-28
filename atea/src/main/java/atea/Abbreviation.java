package atea;
import java.util.ArrayList;

/**
 * An object representing an abbreviation and the string it was used in.
 */
public final class Abbreviation implements Comparable<Abbreviation> {
  private int id;
  private String value;
  private SplitString text;
  private int index;
  private ArrayList<Expansion> expansions;

  /**
   *
   * @param id          The id of the abbreviation in the database
   * @param value       The abbreviation
   * @param text        The text in which the abbreviation was used
   * @param index       The index representing which word in the text the abbreviation can be found at
   */
  Abbreviation(int id, String value, SplitString text, int index) {
    this.id = id;
    this.value = value;
    this.text = text;
    this.index = index;
    this.expansions = new ArrayList<Expansion>();
  }

  /**
   *
   * @param id          The id of the abbreviation in the database
   * @param value       The abbreviation
   * @param text        The text in which the abbreviation was used
   * @param index       The index representing which word in the text the abbreviation can be found at
   * @param expansions  A list of Expansion objects representing what the abbreviation stands for
   */
  Abbreviation(int id, String value, SplitString text, int index, ArrayList<Expansion> expansions) {
    this.id = id;
    this.value = value;
    this.text = text;
    this.index = index;
    this.expansions = expansions;
  }

  public int getId() { return id; }

  public String getValue() { return this.value; }

  public SplitString getText() { return text; }

  public int getIndex() { return index; }

  public ArrayList<Expansion> getExpansions() { return expansions; }

  public void setId(int id) { this.id = id; }

  public void setExpansions(ArrayList<Expansion> expansions) { this.expansions = expansions; }

  /**
   * Gets the Expansion object with the highest confidence value.
   * @return            An Expansion object
   */
  public Expansion getBestExpansion() {
    Expansion best = null;
    for(Expansion expansion : this.expansions) {
      if(best == null || expansion.getConfidence() > best.getConfidence()) {
        best = expansion;
      }
    }

    return best;
  }

  /**
   * Sorts Abbreviation objects from first occurrence (smallest index) to last occurrence
   * @param a           The Abbreviation object to compare to this one
   * @return            A negative number if this Abbreviation comes first
   *                    A positive number if a comes first
   *                    0 if both a and this Abbreviation have the same index
   */
  @Override
  public int compareTo(Abbreviation a) {
    return this.index - a.index;
  }

  /**
   * Checks if an Abbreviation object is equivalent to this once
   * @param obj         The Abbreviation object to compare to this one
   * @return            True if they are equivalent
   *                    False if they are not equivalent
   */
  @Override
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }

    if (!(obj instanceof Abbreviation)) {
      return false;
    }

    Abbreviation a = (Abbreviation) obj;

    return a.getValue().equals(value) && a.getIndex() == index && a.getExpansions().equals(expansions);
  }

  /**
   * Creates a string representation of the Abbreviation
   * @return A string representation of the Abbreviation
   */
  @Override
  public String toString() {
    StringBuilder output = new StringBuilder("[id:" + this.id);
    output.append(", value:" + this.value);
    output.append(", index:").append(this.index);
    output.append(", expansions:[");
    for(int i=0; i<expansions.size(); i++) {
      if(i > 0) {
        output.append(", ");
      }
      output.append(this.expansions.get(i));
    }
    output.append("]]");

    return output.toString();
  }
}
