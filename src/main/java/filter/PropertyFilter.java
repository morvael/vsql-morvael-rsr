package filter;

import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.counters.PropertiesPieceFilter;
import java.util.HashMap;

/**
 *
 * @author Dominik
 */
public class PropertyFilter extends Filter implements FilterCheck {

  protected HashMap<String, PieceFilter> formulas = new HashMap<String, PieceFilter>();
  protected PieceFilter f;

  public PropertyFilter() {
  }

  @Override
  protected FilterCheck getFilter(String filter) {
    if (formulas.containsKey(filter)) {
      f = formulas.get(filter);
    } else {
      f = PropertiesPieceFilter.parse(filter);
      formulas.put(filter, f);
    }
    return this;
  }

  public boolean accept(GamePiece gp) {
    return f.accept(gp);
  }

}