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
 * @author derwido
 * @since 2009-05-21
 */
public final class RsrMapShaderHQReinforcement extends RsrMapShaderBase implements HexControlInfo {

  private String side;
  private String capital;
	private Set<HexRef> occupiedByFriend;
	private Set<HexRef> occupiedByEnemy;
	private Set<HexRef> inEnemyZOC;

	public RsrMapShaderHQReinforcement() {
		super();
	}

	@Override
	public void reset(Map map, TerrainHexGrid grid, TerrainMap terrainMap, String params, GamePiece piece) {
		super.reset(map, grid, terrainMap, params, piece);
		String[] lineParams = params.split(";");
    side = lineParams[0].equals(NATIONALITY_SOVIET) ? SIDE_SOVIET : SIDE_AXIS;
    capital = lineParams.length > 1 ? lineParams[1] : "";
    //Soviet;
    //Finnish;Helsinki
    //Romanian;Bucharest
	}

	@Override
	public void start(HashMap<HexRef, HashMap<String, TerrainMapShaderCost>> selectedHexes) {
		//find hex control
		occupiedByFriend = pieceCache.getHexesOccupiedBy(side);
		occupiedByEnemy = pieceCache.getHexesOccupiedByEnemy(side);
		inEnemyZOC = pieceCache.getHexesInZOCEnemy(side);
		//find supply sources
    if (side.equals(SIDE_SOVIET)) {
      for (HexRef hr : core.getPossibleSupplySources(side, null)) {
        if (canEnter(hr)) {
          addHex(selectedHexes, hr, create(null, true, 4.0, false, null, 0));
        }
      }
    } else {
      HexRef hr = core.getCityHexRef(capital);
      if ((canEnter(hr)) && (core.isCityAxisControlled(capital))) {
        addHex(selectedHexes, hr, create(null, true, 4.0, false, null, 0));
      }
    }
		//find all hexes with railroad line of communication to supply sources/capital, there are the only hexes to return
		Set<HexRef> withLOC = getHexes(map, grid, terrainMap, new RsrHelperRailroadLine(this, selectedHexes.keySet()), "", null).keySet();
    for (HexRef hr : withLOC) {
      addHex(selectedHexes, hr, create(null, true, 0.0d, false, null, 0));
    }
	}

	public String getSide() {
		return side;
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
  	return result(fromHex, false, 0.0, false, null, 0);
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

