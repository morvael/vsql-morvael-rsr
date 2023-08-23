package rsr;

import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;
import filter.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import terrain.HexRef;
import terrain.TerrainHexGrid;

/**
 *
 * @author morvael
 * @since 2009-05-21
 */
public final class PieceCache extends Cache {

  private RsrAdvanceBase core;
  private Map mainMap;
  private TerrainHexGrid mainGrid;
  private Filter filter;

  private boolean invalid = true;
  private HashMap<HexRef, ArrayList<GamePiece>> axisPieceGroups;
  private HashMap<HexRef, ArrayList<GamePiece>> sovietPieceGroups;
  private HashMap<HexRef, ArrayList<GamePiece>> allPieces;
  private HashMap<HexRef, StackInfo> stackInfo = new HashMap<HexRef, StackInfo>();

  public PieceCache(RsrAdvanceBase core, Map mainMap, TerrainHexGrid mainGrid, Filter filter) {
    this.core = core;
    this.mainMap = mainMap;
    this.mainGrid = mainGrid;
    this.filter = filter;
  }

  @Override
  public void clear() {
    super.clear();
    invalid = true;
    axisPieceGroups = null;
    sovietPieceGroups = null;
    allPieces = null;
    stackInfo.clear();
  }

  private void validate() {
    if (invalid) {
      axisPieceGroups = filter.filterPieceGroups(mainMap, mainGrid, "Side=='" + SIDE_AXIS + "'");
      sovietPieceGroups = filter.filterPieceGroups(mainMap, mainGrid, "Side=='" + SIDE_SOVIET + "'");
      allPieces = filter.filterPieceGroups(mainMap, mainGrid);
      invalid = false;
    }
  }

  @Override
  protected HashSet<HexRef> calculate1(String id) {
    validate();    
    char mode = id.charAt(0);
    String[] data = id.substring(1).split("_");
    String side = data[0];
    int range = Integer.parseInt(data[1]);
    HashMap<HexRef, ArrayList<GamePiece>> pieceGroups = side.equals(SIDE_AXIS) ? axisPieceGroups : sovietPieceGroups;
    HashSet<HexRef> result = new HashSet<HexRef>();
    switch (mode) {
      case 'O' :
        for (HexRef hr : pieceGroups.keySet()) {
          if (core.canContestOwnSpace(pieceGroups.get(hr))) {
            result.add(hr);
          }
        }
        break;
      case 'C' :
        for (HexRef hr : pieceGroups.keySet()) {
          if (core.canContestNeighbourSpace1(pieceGroups.get(hr))) {
            result.add(hr);
          }
        }
        break;
      case 'Z' :
        ArrayList<GamePiece> stack;
        for (HexRef hr : pieceGroups.keySet()) {
          stack = pieceGroups.get(hr);
          if (core.canContestNeighbourSpace1(stack)) {
            HexRef[] neighbours = mainGrid.getAdjacentHexes(hr);
            for (int i = 0; i < 6; i++) {
              if (neighbours[i] == null) {
                break;
              }
              if (core.canContestNeighbourSpace2(stack, hr, neighbours[i])) {
                result.add(neighbours[i]);
              }
            }
          }
        }
        break;
      case 'R' :
        for (HexRef hr : pieceGroups.keySet()) {
          mainGrid.getHexesInRange(result, hr, range);
        }
        break;
      case 'P' :
        for (HexRef hr : pieceGroups.keySet()) {
          if (core.canContestOwnSpace(pieceGroups.get(hr))) {
            mainGrid.getHexesInRange(result, hr, range);
          }
        }
        break;
      case 'D' :
        for (GamePiece gp : getSecurityDivisions()) {
          mainGrid.getHexesInRange(result, mainGrid.getHexPos(gp.getPosition()), 2);
        }
        for (GamePiece gp : getPartisans()) {
          result.add(mainGrid.getHexPos(gp.getPosition()));
        }
        result.addAll(getHexesOccupiedByEnemy(SIDE_SOVIET));
        break;
    }
    return result;
  }

  @Override
  protected ArrayList<GamePiece> calculate4(String id) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    char mode = id.charAt(0);
    switch (mode) {
      case 'L' : result.addAll(filter.filterPieces(mainMap, "Type=='Leader'")); break;
      case 'S' : result.addAll(filter.filterPieces(mainMap, "Type=='SiegeArtillery'")); break;
      case 'H' : result.addAll(filter.filterPieces(mainMap, "Type=='Headquarters'&&Nationality=='" + id.substring(1) + "'")); break;
      case 'D' : result.addAll(filter.filterPieces(mainMap, "Type=='SecurityDivision'")); break;
      case 'P' : result.addAll(filter.filterPieces(mainMap, "Type=='Partisans'")); break;
    }
    return result;
  }

  public HashSet<HexRef> getHexesOccupiedBy(String side) {
    return get1("O" + side + "_0");
  }

  public HashSet<HexRef> getHexesOccupiedByContesting(String side) {
    return get1("C" + side + "_0");
  }
  
  public HashSet<HexRef> getHexesInZOC(String side) {
    return get1("Z" + side + "_0");
  }

  public HashSet<HexRef> getHexesInRange(String side, int range) {
    return get1("R" + side + "_" + range);
  }

  public HashSet<HexRef> getHexesOccupiedByEnemy(String side) {
    return get1("O" + (side.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS) + "_0");
  }

  public HashSet<HexRef> getHexesOccupiedByContestingEnemy(String side) {
    return get1("C" + (side.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS) + "_0");
  }

  public HashSet<HexRef> getHexesInZOCEnemy(String side) {
    return get1("Z" + (side.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS) + "_0");
  }

  /**
   * Set of hexes in range of enemy units.
   * @param side
   * @param range
   * @return
   */
  public HashSet<HexRef> getHexesInRangeAnyEnemy(String side, int range) {
    return get1("R" + (side.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS) + "_" + range);
  }

  /**
   * Set of hexes in range of enemy units that contest own space.
   * @param side
   * @param range
   * @return
   */
  public HashSet<HexRef> getHexesInRangeOccupiedEnemy(String side, int range) {
    return get1("P" + (side.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS) + "_" + range);
  }

  public HashSet<HexRef> getHexesProtectedFromPartisans() {
    return get1("D" + SIDE_AXIS + "_2");
  }
  
  public ArrayList<GamePiece> getPiecesIn(HexRef hr) {
    validate();
    return allPieces.get(hr);
  }

  public StackInfo getStackInfo(HexRef hr) {
    validate();
    if (stackInfo.containsKey(hr)) {
      return stackInfo.get(hr);
    } else {
      StackInfo si = new StackInfo(core, getPiecesIn(hr));
      stackInfo.put(hr, si);
      return si;
    }
  }

  public int getLeaderFirepowerValue(HashSet<HexRef> dH) {
    int max = 0;
    for (GamePiece gp : get4("L")) {
      if (mainGrid.getHexesInRange(mainGrid.getHexPos(gp.getPosition()), RsrTrait.getRange(gp)).containsAll(dH)) {
        max = Math.max(max, RsrTrait.getFirepower(gp));
      }
    }
    return max;
  }

  public int getSiegeArtilleryFirepowerValue(HashSet<HexRef> dH) {
    HexRef hr;
    for (GamePiece gp : get4("S")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      if ((RsrTrait.getFired(gp) == 0) && (mainGrid.getHexesInRange(hr, RsrTrait.getRange(gp)).containsAll(dH))) {
        if ((getHexesInZOCEnemy(SIDE_AXIS).contains(hr) == false) || (filter.filterPieces(axisPieceGroups.get(hr), T_ATTACKER + ">0").size() > 0)) {
          core.commandBaseSetProperty(gp, T_HL_ACTION, 0, T_FIRED, 1);
          return 4;
        }
      }
    }
    return 0;
  }

  public ArrayList<GamePiece> getHeadquarters(String nationality) {
    return get4("H" + nationality);
  }

  public ArrayList<GamePiece> getSecurityDivisions() {
    return get4("D");
  }

  public ArrayList<GamePiece> getPartisans() {
    return get4("P");
  }

  public Set<HexRef> getOccupiedHexes() {
    validate();
    return allPieces.keySet();
  }

}
