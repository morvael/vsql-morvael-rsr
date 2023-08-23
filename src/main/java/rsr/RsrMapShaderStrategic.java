package rsr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import filter.Filter;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.board.MapGrid.BadCoords;
import VASSAL.counters.GamePiece;

/**
 * 
 * @author morvael
 */
public final class RsrMapShaderStrategic extends RsrMapShaderBase {

	private String side;
  private String nationality;
  private String type;
  //private String unitType;
	private int movementMode;
  //private double mySize;
	private HexRef startHex;
	private Set<HexRef> blockedByEnemy;
  private HashMap<HexRef, TerrainMapShaderCost> inSupply;
	
	public RsrMapShaderStrategic() {
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
    type = RsrTrait.getType(piece);
    //unitType = RsrTrait.getUnitType(piece);
		movementMode = RsrTrait.getMovementTypeAsInt(piece);
    //mySize = RsrTrait.getStackPoints(piece);
		//find blocked hexes
    blockedByEnemy = pieceCache.getHexesInRangeAnyEnemy(side, 4);
    if (blockedByEnemy == null) {
      blockedByEnemy = new HashSet<HexRef>();
    }
		//
    //find hexes in supply
    inSupply = RsrAdvance.getInstance().getSupplyCache().getSupply(nationality, false, true);
    if (inSupply == null) {
      inSupply = new HashMap<HexRef, TerrainMapShaderCost>();
    }
    //
    double mp = core.getStrategicMovementAllowance(piece);
    try {
      startHex = grid.getHexPos(grid.getLocation(RsrTrait.getStartHex(piece)));
    } catch (BadCoords ex) {
      startHex = grid.getHexPos(piece.getPosition());
    }    
		if ((blockedByEnemy.contains(startHex)) || (inSupply(startHex) == false)) {
			mp = 0.0d;
		} else {
      addHex(selectedHexes, startHex, create(null, true, mp, true, MODE_STRATEGIC, 0));
    }
	}
	
	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		if ((blockedByEnemy.contains(toHex)) || (inSupply(toHex) == false)) {
			return result(fromHex, false, 0.0, false, MODE_STRATEGIC, 0);
		}
		String tt = terrainMap.getHexTerrainName(toHex);
		String et = terrainMap.getEdgeTerrainName(fromHex, toHex);
		String lt = terrainMap.getLineTerrainName(fromHex, toHex);
		if (core.isImpassable(et)) {
			return result(fromHex, false, 0.0, false, MODE_STRATEGIC, 0);
		} else
		if (core.isLakeSea(et)) {
			if ((lt.equals(LINE_CHONGARCAUSEWAY) == false) && ((side.equals(SIDE_AXIS)) || (isWinter == false) || (lt.equals(LINE_FROZENSURFACE) == false) || (movementMode != MOVEMENT_NONMOTORIZED))) {
				return result(fromHex, false, 0.0, false, MODE_STRATEGIC, 0);
			}
		} else
		if (tt.equals(HEX_WATER)) {
			return result(fromHex, false, 0.0, false, MODE_STRATEGIC, 0);
		} else if ((movementMode == MOVEMENT_RAIL) && (Filter.isAny(lt, LINE_RAILROAD, LINE_CHONGARCAUSEWAY) == false)) {
      return result(fromHex, false, 0.0, false, MODE_MOVEMENT, 0);
    }
		return result(fromHex, true, currentCost.getPointsLeft()-1.0, false/*(core.isStackSizeOK(toHex, side, mySize) == false) || (core.isStackFriendly(toHex, side, nationality, unitType) == false) */, MODE_STRATEGIC, 0);
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
		return costs.get(MODE_STRATEGIC);
	}

  private boolean inSupply(HexRef hr) {
    if (inSupply.containsKey(hr) == false) {
      return false;
    } else {
      TerrainMapShaderCost cost = inSupply.get(hr);
      if (MODE_SEA.equals(cost.getFlag())) {
        return ((side.equals(SIDE_SOVIET)) && (Filter.isAny(type, "Headquarters", "Leader") == false)) || ((nationality.equals(NATIONALITY_GERMAN)) && (Filter.isAny(type, "Airbase") == false));
      } else {
        return true;
      }
    }
  }
	
}
