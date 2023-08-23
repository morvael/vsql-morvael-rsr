/*
 * TrueFormula.java
 *
 * Created on 23 January 2006, 21:37
 *
 */

package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author morvael
 */
public final class TrueFormula extends Formula {
  
  /** Creates a new instance of TrueFormula */
  public TrueFormula() {
  }
  
  public boolean eval(PropertyReaderSource prs, GamePiece gp) {
    return true;
  }  
  
}
