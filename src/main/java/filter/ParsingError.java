/*
 * FilterParsingException.java
 *
 * Created on 17 kwiecie? 2005, 16:20
 */

package filter;

/**
 *
 * @author Dominik
 */
public class ParsingError extends Error {
  
  private static final long serialVersionUID = 1L;
  private String comment;
  private Token ft;
  private boolean after;
  
  /**
   * Creates a new instance of FilterParsingException
   * @param comment 
   * @param ft 
   * @param after 
   */
  public ParsingError(String comment, Token ft, boolean after) {
    this.comment = comment;
    this.ft = ft;
    this.after = after;
  }
  
  /**
   * 
   * @return 
   */
  public String getMessage() {
    if (after) {
      return comment + " (after \"" + ft.getText() + "\")";
    } else {
      return comment + " (\"" + ft.getText() + "\", column " + ft.getStart() + "-" + ft.getEnd() + ")";
    }
  }
  
}
