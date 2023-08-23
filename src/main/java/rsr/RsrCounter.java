package rsr;

import VASSAL.counters.GamePiece;
import java.util.HashMap;

public final class RsrCounter {

  private RsrCounter() {

  }

  private static HashMap<String, String> mapString = new HashMap<String, String>();
  private static HashMap<String, Double> mapDouble = new HashMap<String, Double>();
  private static HashMap<String, Integer> mapInteger = new HashMap<String, Integer>();
  private static HashMap<String, Boolean> mapBoolean = new HashMap<String, Boolean>();

	public static String getStringProperty(GamePiece gp, String name, String def, boolean constant) {
    String key = gp.getId() + "_" + name;
		String result;
    if ((constant == false) || (mapString.containsKey(key) == false)) {
      try {
        result = gp.getProperty(name).toString();
      } catch (Exception e) {
        result = def;
      }
      if (constant) {
        mapString.put(key, result);
      }
    } else {
      result = mapString.get(key);
    }
		return result;
	}
	
	public static double getDoubleProperty(GamePiece gp, String name, double def, boolean constant) {
    String key = gp.getId() + "_" + name;
		double result;
    if ((constant == false) || (mapDouble.containsKey(key) == false)) {
      try {
        result = Double.parseDouble(gp.getProperty(name).toString());
      } catch (Exception e) {
        result = def;
      }
      if (constant) {
        mapDouble.put(key, result);
      }
    } else {
      result = mapDouble.get(key);
    }
		return result;
	}
	
	public static int getIntProperty(GamePiece gp, String name, int def, boolean constant) {
    String key = gp.getId() + "_" + name;
		int result;
    if ((constant == false) || (mapInteger.containsKey(key) == false)) {
      try {
        result = Integer.parseInt(gp.getProperty(name).toString());
      } catch (Exception e) {
        result = def;
      }
      if (constant) {
        mapInteger.put(key, result);
      }
    } else {
      result = mapInteger.get(key);
    }
		return result;
	}
	
	public static boolean getBooleanProperty(GamePiece gp, String name, boolean def, boolean constant) {
    String key = gp.getId() + "_" + name;
		boolean result;
    if ((constant == false) || (mapBoolean.containsKey(key) == false)) {
      try {
        result = Boolean.parseBoolean(gp.getProperty(name).toString());
      } catch (Exception e) {
        result = def;
      }
      if (constant) {
        mapBoolean.put(key, result);
      }
    } else {
      result = mapBoolean.get(key);
    }
		return result;
	}

}
