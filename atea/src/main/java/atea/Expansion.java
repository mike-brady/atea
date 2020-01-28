package atea;

/**
 * An object representing an expansion of an abbreviation.
 */
public final class Expansion implements Comparable<Expansion>
{
  private final int id;
  private final String value;
  private double confidence;

  /**
   *
   * @param id          The id of the expansion in the database
   * @param value       The value of the expansion
   */
  public Expansion(int id, String value) {
    this.id = id;
    this.value = value;
    this.confidence = 0;
  }

  /**
   *
   * @param id          The id of the expansion in the database
   * @param value       The value of the expansion
   * @param confidence  A value from 0 - 1 representing the confidence in this abbreviation
   *                    // TODO - Remove this property and create an ArrayList as a property
   *                    // of the Abbreviation class to store the confidences in.
   */
  public Expansion(int id, String value, double confidence) {
    this.id = id;
    this.value = value;
    this.confidence = confidence;
  }

  int getId() { return id; }

  public String getValue() { return value; }

  public double getConfidence() { return confidence; }

  public void setConfidence(double confidence) { this.confidence = confidence; }

  // Sorts Expansion objects from greatest confidence to least confident
  /**
   * Sorts Expansion objects from greatest confidence to least confidente
   * @param a           The Expansion object to compare to this one
   * @return            A negative number if this Expansion comes first
   *                    A positive number if a comes first
   *                    0 if both a and this Expansion have the same index
   */
  @Override
  public int compareTo(Expansion a) {
    if(a.confidence > this.confidence) {
      return 1;
    } else if(a.confidence < this.confidence) {
      return -1;
    }

    return 0;
  }

  /**
   * Checks if an Expansion object is equivalent to this once
   * @param obj         The Expansion object to compare to this one
   * @return            True if they are equivalent
   *                    False if they are not equivalent
   */
  @Override
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }

    if (!(obj instanceof Expansion)) {
      return false;
    }

    Expansion e = (Expansion) obj;

    return e.getId() == id && e.getValue().equals(value) && e.getConfidence() == confidence;
  }

  /**
   * Creates a string representation of the Expansion
   * @return A string representation of the Expansion
   */
  @Override
  public String toString() {
    return "[id:" + this.id + ", value:" + this.value + ", confidence:" + this.confidence + "]";
  }
}
