package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author morvael
 * @since 2009-05-22
 */
public interface FilterCheck {

  boolean accept(GamePiece gp);
  
}
