package rsr;

import java.util.HashMap;
import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;
import filter.Filter;
import java.util.HashSet;

/**
 * 
 * @author morvael
 */
public final class RsrMapShaderMovement extends RsrMapShaderBase {

  private static final int[] EDGE_MINORRIVER_COST = new int[]{1, 2, 0};
  private static final int[] EDGE_MAJORRIVER_COST = new int[]{2, 4, 0};
  private static final int[] HEX_CLEAR_COST = new int[]{1, 1, 0};
  private static final int[] HEX_WOODS_COST = new int[]{1, 2, 0};
  private static final int[] HEX_SWAMP_COST = new int[]{2, 4, 0};
  private static final int[] HEX_MOUNTAIN_COST = new int[]{2, 3, 0};
  private static final int[] HEX_MINORCITY_COST = new int[]{1, 1, 0};
  private static final int[] HEX_MAJORCITY_COST = new int[]{1, 1, 0};
  private static final int[] HEX_SEVASTOPOL_COST = new int[]{2, 4, 0};
  private String side;
  private HexRef startHex;
  private int movementMode;
  private double movement;
  private double maxMovement;
  private double overrunCost;
  //private double mySize;
  //private String nationality;
  private String type;
  private String unitType;
  private boolean isMountain;
  private HashSet<HexRef> occupiedByFriend;
  private HashSet<HexRef> occupiedByEnemy;
  private HashSet<HexRef> inEnemyZOC;
  private HashSet<HexRef> blockedByEnemy;

  public RsrMapShaderMovement() {
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
    movementMode = RsrTrait.getMovementTypeAsInt(piece);
    maxMovement = core.getMovementAllowance(piece);
    movement = RsrTrait.getMovementLeft(piece);
    if (movement > maxMovement) {
      movement = maxMovement;
    }
    overrunCost = core.getOverrunCost(piece);
    //mySize = RsrTrait.getStackPoints(piece);
    type = RsrTrait.getType(piece);
    unitType = RsrTrait.getUnitType(piece);
    isMountain = unitType.indexOf("Mountain") >= 0;
    //find hex control
    occupiedByFriend = pieceCache.getHexesOccupiedByContesting(side);
    occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(side);
    inEnemyZOC = pieceCache.getHexesInZOCEnemy(side);
    blockedByEnemy = type.equals("SecurityDivision") ? pieceCache.getHexesInRangeOccupiedEnemy(SIDE_AXIS, 4) : new HashSet<HexRef>();
    //
    startHex = grid.getHexPos(piece.getPosition());
    addHex(selectedHexes, startHex, create(null, true, movement, true, MODE_MOVEMENT, 0));
  }

  @Override
  public boolean getCrossCost(HexRef fromHex, HexRef toHex, TerrainMapShaderCost currentCost) {
    String tt = terrainMap.getHexTerrainName(toHex);
    String et = terrainMap.getEdgeTerrainName(fromHex, toHex);
    String lt = terrainMap.getLineTerrainName(fromHex, toHex);
    if (occupiedByEnemy.contains(toHex)) {
      //prevent from overrunning while not starting next to enemy units - this would distort stack size as well as would place units coming from afar in random hex with equal cost to enter next to overrun hex
      if (fromHex.equals(startHex) == false) {
        return result(fromHex, false, 0.0d, false, MODE_MOVEMENT, 0);
      }
      if (core.isStackSizeOK(fromHex, side, 0.0d) == false) {
        return result(fromHex, false, 0.0d, false, MODE_MOVEMENT, 0);
      }
      if ((tt.equals(HEX_WATER)) || ((tt.equals(HEX_SWAMP)) && (isWinter == false)) || (tt.equals(HEX_MOUNTAIN)) || (core.isMajorCity(tt))) {
        return result(fromHex, false, 0.0d, false, MODE_MOVEMENT, 0);
      }
      if ((core.isMajorRiver(et)) || ((core.isMinorRiver(et)) && ((turnNumber > 1) || (side.equals(SIDE_SOVIET)))) || ((core.isLakeSea(et)) && ((side.equals(SIDE_AXIS)) || (isWinter == false) || (lt.equals(LINE_FROZENSURFACE) == false) || (movementMode != RsrTrait.MOVEMENT_NONMOTORIZED)))) {
        return result(fromHex, false, 0.0d, false, MODE_MOVEMENT, 0);
      }
      if (overrunCost <= currentCost.getPointsLeft()) {
        return result(fromHex, true, 0.0d, false, MODE_OVERRUN, 0);
      } else {
        return result(fromHex, false, 0.0d, false, MODE_MOVEMENT, 0);
      }
    }
    if (Filter.isAny(type, "Airbase", "Headquarters", "Leader", "SiegeArtillery", "Depot")) {
      if (inEnemyZOC.contains(toHex)) {
        if (occupiedByFriend.contains(toHex) == false) {
          return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
        }
      }
    }
    boolean isArctic = isArctic(toHex);
    boolean fromZOC = inEnemyZOC.contains(fromHex);
    boolean toZOC = inEnemyZOC.contains(toHex);
    if (lt.equals(LINE_CHONGARCAUSEWAY)) {
      return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
    }
    if ((movementMode == MOVEMENT_RAIL) && (lt.equals(LINE_RAILROAD) == false)) {
      return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
    }
    if ((fromZOC) && (toZOC)) {
      if (Filter.isAny(unitType, "AxisGermanTankCorps", "AxisGermanTankBattlegroup", "AxisGermanTankDivision")) {

      } else
      if ((isWinter) && (unitType.equals("SovietGuardsCavalryCorps"))) {

      } else {
        return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
      }
    }
    double cost = 0.0d;
    if (core.isImpassable(et)) {
      return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
    } else if (core.isLakeSea(et)) {
      if ((side.equals(SIDE_AXIS)) || (isWinter == false) || (lt.equals(LINE_FROZENSURFACE) == false) || (movementMode != RsrTrait.MOVEMENT_NONMOTORIZED)) {
        return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
      } else {
        cost += EDGE_MINORRIVER_COST[movementMode];
      }
    } else if (core.isMinorRiver(et)) {
      if ((turnNumber > 1) || (side.equals(SIDE_SOVIET))) {
        cost += EDGE_MINORRIVER_COST[movementMode];
      }
    } else if (core.isMajorRiver(et)) {
      cost += EDGE_MAJORRIVER_COST[movementMode];
    }
    if (tt.equals(HEX_WATER)) {
      return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
    } else if (tt.equals(HEX_WOODS)) {
      cost += HEX_WOODS_COST[movementMode] * (isArctic ? 2 : 1);
    } else if (tt.equals(HEX_SWAMP)) {
      if (isWinter) {
        cost += HEX_WOODS_COST[movementMode];
      } else {
        cost += HEX_SWAMP_COST[movementMode];
      }
    } else if (tt.equals(HEX_MOUNTAIN)) {
      cost += HEX_MOUNTAIN_COST[movementMode] - (isMountain ? 1 : 0);
    } else if (tt.equals(HEX_SEVASTOPOL)) {
      if (core.isCityDestroyed(TAG_CITY_SEVASTOPOL) == false) {
        cost += HEX_SEVASTOPOL_COST[movementMode];
      } else {
        cost += HEX_MINORCITY_COST[movementMode];
      }
    } else if (tt.equals(HEX_MINORCITY)) {
      cost += HEX_MINORCITY_COST[movementMode] * (isArctic ? 2 : 1);
    } else if (tt.equals(HEX_MAJORCITY)) {
      cost += HEX_MAJORCITY_COST[movementMode] * (isArctic ? 2 : 1);
    } else {
      cost += HEX_CLEAR_COST[movementMode] * (isArctic ? 2 : 1);
    }
    if (movementMode == RsrTrait.MOVEMENT_RAIL) {
      cost = 1.0;
    }
    if (fromZOC) {
      if ((side.equals(SIDE_AXIS)) || (isWinter == false)) {
        cost += 3.0;
      } else {
        cost += 2.0;
      }
    }
    if ((fromHex.equals(startHex)) && (cost > currentCost.getPointsLeft()) && (movement == maxMovement) && (inEnemyZOC.contains(startHex) == false) && (phaseName.equals(PHASE_GERMAN_TANK_MOVEMENT) == false)) {
      cost = currentCost.getPointsLeft();
    }
    return result(fromHex, true, currentCost.getPointsLeft() - cost, /*(core.isStackSizeOK(toHex, side, mySize) == false) || (core.isStackFriendly(toHex, side, nationality, unitType) == false) ||  */(blockedByEnemy.contains(toHex)), MODE_MOVEMENT, 0);
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
    TerrainMapShaderCost result = costs.get(MODE_MOVEMENT);
    if ((result == null) || (result.isTraceOnly())) {
      result = costs.get(MODE_OVERRUN);
      if ((result == null) || (result.isTraceOnly())) {
        result = null;
      }
    }
    return result;
  }
}
