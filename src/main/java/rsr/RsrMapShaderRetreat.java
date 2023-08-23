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
public final class RsrMapShaderRetreat extends RsrMapShaderBase {

  private String side;
  //private String nationality;
  //private String unitType;
  private HexRef startHex;
  private int movementMode;
  private int supplyLevel;
  private int retreatDistance;
  private Set<HexRef> occupiedByFriend;
  private Set<HexRef> occupiedByEnemy;
  private Set<HexRef> inEnemyZOC;
  private Set<HexRef> tooClose;

  public RsrMapShaderRetreat() {
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
    //nationality = RsrTrait.getNationality(piece);
    //unitType = RsrTrait.getUnitType(piece);
    movementMode = RsrTrait.getMovementTypeAsInt(piece);
    supplyLevel = RsrTrait.getSupplyLevel(piece);
    retreatDistance = RsrTrait.getRetreating(piece);
    if (retreatDistance <= 0) {
      retreatDistance = RsrTrait.getOverstackRetreating(piece);
      startHex = grid.getHexPos(piece.getPosition());
    } else {
      try {
        startHex = grid.getHexPos(grid.getLocation(RsrTrait.getCombatHex(piece)));
      } catch (BadCoords ex) {
        startHex = grid.getHexPos(piece.getPosition());
      }
    }
    occupiedByFriend = pieceCache.getHexesOccupiedBy(side);
    occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(side);
    inEnemyZOC = pieceCache.getHexesInZOCEnemy(side);
    tooClose = grid.getHexesInRange(startHex, retreatDistance-1);
    addHex(selectedHexes, startHex, create(null, true, retreatDistance, true, MODE_RETREAT, 0));
  }

  @Override
  public boolean getCrossCost(HexRef fromHex, HexRef toHex, TerrainMapShaderCost currentCost) {
    String tt = terrainMap.getHexTerrainName(toHex);
    String et = terrainMap.getEdgeTerrainName(fromHex, toHex);
    String lt = terrainMap.getLineTerrainName(fromHex, toHex);
    int val = currentCost.getValue();
    if (core.isImpassable(et)) {
      return result(fromHex, false, 0.0d, false, MODE_RETREAT, 0);
    }
    if (core.isLakeSea(et)) {
      return result(fromHex, false, 0.0d, false, MODE_RETREAT, 0);
    }
    if ((movementMode == MOVEMENT_RAIL) && (lt.equals(LINE_RAILROAD) == false)) {
      return result(fromHex, false, 0.0d, false, MODE_RETREAT, 0);
    }
    if (tt.equals(HEX_WATER)) {
      return result(fromHex, false, 0.0d, false, MODE_RETREAT, 0);
    }
    if (occupiedByEnemy.contains(toHex)) {
      return result(fromHex, false, 0.0d, false, MODE_RETREAT, 0);
    }
    if ((movementMode == MOVEMENT_MOTORIZED) && (core.isMajorRiver(et))) {
      val += 1;
    }
    if ((inEnemyZOC.contains(toHex)) && (occupiedByFriend.contains(toHex) == false)) {
      if ((supplyLevel == SUPPLY_LACK) || (supplyLevel == SUPPLY_OUT)) {
        return result(fromHex, false, 0.0d, false, MODE_RETREAT, 0);
      } else {
        val += 10;
      }
    }
    return result(fromHex, true, currentCost.getPointsLeft() - 1, (tooClose.contains(toHex))/* || (core.isStackFriendly(toHex, side, nationality, unitType) == false)*/, MODE_RETREAT, val);
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
    return costs.get(MODE_RETREAT);
  }

}
