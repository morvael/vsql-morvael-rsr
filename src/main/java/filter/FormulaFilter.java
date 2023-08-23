package filter;

import VASSAL.counters.GamePiece;

/**
 *
 * @author morvael
 */
public class FormulaFilter extends Filter implements FilterCheck {

  protected FormulaParser formulas = new FormulaParser();
  protected PropertyReaderSource prs;
  protected Formula f;

  public FormulaFilter(PropertyReaderSource prs) {
    this.prs = prs;
  }

  @Override
  protected FilterCheck getFilter(String filter) {
    f = formulas.parse(filter);
    return this;
  }

  public boolean accept(GamePiece gp) {
    return f.eval(prs, gp);
  }

}
