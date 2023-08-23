package filter;

import VASSAL.counters.GamePiece;
import automation.GameProperties;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 *
 * @author Dominik
 */
public class DefaultPropertyReaderSource implements PropertyReaderSource {

  private HashMap<String, PropertyReader> customReaders = new HashMap<String, PropertyReader>();
  private HashMap<String, PropertyReader> defaultReaders = new HashMap<String, PropertyReader>();

  public DefaultPropertyReaderSource() {
    
  }

  public void addReader(String name, PropertyReader reader) {
    customReaders.put(name, reader);
  }

  public void addReader(final String name) {
    customReaders.put(name, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return GameProperties.getStringProperty(gp, name);
      }
    });
  }

  public void addReader(Class<?> c) {
    for (Field f : c.getFields()) {
      if ((Modifier.isStatic(f.getModifiers())) && (f.getName().startsWith("T_"))) {
        try {
          addReader(String.valueOf(f.get(null)));
        } catch (IllegalAccessException ex) {

        }
      }
    }
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
