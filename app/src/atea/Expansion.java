package atea;

public final class Expansion implements Comparable<Expansion>
{
  private String value;
  private float confidence;

  public Expansion(String value, float confidence) {
    this.value = value;
    this.confidence = confidence;
  }
  
  public String getValue() {
  	return value;
  }

  // Sorts Expansion objects from greatest confidence to least confident
  @Override
  public int compareTo(Expansion a) {
    if(a.confidence > this.confidence) {
      return 1;
    } else if(a.confidence < this.confidence) {
      return -1;
    }

    return 0;
  }

  @Override
  public String toString() {
    return "[" + this.value + ", " + this.confidence + "]";
  }
}
