/*
 * CardFilterParser.java
 *
 * Created on 17 kwiecie? 2005, 15:01
 */

package filter;

import java.util.*;
import java.util.regex.*;

/**
 *
 * @author Dominik
 */
public final class Parser {
  
  private static final Pattern identRegex = Pattern.compile("^([_a-zA-Z]([_a-zA-Z0-9])*|[_a-zA-Z]([_a-zA-Z0-9])*\\.([_a-zA-Z]([_a-zA-Z0-9])*))$");
  private static final Pattern numberRegex = Pattern.compile("^-?(0|([1-9]([0-9])*(\\.([0-9])+)?)|0\\.([0-9])+)$");
  private static final Pattern operatorRegex = Pattern.compile("^(&&|\\|\\||==|!=|<|>|<=|>=|><|!|\\(|\\)|,|\\*|\\{|\\})$");

  private static final Pattern identStartChar = Pattern.compile("^[_a-zA-Z]$");
  private static final Pattern numberStartChar = Pattern.compile("^[-0-9]$");
  private static final Pattern operatorStartChar = Pattern.compile("^[&\\|=!<>\\(\\),\\*\\{\\}\\\']$");

  private static final Pattern identChar = Pattern.compile("^[\\._a-zA-Z0-9]$");
  private static final Pattern numberChar = Pattern.compile("^[0-9\\.]$");
  private static final Pattern operatorChar = Pattern.compile("^[&\\|=<]$");
  
  private static final LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
  
  /**
   * 
   * @param from 
   * @param to 
   */
  public static void addReplacement(String from, String to) {
    replacements.put(from, to);
  }
  
  private char[] line;
  private int head;
  private StringBuilder sb = new StringBuilder();
  private ArrayList<Token> list = new ArrayList<Token>();

  /** Creates a new instance of CardFilterParser */
  public Parser() {
  }
  
  private char getCurrentChar() {
    return line[head];
  }
		
  private String getCurrentString() {
    return String.valueOf(line[head]);
  }
  
  private String getToken() {
    return sb.toString();
  }
  
  private boolean isEnd() {
    return (line == null) || (head >= line.length);
  }
  
  /**
   * 
   * @return 
   */
  public ArrayList<Token> getList() {
    return list;
  }
  
  /**
   * 
   * @param text 
   * @return 
   */
  public boolean parse(String text) {
    for (String from : replacements.keySet()) {
      text = text.replaceAll(from, replacements.get(from));
    }
    line = text.toCharArray();
    head = 0;
    list.clear();
    while (isEnd() == false) {
      list.add(nextToken());
    }
    return list.size() > 0;
  }
  
  private void skipWhitespaces() {
    while ((isEnd() == false) && (Character.isWhitespace(getCurrentChar()))) {
      head++;
    }
  }
  
  private void readChar() {
    sb.append(line[head++]);
  }
  
  private boolean isWhitespace() {
    return (isEnd()) || (Character.isWhitespace(getCurrentChar()));
  }
  
  private boolean isWhitespaceOrOperator() {
    return isWhitespace() || !identStartChar.matcher(getCurrentString()).matches() || !numberStartChar.matcher(getCurrentString()).matches();
  }
  
  private Token nextToken() {
    sb.setLength(0);
    skipWhitespaces();
    int start = head + 1;
    boolean ignoreNext = false;
    TokenType tt = TokenType.Unknown;
    if (getCurrentChar() == '\'') {
      readChar();
      while ((isEnd() == false) && ((getCurrentChar() != '\'') || (ignoreNext == true))) {
        ignoreNext = getCurrentChar() == '\\';
        if (ignoreNext) {
          head++;
        } else {
          readChar();
        }
      }
      readChar();
      tt = TokenType.String;
    } else {
      readChar();
      if (operatorStartChar.matcher(getToken()).matches()) {
        while ((isEnd() == false) && (operatorChar.matcher(getCurrentString()).matches()) && (operatorRegex.matcher(getToken() + getCurrentChar()).matches())) {
          readChar();
        }
        if (operatorRegex.matcher(getToken()).matches()) {
          tt = TokenType.Operator;
        }
      } else
      if (numberStartChar.matcher(getToken()).matches()) {
        while ((isEnd() == false) && (numberChar.matcher(getCurrentString()).matches())) {
          readChar();
        }
        if ((isWhitespaceOrOperator()) && (numberRegex.matcher(getToken()).matches())) {
          tt = TokenType.Number;
        }
      } else
      if (identStartChar.matcher(getToken()).matches()) {
        while ((isEnd() == false) && (identChar.matcher(getCurrentString()).matches())) {
          readChar();
        }
        if ((isWhitespaceOrOperator()) && (identRegex.matcher(getToken()).matches())) {
          tt = TokenType.Identifier;
        }
      }
    }
    skipWhitespaces();
    return new Token(getToken(), tt, start, head);
  }

}
