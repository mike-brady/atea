package atea;
import java.util.ArrayList;

public final class Abbreviation implements Comparable<Abbreviation>
{
  private final String value;
  private final int index;
  private final Context context;
  private ArrayList<Expansion> expansions;

  Abbreviation(String value, int index, Context context) {
    this.value = value;
    this.index = index;
    this.context = context;
    this.expansions = new ArrayList<Expansion>();
  }

  Abbreviation(String value, int index, Context context, ArrayList<Expansion> expansions) {
    this.value = value;
    this.index = index;
    this.context = context;
    this.expansions = expansions;
  }

  public String getValue() { return this.value; }

  public int getIndex() { return index; }

  public Context getContext() { return context; }

  public ArrayList<Expansion> getExpansions() { return expansions; }

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
    StringBuilder output = new StringBuilder("[value:" + this.value);
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
