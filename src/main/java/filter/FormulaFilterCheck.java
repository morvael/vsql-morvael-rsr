package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author morvael
 */
public class FormulaFilterCheck implements FilterCheck {

  protected PropertyReaderSource prs;
  protected Formula f;

  public FormulaFilterCheck(PropertyReaderSource prs, Formula f) {
    this.prs = prs;
    this.f = f;
  }

  public boolean accept(GamePiece gp) {
    return f.eval(prs, gp);
  }

}
