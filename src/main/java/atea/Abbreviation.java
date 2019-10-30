package atea;
import java.util.ArrayList;

final class Abbreviation implements Comparable<Abbreviation>
{
  private final String value;
  private final int index;
  private final ArrayList<Expansion> expansions;

  public Abbreviation(String value, int index, ArrayList<Expansion> expansions) {
    this.value = value;
    this.index = index;
    this.expansions = expansions;
  }

  public String getValue() { return this.value; }

  public int getIndex() { return index; }

  public ArrayList<Expansion> getExpansions() { return expansions; }

  // Sorts Abbreviation objects from first occurance (smallest index) to last occurance
  @Override
  public int compareTo(Abbreviation a) {
    return this.index - a.index;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }

    Abbreviation a = (Abbreviation) obj;

    if(a.getValue().equals(value) && a.getIndex() == index && a.getExpansions().equals(expansions)) {
      return true;
    }

    return false;
  }

  @Override
  public String toString() {
    String output = "[value:" + this.value;
    output += ", index:" + this.index;
    output += ", expansions:[";
    for(int i=0; i<expansions.size(); i++) {
      if(i > 0) {
        output += ", ";
      }
      output += this.expansions.get(i);
    }
    output += "]]";

    return output;
  }
}
