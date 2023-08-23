/*
 * CardFilterAttributeFormula.java
 *
 * Created on 17 kwiecie? 2005, 14:57
 */

package filter;

import VASSAL.counters.GamePiece;
import java.util.ArrayList;

/**
 *
 * @author Dominik
 */
public final class AttributesFormula extends LeafFormula {
  
  private ArrayList<String> names = new ArrayList<String>();
  private ArrayList<PropertyReader> readers = new ArrayList<PropertyReader>();
  
  /** Creates a new instance of CardFilterAttributeFormula */
  public AttributesFormula() {
  }
  
  /**
   * 
   * @param name 
   */
  public void add(String name) {
    names.add(name);
    readers.add(null);
  }
  
  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String name : names) {
      sb.append(name);
      sb.append(",");
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
  public ArrayList<String> getNames() {
    return names;
  }
  
  /**
   * 
   * @return 
   */
  public int size() {
    return names.size();
  }
  
  /**
   * 
   * @param g 
   * @param c 
   * @param index 
   * @return 
   */
  public String get(PropertyReaderSource prs, GamePiece gp, int index) {
    String name = names.get(index);
    PropertyReader pr = readers.get(index);
    if (pr == null) {
      pr = prs.getPropertyReader(name);
      readers.set(index, pr);
    }
    return pr.getProperty(gp);
  }
  
 
  
}
