package rsr;

import VASSAL.build.module.map.boardPicker.board.MapGrid.BadCoords;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import terrain.HexRef;
import terrain.TerrainHexGrid;

/**
 *
 * @author Dominik
 */
public class CombatHexList implements Iterable<String> {

  protected LinkedHashSet<String> hexes = new LinkedHashSet<String>();

  public CombatHexList(String list) {
    if (list.length() > 0) {
      for (String s : list.split(",")) {
        hexes.add(s);
      }
    }
  }

  public int size() {
    return hexes.size();
  }

  public boolean contains(String hex) {
    return hexes.contains(hex);
  }

  public boolean add(String hex) {
    return hexes.add(hex);
  }
  
  public boolean remove(String hex) {
    return hexes.remove(hex);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String s : hexes) {
      sb.append(s);
      sb.append(",");
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length()-1);
    }
    return sb.toString();
  }

  public HashSet<HexRef> getHexes() {
    TerrainHexGrid grid = RsrAdvance.getInstance().getMainGrid();
    HashSet<HexRef> result = new HashSet<HexRef>();
    for (String s : hexes) {
      try {
        result.add(grid.getHexPos(grid.getLocation(s)));
      } catch (BadCoords e) {

      }
    }
    return result;
  }

  public Iterator<String> iterator() {
    return hexes.iterator();
  }

}
