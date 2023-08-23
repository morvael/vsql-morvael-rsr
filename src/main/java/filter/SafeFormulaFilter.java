package filter;

/**
 *
 * @author morvael
 */
public class SafeFormulaFilter extends Filter {

  protected FormulaParser formulas = new FormulaParser();
  protected PropertyReaderSource prs;

  public SafeFormulaFilter(PropertyReaderSource prs) {
    this.prs = prs;
  }

  @Override
  protected FilterCheck getFilter(String filter) {
    return new FormulaFilterCheck(prs, formulas.parse(filter));
  }

}
