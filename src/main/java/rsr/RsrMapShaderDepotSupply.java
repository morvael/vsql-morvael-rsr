package rsr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;

/**
 * 
 * @author morvael
 */
public final class RsrMapShaderDepotSupply extends RsrMapShaderBase implements HexControlInfo {

	private String[] supplyParams;
	private HashSet<HexRef> respawnHexes = new HashSet<HexRef>();
	private HashSet<HexRef> partisanHexes = new HashSet<HexRef>();
	private Set<HexRef> occupiedByFriend;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	private boolean isAttritionCheck;
	private boolean useStoredPoints;
	private double rangeMod;
	
	public RsrMapShaderDepotSupply() {
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
		occupiedByFriend = pieceCache.getHexesOccupiedBy(SIDE_AXIS);
		occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(SIDE_AXIS);
		inEnemyZOC = pieceCache.getHexesInZOCEnemy(SIDE_AXIS);
		//find supply sources
		for (HexRef hr : core.getPossibleSupplySources(SIDE_AXIS, null)) {
			if (canEnter(hr)) {
				addHex(selectedHexes, hr, create(null, true, rangeMod * 4.0, false, MODE_NORMAL, 0));
			}			
		}
		//find hexes for sea supply
		if ((piece == null) || ((RsrTrait.getNationality(piece).equals(NATIONALITY_GERMAN)) && (RsrTrait.getType(piece).equals("Airbase") == false))) {
			if ((core.isCityAxisControlled(TAG_CITY_ODESSA)) && (core.isCityAxisControlled(TAG_CITY_SEVASTOPOL))) {
				Set<HexRef> supplied = getHexes(map, grid, terrainMap, new RsrHelperRailroadLine(this, selectedHexes.keySet()), "", null).keySet();
				if ((supplied.contains(core.getCityHexRef(TAG_CITY_ODESSA))) && (supplied.contains(core.getCityHexRef(TAG_CITY_SEVASTOPOL)))) {
					for (HexRef hr : terrainMap.findHexesWithTag(TAG_PORT, TAG_PORT_BLACKSEA)) {
						if (core.isHexAxisControlled(hr)) {
							addHex(selectedHexes, hr, create(null, true, rangeMod * 2.0, false, MODE_SEA, 0));
						}
					}
				}
			}
		}
    if (useStoredPoints) {
      CombatHexList chl;
      //find hexes with emergency depots, using stored locations
      chl = new CombatHexList(RsrGlobal.getAxisEmergencyStratHexes());
      for (HexRef hr : chl.getHexes()) {
        if (canEnter(hr)) {
          addHex(selectedHexes, hr, create(null, true, rangeMod * 4.0, false, MODE_EMERGENCY, 0));
        }        
      }
      //find hexes with normal depots, usin stored locations
      chl = new CombatHexList(RsrGlobal.getAxisNormalStratHexes());
      for (HexRef hr : chl.getHexes()) {
        if (canEnter(hr)) {
          respawnHexes.add(hr);
        }       
      }
    } else {		
  		//find hexes with emergency depots
  		for (GamePiece gp : filter.filterPieces(map, "Type=='Depot'&&SupplyMode==2&&" + T_PLACED + "==0")) {
  			HexRef hr = grid.getHexPos(gp.getPosition());
  			if (canEnter(hr)) {
  				addHex(selectedHexes, hr, create(null, true, rangeMod * 4.0, false, MODE_EMERGENCY, 0));
  			}
  		}		
  		//find hexes with normal depots
  		respawnHexes.clear();
  		for (GamePiece gp : filter.filterPieces(map, "Type=='Depot'&&SupplyMode==1&&" + T_PLACED + "==0")) {
  			HexRef hr = grid.getHexPos(gp.getPosition());
  			if (canEnter(hr)) {
  				respawnHexes.add(hr);
  			}
  		}
    }
		//find hexes with partisans
		partisanHexes.clear();
		for (GamePiece gp : pieceCache.getPartisans()) {
			partisanHexes.add(grid.getHexPos(gp.getPosition()));
		}
	}
	
	public String getSide() {
		return SIDE_AXIS;
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
		double cost = getSupplyEnterCost(fromHex, toHex, SIDE_AXIS, partisanHexes);
		if (cost == Double.MAX_VALUE) {
			return result(fromHex, false, 0.0, false, currentCost.getFlag(), 0);
		} else {
			double left = currentCost.getPointsLeft() - cost;
			if ((respawnHexes.contains(toHex)) && (left >= 0.0)) {
				respawnHexes.remove(toHex);
				return result(fromHex, true, rangeMod * 4.0, false, currentCost.getFlag(), 0);
			} else {
				return result(fromHex, true, left, false, currentCost.getFlag(), 0);
			}			
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
			if ((result == null) || (result.isTraceOnly())) {
				result = costs.get(MODE_EMERGENCY);
				if ((result == null) || (result.isTraceOnly())) {
					result = null;
				}
			}
		}
		return result;
	}
	
}
