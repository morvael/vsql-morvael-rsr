/*
 * CardFilterOperator2Formula.java
 *
 * Created on 17 kwiecie? 2005, 14:51
 */

package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author morvael
 */
public final class Operator2Formula extends Formula {
  
  private Formula f1;
  private Operator2 o;
  private Formula f2;
  
  /**
   * Creates a new instance of CardFilterOperator2Formula
   * @param f1 
   * @param o 
   * @param f2 
   */
  public Operator2Formula(Formula f1, Operator2 o, Formula f2) {
    this.f1 = f1;
    this.o = o;
    this.f2 = f2;
  }

  /**
   * 
   * @return 
   */
  @Override
  public String toString() {
    try {
      switch (o) {
        case Or : return f1.toString() + "||" + f2.toString();
        case And : return f1.toString() + "&&" + f2.toString();
        case Equals : return f1.toString() + "==" + f2.toString();
        case NotEquals : return f1.toString() + "!=" + f2.toString();
        case LessThan : return f1.toString() + "<" + f2.toString();
        case MoreThan : return f1.toString() + ">" + f2.toString();
        case LessOrEqualThan : return f1.toString() + "<=" + f2.toString();
        case MoreOrEqualThan : return f1.toString() + ">=" + f2.toString();
        case Contains : return f1.toString() + "><" + f2.toString();
      }
      throw new Error("CardFilterOperator2Formula error!");
    } catch (Throwable t) {
      throw new Error("Incorrect formula!");
    }
  }
  
  /**
   * 
   * @return 
   */
  public Formula getFormula1() {
    return f1;
  }

  /**
   * 
   * @return 
   */
  public Operator2 getOperator() {
    return o;
  }
  
  /**
   * 
   * @return 
   */
  public Formula getFormula2() {
    return f2;
  }

  /**
   * 
   * @param g 
   * @param c 
   * @return 
   */
  public boolean eval(PropertyReaderSource prs, GamePiece gp) {
    switch (o) {
      case Or : return f1.eval(prs, gp) || f2.eval(prs, gp);
      case And : return f1.eval(prs, gp) && f2.eval(prs, gp);
      case Equals : return testEquals(prs, gp);
      case NotEquals : return testNotEquals(prs, gp);
      case LessThan : return testLess(prs, gp);
      case MoreThan : return testMore(prs, gp);
      case LessOrEqualThan : return testLessOrEqualThan(prs, gp);
      case MoreOrEqualThan : return testMoreOrEqualThan(prs, gp);
      case Contains : return testContains(prs, gp);
    }    
    return false;
  }
  
  private boolean testEquals(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (l1.get(prs, gp, i).equals(l2.get(prs, gp, j))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean testNotEquals(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (l1.get(prs, gp, i).equals(l2.get(prs, gp, j))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean testLess(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (parseDouble(l1.get(prs, gp, i)) >= parseDouble(l2.get(prs, gp, j))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean testMore(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (parseDouble(l1.get(prs, gp, i)) <= parseDouble(l2.get(prs, gp, j))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean testLessOrEqualThan(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (parseDouble(l1.get(prs, gp, i)) > parseDouble(l2.get(prs, gp, j))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean testMoreOrEqualThan(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (parseDouble(l1.get(prs, gp, i)) < parseDouble(l2.get(prs, gp, j))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean testContains(PropertyReaderSource prs, GamePiece gp) {
    LeafFormula l1 = (LeafFormula)f1;
    LeafFormula l2 = (LeafFormula)f2;
    for (int i=0; i<l1.size(); i++) {
      for (int j=0; j<l2.size(); j++) {
        if (l1.get(prs, gp, i).indexOf(l2.get(prs, gp, j)) != -1) {
          return true;
        }
      }
    }
    return false;
  }
  
  private double parseDouble(String v) {
    try {
      return Double.parseDouble(v);
    } catch (Exception e) {
      return 0.0d;
    }
  }

  
}
