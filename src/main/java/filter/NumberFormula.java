/*
 * CardFilterConstantFormula.java
 *
 * Created on 17 kwiecie? 2005, 14:58
 */

package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author Dominik
 */
public final class NumberFormula extends LeafFormula {
  
  private String value;
  
  /**
   * Creates a new instance of CardFilterConstantFormula
   * @param value 
   */
  public NumberFormula(String value) {
    this.value = value;
  }
  
  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    return value;
  }
  
  /**
   * 
   * @return 
   */
  public String getValue() {
    return value;
  }

  /**
   * 
   * @return 
   */
  public int size() {
    return 1;
  }
  
  /**
   * 
   * @param g 
   * @param c 
   * @param index 
   * @return 
   */
  public String get(PropertyReaderSource prs, GamePiece gp, int index) {
    return value;
  }
  
}
