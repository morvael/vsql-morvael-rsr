package rsr;

import java.util.Set;

import terrain.HexRef;
import terrain.TerrainMapShaderRule;
import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;
import filter.Filter;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;

/**
 * 
 * @author morvael
 */
public abstract class RsrMapShaderBase extends TerrainMapShaderRule implements RsrConstants {


  protected int scenarioNumber;
  protected int turnNumber;
  protected String phaseName;
  protected PieceCache pieceCache;
  protected RsrAdvance core;
  protected Filter filter;
  protected boolean isWinter;

  protected RsrMapShaderBase() {
    super();
  }

  @Override
  public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
    super.reset(map, grid, terrainMap, params, piece);
    scenarioNumber = RsrGlobal.getScenario();
    core = RsrAdvance.getInstance();
    turnNumber = core.getTurnNumber();
    phaseName = core.getPhaseName();
    pieceCache = core.getPieceCache();
    filter = core.getFilter();
    isWinter = core.isWinter(turnNumber);
  }

  protected boolean isRailroadSpace(HexRef hex) {
    HexRef[] neighbours = grid.getAdjacentHexes(hex);
    for (int i = 0; i < 6; i++) {
      if (neighbours[i] == null) {
        break;
      }
      if (Filter.isAny(terrainMap.getLineTerrainName(hex, neighbours[i]), LINE_RAILROAD, LINE_CHONGARCAUSEWAY)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isArctic(HexRef hex) {
    return terrainMap.getAttributeTerrainValue(hex, TAG_ARCTIC).equals(TAG_ARCTIC_TRUE);
  }

  protected double getSupplyEnterCost(HexRef fromHex, HexRef toHex, String side, Set<HexRef> partisanHexes) {
    String tt = terrainMap.getHexTerrainName(fromHex);
    String ttt = terrainMap.getHexTerrainName(toHex);
    String et = terrainMap.getEdgeTerrainName(fromHex, toHex);
    String lt = terrainMap.getLineTerrainName(fromHex, toHex);
    boolean isArctic = isArctic(fromHex);
    boolean isRailroad = Filter.isAny(lt, LINE_RAILROAD, LINE_CHONGARCAUSEWAY);
    boolean isAxis = side.equals(SIDE_AXIS);
    if (core.isImpassable(et)) {
      return Double.MAX_VALUE;
    } else
    if (core.isLakeSea(et)) {
      if ((isRailroad == false) && ((isAxis) || (isWinter == false) || (lt.equals(LINE_FROZENSURFACE) == false))) {
        return Double.MAX_VALUE;
      }
    } else
    if (core.isMajorRiver(et)) {
      if (isRailroad == false) {
        return Double.MAX_VALUE;
      }
    }
    if ((tt.equals(HEX_WATER)) || (ttt.equals(HEX_WATER))) {
      return Double.MAX_VALUE;
    }
    double cost;
    if (isRailroad) {
      cost = 0.5;
    } else if (((tt.equals(HEX_SWAMP)) && (isWinter == false)) || (isArctic)) {
      cost = 2.0;
    } else {
      cost = 1.0;
    }
    if ((isAxis) && (partisanHexes != null) && (partisanHexes.contains(fromHex))) {
      cost *= 2.0;
    }
    return cost;
  }




}
