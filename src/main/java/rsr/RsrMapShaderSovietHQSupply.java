package rsr;

import java.util.HashMap;
import java.util.Set;

import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;
import filter.Filter;

/**
 * 
 * @author morvael
 */
public final class RsrMapShaderSovietHQSupply extends RsrMapShaderBase implements HexControlInfo {

	private String[] supplyParams;
	private Set<HexRef> occupiedByFriend;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	private boolean isAttritionCheck;
	private boolean useStoredPoints;
	private double rangeMod;
	
	public RsrMapShaderSovietHQSupply() {
		super();	
	}
	
	@Override
	public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(map, grid, terrainMap, params, piece);
		supplyParams = params.split(";");
		try {
			isAttritionCheck = supplyParams[0].equals("Attrition");
		} catch (Exception e) {
			isAttritionCheck = false;
		}		
    try {
      useStoredPoints = supplyParams[1].equals("1");
    } catch (Exception e) {
      useStoredPoints = false;
    } 		
		//+Attrition/Normal
    //+1/0 useStoredPoints		
		rangeMod = isAttritionCheck ? 2.0 : 1.0;
	}
	
	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		//find hex control
		occupiedByFriend = pieceCache.getHexesOccupiedBy(SIDE_SOVIET);
		occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(SIDE_SOVIET);
		inEnemyZOC = pieceCache.getHexesInZOCEnemy(SIDE_SOVIET);
		//find supply sources
		for (HexRef hr : core.getPossibleSupplySources(SIDE_SOVIET, null)) {
			if (canEnter(hr)) {
				addHex(selectedHexes, hr, create(null, true, rangeMod * 6.0, false, MODE_NORMAL, 0));
			}			
		}
		//find all hexes with railroad line of communication to supply sources
		Set<HexRef> withLOC = getHexes(map, grid, terrainMap, new RsrHelperRailroadLine(this, selectedHexes.keySet()), "", null).keySet();
		//find hexes for sea supply
		if ((piece == null) || ((Filter.isAny(RsrTrait.getType(piece), "Headquarters", "Leader") == false))) {
			Set<HexRef> supplied = getHexes(map, grid, terrainMap, new RsrHelperRailroadLine(this, selectedHexes.keySet()), "", null).keySet();
			if (((core.isCitySovietControlled(TAG_CITY_NOVOROSSIISK)) && (supplied.contains(core.getCityHexRef(TAG_CITY_NOVOROSSIISK)))) || ((core.isCitySovietControlled(TAG_CITY_TUAPSE)) && (supplied.contains(core.getCityHexRef(TAG_CITY_TUAPSE))))) {
				Set<HexRef> ports = terrainMap.findHexesWithTag(TAG_PORT, TAG_PORT_BLACKSEA);
				if (core.isCityAxisControlled(TAG_CITY_SEVASTOPOL)) {
					ports.remove(core.getCityHexRef(TAG_CITY_ODESSA));
				}
				for (HexRef hr : ports) {
					if (core.isHexSovietControlled(hr)) {
						addHex(selectedHexes, hr, create(null, true, rangeMod * 2.0, false, MODE_SEA, 0));
					}
				}
			}
		}
    if (useStoredPoints) {
      //find hexes with HQs that have railroad line of communication to supply sources, using stored locations
      CombatHexList chl = new CombatHexList(RsrGlobal.getSovietStratHexes());
      for (HexRef hr : chl.getHexes()) {
        if ((withLOC.contains(hr)) && (canEnter(hr))) {
          addHex(selectedHexes, hr, create(null, true, rangeMod * 6.0, false, MODE_NORMAL, 0));
        }       
      }
    } else {		
      //find hexes with HQs that have railroad line of communication to supply sources
  		for (GamePiece gp : filter.filterPieces(map, "Type=='Headquarters'&&Nationality=='Soviet'")) {
  			HexRef hr = grid.getHexPos(gp.getPosition());
  			if ((withLOC.contains(hr)) && (canEnter(hr))) {
  				addHex(selectedHexes, hr, create(null, true, rangeMod * 6.0, false, MODE_NORMAL, 0));
  			}
  		}
    }
	}
		
	public String getSide() {
		return SIDE_SOVIET;
	}
	
	public boolean canEnter(HexRef hex) {
		if (occupiedByEnemy.contains(hex)) {
			return false;
		} else
		if (inEnemyZOC.contains(hex)) {
			return occupiedByFriend.contains(hex);
		} else {
			return true;
		}
	}	
	
	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		if (canEnter(toHex) == false) {
			return result(fromHex, false, 0.0, false, currentCost.getFlag(), 0);
		}
		double cost = getSupplyEnterCost(fromHex, toHex, SIDE_SOVIET, null);
		if (cost == Double.MAX_VALUE) {
			return result(fromHex, false, 0.0, false, currentCost.getFlag(), 0);
		} else {
			return result(fromHex, true, currentCost.getPointsLeft()-cost, false, currentCost.getFlag(), 0);
		}
	}

	@Override
	public void stop() {
	}

	@Override
	public HexRef getBestPathDestination() {
		if (piece != null) {
			return grid.getHexPos(piece.getPosition());
		} else {
			return null;
		}
	}

	@Override
	public TerrainMapShaderCost getBestCost(
			HashMap<String, TerrainMapShaderCost> costs) {
		TerrainMapShaderCost result = costs.get(MODE_NORMAL);
		if ((result == null) || (result.isTraceOnly())) {
			result = costs.get(MODE_SEA);
			if (((result == null) || (result.isTraceOnly()))) {
				result = null;
			}
		}
		return result;
	}
	
}
