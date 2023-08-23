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
public final class RsrMapShaderAxisHQSupply extends RsrMapShaderBase implements HexControlInfo {

	private String[] supplyParams;
	private HashSet<HexRef> partisanHexes = new HashSet<HexRef>();	
	private Set<HexRef> occupiedByFriend;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	private boolean isAttritionCheck;
	private boolean useStoredPoints;
	private double rangeMod;
	
	public RsrMapShaderAxisHQSupply() {
		super();
	}
	
	@Override
	public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(map, grid, terrainMap, params, piece);
		supplyParams = params.split(";");
		try {
			isAttritionCheck = supplyParams[2].equals("Attrition");
		} catch (Exception e) {
			isAttritionCheck = false;
		}
    try {
      useStoredPoints = supplyParams[3].equals("1");
    } catch (Exception e) {
      useStoredPoints = false;
    } 		
		//Helsinki;Finnish
		//Bucharest;Romanian
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
		for (HexRef hr : core.getPossibleSupplySources(SIDE_AXIS, supplyParams[0])) {
			if (canEnter(hr)) {
				addHex(selectedHexes, hr, create(null, true, rangeMod * 4.0, false, null, 0));
			}			
		}
		//find all hexes with railroad line of communication to supply sources
		Set<HexRef> withLOC = getHexes(map, grid, terrainMap, new RsrHelperRailroadLine(this, selectedHexes.keySet()), "", null).keySet();
		if (useStoredPoints) {
      //find hexes with HQs that have railroad line of communication to supply sources, using stored locations
		  CombatHexList chl;
		  if (supplyParams[1].equals(NATIONALITY_FINNISH)) {
		    chl = new CombatHexList(RsrGlobal.getFinnishStratHexes());
		  } else {
		    chl = new CombatHexList(RsrGlobal.getRomanianStratHexes());
		  }
		  for (HexRef hr : chl.getHexes()) {
        if ((withLOC.contains(hr)) && (canEnter(hr))) {
          addHex(selectedHexes, hr, create(null, true, rangeMod * 4.0, false, null, 0));
        }		    
		  }
		} else {
	    //find hexes with HQs that have railroad line of communication to supply sources
  		for (GamePiece gp : filter.filterPieces(map, "Type=='Headquarters'&&Nationality=='" + supplyParams[1] + "'")) {
  			HexRef hr = grid.getHexPos(gp.getPosition());
  			if ((withLOC.contains(hr)) && (canEnter(hr))) {
  				addHex(selectedHexes, hr, create(null, true, rangeMod * 4.0, false, null, 0));
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
	public boolean getCrossCost(HexRef fromHex, HexRef toHex, TerrainMapShaderCost currentCost) {
		if (canEnter(toHex) == false) {
			return result(fromHex, false, 0.0, false, null, 0);
		}
		double cost = getSupplyEnterCost(fromHex, toHex, SIDE_AXIS, partisanHexes);
		if (cost == Double.MAX_VALUE) {
			return result(fromHex, false, 0.0, false, null, 0);
		} else {
			return result(fromHex, true, currentCost.getPointsLeft()-cost, false, null, 0);
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
	public TerrainMapShaderCost getBestCost(HashMap<String, TerrainMapShaderCost> costs) {
		return costs.get(null);
	}			
	
}
