/*
 * CardFilerOperator1Formula.java
 *
 * Created on 17 kwiecie? 2005, 14:47
 */

package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author Dominik
 */
public final class Operator1Formula extends Formula {
  
  private Operator1 o;
  private Formula f;
  
  /**
   * Creates a new instance of CardFilerOperator1Formula
   * @param o 
   * @param f 
   */
  public Operator1Formula(Operator1 o, Formula f) {
    this.o = o;
    this.f = f;
  }

  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    try {
      switch (o) {
        case Not : return "!" + f.toString();
        case Brackets : return "(" + f.toString() + ")";
      }
      throw new Error("CardFilterOperator1Formula error!");
    } catch (Throwable t) {
      throw new Error("Incorrect formula!");
    }
  }
  
  /**
   * 
   * @return 
   */
  public Operator1 getOperator() {
    return o;
  }
  
  /**
   * 
   * @return 
   */
  public Formula getFormula() {
    return f;
  }
  
  /**
   * 
   * @param g 
   * @param c 
   * @return 
   */
  public boolean eval(PropertyReaderSource prs, GamePiece gp) {
    switch (o) {
      case Not : return !f.eval(prs, gp);
      case Brackets : return f.eval(prs, gp);
    }
    return false;
  }
  
}
