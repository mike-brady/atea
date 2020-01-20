package atea;
import java.util.ArrayList;

public final class Abbreviation implements Comparable<Abbreviation> {
    private int id;
  private String value;
  private SplitString text;
  private int index;
  private ArrayList<Expansion> expansions;

  Abbreviation(int id, String value, SplitString text, int index) {
      this.id = id;
    this.value = value;
    this.text = text;
    this.index = index;
    this.expansions = new ArrayList<Expansion>();
  }

  Abbreviation(int id, String value, SplitString text, int index, ArrayList<Expansion> expansions) {
      this.id = id;
      this.value = value;
      this.text = text;
      this.index = index;
    this.expansions = expansions;
  }

    public int getId() {
        return id;
    }

    public String getValue() { return this.value; }

  public SplitString getText() { return text; }

  public int getIndex() { return index; }

  public ArrayList<Expansion> getExpansions() { return expansions; }

    public void setId(int id) {
        this.id = id;
    }

    public Expansion getBestExpansion() {
    Expansion best = null;
    for(Expansion expansion : this.expansions) {
      if(best == null || expansion.getConfidence() > best.getConfidence()) {
        best = expansion;
      }
    }

    return best;
  }

  public void setExpansions(ArrayList<Expansion> expansions) {
    this.expansions = expansions;
  }

  // Sorts Abbreviation objects from first occurrence (smallest index) to last occurrence
  @Override
  public int compareTo(Abbreviation a) {
    return this.index - a.index;
  }

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
