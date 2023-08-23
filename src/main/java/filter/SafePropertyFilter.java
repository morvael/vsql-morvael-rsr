package filter;

import VASSAL.counters.PieceFilter;
import VASSAL.counters.PropertiesPieceFilter;
import java.util.HashMap;

/**
 *
 * @author morvael
 */
public class SafePropertyFilter extends Filter {

  protected HashMap<String, PieceFilter> formulas = new HashMap<String, PieceFilter>();

  public SafePropertyFilter() {
  }

  @Override
  protected FilterCheck getFilter(String filter) {
    PieceFilter f;
    if (formulas.containsKey(filter)) {
      f = formulas.get(filter);
    } else {
      f = PropertiesPieceFilter.parse(filter);
      formulas.put(filter, f);
    }
    return new PropertyFilterCheck(f);
  }

}