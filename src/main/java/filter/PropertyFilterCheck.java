package filter;

import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;

/**
 *
 * @author morvael
 */
public class PropertyFilterCheck implements FilterCheck {

  protected PieceFilter f;

  public PropertyFilterCheck(PieceFilter f) {
    this.f = f;
  }

  public boolean accept(GamePiece gp) {
    return f.accept(gp);
  }

}
