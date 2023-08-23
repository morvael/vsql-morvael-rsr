/*
 * CardFilterFormula.java
 *
 * Created on 17 kwiecie? 2005, 14:46
 */

package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author Dominik
 */
public abstract class Formula {
  
  /** Creates a new instance of CardFilterFormula */
  public Formula() {
  }
  
  /**
   * 
   * @param g 
   * @param c 
   * @return 
   */
  public abstract boolean eval(PropertyReaderSource prs, GamePiece gp);
  
}
