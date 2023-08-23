package rsr;

import VASSAL.build.module.Map;
import VASSAL.counters.GamePiece;
import java.util.HashSet;
import terrain.HexRef;
import filter.Filter;
import terrain.TerrainHexGrid;

/**
 *
 * @author derwido
 * @since 2009-05-20
 */
public final class AirFleetCache extends Cache {

  private Map mainMap;
  private TerrainHexGrid mainGrid;
  private Filter filter;

  public AirFleetCache(Map mainMap, TerrainHexGrid mainGrid, Filter filter) {
    this.mainMap = mainMap;
    this.mainGrid = mainGrid;
    this.filter = filter;
  }

  @Override
  protected HashSet<HexRef> calculate1(String id) {
    HashSet<HexRef> result = new HashSet<HexRef>();
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='AirFleet'")) {
      result.addAll(mainGrid.getHexesInRange(mainGrid.getHexPos(gp.getPosition()), RsrTrait.getRange(gp)));
    }
    return result;
  }

  public HashSet<HexRef> getAirFleetSupportZone() {
    return get1();
  }

}
