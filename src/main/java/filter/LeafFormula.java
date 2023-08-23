/*
 * LeafFormula.java
 *
 * Created on 17 kwiecie? 2005, 17:09
 */

package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author Dominik
 */
public abstract class LeafFormula extends Formula {
  
  /** Creates a new instance of LeafFormula */
  public LeafFormula() {
  }
  
  /**
   * 
   * @param g 
   * @param c 
   * @return 
   */
  public boolean eval(PropertyReaderSource prs, GamePiece gp) {
    return false;
  }
  
  /**
   * 
   * @return 
   */
  public abstract int size();
  /**
   * 
   * @param g 
   * @param c 
   * @param index 
   * @return 
   */
  public abstract String get(PropertyReaderSource prs, GamePiece gp, int index);
  
}
