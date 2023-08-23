package rsr;

import VASSAL.counters.GamePiece;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import terrain.HexRef;
import terrain.TerrainMapShaderCost;

/**
 *
 * @author morvael
 * @since 2009-05-20
 */
public abstract class Cache implements RsrConstants {

  private HashMap<String, HashSet<HexRef>> map1;
  private HashMap<String, HashMap<HexRef, TerrainMapShaderCost>> map2;
  private HashMap<String, RsrAllowedMovement> map3;
  private HashMap<String, ArrayList<GamePiece>> map4;

  public Cache() {

  }

  public void clear() {
    if (map1 != null) {
      map1.clear();
    }
    if (map2 != null) {
      map2.clear();
    }
    if (map3 != null) {
      map3.clear();
    }
    if (map4 != null) {
      map4.clear();
    }
  }

  /*
  public void invalidate() {
    invalidate(null);
  }
  
  public void invalidate(String id) {
    if (map1 != null) {
      map1.remove(id);
    }
    if (map2 != null) {
      map2.remove(id);
    }
    if (map3 != null) {
      map3.remove(id);
    }
  }
  */

  private void update1(String id, HashSet<HexRef> data) {
    if (map1 == null) {
      map1 = new HashMap<String, HashSet<HexRef>>();
    }
    map1.put(id, data);
  }

  private void update2(String id, HashMap<HexRef, TerrainMapShaderCost> data) {
    if (map2 == null) {
      map2 = new HashMap<String, HashMap<HexRef, TerrainMapShaderCost>>();
    }
    map2.put(id, data);
  }

  private void update3(String id, RsrAllowedMovement data) {
    if (map3 == null) {
      map3 = new HashMap<String, RsrAllowedMovement>();
    }
    map3.put(id, data);
  }

  private void update4(String id, ArrayList<GamePiece> data) {
    if (map4 == null) {
      map4 = new HashMap<String, ArrayList<GamePiece>>();
    }
    map4.put(id, data);
  }

  protected HashSet<HexRef> get1() {
    return get1(null);
  }

  protected HashSet<HexRef> get1(String id) {
    HashSet<HexRef> result = null;
    if (map1 != null) {
      result = map1.get(id);
    }
    if ((result == null) && ((map1 == null) || (map1.containsKey(id) == false))) {
      result = calculate1(id);
      update1(id, result);
    }
    return result;
  }

  protected HashMap<HexRef, TerrainMapShaderCost> get2() {
    return get2(null);
  }

  protected HashMap<HexRef, TerrainMapShaderCost> get2(String id) {
    HashMap<HexRef, TerrainMapShaderCost> result = null;
    if (map2 != null) {
      result = map2.get(id);
    }
    if ((result == null) && ((map2 == null) || (map2.containsKey(id) == false))) {
      result = calculate2(id);
      update2(id, result);
    }
    return result;
  }

  protected RsrAllowedMovement get3() {
    return get3(null);
  }

  protected RsrAllowedMovement get3(String id) {
    RsrAllowedMovement result = null;
    if (map3 != null) {
      result = map3.get(id);
    }
    if ((result == null) && ((map3 == null) || (map3.containsKey(id) == false))) {
      result = calculate3(id);
      update3(id, result);
    }
    return result;
  }

  protected ArrayList<GamePiece> get4() {
    return get4(null);
  }

  protected ArrayList<GamePiece> get4(String id) {
    ArrayList<GamePiece> result = null;
    if (map4 != null) {
      result = map4.get(id);
    }
    if ((result == null) && ((map4 == null) || (map4.containsKey(id) == false))) {
      result = calculate4(id);
      update4(id, result);
    }
    return result;
  }

  protected HashSet<HexRef> calculate1(String id) {
    return null;
  }

  protected HashMap<HexRef, TerrainMapShaderCost> calculate2(String id) {
    return null;
  }

  protected RsrAllowedMovement calculate3(String id) {
    return null;
  }

  protected ArrayList<GamePiece> calculate4(String id) {
    return null;
  }

}
