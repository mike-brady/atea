package atea;

final class Expansion implements Comparable<Expansion>
{
  private final int id;
  private final String value;
  private float confidence;

  public Expansion(int id, String value) {
    this.id = id;
    this.value = value;
  }

  public Expansion(int id, String value, float confidence) {
    this.id = id;
    this.value = value;
    this.confidence = confidence;
  }

  public void setConfidence(float confidence) { this.confidence = confidence; }

  public int getId() { return id; }

  public String getValue() { return value; }

  public float getConfidence() { return confidence; }

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
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }

    Expansion e = (Expansion) obj;

    if(e.getId() == id && e.getValue().equals(value) && e.getConfidence() == confidence) {
      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    return "[id:" + this.id + ", value:" + this.value + ", confidence:" + this.confidence + "]";
  }
}