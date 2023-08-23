package rsr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;

import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;

/**
 * 
 * @author morvael
 */
public final class RsrHelperAnyLine extends RsrMapShaderBase {

	private HexControlInfo info;
	private Set<HexRef> supplySources;
	private HexRef destination;
	
	public RsrHelperAnyLine(HexControlInfo info, Set<HexRef> supplySources) {
		super();
		this.info = info;
		this.supplySources = new HashSet<HexRef>(supplySources);
	}
		
	@Override
	public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(map, grid, terrainMap, params, piece);
		if (piece != null) {
			destination = grid.getHexPos(piece.getPosition());
		} else {
			destination = null;
		}
	}	
	
	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		for (HexRef hr : supplySources) {
			if (info.canEnter(hr)) {
				addHex(selectedHexes, hr, create(null, true, 1.0, false, null, 0));
			}
		}
	}

	@Override
	public boolean getCrossCost(HexRef fromHex, HexRef toHex,
			TerrainMapShaderCost currentCost) {
		if (info.canEnter(toHex) == false) {
			return result(fromHex, false, 0.0d, false, null, 0);
		}
		if (getSupplyEnterCost(fromHex, toHex, info.getSide(), null) == Double.MAX_VALUE) {
			return result(fromHex, false, 0.0d, false, null, 0);
		}
		return result(fromHex, true, 1.0d, false, null, 0);
	}

	@Override
	public void stop() {
	}

	@Override
	public HexRef getBestPathDestination() {
		return destination;
	}

	@Override
	public TerrainMapShaderCost getBestCost(HashMap<String, TerrainMapShaderCost> costs) {
		return costs.get(null);
	}
	
}
