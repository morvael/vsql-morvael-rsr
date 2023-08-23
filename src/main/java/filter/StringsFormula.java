/*
 * StringsFormula.java
 *
 * Created on 17 kwiecie? 2005, 16:44
 */

package filter;

import VASSAL.counters.GamePiece;
import java.util.*;

/**
 *
 * @author morvael
 */
public final class StringsFormula extends LeafFormula {
  
  private ArrayList<String> strings = new ArrayList<String>();
  
  /** Creates a new instance of StringsFormula */
  public StringsFormula() {
  }
  
  /**
   * 
   * @param string 
   */
  public void add(String string) {
    strings.add(string.substring(1, string.length()-1));
  }
  
  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String string : strings) {
      sb.append("'");
      sb.append(string.replaceAll("\\x27", "\\\\'"));
      sb.append("',");
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length()-1);
    }
    return sb.toString();
  }
  
  /**
   * 
   * @return 
   */
  public ArrayList<String> getStrings() {
    return strings;
  }

  /**
   * 
   * @return 
   */
  public int size() {
    return strings.size();
  }
  
  /**
   * 
   * @param g 
   * @param c 
   * @param index 
   * @return 
   */
  public String get(PropertyReaderSource prs, GamePiece gp, int index) {
    return strings.get(index);
  }
  
}
