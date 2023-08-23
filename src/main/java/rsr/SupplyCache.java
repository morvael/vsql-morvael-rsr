package rsr;

import VASSAL.build.module.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import terrain.HexRef;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import terrain.TerrainMapShaderRule;

/**
 *
 * @author morvael
 * @since 2009-05-20
 */
public final class SupplyCache extends Cache implements HexControlInfo {

  private RsrAdvanceBase core;
  private Map mainMap;
  private TerrainHexGrid mainGrid;
  private TerrainMap mainTerrain;
  private PieceCache zocCache;
  private RsrMapShaderDepotSupply rulesAxisDepot = new RsrMapShaderDepotSupply();
  private RsrMapShaderAxisHQSupply rulesAxisHQ = new RsrMapShaderAxisHQSupply();
  private RsrMapShaderSovietHQSupply rulesSovietHQ = new RsrMapShaderSovietHQSupply();
  private TerrainMapShaderRule rules;
  private String side;
  private Set<HexRef> occupiedByFriend;
  private Set<HexRef> occupiedByEnemy;
  private Set<HexRef> inEnemyZOC;

  public SupplyCache(RsrAdvanceBase core, Map mainMap, TerrainHexGrid mainGrid, TerrainMap mainTerrain, PieceCache zocCache) {
    this.core = core;
    this.mainMap = mainMap;
    this.mainGrid = mainGrid;
    this.mainTerrain = mainTerrain;
    this.zocCache = zocCache;
  }

  @Override
  protected HashSet<HexRef> calculate1(String id) {
    if (id.charAt(0) == 'S') {
      side = SIDE_SOVIET;
      rules = new RsrHelperRailroadLine(this, core.getPossibleSupplySources(side, null));
    } else {
      side = SIDE_AXIS;
      rules = new RsrHelperAnyLine(this, core.getPossibleSupplySources(side, null));
    }
    occupiedByFriend = zocCache.getHexesOccupiedBy(side);
    occupiedByEnemy = zocCache.getHexesOccupiedByEnemy(side);
    inEnemyZOC = zocCache.getHexesInZOCEnemy(side);
    return new HashSet<HexRef>(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rules, "", null).keySet());
  }

  @Override
  protected HashMap<HexRef, TerrainMapShaderCost> calculate2(String id) {
    String nationality = id.substring(2);
    String attrition = id.charAt(0) == 'A' ? "Attrition" : "Normal";
    String useStoredPoints = id.charAt(1) == '1' ? "1" : "0";
    if ((nationality.equals(NATIONALITY_GERMAN)) || (nationality.equals(NATIONALITY_HUNGARIAN)) || (nationality.equals(NATIONALITY_ITALIAN))) {
      return TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesAxisDepot, attrition + ";" + useStoredPoints, null);
    } else
    if (nationality.equals(NATIONALITY_SOVIET)) {
      return TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesSovietHQ, attrition + ";" + useStoredPoints, null);
    } else
    if (nationality.equals(NATIONALITY_ROMANIAN)) {
      return TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesAxisHQ, "Bucharest;Romanian;" + attrition + ";" + useStoredPoints, null);
    } else
    if (nationality.equals(NATIONALITY_FINNISH)) {
      return TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesAxisHQ, "Helsinki;Finnish;" + attrition + ";" + useStoredPoints, null);
    } else {
      return null;
    }
  }

  public HashMap<HexRef, TerrainMapShaderCost> getSupply(String nationality, boolean attrition, boolean useStoredPoints) {
    return get2((attrition ? "A" : "N") + (useStoredPoints ? "1" : "0") + nationality);
  }

  public HashSet<HexRef> getSovietLine() {
    return get1("S");
  }

  public HashSet<HexRef> getAxisLine() {
    return get1("A");
  }

  public String getSide() {
    return side;
  }

  public boolean canEnter(HexRef hex) {
    if (occupiedByEnemy.contains(hex)) {
      return false;
    } else if (inEnemyZOC.contains(hex)) {
      return occupiedByFriend.contains(hex);
    } else {
      return true;
    }
  }

}
