/*
 * CardFilterParserToken.java
 *
 * Created on 17 kwiecie? 2005, 15:01
 */

package filter;

/**
 *
 * @author morvael
 */
public final class Token {
  
  private String text;
  private TokenType type;
  private int start;
  private int end;
    
  /**
   * 
   * @param text 
   * @param type 
   * @param start 
   * @param end 
   */
  public Token(String text, TokenType type, int start, int end) {
    this.text = text;
    this.type = type;
    this.start = start;
    this.end = end;
  }
  
  /**
   * 
   * @return 
   */
  public String getText() {
    return text;
  }
  
  /**
   * 
   * @return 
   */
  public TokenType getType() {
    return type;
  }
  
  /**
   * 
   * @return 
   */
  public int getStart() {
    return start;
  }
  
  /**
   * 
   * @return 
   */
  public int getEnd() {
    return end;
  }
  
  /**
   * 
   * @param args 
   * @return 
   */
  public boolean isOneOf(String... args) {
    for (String arg : args) {
      if (text.equals(arg)) {
        return true;
      }
    }
    return false;
  }
  
}
