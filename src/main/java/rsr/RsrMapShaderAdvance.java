package rsr;

import java.util.HashMap;
import java.util.Set;

import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.board.MapGrid.BadCoords;
import VASSAL.counters.GamePiece;

/**
 *
 * @author morvael
 * @since 2009-05-18
 */
public final class RsrMapShaderAdvance extends RsrMapShaderBase {

  private String side;
  private String nationality;
  private String unitType;
  private HexRef startHex;
  private int advanceDistance;
  private Set<HexRef> occupiedByEnemy;
  private Set<HexRef> inEnemyZOC;
  private double mySize;

  public RsrMapShaderAdvance() {
    super();
  }

  @Override
  public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
    super.reset(map, grid, terrainMap, params, piece);
  }

  @Override
  public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
    if (piece == null) {
      return;
    }
    side = RsrTrait.getSide(piece);
    nationality = RsrTrait.getNationality(piece);
    unitType = RsrTrait.getUnitType(piece);
    advanceDistance = RsrTrait.getAdvancing(piece);
    mySize = RsrTrait.getStackPoints(piece);
    occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(side);
    inEnemyZOC = pieceCache.getHexesInZOCEnemy(side);
    //
    try {
      startHex = grid.getHexPos(grid.getLocation(RsrTrait.getCombatHex(piece)));
    } catch (BadCoords ex) {
      startHex = grid.getHexPos(piece.getPosition());
    }
    //
    TerrainMapShaderCost startCost = create(null, true, advanceDistance, true, MODE_ADVANCE, 0);
    TerrainMapShaderCost cost;
    //
    CombatHexList enemyHexes = new CombatHexList(RsrTrait.getAttacker(piece) > 0 ? RsrGlobal.getDefenderHexes() : RsrGlobal.getAttackerHexes());
    for (HexRef toHex : enemyHexes.getHexes()) {
      if (getCrossCost(startHex, toHex, startCost)) {
        cost = new TerrainMapShaderCost(this);
        cost.setFrom(null);
        addHex(selectedHexes, toHex, cost);
      }
    }
  }

  @Override
  public boolean getCrossCost(HexRef fromHex, HexRef toHex, TerrainMapShaderCost currentCost) {
    String tt = terrainMap.getHexTerrainName(toHex);
    String et = terrainMap.getEdgeTerrainName(fromHex, toHex);
    //String lt = terrainMap.getLineTerrainName(fromHex, toHex);
    boolean firstSpace = currentCost.getPointsLeft() == advanceDistance;
    if (core.isImpassable(et)) {
      return result(fromHex, false, 0.0d, false, MODE_ADVANCE, 0);
    }
    if (core.isLakeSea(et)) {
      return result(fromHex, false, 0.0d, false, MODE_ADVANCE, 0);
    }
    if ((core.isMajorRiver(et)) && (firstSpace == false)) {
      return result(fromHex, false, 0.0d, false, MODE_ADVANCE, 0);
    }
    if (tt.equals(HEX_WATER)) {
      return result(fromHex, false, 0.0d, false, MODE_ADVANCE, 0);
    }
    if ((tt.equals(HEX_SWAMP)) && (isWinter == false) && (firstSpace == false)) {
      return result(fromHex, false, 0.0d, false, MODE_ADVANCE, 0);
    }
    if (occupiedByEnemy.contains(toHex)) {
      return result(fromHex, false, 0.0d, false, MODE_ADVANCE, 0);
    }
    if ((inEnemyZOC.contains(toHex)) && (firstSpace == false)) {
      return result(fromHex, true, 0.0d, false, MODE_ADVANCE, 0);
    }
    return result(fromHex, true, currentCost.getPointsLeft() - 1, (core.isStackSizeOK(toHex, side, mySize) == false) || (core.isStackFriendly(toHex, side, nationality, unitType) == false), MODE_ADVANCE, 0);
  }

  @Override
  public void stop() {
  }

  @Override
  public HexRef getBestPathDestination() {
    return null;
  }

  @Override
  public TerrainMapShaderCost getBestCost(HashMap<String, TerrainMapShaderCost> costs) {
    return costs.get(MODE_ADVANCE);
  }
  
}
