package filter;

import VASSAL.counters.GamePiece;
import java.util.HashMap;

/**
 *
 * @author morvael
 */
public class DefaultPropertyReaderSource implements PropertyReaderSource {

  private HashMap<String, PropertyReader> customReaders = new HashMap<String, PropertyReader>();
  private HashMap<String, PropertyReader> defaultReaders = new HashMap<String, PropertyReader>();

  public DefaultPropertyReaderSource() {
    
  }

  public void addReader(String name, PropertyReader reader) {
    customReaders.put(name, reader);
  }

  public PropertyReader getPropertyReader(final String name) {
    if (customReaders.containsKey(name)) {
      return customReaders.get(name);
    } else {
      if (defaultReaders.containsKey(name)) {
        return defaultReaders.get(name);
      } else {
        PropertyReader pr = new PropertyReader() {
          public String getProperty(GamePiece gp) {
            return String.valueOf(gp.getProperty(name));
          }
        };
        defaultReaders.put(name, pr);
        return pr;
      }
    }
  }

}
