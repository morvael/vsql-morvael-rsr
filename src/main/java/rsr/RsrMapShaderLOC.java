package rsr;

import java.util.HashMap;
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
public final class RsrMapShaderLOC extends RsrMapShaderBase implements HexControlInfo {

	private String[] locParams;
	private String sideFriend;
	private String capital;
	private boolean railroadOnly;
	private Set<HexRef> occupiedByFriend;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;
	private Set<HexRef> withLOC;
	private HexRef destination;
	
	public RsrMapShaderLOC() {
		super();
	}
	
	@Override
	public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(map, grid, terrainMap, params, piece);
		locParams = params.split(";");
		if (locParams[0].equals(SIDE_AXIS)) {
			sideFriend = SIDE_AXIS;
			if (locParams[1].length() > 0) {
				capital = locParams[1];
			} else {
				capital = null;
			}
		} else {
			sideFriend = SIDE_SOVIET;
			capital = null;
		}
		railroadOnly = locParams[2].equals("Railroad");
		//Axis;;
		//Axis;Helsinki;
		//Axis;Bucharest;
		//Soviet;;
		// + Railroad/Any
	}
	
	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		if (piece == null) {
			return;
		}
		//find destination
		destination = grid.getHexPos(piece.getPosition());
		//find hex control
		occupiedByFriend = pieceCache.getHexesOccupiedBy(sideFriend);
		occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(sideFriend);
		inEnemyZOC = pieceCache.getHexesInZOCEnemy(sideFriend);
		//find supply sources
		for (HexRef hr : core.getPossibleSupplySources(sideFriend, capital)) {
			if (canEnter(hr)) {
				addHex(selectedHexes, hr, create(null, true, 1.0, false, null, 0));
			}			
		}
		//find all hexes with railroad line of communication to supply sources
		if (railroadOnly) {
			withLOC = getHexes(map, grid, terrainMap, new RsrHelperRailroadLine(this, selectedHexes.keySet()), "", piece).keySet();
		} else {
			withLOC = getHexes(map, grid, terrainMap, new RsrHelperAnyLine(this, selectedHexes.keySet()), "", piece).keySet();
		}
		selectedHexes.keySet().retainAll(withLOC);
	}
		
	public String getSide() {
		return sideFriend;
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
		if (withLOC.contains(toHex) == false) {
			return result(fromHex, false, 0.0, false, null, 0);
		}
		return result(fromHex, true, 1.0, false, null, 0);
	}

	@Override
	public void stop() {
	}

	@Override
	public HexRef getBestPathDestination() {
		return destination;
	}

	@Override
	public TerrainMapShaderCost getBestCost(
			HashMap<String, TerrainMapShaderCost> costs) {
		return costs.get(null);
	}
	
}
