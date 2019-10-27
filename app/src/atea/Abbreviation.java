package atea;
import java.util.ArrayList;

public final class Abbreviation implements Comparable<Abbreviation>
{
  private String value;
  private int index;
  private ArrayList<Expansion> expansions;

  public Abbreviation(String value) {
    this.value = value;
    this.index = -1;
  }

  public Abbreviation(String value, int index) {
    this.value = value;
    this.index = index;
  }

  public Abbreviation(String value, int index, ArrayList<Expansion> expansions) {
    this.value = value;
    this.index = index;
    this.expansions = expansions;
  }

  public String getValue() {
    return this.value;
  }

  public int getIndex() {
  	return index;
  }

  public ArrayList<Expansion> getExpansions() {
  	return expansions;
  }

  public int length() {
    return this.value.length();
  }

  public int getEndIndex() {
    return this.index + this.length();
  }

  // Sorts Abbreviation objects from first occurance (smallest index) to last occurance
  @Override
  public int compareTo(Abbreviation a) {
    return this.index - a.index;
  }

  @Override
  public String toString() {
    String output = "\n";
    output += this.value + " > ";
    for(int i=0; i<expansions.size(); i++) {
      if(i > 0) {
        output += ", ";
      }
      output += this.expansions.get(i);
    }
    output += "\n";

    return output;
  }
}
