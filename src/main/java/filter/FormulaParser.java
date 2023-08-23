/*
 * FormulaParser.java
 *
 * Created on 17 kwiecie? 2005, 16:11
 */

package filter;

import java.util.*;

/**
 *
 * @author Dominik
 */
public final class FormulaParser {
  
  private TrueFormula trueFormula = new TrueFormula();
  private Parser parser = new Parser();
  private int current;
  private HashMap<String, Formula> formulas = new HashMap<String, Formula>();
  
  /** Creates a new instance of FormulaParser */
  public FormulaParser() {
  }
  
  /**
   * 
   * @param text 
   * @return 
   */
  public Formula parse(String text) {
    if ((text == null) || (text.trim().length() == 0)) {
      return trueFormula;
    }
    if (formulas.containsKey(text)) {
      return formulas.get(text);
    } else {
      current = 0;
      if (parser.parse(text)) {
        Formula ff = parseSentence();
        formulas.put(text, ff);
        return ff;
      } else {
        return null;
      }
    }
  }
  
  private Token getCurrent() {
    try {
      return parser.getList().get(current);
    } catch (Throwable t) {
      error("Unexpected end of line");
      return null;
    }
  }

  private void match(TokenType tt, String text) {
    if ((getCurrent().getType() != tt) || (getCurrent().getText().equals(text) == false)) {
      error("Expected " + tt + " \"" + text + "\"");
    }
  }
    
  private void match(TokenType tt) {
    if (getCurrent().getType() != tt) {
      error("Expected " + tt);
    }
  }
  
  private boolean isEnd() {
    return current >= parser.getList().size();
  }
    
  private void next() {
    if (isEnd() == false) {
      current++;
    }
  }
  
  private void error(String comment) {
    if (isEnd() == true) {
      throw new ParsingError(comment, parser.getList().get(current-1), true);
    } else {
      throw new ParsingError(comment, getCurrent(), false);
    }
  }
                
  private Formula parseSentence() {
    return formulaA();
  }
  
  private Formula formulaA() {
    Formula result = formulaB();
    while (((isEnd() == false)) && (getCurrent().isOneOf("||"))) {
      next();
      result = new Operator2Formula(result, Operator2.Or, formulaB());
    }
    return result;
  }

  private Formula formulaB() {
    Formula result = formulaC();
    while (((isEnd() == false)) && (getCurrent().isOneOf("&&"))) {
      next();
      result = new Operator2Formula(result, Operator2.And, formulaC());
    }
    return result;
  }
  
  private Formula formulaC() {
    Formula result;
    if (getCurrent().isOneOf("!")) {
      next();
      result = new Operator1Formula(Operator1.Not, formulaD());
    } else {
      result = formulaD();
    }
    return result;
  }
  
  private Formula formulaD() {
    Formula result;
    if (getCurrent().isOneOf("(")) {
      next();
      result = formulaA();
      match(TokenType.Operator, ")"); next();
      result = new Operator1Formula(Operator1.Brackets, result);
    } else {
      result = formulaE();
    }
    return result;
  }
  
  private Formula formulaE() {
    String o;
    Operator2 op;
    Formula result = formulaF();
    while (((isEnd() == false)) && (getCurrent().isOneOf("==", "!=", "<", ">", "<=", ">=", "><"))) {
      o = getCurrent().getText();
      if (o.equals("!=")) {
        op = Operator2.NotEquals;
      } else
      if (o.equals("<")) {
        op = Operator2.LessThan;
      } else
      if (o.equals(">")) {
        op = Operator2.MoreThan;
      } else
      if (o.equals("<=")) {
        op = Operator2.LessOrEqualThan;
      } else
      if (o.equals(">=")) {
        op = Operator2.MoreOrEqualThan;
      } else
      if (o.equals("><")) {
        op = Operator2.Contains;
      } else {
        op = Operator2.Equals;
      }
      next();
      result = new Operator2Formula(result, op, formulaF());
    }
    return result;
  }
  
  private Formula formulaF() {
    Formula result;
    if (getCurrent().getType() == TokenType.Identifier) {
      result = formulaF1();
    } else
    if (getCurrent().getType() == TokenType.String) {
      result = formulaF2();
    } else {
      match(TokenType.Number);
      result = new NumberFormula(getCurrent().getText());
      next();
    }
    return result;
  }
  
  private Formula formulaF1() {
    AttributesFormula result = new AttributesFormula();
    result.add(getCurrent().getText());
    next();
    while ((isEnd() == false) && (getCurrent().isOneOf(","))) {
      match(TokenType.Operator, ",");
      next();
      match(TokenType.Identifier);
      result.add(getCurrent().getText());
      next();
    }
    return result;
  }

  private Formula formulaF2() {
    StringsFormula result = new StringsFormula();
    result.add(getCurrent().getText());
    next();
    while ((isEnd() == false) && (getCurrent().isOneOf(","))) {
      match(TokenType.Operator, ",");
      next();
      match(TokenType.String);
      result.add(getCurrent().getText());
      next();
    }
    return result;
  }
  
}
