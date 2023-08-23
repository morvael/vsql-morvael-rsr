package rsr;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;

import terrain.HexRef;
import terrain.TerrainDefinitions;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import terrain.TerrainMapShaderCost;
import terrain.TerrainMapShaderRule;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.map.MovementReporter;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.map.boardPicker.board.Region;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.RemovePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyBuffer;
import VASSAL.tools.ErrorDialog;
import filter.DefaultPropertyReaderSource;
import filter.Filter;
import filter.PropertyReader;
import filter.SafeFormulaFilter;

/**
 *
 * @author derwido
 * @since 2009-05-08
 */
public abstract class RsrAdvanceBase implements RsrButtonCommand, RsrConstants, GameComponent {

  private boolean initialized;
  protected DefaultPropertyReaderSource propertyReaderSource;
  protected Filter filter;
  protected Random ran;
  protected Map mainMap;
  protected Board mainBoard;
  protected TerrainHexGrid mainGrid;
  protected TerrainMap mainTerrain;
  protected Map turnMap;
  protected Board turnBoard;
  protected MapGrid turnGrid;
  protected Map offboardMap;
  protected Board offboardBoard;
  protected MapGrid offboardGrid;
  protected RsrMapShader shader;
  protected RsrMapShader shaderSupply;
  protected RsrMapShader shaderOverstack;
  protected RsrMapShaderDepotSupply rulesDepotSupply;
  protected RsrMapShaderAxisHQSupply rulesAxisHQSupply;
  protected RsrMapShaderSovietHQSupply rulesSovietSupply;
  protected RsrMapShaderMovement rulesMovement;
  protected RsrMapShaderStrategic rulesStrategic;
  protected RsrMapShaderRetreat rulesRetreat;
  protected RsrMapShaderAdvance rulesAdvance;
  protected RsrMapShaderHQReinforcement rulesHQReinforcement;
  protected GamePiece turnPiece;
  protected GamePiece phasePiece;
  protected GamePiece dataPiece;
  protected Command c = null;
  protected boolean autoAdvance = false;
  /**
   * Cache of allowed units, cleared after each command is executed.
   */
  protected HashMap<GamePiece, RsrAllowedMovement> allowedMovement = new HashMap<GamePiece, RsrAllowedMovement>();
  /**
   * Current move mode,
   */
  protected int moveMode = MOVE_FREE;
  protected HashMap<HexRef, GamePiece> controlMarkers = new HashMap<HexRef, GamePiece>();
  protected HashMap<HexRef, GamePiece> destroyedMarkers = new HashMap<HexRef, GamePiece>();
  private HashSet<HexRef> variants = new HashSet<HexRef>();

  protected AirFleetCache airFleetCache;
  protected PieceCache pieceCache;
  protected SupplyCache supplyCache;

  protected boolean isNorthMapUsed = true;
  protected boolean isSouthwestMapUsed = true;
  protected boolean isSubMapUsed = false;
  protected int lastSupplyDisplayed = -1;

  protected Board findBoard(Map map, String name) {
    if (map != null) {
      for (Board b : map.getBoards()) {
        if (b.getLocalizedName().equals(name)) {
          return b;
        }
      }
    }
    return null;
  }

  protected RsrMapShader getShader(String name) {
    for (RsrMapShader ms : mainMap.getAllDescendantComponentsOf(RsrMapShader.class)) {
      if (ms.getConfigureName().equals(name)) {
        return ms;
      }
    }
    return null;
  }

  protected GamePiece getSinglePiece(Map map, String f) {
    for (GamePiece gp : filter.filterPieces(map, f)) {
      return gp;
    }
    return null;
  }

  protected boolean initialize(String param) {
    if (initialized == false) {
      try {
        propertyReaderSource = new DefaultPropertyReaderSource();
        initializePropertyReaderSource();
        filter = new SafeFormulaFilter(propertyReaderSource);
        ran = GameModule.getGameModule().getRNG();
        mainMap = Map.getMapById("Main Map");
        mainBoard = findBoard(mainMap, "Main Board");
        mainGrid = (TerrainHexGrid) mainBoard.getGrid();
        mainTerrain = TerrainDefinitions.getInstance().getTerrainMap(mainGrid);
        turnMap = Map.getMapById("Turn");
        turnBoard = findBoard(turnMap, "Turn");
        turnGrid = turnBoard.getGrid();
        offboardMap = Map.getMapById("Offboard");
        offboardBoard = findBoard(offboardMap, "Offboard");
        offboardGrid = offboardBoard.getGrid();
        shader = getShader("AllowedShading");
        shaderSupply = getShader("SupplyShading");
        shaderOverstack = getShader("OverstackShading");
        rulesDepotSupply = new RsrMapShaderDepotSupply();
        rulesAxisHQSupply = new RsrMapShaderAxisHQSupply();
        rulesSovietSupply = new RsrMapShaderSovietHQSupply();
        rulesMovement = new RsrMapShaderMovement();
        rulesStrategic = new RsrMapShaderStrategic();
        rulesRetreat = new RsrMapShaderRetreat();
        rulesAdvance = new RsrMapShaderAdvance();
        rulesHQReinforcement = new RsrMapShaderHQReinforcement();
        turnPiece = getSinglePiece(turnMap, "BasicName=='Game Turn'");
        phasePiece = getSinglePiece(turnMap, "BasicName=='Turn Phase'");
        dataPiece = getSinglePiece(turnMap, "BasicName=='Data'");
        airFleetCache = new AirFleetCache(mainMap, mainGrid, filter);
        pieceCache = new PieceCache(this, mainMap, mainGrid, filter);
        supplyCache = new SupplyCache(this, mainMap, mainGrid, mainTerrain, pieceCache);
        scenarioChanged(RsrGlobal.getScenario());
        discoverOverstackHexes();
        initialized = true;
      } catch (Exception ex) {
        ErrorDialog.bug(ex);
      }
    }
    return initialized;
  }

  private ArrayList<GamePiece> getSelectedPieces() {
    return getSelectedPieces(mainMap);
  }
  
  private ArrayList<GamePiece> getSelectedPieces(Map selectedMap) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    Iterator<GamePiece> it = KeyBuffer.getBuffer().getPiecesIterator();
    GamePiece gp;
    while (it.hasNext()) {
      gp = it.next();
      if (gp.getMap() == selectedMap) {
        result.add(gp);
      }
    }
    return result;    
  }
  
  public final void execute(String param, boolean alternate) {
    if (param.equals("")) {
      commandAdvance(false);
      sendAndLog();
    } else if (param.equals("clear")) {
      if (RsrGlobal.getCombatStage() > 0) {
        commandClearCombat(false);
        sendAndLog();
      }
    } else if (param.equals("advance")) {
      if (RsrGlobal.getCombatStage() > 0) {
        commandResolveCombat(false);
        sendAndLog();
      }
    } else if (param.equals("breakdown")) {
      if (getPhaseName().equals(PHASE_ADMINISTRATIVE)) {
        int phasePart = RsrGlobal.getPhasePart() - 1;
        if (alternate) {
          if (phasePart == 3) {
            commandSwitchBreakdownMethod();
            sendAndLog();
          }
        } else {
          if ((phasePart == 1) || (phasePart == 3)) {
            commandBreakdown(getSelectedPieces());
            sendAndLog();
          }
        }
      }
    } else if (param.equals("buildup")) {
      if (getPhaseName().equals(PHASE_ADMINISTRATIVE)) {
        int phasePart = RsrGlobal.getPhasePart() - 1;
        if ((phasePart == 1) || (phasePart == 3)) {
          commandBuildup(getSelectedPieces());
          sendAndLog();
        }
      }
    } else if (param.equals("incorporate")) {
      if (getPhaseName().equals(PHASE_ADMINISTRATIVE)) {
        int phasePart = RsrGlobal.getPhasePart() - 1;
        if (phasePart == 3) {
          commandIncorporate(getSelectedPieces());
          sendAndLog();
        }
      }
    } else if (param.equals("germanSupply")) {
      showSupplyShading(1);
    } else if (param.equals("sovietSupply")) {
      showSupplyShading(4);
    } else if (param.equals("romanianSupply")) {
      showSupplyShading(2);
    } else if (param.equals("finnishSupply")) {
      showSupplyShading(3);
    } else if (param.equals("vp")) {
      calculateVP(true);
    }
  }

  protected void appendCommand(Command cmd) {
    if (cmd != null) {
      if (c == null) {
        c = cmd;
      } else {
        c = c.append(cmd);
      }
    }
  }

  public void sendAndLog() {
    if (c != null) {
      commandBaseClearAll();
      controlMarkers.clear();
      destroyedMarkers.clear();
      moveMode = MOVE_FREE;
      allowedMovement.clear();
      GameModule.getGameModule().sendAndLog(new OverstackCommand().append(c));
      c = null;
      mainMap.repaint();
      turnMap.repaint();
      offboardMap.repaint();
    }
  }

  public void clearAll() {
    airFleetCache.clear();
    supplyCache.clear();
    pieceCache.clear();
    controlMarkers.clear();
    destroyedMarkers.clear();
    moveMode = MOVE_FREE;
    allowedMovement.clear();
  }

  public void clearCommand() {
    c = null;
  }

  public void commandBaseClearAll() {
    appendCommand(new ClearCacheCommand());
  }

  public void commandBaseLog(String message) {
    Command cmd = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), message);
    cmd.execute();
    appendCommand(cmd);
  }

  public void commandLogMy(String message) {
    commandBaseLog("* " + GlobalOptions.getInstance().getPlayerId() + ": " + message);
  }

  public void commandBaseSetProperty(GamePiece gp, Object key, Object val) {
    pieceCache.clear(); //step down may cause loss of zoc
    ChangeTracker tracker = new ChangeTracker(gp);
    gp.setProperty(key.toString(), String.valueOf(val));
    appendCommand(tracker.getChangeCommand());
  }

  public void commandBaseSetProperty(GamePiece gp, Object... key_val) {
    pieceCache.clear(); //step down may cause loss of zoc
    ChangeTracker tracker = new ChangeTracker(gp);
    for (int i = 0; i < key_val.length; i += 2) {
      gp.setProperty(key_val[i].toString(), String.valueOf(key_val[i + 1]));
    }
    appendCommand(tracker.getChangeCommand());
  }

  public void commandBaseMove(Map map, GamePiece gp, Point p) {
    pieceCache.clear(); //moving units may cause change of zoc
    appendCommand(map.placeOrMerge(gp, p));
  }

  public void commandAdjustControlAndPartisans(Map map, ArrayList<GamePiece> pieces, Point p) {
    if ((map == mainMap) && (pieces.size() > 0)) {
      HexRef hr = mainGrid.getHexPos(p);
      GamePiece firstMover = pieces.get(0);
      String side = RsrTrait.getSide(firstMover);
      RsrAllowedMovement am = allowedMovement.get(firstMover);
      if (am != null) {
        TerrainMapShaderCost cost = am.getCost(hr);
        while (cost != null) {
          commandAdjustControl(hr, side, pieces);
          hr = getCommonFrom(pieces, hr);
          cost = am.getCost(hr);
        }
      } else {
        commandAdjustControl(hr, side, pieces);
      }
    }
  }

  private HexRef getCommonFrom(ArrayList<GamePiece> pieces, HexRef hr) {
    //
    variants.clear();
    //
    RsrAllowedMovement am = allowedMovement.get(pieces.get(0));
    TerrainMapShaderCost cost = am.getCost(hr);
    //
    if (cost.getVariants() != null) {
      variants.addAll(cost.getVariants());
    } else {
      variants.add(cost.getFrom());
    }
    //
    for (int i=1; i<pieces.size(); i++) {
      am = allowedMovement.get(pieces.get(i));
      cost = am.getCost(hr);
      if (cost.getVariants() != null) {
        variants.retainAll(cost.getVariants());
      } else {
        Iterator<HexRef> it = variants.iterator();
        HexRef hri;
        HexRef hrf = cost.getFrom();
        while (it.hasNext()) {
          hri = it.next();
          if ((hrf == null) && (hri == null)) {
            continue;
          } else
          if ((hrf == null) && (hri != null)) {
            it.remove();
          } else
          if ((hrf != null) && (hri == null)) {
            it.remove();
          } else
          if (hrf.equals(hri) == false) {
            it.remove();
          }
        }
      }
    }
    //
    for (HexRef hrf : variants) {
      return hrf;
    }
    return null;
  }

  private void commandAdjustControl(HexRef hr, String side, ArrayList<GamePiece> pieces) {
    if (controlMarkers.size() == 0) {
      for (GamePiece gpf : filter.filterPieces(mainMap, "MarkerType=='Control'")) {
        controlMarkers.put(mainGrid.getHexPos(gpf.getPosition()), gpf);
      }
    }
    if (destroyedMarkers.size() == 0) {
      for (GamePiece gpf : filter.filterPieces(mainMap, "MarkerType=='Destruction'")) {
        destroyedMarkers.put(mainGrid.getHexPos(gpf.getPosition()), gpf);
      }
    }
    //include units already is this space
    ArrayList<GamePiece> allPieces = new ArrayList<GamePiece>(pieces);
    for (GamePiece gp : filter.filterPieces(pieceCache.getPiecesIn(hr), "Side=='" + side + "'")) {
      if (allPieces.contains(gp) == false) {
        allPieces.add(gp);
      }
    }
    if (canContestNeighbourSpace1(allPieces)) {
      GamePiece gpc = controlMarkers.get(hr);
      if (gpc != null) {
        int desiredControl = side.equals(SIDE_SOVIET) ? 1 : 2;
        if (RsrCounter.getIntProperty(gpc, "Control", 1, false) != desiredControl) {
          commandBaseSetProperty(gpc, "Control", String.valueOf(desiredControl));
          commandBaseLog("Space " + getLocationName(hr) + " is now under " + side + " control.");
        }
        if (desiredControl == 2) {
          GamePiece gpd = destroyedMarkers.get(hr);
          if (gpd != null) {
            if (RsrCounter.getIntProperty(gpd, "Destruction", 1, false) != desiredControl) {
              commandBaseSetProperty(gpd, "Destruction", String.valueOf(desiredControl));
              commandBaseLog("Space " + getLocationName(hr) + " is now destroyed.");
              String message = "";
              if (hr.equals(getCityHexRef(TAG_CITY_MOSCOW))) {
                int mod = 0;
                if (isCityAxisControlled(TAG_CITY_LENINGRAD)) {
                  mod--;
                }
                if (isCityAxisControlled(TAG_CITY_BAKU)) {
                  mod--;
                }
                if ((isCityAxisControlled(TAG_CITY_STALINGRAD)) && (isCityAxisControlled(TAG_CITY_SEVASTOPOL))) {
                  mod--;
                }
                int die = ran.nextInt(6) + 1;
                int result = Math.max(1, die + mod);
                if (result == 1) {
                  message = "Soviets surrender!";
                } else
                if ((result == 2) || (result == 3)) {
                  message = "Soviet morale weakened!";
                } else {
                  message = "Soviets fight on!";
                }
                commandBaseLog("Moscow surrender die roll is " + die + (mod < 0 ? " with " + mod + " modifier" : "") + ": " + message);
              } else if (hr.equals(getCityHexRef(TAG_CITY_BAKU))) {
                int mod = 0;
                if (isCityAxisControlled(TAG_CITY_LENINGRAD)) {
                  mod--;
                }
                if (isCityAxisControlled(TAG_CITY_MOSCOW)) {
                  mod--;
                }
                if ((isCityAxisControlled(TAG_CITY_STALINGRAD)) && (isCityAxisControlled(TAG_CITY_SEVASTOPOL))) {
                  mod--;
                }
                int die = ran.nextInt(6) + 1;
                int result = Math.max(1, die + mod);
                if (result == 1) {
                  message = "Soviets surrender!";
                } else {
                  message = "Soviets fight on!";
                }
                commandBaseLog("Baku surrender die roll is " + die + (mod < 0 ? " with " + mod + " modifier" : "") + ": " + message);
              }
              if (message.equals("Soviets surrender!")) {
                commandBaseSetProperty(dataPiece, G_GAME_STATUS, 2);
              } else if (message.equals("Soviet morale weakened!")) {
                for (GamePiece fgp : findInOffboardZone("At-Start Strength Marker Box", "Strength", null, Integer.MAX_VALUE)) {
                  commandSendToOffboardZone(fgp, RsrTrait.getStrengthClass(fgp) + "-strength pool");
                }
              }
            }
          }
        }
      }
    }
    if ((side.equals(SIDE_AXIS)) && (canContestOwnSpace(allPieces))) {
      int range = 0;
      for (GamePiece gp : allPieces) {
        if (RsrTrait.getType(gp).equals("SecurityDivision")) {
          range = 2;
          break;
        }
      }
      int count = 0;
      ArrayList<GamePiece> list;
      for (HexRef hrf : mainGrid.getHexesInRange(hr, range)) {
        list = pieceCache.getPiecesIn(hrf);
        if (list != null) {
          for (GamePiece gp : list) {
            if (RsrTrait.getType(gp).equals("Partisans")) {
              if (getPartisanRepositionHexes(gp).size() == 0) {
                commandEliminate(gp, false, false);
              } else {
                commandBaseSetProperty(gp, T_REPOSITIONING, 1);
                count++;
              }
            }
          }
        }
      }
      if (count > 0) {
        commandBaseSetProperty(dataPiece, G_REPOSITIONING_REQUIRED, count);
        commandBaseLog("There are partisans units to reposition.");
      }
    }
  }

  public void commandBaseReportMoves() {
    MovementReporter r = new MovementReporter(c);
    Command cmd = r.getReportCommand();
    if (cmd != null) {
      cmd.execute();
    }
    appendCommand(cmd);
  }

  public void commandBaseDelete(GamePiece gp) {
    pieceCache.clear(); //deleting units may cause change of zoc
    Command cmd = new RemovePiece(gp);
    cmd.execute();
    appendCommand(cmd);
  }

  public GamePiece findRandomStrength(String strengthClass) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    for (GamePiece gp : filter.filterPieces(offboardMap, "CurrentZone=='" + strengthClass + "-strength pool'&&Type=='Strength'&&StrengthClass=='" + strengthClass + "'")) {
      result.add(gp);
    }
    int size = result.size();
    if (size == 0) {
      return null;
    } else if (size == 1) {
      return result.get(0);
    } else {
      return result.get(ran.nextInt(size));
    }
  }

  public GamePiece findStrength(String strengthClass, String id) {
    if ((id == null) || (id.length() == 0)) {
      return null;
    }
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    for (GamePiece gp : filter.filterPieces(offboardMap, "CurrentZone=='" + strengthClass + "-used strength pool'&&Type=='Strength'&&StrengthClass=='" + strengthClass + "'&&BasicName=='" + id + "'")) {
      result.add(gp);
    }
    int size = result.size();
    if (size == 0) {
      return null;
    } else if (size == 1) {
      return result.get(0);
    } else {
      return result.get(ran.nextInt(size));
    }
  }

  public ArrayList<GamePiece> findInOffboardZone(String offboardZone, String type, String unitType, int count) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    for (GamePiece gp : filter.filterPieces(offboardMap, "CurrentZone=='" + offboardZone + "'&&Type=='" + type + (unitType != null ? "'&&UnitType=='" + unitType + "'" : "'"))) {
      if (result.size() < count) {
        result.add(gp);
      } else {
        break;
      }
    }
    return result;
  }

  public void commandUpdateStrength(GamePiece gp, GamePiece sgp) {
    if (sgp == null) {
      commandBaseSetProperty(gp, T_STRENGTH_ID, "");
    } else {
      commandBaseSetProperty(gp, T_STRENGTH_ID, sgp.getProperty("BasicName"));
    }
  }

  public void commandFetchCounter(GamePiece gp) {
    GamePiece sgp = findRandomStrength(RsrTrait.getCombatClass(gp));
    if (sgp != null) {
      commandSendToOffboardZone(sgp, RsrTrait.getCombatClass(gp) + "-used strength pool");
      commandUpdateStrength(gp, sgp);
      commandBaseLog(gp.getName() + " fetched a Combat Strength Marker.");
    }
  }

  public void commandReleaseCounter(GamePiece gp) {
    GamePiece sgp = findStrength(RsrTrait.getCombatClass(gp), RsrTrait.getStrengthID(gp));
    if (sgp != null) {
      commandBaseSetProperty(gp, T_CURRENT_STEP, 2, T_STRENGTH_ID, "");
      if (RsrTrait.getStrengthType(sgp).equals("Normal")) {
        commandSendToOffboardZone(sgp, RsrTrait.getCombatClass(gp) + "-strength pool");
      } else {
        commandSendToOffboardZone(sgp, "At-Start Strength Marker Box");
      }
      commandBaseLog(gp.getName() + " released a Combat Strength Marker.");
    }
  }

  public void commandSendToOffboardZone(GamePiece gp, String offboardZone) {
    //
    Zone z = offboardMap.findZone(offboardZone);
    if (z == null) {
      return;
    }
    //
    if (RsrTrait.getType(gp).equals("Strength") == false) {
      commandReleaseCounter(gp);
    }
    //
    Rectangle r1 = z.getBounds();
    Rectangle r2 = z.getBoard().bounds();
    Point dest = offboardMap.snapTo(new Point(r2.x + r1.x + r1.width / 2, r2.y + r1.y + r1.height / 2));
    //
    commandBaseMove(offboardMap, gp, dest);
    RsrTrait.commandClearAttributes(gp);
    commandBaseLog(gp.getName() + " was sent to " + offboardZone + ".");
  }

  public void commandSendToReinforcementZone(GamePiece gp, int turnNumberMod, String offboardZone) {
    //
    int turnNumber = getTurnNumber() + turnNumberMod;
    if ((turnNumber < 1) || (turnNumber > 40)) {
      if (offboardZone != null) {
        commandSendToOffboardZone(gp, offboardZone);
      }
      return;
    }
    //
    Zone z = turnMap.findZone(RsrTrait.getSide(gp) + " Track");
    if (z == null) {
      return;
    }
    //
    Rectangle r2 = z.getBoard().bounds();
    Point dest = turnMap.snapTo(new Point(r2.x + (RsrTrait.getSide(gp).equals(SIDE_AXIS) ? 272 : 166), r2.y + 50 + (turnNumber - 1) * 91));
    String regionName = z.locationName(dest);
    Region r = turnMap.findRegion(regionName);
    if (r == null) {
      return;
    }
    //
    if (RsrTrait.getType(gp).equals("Strength") == false) {
      commandReleaseCounter(gp);
    }
    //
    commandBaseMove(turnMap, gp, dest);
    RsrTrait.commandClearAttributes(gp);
    commandBaseLog(gp.getName() + " will return as reinforcement on turn " + regionName.substring(1) + ".");
  }

  public void commandSendToPiece(GamePiece gp, GamePiece rgp, boolean copyProperties) {
    commandBaseMove(gp.getMap(), rgp, gp.getPosition());
    if (copyProperties) {
      RsrTrait.commandCopyAttributes(gp, rgp);
    }
    commandBaseLog(rgp.getName() + " was sent to " + gp.getName() + "'s location.");
  }

  public void commandParadrop(HexRef hr, ArrayList<GamePiece> pieces) {
    int modifier = 0;
    if ((isMud()) || (isWinter())) {
      modifier -= 3;
    }
    if (pieceCache.getHexesInZOCEnemy(SIDE_SOVIET).contains(hr)) {
      modifier += 1;
    }
    int die = ran.nextInt(6) + 1 + modifier;
    if (die >= 3) {
      for (GamePiece gp : pieces) {
        commandEliminate(gp, false, false);
      }
      pieces.clear();
    }
    commandAdvance(true);
  }

  public void commandPromote(GamePiece gp) {
    ArrayList<GamePiece> converts = findInOffboardZone("Awaiting Promotion Box", "Unit", null, 1);
    if (converts.size() > 0) {
      GamePiece pgp = converts.get(0);
      String strengthID = RsrTrait.getStrengthID(gp);
      commandSendToPiece(gp, pgp, false);
      if (strengthID.length() > 0) {
        commandReleaseCounter(gp);
      }
      commandSendToOffboardZone(gp, "Promoted Army Box");
      if (strengthID.length() > 0) {
        commandFetchCounter(pgp);
      }
    }
  }

  public void commandEliminate(GamePiece gp, boolean inCombat, boolean inRetreat) {
    String type = RsrTrait.getType(gp);
    int supply = RsrTrait.getSupplyLevel(gp);
    if (((supply == SUPPLY_LACK) || (supply == SUPPLY_OUT)) && (inRetreat)) {
      commandSendToOffboardZone(gp, RsrTrait.getSide(gp) + " Permanently Eliminated Units Box");
    } else {
      if (type.equals("Unit")) {
        String unitType = RsrTrait.getUnitType(gp);
        if (unitType.equals("SovietTankArmy")) {
          int roll = ran.nextInt(6) + 1;
          if (roll <= (supply == 0 ? 1 : 2)) {
            commandSendToOffboardZone(gp, "Soviet Permanently Eliminated Units Box");
            for (GamePiece fgp : findInOffboardZone("Force Pool Box", "Unit", "SovietMechanizedCorps", 1)) {
              commandSendToReinforcementZone(fgp, 2, null);
            }
          } else {
            commandSendToReinforcementZone(gp, 2 * roll, "Soviet Permanently Eliminated Units Box");
          }
        } else if ((unitType.equals("SovietGuardsArmy")) || (unitType.equals("SovietShockArmy"))) {
          int roll = ran.nextInt(6) + 1;
          if (roll <= (supply == 0 ? 1 : 2)) {
            ArrayList<GamePiece> promoted = findInOffboardZone("Promoted Army Box", "Unit", "SovietInfantryArmy", 1);
            if (promoted.size() == 0) {
              commandSendToOffboardZone(gp, "Soviet Permanently Eliminated Units Box");
            } else {
              commandSendToOffboardZone(gp, "Awaiting Promotion Box");
              commandSendToOffboardZone(promoted.get(0), "Soviet Permanently Eliminated Units Box");
            }
          } else {
            commandSendToOffboardZone(gp, "Awaiting Promotion Box");
            for (GamePiece fgp : findInOffboardZone("Promoted Army Box", "Unit", "SovietInfantryArmy", 1)) {
              roll = ran.nextInt(6) + 1;
              commandSendToReinforcementZone(fgp, 2 * roll, null);
            }
          }
        } else if (unitType.equals("SovietInfantryArmy")) {
          int roll = ran.nextInt(6) + 1;
          if (roll <= (supply == 0 ? 1 : 2)) {
            commandSendToOffboardZone(gp, "Soviet Permanently Eliminated Units Box");
            for (GamePiece fgp : findInOffboardZone("Force Pool Box", "Unit", "SovietInfantryCorps", 1)) {
              commandSendToReinforcementZone(fgp, 2, null);
            }
          } else {
            commandSendToReinforcementZone(gp, 2 * roll, "Soviet Permanently Eliminated Units Box");
          }
        } else if ((unitType.equals("SovietInfantryCorps")) || (unitType.equals("SovietMarineCorps")) || (unitType.equals("SovietMechanizedCorps"))) {
          commandSendToOffboardZone(gp, "Force Pool Box");
        } else if ((unitType.equals("SovietAirborneCorps")) || (unitType.equals("SovietGuardsCavalryCorps"))) {
          commandSendToOffboardZone(gp, "Soviet Permanently Eliminated Units Box");
        } else if ((unitType.equals("AxisGermanTankCorps")) || (unitType.equals("AxisGermanInfantryCorps")) || (unitType.equals("AxisGermanMountainCorps")) || (unitType.equals("AxisGermanTankBattlegroup")) || (unitType.equals("AxisGermanInfantryBattlegroup")) || (unitType.equals("AxisGermanMountainBattlegroup"))) {
          commandSendToOffboardZone(gp, "German Breakdown & Buildup Box");
        } else if ((unitType.equals("AxisGermanTankDivision")) || (unitType.equals("AxisItalianArmy")) || (unitType.equals("AxisHungarianArmy")) || (unitType.equals("AxisFinnishInfantryCorps")) || (unitType.equals("AxisRomanianInfantryCorps")) || (unitType.equals("AxisRomanianMountainCorps"))) {
          commandSendToOffboardZone(gp, "Axis Permanently Eliminated Units Box");
        }
      } else if (type.equals("Leader")) {
        if (inCombat) {
          commandSendToOffboardZone(gp, "Soviet Permanently Eliminated Units Box");
        } else {
          commandSendToOffboardZone(gp, "Leader pool");
        }
      } else if (type.equals("Headquarters")) {
        int roll = ran.nextInt(6) + 1;
        if (roll <= 3) {
          commandSendToReinforcementZone(gp, 1, RsrTrait.getSide(gp) + " Permanently Eliminated Units Box");
        } else {
          commandSendToReinforcementZone(gp, 2, RsrTrait.getSide(gp) + " Permanently Eliminated Units Box");
        }
      } else if ((type.equals("Depot")) || (type.equals("Airbase")) || (type.equals("AirFleet"))) {
        commandSendToReinforcementZone(gp, 4, "Axis Permanently Eliminated Units Box");
      } else if ((type.equals("SiegeArtillery")) || (type.equals("SecurityDivision"))) {
        commandSendToOffboardZone(gp, "Axis Permanently Eliminated Units Box");
      } else if (type.equals("Partisans")) {
        commandSendToOffboardZone(gp, "Soviet Permanently Eliminated Units Box");
      } else if (type.equals("Asset")) {
        commandSendToOffboardZone(gp, "Eliminated Economic Assets");
      }
    }
  }

  public void commandStepLoss(GamePiece gp, boolean inCombat, boolean inRetreat) {
    int currentStep = RsrTrait.getCurrentStep(gp);
    String type = RsrTrait.getType(gp);
    String unitType = RsrTrait.getUnitType(gp);
    boolean isAxisGermanCorps = Filter.isAny(unitType, "AxisGermanTankCorps", "AxisGermanInfantryCorps", "AxisGermanMountainCorps");
    if ((currentStep == 1) && ((type.equals("Unit") == false) || (isAxisGermanCorps == false))) {
      commandEliminate(gp, inCombat, inRetreat);
    } else {
      if (currentStep == 2) {
        commandBaseSetProperty(gp, T_CURRENT_STEP, 1);
        commandUpdateStrength(gp, findStrength(RsrTrait.getCombatClass(gp), RsrTrait.getStrengthID(gp)));
        commandBaseLog(gp.getName() + " lost a step.");
      } else if ((currentStep == 1) && (isAxisGermanCorps)) {
        if (unitType.equals("AxisGermanTankCorps")) {
          for (GamePiece fgp : findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanTankBattlegroup", 1)) {
            commandSendToPiece(gp, fgp, true);
          }
        } else if (unitType.equals("AxisGermanInfantryCorps")) {
          for (GamePiece fgp : findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanInfantryBattlegroup", 1)) {
            commandSendToPiece(gp, fgp, true);
          }
        } else {
          for (GamePiece fgp : findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanMountainBattlegroup", 1)) {
            commandSendToPiece(gp, fgp, true);
            commandBaseSetProperty(fgp, T_CURRENT_STEP, 1);
          }
        }
        commandSendToOffboardZone(gp, "German Breakdown & Buildup Box");
      }
    }
  }

  public void commandEvacuate(GamePiece gp) {
    commandSendToOffboardZone(gp, "Evacuated Factories Box");
    int newValue = RsrGlobal.getEvacuated()+1;
    commandBaseSetProperty(dataPiece, G_EVACUATED, newValue);
    if (newValue >= 3) {
      commandAdvance(true);
    }
  }

  public void commandAdvance(boolean auto) {
    if (canAdvance(auto) == false) {
      return;
    }
    autoAdvance = true;
    while (autoAdvance) {
      autoAdvance = false;
      int phaseParts = RsrGlobal.getPhaseParts();
      if (phaseParts > 0) {
        int phasePart = RsrGlobal.getPhasePart();
        if (phasePart > phaseParts) {
          executeLeavePhase();
          Point p = phasePiece.getPosition();
          if (p.y == 701) {
            //
            int length = 40;
            switch (RsrGlobal.getScenario()) {
              case 1 :
              case 3 : length = 24; break;
              case 2 : length = 12; break;
            }
            if (getTurnNumber() == length) {
              commandBaseSetProperty(dataPiece, G_GAME_STATUS, 1);
              commandGameOverMessage();
              p.y = 0;
            } else {
              Point p2 = turnPiece.getPosition();
              if (p2.y < 3599) {
                p2.translate(0, 91);
                commandBaseMove(turnMap, turnPiece, p2);
                commandBaseLog("Turn changes to " + turnGrid.locationName(p2) + ".");
              }
              p.y = 45;
            }
          } else {
            p.translate(0, 82);
          }
          if (p.y > 0) {
            commandBaseMove(turnMap, phasePiece, p);
            commandBaseLog("Phase changes to " + turnGrid.locationName(p) + ".");
            executeEnterPhase();
          }
        } else {
          executePart(phasePart);
        }
      } else {
        executeFirst();
      }
    }
  }

  private void executeFirst() {
    if (RsrGlobal.getScenario() == 4) {
      ArrayList<GamePiece> reserveHQ = filter.filterPieces(mainMap, "BasicName=='Reserve HQ'");
      ArrayList<GamePiece> leadersInPool = findInOffboardZone("Leader pool", "Leader", null, Integer.MAX_VALUE);
      if ((reserveHQ.size() == 1) && (leadersInPool.size() > 0)) {
        GamePiece gp = reserveHQ.get(0);
        GamePiece lgp = leadersInPool.get(ran.nextInt(leadersInPool.size()));
        commandSendToPiece(gp, lgp, false);
      }
    }
    executeEnterPhase();
  }

  private void executeLeavePhase() {
    executePartPhase(RsrGlobal.getPhaseParts() + 1);
  }

  private void executeEnterPhase() {
    executeSetPhaseParts();
    executePartPhase(0);
    executePart(1);
  }

  private void executePart(int part) {
    executePartPhase(part);
    commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PART, part + 1);
  }

  protected abstract void executeSetPhaseParts();
  protected abstract void executePartPhase(int part);

  public void commandAllowCombatAction(GamePiece gp) {
    commandBaseSetProperty(gp, T_HL_LOSS, 1);
  }

  public void commandDisallowCombatActionAll() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_HL_LOSS + ">0")) {
      commandDisallowCombatAction(gp);
    }
  }

  public void commandDisallowCombatAction(GamePiece gp) {
    commandBaseSetProperty(gp, T_HL_LOSS, 0);
  }

  public void commandAllowAction(GamePiece gp) {
    commandBaseSetProperty(gp, T_HL_ACTION, 1);
  }

  public void commandDisallowActionAll() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_HL_ACTION + ">0")) {
      commandDisallowAction(gp);
    }
  }

  public void commandDisallowAction(GamePiece gp) {
    commandBaseSetProperty(gp, T_HL_ACTION, 0);
  }

  public void commandAllowMovement(GamePiece gp, double mp, boolean newMove) {
    if (newMove) {
      if (gp.getMap() == mainMap) {
        commandBaseSetProperty(gp, T_MOVEMENT_LEFT, mp, T_HL_MOVEMENT, 1, T_START_HEX, mainGrid.locationName(gp.getPosition()), T_MOVE_INDEX, 0);
      } else {
        commandBaseSetProperty(gp, T_MOVEMENT_LEFT, mp, T_HL_MOVEMENT, 1, T_START_HEX, "", T_MOVE_INDEX, 0);
      }
    } else {
      commandBaseSetProperty(gp, T_MOVEMENT_LEFT, mp, T_HL_MOVEMENT, 1);
    }
  }

  public void commandDisallowMovementAll() {
    int moveIndex = RsrGlobal.getLastMovedIndex() + 1;
    if (moveIndex > 1) {
      commandBaseSetProperty(dataPiece, PROPERTY_LAST_MOVED_INDEX, moveIndex);
      for (GamePiece gp : filter.filterPieces(mainMap, T_MOVE_INDEX + ">0&&" + T_MOVE_INDEX + "<" + moveIndex)) {
        commandDisallowMovement(gp);
        commandMarkAsMoved(gp);
      }
    }
    for (GamePiece gp : filter.filterPieces(mainMap, T_HL_MOVEMENT + ">0")) {
      commandDisallowMovement(gp);
    }
  }

  public void commandDisallowMovement(GamePiece gp) {
    commandBaseSetProperty(gp, T_MOVEMENT_LEFT, 0.0d, T_HL_MOVEMENT, 0, T_START_HEX, "", T_MOVE_INDEX, 0);
  }

  public void commandMarkAsMoved(GamePiece gp) {
    commandBaseSetProperty(gp, T_MOVED, 1);
  }

  public void commandMarkAsUnmovedAll() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_MOVED + ">0")) {
      commandBaseSetProperty(gp, T_MOVED, 0);
    }
  }

  public void commandMarkAsReorganized(GamePiece gp) {
    commandBaseSetProperty(gp, T_REORGANIZED, 1);
  }

  public void commandMarkAsUnreorganizedAll() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_REORGANIZED + ">0")) {
      commandBaseSetProperty(gp, T_REORGANIZED, 0);
    }
  }

  public void commandSetMovementIndex(ArrayList<GamePiece> pieces) {
    int moveIndex = RsrGlobal.getLastMovedIndex() + 1;
    for (GamePiece gp : pieces) {
      commandBaseSetProperty(gp, T_MOVE_INDEX, moveIndex);
    }
    commandBaseSetProperty(dataPiece, PROPERTY_LAST_MOVED_INDEX, moveIndex);
    for (GamePiece gp : filter.filterPieces(mainMap, T_MOVE_INDEX + ">0&&" + T_MOVE_INDEX + "<" + moveIndex)) {
      commandDisallowMovement(gp);
      commandMarkAsMoved(gp);
    }
  }

  public void commandResetMovementIndex() {
    commandBaseSetProperty(dataPiece, PROPERTY_LAST_MOVED_INDEX, 0);
  }

  public HexRef getStartHex(GamePiece gp) {
    if (gp.getMap() == mainMap) {
      String startHex = RsrTrait.getStartHex(gp);
      if (startHex.length() > 0) {
        try {
          return mainGrid.getHexPos(mainGrid.getLocation(startHex));
        } catch (MapGrid.BadCoords e) {
          return null;
        }
      }
    }
    return null;
  }

  public HexRef getCombatHex(GamePiece gp) {
    if (gp.getMap() == mainMap) {
      String startHex = RsrTrait.getCombatHex(gp);
      if (startHex.length() > 0) {
        try {
          return mainGrid.getHexPos(mainGrid.getLocation(startHex));
        } catch (MapGrid.BadCoords e) {
          return null;
        }
      }
    }
    return null;
  }

  public void commandGameOverMessage() {
    int status = RsrGlobal.getGameStatus();
    if (status == 2) {
      commandBaseLog("Game is over (Really, Really Big German Win).");
    } else {
      commandBaseLog("Game is over (" + calculateVP(false) + ").");
    }
  }

  public boolean canAdvance(boolean auto) {
    if ((auto == false) && (checkSide(RsrGlobal.getPhaseOwner()) == false)) {
      return false;
    }
    if (RsrGlobal.getGameStatus() > 0) {
      commandGameOverMessage();
      return false;
    }
    if (RsrGlobal.getRepositioningRequired() > 0) {
      commandBaseLog("Reposition partisans unit(s) first.");
      return false;
    }
    if (RsrGlobal.getCombatStage() > 0) {
      commandBaseLog("Resolve or cancel combat first.");
      return false;
    }
    if (filter.filterPieces(turnMap, T_HL_MOVEMENT + ">0").size() > 0) {
      commandBaseLog("Put reinforcements on map first.");
      return false;
    }
    return true;
  }

  public boolean canResolve(boolean auto) {
    if ((auto == false) && (checkSide(RsrGlobal.getPhaseOwner()) == false)) {
      return false;
    }
    if (RsrGlobal.getGameStatus() > 0) {
      commandGameOverMessage();
      return false;
    }
    if (RsrGlobal.getRepositioningRequired() > 0) {
      commandBaseLog("Reposition partisans unit(s) first.");
      return false;
    }
    return true;
  }

  public boolean canCommand(GamePiece gp) {
    if (checkSide(RsrTrait.getSide(gp)) == false) {
      return false;
    }
    if (RsrGlobal.getGameStatus() > 0) {
      commandGameOverMessage();
      return false;
    }
    if ((RsrTrait.getRepositioning(gp) == 0) && (RsrGlobal.getRepositioningRequired() > 0)) {
      commandBaseLog("Reposition partisans unit(s) first.");
      return false;
    }
    return true;
  }

  private boolean checkSide(String side) {
    String mySide = PlayerRoster.getMySide();
    if (mySide == null) {
      return false;
    } else {
      return mySide.equals("Solo") || mySide.equals(side) || (side.equals(""));
    }
  }

  public void commandSetPhaseOwner(String side) {
    commandBaseSetProperty(dataPiece, G_PHASE_OWNER, side);
  }

  public void commandTogglePhaseOwner() {
    String side = RsrGlobal.getPhaseOwner();
    if (side.equals(SIDE_AXIS)) {
      commandSetPhaseOwner(SIDE_SOVIET);
    } else if (side.equals(SIDE_SOVIET)) {
      commandSetPhaseOwner(SIDE_AXIS);
    }
  }

  public void commandClearCombat(boolean auto) {
    if (canResolve(auto) == false) {
      return;
    }
    int combatStage = RsrGlobal.getCombatStage();
    if ((combatStage == 1) || (combatStage == 6)) {
      boolean overrun = RsrGlobal.getCombatMode() == 1;
      commandLogMy("Combat " + (combatStage == 1 ? "cancelled" : "ended") + ".");
      for (GamePiece gp : filter.filterPieces(mainMap, T_ATTACKER + ">0||" + T_DEFENDER + ">0")) {
        if ((RsrTrait.getAttacker(gp) > 0) && (combatStage == 6) && (overrun == false)) {
          commandDisallowMovement(gp);
        }
        RsrTrait.commandClearCombatAttributes(gp, (RsrTrait.getDefender(gp) > 0) && (combatStage == 6) && (overrun == false));
      }
      //set combat parameters
      commandBaseSetProperty(dataPiece, COMBAT_STAGE, 0,
                                    COMBAT_MODE, 0,
                                    ATTACKER_HEXES, "",
                                    DEFENDER_HEXES, "",
                                    ATTACKER_LOSS, 0,
                                    ATTACKER_RETREAT, 0,
                                    DEFENDER_LOSS, 0,
                                    DEFENDER_RETREAT, 0,
                                    ATTACKER_LOST, 0,
                                    DEFENDER_LOST, 0,
                                    URBAN_LOSS_REQUIRED, 0,
                                    MOTORIZED_LOSS_REQUIRED, 0,
                                    CONTESTED_LOSS_REQUIRED, 0,
                                    G_ATTACKER_SIDE, "",
                                    G_DEFENDER_SIDE, "");
    }
  }

  /**
   *
   * @param pieces List of moving pieces (stack) that must come from one hex
   * @param target Target hex
   * @param overrun Is this an overrun combat?
   */
  public void commandAddToCombat(ArrayList<GamePiece> pieces, HexRef target, boolean overrun) {
    int combatStage = RsrGlobal.getCombatStage();
    //do not add more units to combat if overrun combat already set-up or combat is on further stages
    if ((RsrGlobal.getCombatMode() == 1) || (combatStage > 1)) {
      return;
    }
    GamePiece firstAttacker = pieces.get(0);

    String attackerSide = RsrTrait.getSide(firstAttacker);
    String defenderSide = attackerSide.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS;

    HexRef source = mainGrid.getHexPos(firstAttacker.getPosition());

    String attackerHex = mainGrid.locationName(mainGrid.getHexCenter(source.getColumn(), source.getRow()));
    String defenderHex = mainGrid.locationName(mainGrid.getHexCenter(target.getColumn(), target.getRow()));

    CombatHexList attackerHexes = new CombatHexList(RsrGlobal.getAttackerHexes());
    CombatHexList defenderHexes = new CombatHexList(RsrGlobal.getDefenderHexes());
    
    attackerHexes.add(attackerHex);
    defenderHexes.add(defenderHex);

    //do not add more units to combat if it would cause more than 1 hex involved per side (only one-to-many and many-to-one allowed)
    if ((attackerHexes.size() > 1) && (defenderHexes.size() > 1)) {
      return;
    }

    int strength, totalInvolved;
    LinkedHashMap<GamePiece, Integer> strengthA = new LinkedHashMap<GamePiece, Integer>();
    LinkedHashMap<GamePiece, Integer> strengthD = new LinkedHashMap<GamePiece, Integer>();
    for (GamePiece gp : pieces) {
      if (RsrTrait.getAttacker(gp) == 0) {
        strength = getAttackStrength(gp, true);
        if (strength != 0) {
          strengthA.put(gp, strength);
        }
      }
    }
    totalInvolved = strengthA.size() + filter.filterPieces(mainMap, T_ATTACKER + ">0").size();
    //do not add more units to combat if it would cause one-to-many combat with more than one unit
    if ((totalInvolved == 0) || ((totalInvolved > 1) && (defenderHexes.size() > 1))) {
      return;
    }
    for (GamePiece gp : filter.filterPieces(pieceCache.getPiecesIn(target), "Side=='" + defenderSide + "'&&Type!='AirFleet','Partisans'")) {
      if (RsrTrait.getDefender(gp) == 0) {
        strength = getDefendStrength(gp, true);
        if (strength != 0) {
          strengthD.put(gp, strength);
        }
      }
    }
    if (strengthD.size() + filter.filterPieces(mainMap, T_DEFENDER + ">0").size() == 0) {
      return;
    }
    if (combatStage == 0) {
      commandLogMy((overrun ? "Overrun combat" : "Combat") + " initiated in space " + defenderHex + ".");
    }
    for (GamePiece gp : strengthA.keySet()) {
      commandBaseSetProperty(gp, T_ATTACKER, 1, T_COMBAT_HEX, attackerHex);
      strength = strengthA.get(gp).intValue();
      if (strength > 0) {
        commandLogMy(gp.getName() + " will attack with strength " + strength + ".");
      } else {
        commandLogMy(gp.getName() + " will attack with unknown strength.");
      }
    }
    for (GamePiece gp : strengthD.keySet()) {
      commandBaseSetProperty(gp, T_DEFENDER, 1, T_COMBAT_HEX, defenderHex);
      strength = strengthD.get(gp).intValue();
      if (strength > 0) {
        commandLogMy(gp.getName() + " will defend with strength " + strength + ".");
      } else {
        commandLogMy(gp.getName() + " will defend with unknown strength.");
      }
    }
    commandBaseSetProperty(dataPiece, COMBAT_STAGE, 1,
                                  COMBAT_MODE, overrun ? 1 : 0,
                                  ATTACKER_HEXES, attackerHexes.toString(),
                                  DEFENDER_HEXES, defenderHexes.toString(),
                                  G_ATTACKER_SIDE, attackerSide,
                                  G_DEFENDER_SIDE, defenderSide);
    //warning about possible losses to motorized forces attacking urban area
    if (overrun == false) {
      boolean urbanLoss = false;
      for (HexRef hr : defenderHexes.getHexes()) {
        if (isMajorCity(mainTerrain.getHexTerrainName(hr))) {
          urbanLoss = true;
          break;
        }
      }
      if (urbanLoss) {
        int steps;
        int totalSteps = 0;
        int motorizedSteps = 0;
        for (GamePiece gp : filter.filterPieces(mainMap, T_ATTACKER + ">0")) {
          steps = RsrTrait.getCurrentNominalSteps(gp);
          totalSteps += steps;
          if (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED) {
            motorizedSteps += steps;
          }
        }
        urbanLoss = 2*motorizedSteps > totalSteps;
        if (urbanLoss) {
          commandBaseLog("Warning! Additional losses may apply to motorized forces attacking urban area (" + motorizedSteps + " step(s) out of " + totalSteps + " step(s) are motorized).");
        }
      }      
    }
  }

  public void commandResolveCombat(boolean auto) {
    if (canResolve(auto) == false) {
      return;
    }
    int combatStage = RsrGlobal.getCombatStage();
    int combatMode = RsrGlobal.getCombatMode();
    ArrayList<GamePiece> attackers = filter.filterPieces(mainMap, T_ATTACKER + ">0");
    ArrayList<GamePiece> defenders = filter.filterPieces(mainMap, T_DEFENDER + ">0");
    CombatHexList attackerHexes = new CombatHexList(RsrGlobal.getAttackerHexes());
    CombatHexList defenderHexes = new CombatHexList(RsrGlobal.getDefenderHexes());
    boolean ignoreOptional = true;
    if (combatStage == 1) {
      commandLogMy("Combat resolution");
      //check if urban loss will apply
      if (combatMode == 0) {
        boolean urbanLoss = false;
        for (HexRef hr : defenderHexes.getHexes()) {
          if (isMajorCity(mainTerrain.getHexTerrainName(hr))) {
            urbanLoss = true;
            break;
          }
        }
        if (urbanLoss) {
          int steps;
          int totalSteps = 0;
          int motorizedSteps = 0;
          for (GamePiece gp : attackers) {
            steps = RsrTrait.getCurrentNominalSteps(gp);
            totalSteps += steps;
            if (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED) {
              motorizedSteps += steps;
            }
          }
          urbanLoss = 2*motorizedSteps > totalSteps;
          if (urbanLoss) {
            commandBaseLog("Additional losses will apply to motorized forces attacking urban area (" + motorizedSteps + " step(s) out of " + totalSteps + " step(s) are motorized).");
            commandBaseSetProperty(dataPiece, URBAN_LOSS_REQUIRED, 1);
          }
        }
      }
      //combatMode += 2 if urban mech loss is to be applied
      String result = getCombatResult(attackers, defenders, attackerHexes, defenderHexes);
      int al = 0;
      int ar = 0;
      int dl = 0;
      int dr = 0;
      if (result.equals("NE")) {
        //NO EFFECT
      } else if (result.equals("DE")) {
        //DEFENDER ELIMINATED
        for (GamePiece gp : defenders) {
          commandEliminate(gp, true, false);
        }
        defenders.clear();
      } else if (result.equals("AE")) {
        //ATTACKER ELIMINATED
        for (GamePiece gp : attackers) {
          commandEliminate(gp, true, false);
        }
        attackers.clear();
      } else if ((result.equals("D1")) || (result.equals("D2")) || (result.equals("D3"))) {
        //DEFENDER RETREAT
        if (result.equals("D1")) {
          dr = 1;
        } else if (result.equals("D2")) {
          dr = 2;
        } else {
          dr = 3;
        }
      } else if ((result.equals("A1")) || (result.equals("A2"))) {
        //ATTACKER RETREAT
        if (result.equals("A1")) {
          ar = 1;
        } else {
          ar = 2;
        }
      } else {
        //MANDATORY STEP LOSS
        if (result.equals("2/1")) {
          al = 2;
          dl = 1;
        } else if (result.equals("1/-")) {
          al = 1;
        } else if (result.equals("1/1")) {
          al = 1;
          dl = 1;
        } else if (result.equals("1/2")) {
          al = 1;
          dl = 2;
        } else {
          al = 1;
          dl = 3;
        }
      }
      //disable retreat for the defenders if they have an asset amongst them
      if (dr > 0) {
        for (GamePiece gp : defenders) {
          if (RsrTrait.getType(gp).equals("Asset")) {
            dl += dr;
            dr = 0;
            break;
          }
        }
      }
      commandBaseSetProperty(dataPiece, COMBAT_STAGE, ++combatStage,
                                    ATTACKER_LOSS, al,
                                    ATTACKER_RETREAT, ar,
                                    DEFENDER_LOSS, dl,
                                    DEFENDER_RETREAT, dr);
      ignoreOptional = false;
    }
    if (combatStage == 2) {
      commandSetPhaseOwner(RsrGlobal.getDefenderSide());
      //defender step loss
      if (commandRefreshCombatLossList(ignoreOptional)) {
        commandBaseSetProperty(dataPiece, COMBAT_STAGE, ++combatStage);
        ignoreOptional = false;
      } else {
        commandBaseLog("Defender is required to lose " + RsrGlobal.getDefenderLoss() + " step(s) and optionally " + RsrGlobal.getDefenderRetreat() + " more.");
        return;
      }
    }
    if (combatStage == 3) {
      commandSetPhaseOwner(RsrGlobal.getAttackerSide());
      //attacker step loss
      if (commandRefreshCombatLossList(ignoreOptional)) {
        commandBaseSetProperty(dataPiece, COMBAT_STAGE, ++combatStage);
        ignoreOptional = false;
      } else {
        commandBaseLog("Attacker is required to lose " + RsrGlobal.getAttackerLoss() + " step(s) and optionally " + RsrGlobal.getAttackerRetreat() + " more.");
        return;
      }
    }
    if (combatStage == 4) {
      //retreats
      int dr = RsrGlobal.getDefenderRetreat();
      int ar = RsrGlobal.getAttackerRetreat();
      //setup parameters if it has not yet been done
      if (dr > 0) {
        commandSetPhaseOwner(RsrGlobal.getDefenderSide());
        //refresh in case any units were removed
        defenders = filter.filterPieces(mainMap, T_DEFENDER + ">0");
        //
        for (GamePiece gp : defenders) {
          commandBaseSetProperty(gp, T_RETREATING, dr);
          if (TerrainMapShaderRule.hasNonTraceHexes(getRetreatHexes(gp))) {
            commandBaseLog(gp.getName() + " must retreat " + dr + " space(s).");
          } else {
            commandEliminate(gp, true, true);
          }
        }
        commandBaseSetProperty(dataPiece, DEFENDER_RETREAT, 0);
      }
      if (ar > 0) {
        commandSetPhaseOwner(RsrGlobal.getAttackerSide());
        //refresh in case any units were removed
        attackers = filter.filterPieces(mainMap, T_ATTACKER + ">0");
        //
        for (GamePiece gp : attackers) {
          commandBaseSetProperty(gp, T_RETREATING, ar);
          if (TerrainMapShaderRule.hasNonTraceHexes(getRetreatHexes(gp))) {
            commandBaseLog(gp.getName() + " must retreat " + ar + " space(s).");
          } else {
            commandEliminate(gp, true, true);
          }
        }
        commandBaseSetProperty(dataPiece, ATTACKER_RETREAT, 0);
      }
      if ((RsrGlobal.getMotorizedLossRequired() > 0) || (RsrGlobal.getContestedLossRequired() > 0)) {
        commandBaseLog("There are units that have to take mandatory step loss(es).");
        return;
      } else if (filter.filterPieces(mainMap, T_RETREATING + ">0").size() > 0) {
        commandBaseLog("There are units that have to retreat.");
        return;
      } else {
        //there are currently no losses required and no T_RETREATING units
        //an attempt to advance combat will be made
        HashSet<HexRef> overstackHexes = new HashSet<HexRef>();
        HashSet<HexRef> stackHexes = new HashSet<HexRef>();
        HexRef hex;
        //if the player has completed normal retreats, check for overstacks
        //where the retreated units are, if yes then mark all units in that
        //stack for secondary retreat and exit
        for (GamePiece gp : filter.filterPieces(mainMap, T_RETREATING + "==-1")) {
          hex = getHexRef(gp.getPosition());
          if (isStackSizeOK(hex, RsrTrait.getSide(gp), 0.0d) == false) {
            overstackHexes.add(hex);
          }
          if (isStackFriendly(hex) == false) {
            overstackHexes.add(hex);
          }
          commandBaseSetProperty(gp, T_RETREATING, -2);
        }
        if (overstackHexes.size() > 0) {
          for (HexRef hr : overstackHexes) {
            for (GamePiece gp : pieceCache.getPiecesIn(hr)) {
              if (RsrTrait.getType(gp).equals("Unit")) {
                commandBaseSetProperty(gp, T_OVERSTACK_RETREATING, 1);
                commandBaseLog(gp.getName() + " may retreat 1 space due to stack rules violation.");
              }
            }
          }
          return;
        } else {
          //since there is either no retreating units or no overstacks, check
          //for overstacks where the secondary retreating units are (either
          //moved or not moved), if yes then "fix" those stacks
          for (GamePiece gp : filter.filterPieces(mainMap, T_OVERSTACK_RETREATING + "<0||" + T_RETREATING + "<0")) {
            hex = getHexRef(gp.getPosition());
            if (isStackSizeOK(hex, RsrTrait.getSide(gp), 0.0d) == false) {
              overstackHexes.add(hex);
            }
            if (isStackFriendly(hex) == false) {
              stackHexes.add(hex);
            }
            commandBaseSetProperty(gp, T_OVERSTACK_RETREATING, 0);
            commandBaseSetProperty(gp, T_RETREATING, 0);
          }
          if (overstackHexes.size() > 0) {
            for (HexRef hr : overstackHexes) {
              commandFixOverstack(hr);
            }
          }
          if (stackHexes.size() > 0) {
            for (HexRef hr : stackHexes) {
              commandFixStack(hr);
            }
          }
          //finally, advance the combat
          commandBaseSetProperty(dataPiece, COMBAT_STAGE, ++combatStage);
        }
      }
    }
    if (combatStage == 5) {
      //apply urban test for motorized units
      if (RsrGlobal.getUrbanLossRequired() == 1) {
        commandBaseLog("Applying additional losses to motorized forces that attacked urban area.");
        //refresh in case any units were removed
        attackers = filter.filterPieces(mainMap, T_ATTACKER + ">0");
        int chance;
        for (GamePiece gp : attackers) {
          if (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED) {
            if ((RsrTrait.getLost(gp) > 0) || (RsrTrait.getCombatHex(gp).equals(getLocationName(getHexRef(gp.getPosition()))) == false)) {
              chance = 3;
            } else {
              chance = 1;
            }
            if ((ran.nextInt(6) + 1) <= chance) {
              commandStepLoss(gp, true, false);
            }
          }
        }
        commandBaseSetProperty(dataPiece, URBAN_LOSS_REQUIRED, 0);
      }
      //find victorious units
      int victorious = 0;
      int advanceRange;
      //refresh in case any units were removed
      defenders = filter.filterPieces(mainMap, T_DEFENDER + ">0");
      //check if there are any victorious defenders
      if (defenders.size() > 0) {
        //check for attacker vacated hexes
        boolean attackerVacated = false;
        ArrayList<GamePiece> piecesIn;
        for (HexRef hr : attackerHexes.getHexes()) {
          piecesIn = pieceCache.getPiecesIn(hr);
          if (filter.filterPieces(piecesIn, T_ATTACKER + ">0").size() == 0) {
            attackerVacated = true;
            //in normal combat, eliminate any non-attacking units on vacated hex
            if (combatMode == 0) {
              for (GamePiece gp : filter.filterPieces(piecesIn, "Type!='Marker','AirFleet','Partisans'")) {
                commandEliminate(gp, true, false);
              }
            }
          }
        }
        //setup advancing defenders
        if (attackerVacated) {
          commandSetPhaseOwner(RsrGlobal.getDefenderSide());
          //
          for (GamePiece gp : defenders) {
            advanceRange = getMaximumAdvance(gp);
            if (advanceRange > 0) {
              commandBaseSetProperty(gp, T_ADVANCING, advanceRange);
              if (TerrainMapShaderRule.hasNonTraceHexes(getAdvanceHexes(gp))) {
                commandBaseLog(gp.getName() + " may advance up to " + advanceRange + " space(s).");
                victorious++;
              } else {
                commandBaseSetProperty(gp, T_ADVANCING, 0);
              }
            }
          }
        }
      }
      //refresh in case any units were removed
      attackers = filter.filterPieces(mainMap, T_ATTACKER + ">0");
      //check if there are any victorious attackers
      if (attackers.size() > 0) {
        ArrayList<GamePiece> piecesIn;
        boolean defenderVacated = false;
        Point p = null;
        //check for defender vacated hexes, storing the target point (for overrun placement)
        for (HexRef hr : defenderHexes.getHexes()) {
          piecesIn = pieceCache.getPiecesIn(hr);
          if (p == null) {
            p = mainMap.snapTo(mainGrid.getHexCenter(hr.getColumn(), hr.getRow()));
          }
          if (filter.filterPieces(piecesIn, T_DEFENDER + ">0").size() == 0) {
            defenderVacated = true;
            //in normal combat, eliminate any non-defending units on vacated hex (this happens when a unit attacked earlier retreated to this hex)
            if (combatMode == 0) {
              for (GamePiece gp : filter.filterPieces(piecesIn, "Type!='Marker','AirFleet','Partisans'")) {
                commandEliminate(gp, true, false);
              }
            }
          }
        }
        //setup advancing attackers, or end movement for units that failed to overrun
        if (defenderVacated) {
          commandSetPhaseOwner(RsrGlobal.getAttackerSide());
          //
          for (GamePiece gp : attackers) {
            //in normal combat let them advance
            if (combatMode == 0) {
              advanceRange = getMaximumAdvance(gp);
              if (advanceRange > 0) {
                commandBaseSetProperty(gp, T_ADVANCING, advanceRange);
                if (TerrainMapShaderRule.hasNonTraceHexes(getAdvanceHexes(gp))) {
                  commandBaseLog(gp.getName() + " may advance up to " + advanceRange + " space(s).");
                  victorious++;
                } else {
                  commandBaseSetProperty(gp, T_ADVANCING, 0);
                }
              }
            } else {
              //in overrun combat place attackers in defender hex and remove movement points for overrun
              commandBaseMove(mainMap, gp, p);
              double mp = RsrTrait.getMovementLeft(gp) - getOverrunCost(gp);
              if (mp > 0.0d) {
                commandAllowMovement(gp, mp, false);
              } else {
                commandDisallowMovement(gp);
                commandMarkAsMoved(gp);
              }
            }
          }
          //set move index and adjust control of space in which overrun units are placed (as no normal movement happens it must be added here)
          if (combatMode == 1) {
            commandSetMovementIndex(attackers);
            commandAdjustControlAndPartisans(mainMap, attackers, p);
          }
        } else {
          if (combatMode == 1) {
            //refresh in case any units were removed
            attackers = filter.filterPieces(mainMap, T_ATTACKER + ">0");
            //
            for (GamePiece gp : attackers) {
              commandDisallowMovement(gp);
              commandMarkAsMoved(gp);
            }
          }
        }
      }
      commandBaseSetProperty(dataPiece, COMBAT_STAGE, ++combatStage);
      //if there are no advances possible, end combat immediately, otherwise let the player decide
      if (victorious == 0) {
        commandClearCombat(true);
        return;
      } else {
        commandBaseLog("Advance victorious units or end combat.");
        return;
      }
    }
    if (combatStage == 6) {
      commandSetPhaseOwner(RsrGlobal.getAttackerSide());
      commandClearCombat(true);
    }
  }

  public void commandFixAllStacks() {
    for (HexRef hr : pieceCache.getOccupiedHexes()) {
      if (isStackSizeOK(hr) == false) {
        commandFixOverstack(hr);
      }
      if (isStackFriendly(hr) == false) {
        commandFixStack(hr);
      }
    }
  }

  public void commandFixOverstack(HexRef hr) {
    commandBaseLog("Stack size violation in " + getLocationName(hr) + " will be cured.");
    ArrayList<GamePiece> pieces = pieceCache.getPiecesIn(hr);
    ArrayList<GamePiece> possiblePieces = new ArrayList<GamePiece>();
    double stackSize = 0.0d;
    double stackPoints;
    String side = null;
    for (GamePiece gp : pieces) {
      stackPoints = RsrTrait.getStackPoints(gp);
      if (stackPoints > 0.0d) {
        stackSize += stackPoints;
        side = RsrTrait.getSide(gp);
        possiblePieces.add(gp);
      }
    }
    double diff = 0.0d;
    if (side != null) {
      if (side.equals(SIDE_AXIS)) {
        diff = stackSize - 14.0d;
      } else {
        diff = stackSize - 2.0d;
      }
    }
    GamePiece gp;
    while ((diff > 0.0d) && (possiblePieces.size() > 0)) {
      gp = selectBestEliminationCandidate(possiblePieces, 0);
      possiblePieces.remove(gp);
      diff -= RsrTrait.getStackPoints(gp);
      commandEliminate(gp, false, false);
    }
  }
  
  public void commandFixStack(HexRef hr) {
    ArrayList<GamePiece> pieces = pieceCache.getPiecesIn(hr);
    if ((pieces == null) || (pieces.size() == 0)) {
      return;
    }
    ArrayList<GamePiece> possiblePieces = new ArrayList<GamePiece>();
    String side = RsrTrait.getSide(pieces.get(0));
    if (side.equals(SIDE_AXIS)) {
      commandBaseLog("Stack rules (Axis ally) violation in " + getLocationName(hr) + " will be cured.");
      HashSet<String> possibleNationalities = new HashSet<String>();
      String nationality;
      for (GamePiece gp : pieces) {
        nationality = RsrTrait.getNationality(gp);
        if (Filter.isAny(nationality, NATIONALITY_FINNISH, NATIONALITY_HUNGARIAN, NATIONALITY_ITALIAN, NATIONALITY_ROMANIAN)) {
          possiblePieces.add(gp);
          possibleNationalities.add(nationality);
        }
      }
      HashSet<GamePiece> toRemove = new HashSet<GamePiece>();
      while (possibleNationalities.size() > 1) {
        nationality = selectBestEliminationNationalityCandidate(possiblePieces);
        toRemove.clear();
        for (GamePiece gp : possiblePieces) {
          if (RsrTrait.getNationality(gp).equals(nationality)) {
            toRemove.add(gp);
          }
        }
        for (GamePiece gp : toRemove) {
          possiblePieces.remove(gp);
          commandEliminate(gp, false, false);
        }
        possibleNationalities.remove(nationality);        
      }
    } else {
      commandBaseLog("Stack rules (Guards cavalry) violation in " + getLocationName(hr) + " will be cured.");
      for (GamePiece gp : pieces) {
        if (RsrTrait.getUnitType(gp).equals("SovietGuardsCavalryCorps")) {
          possiblePieces.add(gp);
        }
      }
      GamePiece gp;
      while (possiblePieces.size() > 1) {
        gp = selectBestEliminationCandidate(possiblePieces, 2);
        possiblePieces.remove(gp);
        commandEliminate(gp, false, false);
      }
    }    
  }

  private double getEliminationScore(GamePiece gp, int flag) {
    int as = getAttackStrength(gp, true);
    int ds = getDefendStrength(gp, true);
    if (flag == 0) {
      if ((as < 0) || (ds < 0)) {
        switch (RsrTrait.getCombatClass(gp).charAt(0)) {
          case 'A' : as = 8; ds = 8; break;
          case 'B' : as = 6; ds = 6; break;
          default  : as = 4; ds = 4; break;
        }
        if (RsrTrait.isSupplyLevel(gp, SUPPLY_LACK, SUPPLY_OUT)) {
          as *= 0.5d;
        }
        if (RsrTrait.isSupplyLevel(gp, SUPPLY_OUT)) {
          ds *= 0.5d;
        }
      }
    }
    double mv = RsrTrait.getMovement(gp);
    if (RsrTrait.isSupplyLevel(gp, SUPPLY_LACK, SUPPLY_OUT)) {
      mv *= 0.5d;
    }
    switch (flag) {
      case 1  :
        if (RsrTrait.getType(gp).equals("Headquarters")) {
          return 1000.0d;
        } else {
          return RsrTrait.getStackPoints(gp) * RsrTrait.getCurrentStep(gp) * (as + 2 * ds + mv);
        }
      case 2  : return as + 2 * ds + mv;
      default : return RsrTrait.getStackPoints(gp) * RsrTrait.getCurrentStep(gp) * (as + 2 * ds + mv);
    }    
  }
  
  public GamePiece selectBestEliminationCandidate(ArrayList<GamePiece> pieces, int flag) {
    double max = Double.MIN_VALUE;
    double val;
    GamePiece result = null;
    for (GamePiece gp : pieces) {
      val = getEliminationScore(gp, flag);
      if (val > max) {
        max = val;
        result = gp;
      }
    }
    return result;
  }
  
  public String selectBestEliminationNationalityCandidate(ArrayList<GamePiece> pieces) {
    HashMap<String, Double> totalScore = new HashMap<String, Double>();
    String nationality;
    double total;
    for (GamePiece gp : pieces) {
      nationality = RsrTrait.getNationality(gp);
      if (totalScore.containsKey(nationality)) {
        total = totalScore.get(nationality);
      } else {
        total = 0.0d;
      }
      total += getEliminationScore(gp, 1);
      totalScore.put(nationality, total);
    }
    double max = Double.MIN_VALUE;
    nationality = null;
    for (String n : totalScore.keySet()) {
      total = totalScore.get(n);
      if (total > max) {
        max = total;
        nationality = n;
      }
    }
    return nationality;
  }

  public void commandCombatStepLoss(GamePiece gp) {
    String property1, property2;
    int newValue1, newValue2;
    if (RsrTrait.getAttacker(gp) == 1) {
      int left = RsrGlobal.getAttackerLoss();
      if (left == 0) {
        property1 = ATTACKER_RETREAT;
        newValue1 = RsrGlobal.getAttackerRetreat()-1;
      } else {
        property1 = ATTACKER_LOSS;
        newValue1 = left-1;
      }
      property2 = ATTACKER_LOST;
      newValue2 = RsrGlobal.getAttackerLost()+1;
    } else {
      int left = RsrGlobal.getDefenderLoss();
      if (left == 0) {
        property1 = DEFENDER_RETREAT;
        newValue1 = RsrGlobal.getDefenderRetreat()-1;
      } else {
        property1 = DEFENDER_LOSS;
        newValue1 = left-1;
      }
      property2 = DEFENDER_LOST;
      newValue2 = RsrGlobal.getDefenderLost()+1;
    }
    commandBaseSetProperty(gp, T_LOST, RsrTrait.getLost(gp) + 1);
    commandBaseSetProperty(dataPiece, property1, newValue1, property2, newValue2);
  }

  public boolean commandRefreshCombatLossList(boolean ignoreOptional) {
    int combatStage = RsrGlobal.getCombatStage();
    String trait;
    int lossMandatory;
    int lossOptional;
    if (combatStage == 2) {
      trait = T_DEFENDER;
      lossMandatory = RsrGlobal.getDefenderLoss();
      lossOptional = RsrGlobal.getDefenderRetreat();
    } else if (combatStage == 3) {
      trait = T_ATTACKER;
      lossMandatory = RsrGlobal.getAttackerLoss();
      lossOptional = RsrGlobal.getAttackerRetreat();
    } else {
      return false;
    }
    ArrayList<GamePiece> pieces = filter.filterPieces(mainMap, trait + ">0");
    //
    if (pieces.size() == 0) {
      return true;
    }
    //
    if (lossMandatory + lossOptional == 0) {
      for (GamePiece gp : pieces) {
        commandDisallowCombatAction(gp);
      }
      return true;
    }
    ArrayList<GamePiece> candidates;
    while (true) {
      //find candidates to take the loss
      candidates = getLossCandidates(pieces);
      //if there is only one candidate automatically apply mandatory losses
      if ((lossMandatory > 0) && (candidates.size() == 1)) {
        //
        lossMandatory--;
        //
        commandCombatStepLoss(candidates.get(0));
        commandStepLoss(candidates.get(0), true, false);
        //
        if (lossMandatory == 0) {
          return true;
        }
        //
        pieces = filter.filterPieces(mainMap, trait + ">0");
        //
        if (pieces.size() == 0) {
          return true;
        }
      } else {
        break;
      }
    }
    if ((ignoreOptional) && (lossMandatory == 0)) {
      for (GamePiece gp : pieces) {
        commandDisallowCombatAction(gp);
      }
      return true;
    } else {
      for (GamePiece gp : pieces) {
        if (candidates.contains(gp)) {
          commandAllowCombatAction(gp);
        } else {
          commandDisallowCombatAction(gp);
        }
      }
      return false;
    }
  }

  public GamePiece getTurnPiece() {
    return turnPiece;
  }

  public GamePiece getPhasePiece() {
    return phasePiece;
  }

  public GamePiece getDataPiece() {
    return dataPiece;
  }

  public Map getMainMap() {
    return mainMap;
  }

  public TerrainHexGrid getMainGrid() {
    return mainGrid;
  }

  public Board getTurnBoard() {
    return turnBoard;
  }

  public int getTurnNumber() {
    return Integer.parseInt(turnBoard.locationName(turnPiece.getPosition()).split(":")[0]);
  }

  public String getTurnName() {
    return turnBoard.locationName(turnPiece.getPosition());
  }

  public String getPhaseName() {
    return turnBoard.locationName(phasePiece.getPosition());
  }

  public boolean isLockedInFinlandOrRomania(GamePiece gp) {
    if ((RsrTrait.getSide(gp).equals(SIDE_AXIS)) && (getTurnNumber() == 1)) {
      String country = mainTerrain.getAttributeTerrainValue(mainGrid.getHexPos(gp.getPosition()), TAG_COUNTRY);
      if ((country.equals(TAG_COUNTRY_FINLAND)) || (country.equals(TAG_COUNTRY_ROMANIA))) {
        return true;
      }
    }
    return false;
  }

  public HashMap<HexRef, TerrainMapShaderCost> getAttackHexes(GamePiece gp) {
    HashMap<HexRef, TerrainMapShaderCost> result = new HashMap<HexRef, TerrainMapShaderCost>();
    //no attack if this is an Axis unit in Finland or Romania on turn 1
    if (isLockedInFinlandOrRomania(gp)) {
      return result;
    }
    //
    String attackerSide = RsrTrait.getSide(gp);
    String defenderSide = attackerSide.equals(SIDE_AXIS) ? SIDE_SOVIET : SIDE_AXIS;
    HexRef fromHex = mainGrid.getHexPos(gp.getPosition());
    int movementMode = RsrTrait.getMovementTypeAsInt(gp);
    //adjacent hexes
    HashSet<HexRef> adjacent = mainGrid.getHexesInRange(fromHex, 1);
    adjacent.remove(fromHex);
    String tt, et, lt;
    for (HexRef toHex : adjacent) {
      tt = mainTerrain.getHexTerrainName(toHex);
      et = mainTerrain.getEdgeTerrainName(fromHex, toHex);
      lt = mainTerrain.getLineTerrainName(fromHex, toHex);
      if (isImpassable(et)) {
        continue;
      }
      if (isLakeSea(et)) {
        if ((attackerSide.equals(SIDE_AXIS)) || (isWinter() == false) || (lt.equals(LINE_FROZENSURFACE) == false) || (movementMode != MOVEMENT_NONMOTORIZED)) {
          continue;
        }
      }
      if (tt.equals(HEX_WATER)) {
        continue;
      }
      if (filter.filterPieces(pieceCache.getPiecesIn(toHex), "Side=='" + defenderSide + "'&&Type!='AirFleet','Partisans'&&" + T_ATTACKED + "==0").size() == 0) {
        continue;
      }
      result.put(toHex, new TerrainMapShaderCost(fromHex, true, 0.0d, false, MODE_ATTACK, 0));
    }
    return result;
  }

  public HashMap<HexRef, TerrainMapShaderCost> getRetreatHexes(GamePiece gp) {
    return TerrainMapShaderRule.leaveBestHexes(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesRetreat, "", gp));
  }

  public HashMap<HexRef, TerrainMapShaderCost> getAdvanceHexes(GamePiece gp) {
    return TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesAdvance, "", gp);
  }

  private void removeInvalidHexes(HashSet<HexRef> result, String country) {
    Iterator<HexRef> it = result.iterator();
    String s;
    HexRef hr;
    boolean sov = country.equals(TAG_COUNTRY_SOVIETUNION);
    while (it.hasNext()) {
      hr = it.next();
      s = mainTerrain.getHexTerrainName(hr);
      if (s.equals(HEX_WATER)) {
        it.remove();
      } else {
        s = mainTerrain.getAttributeTerrainValue(hr, TAG_COUNTRY);
        if (sov) {
          if ((s.equals(TerrainMap.NULL_ATTRIBUTE) == false) && (s.equals(country) == false)) {
            it.remove();
          }
        } else {
          if (s.equals(country) == false) {
            it.remove();
          }
        }
      }
    }
  }

  public HashSet<HexRef> getPartisanRepositionHexes(GamePiece gp) {
    HashSet<HexRef> result = new HashSet<HexRef>();
    result.addAll(mainGrid.getHexesInRange(getHexRef(gp.getPosition()), 5));
    result.removeAll(pieceCache.getHexesProtectedFromPartisans());
    removeInvalidHexes(result, TAG_COUNTRY_SOVIETUNION);
    return result;
  }

  public HashSet<HexRef> getPartisanEntryHexes() {
    HashSet<HexRef> result = new HashSet<HexRef>();
    result.addAll(mainTerrain.findHexesWithTag(TAG_COUNTRY, TAG_COUNTRY_SOVIETUNION));
    result.removeAll(pieceCache.getHexesProtectedFromPartisans());
    removeInvalidHexes(result, TAG_COUNTRY_SOVIETUNION);
    return result;
  }

  public HashSet<HexRef> getAirborneAssaultHexes() {
    HashSet<HexRef> result = new HashSet<HexRef>();
    //add hexes around in-supply soviet headquarters (radius 6)
    HashMap<HexRef, TerrainMapShaderCost> inSupply = supplyCache.getSupply(NATIONALITY_SOVIET, false, false);
    HexRef hr;
    for (GamePiece gp : pieceCache.getHeadquarters(NATIONALITY_SOVIET)) {
      hr = getHexRef(gp.getPosition());
      if (inSupply.containsKey(hr)) {
        result.addAll(mainGrid.getHexesInRange(hr, 6));
      }
    }
    removeInvalidHexes(result, TAG_COUNTRY_SOVIETUNION);
    result.removeAll(pieceCache.getHexesOccupiedByEnemy(SIDE_SOVIET));
    return result;
  }

  public HashSet<HexRef> getReinforcementHexes(GamePiece gp) {
    String side = RsrTrait.getSide(gp);
    String nationality = RsrTrait.getNationality(gp);
    String type = RsrTrait.getType(gp);
    HashSet<HexRef> result = new HashSet<HexRef>();
    if (type.equals("Headquarters")) {
      if (nationality.equals(NATIONALITY_SOVIET)) {
        result.addAll(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesHQReinforcement, NATIONALITY_SOVIET, null).keySet());
        removeInvalidHexes(result, TAG_COUNTRY_SOVIETUNION);
      } else if (nationality.equals(NATIONALITY_FINNISH)) {
        result.addAll(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesHQReinforcement, NATIONALITY_FINNISH + ";" + TAG_CITY_HELSINKI, null).keySet());
      } else if (nationality.equals(NATIONALITY_ROMANIAN)) {
        result.addAll(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesHQReinforcement, NATIONALITY_ROMANIAN + ";" + TAG_CITY_BUCHAREST, null).keySet());
      }
      result.removeAll(pieceCache.getHexesInRangeOccupiedEnemy(side, 4));
    } else {
      result.addAll(supplyCache.getSupply(nationality, false, false).keySet());
      if (side.equals(SIDE_AXIS)) {
        if (RsrTrait.getType(gp).equals("SecurityDivision")) {
          result.removeAll(pieceCache.getHexesInRangeOccupiedEnemy(side, 4));
          for (GamePiece pgp : pieceCache.getPartisans()) {
            result.removeAll(mainGrid.getHexesInRange(getHexRef(pgp.getPosition()), 2));
          }
        } else {
          result.removeAll(pieceCache.getHexesInRangeOccupiedEnemy(side, 3));
          for (GamePiece pgp : pieceCache.getPartisans()) {
            result.remove(getHexRef(pgp.getPosition()));
          }
        }
      } else {
        result.removeAll(pieceCache.getHexesOccupiedByEnemy(side));
        result.removeAll(pieceCache.getHexesInZOCEnemy(side));
        HashSet<HexRef> withLOC = supplyCache.getSovietLine();
        HashSet<HexRef> aroundHQ = new HashSet<HexRef>();
        HexRef hr;
        for (GamePiece gpf : filter.filterPieces(mainMap, "Side=='Soviet'&&Type=='Headquarters'")) {
          hr = getHexRef(gpf.getPosition());
          if (withLOC.contains(hr)) { //check if current HQ is "in supply" - depart from original rules
            mainGrid.getHexesInRange(aroundHQ, hr, 1);
          }
        }
        result.retainAll(aroundHQ);
      }
    }
    //Iterator<HexRef> it = result.iterator();
    //String unitType = RsrTrait.getUnitType(gp);
    //while (it.hasNext()) {
    //  if (isStackFriendly(it.next(), side, nationality, unitType) == false) {
    //    it.remove();
    //  }
    //}
    return result;
  }




  /**
   * 
   * @param gp
   * @return
   */
  public int getMaximumAdvance(GamePiece gp) {
    if (RsrTrait.getType(gp).equals("Unit")) {
      String unitType = RsrTrait.getUnitType(gp);
      int lost = RsrTrait.getAttacker(gp) > 0 ? RsrGlobal.getDefenderLost() : RsrGlobal.getAttackerLost();
      if (unitType.equals("SovietGuardsCavalryCorps")) {
        return 0;
      } else
      if (unitType.startsWith("AxisGermanTank")) {
        return Math.max(0, Math.min(3, 3 - lost));
      } else
      if ((RsrTrait.getSide(gp).equals(SIDE_SOVIET)) && (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED)) {
        return Math.max(0, Math.min(2, 3 - lost));
      } else {
        return Math.max(0, Math.min(1, 3 - lost));
      }
    } else {
      return 0;
    }
  }

  /**
   * Setup MOVE_FREE movement based on range.
   * @param gp
   * @param counter
   * @param range
   * @return
   */
  protected boolean allowMovement(GamePiece gp, HexRef startHex, int range) {
    moveMode = MOVE_FREE;
    if ((startHex == null) || (range < 1)) {
      return false;
    }
    HashSet<HexRef> area = mainGrid.getHexesInRange(getStartHex(gp), range);
    if (area.size() > 0) {
      allowedMovement.put(gp, new RsrAllowedMovement(gp, area));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Setup MOVE_ONCE movement based on given area.
   * @param gp
   * @param area
   * @return
   */
  protected boolean allowMovement(GamePiece gp, HashMap<HexRef, TerrainMapShaderCost> area) {
    moveMode = MOVE_ONCE;
    if (area.size() > 0) {
      allowedMovement.put(gp, new RsrAllowedMovement(gp, area, false));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Setup MOVE_ONCE movement based on given area.
   * @param gp
   * @param area
   * @return
   */
  protected boolean allowMovement(GamePiece gp, HashSet<HexRef> area) {
    moveMode = MOVE_ONCE;
    if (area.size() > 0) {
      allowedMovement.put(gp, new RsrAllowedMovement(gp, area));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Setup MOVE_UNTIL_NEXT movement based on shader.
   * @param gp
   * @param counter
   * @param shader
   * @param substractCost
   * @return
   */
  protected boolean allowMovement(GamePiece gp, TerrainMapShaderRule rules, boolean substractCost) {
    moveMode = MOVE_UNTIL_NEXT;
    HexRef startHex = getStartHex(gp);
    if (startHex == null) {
      return false;
    }
    HashMap<HexRef, TerrainMapShaderCost> area = TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rules, "", gp);
    if (area.size() > 0) {
      allowedMovement.put(gp, new RsrAllowedMovement(gp, area, substractCost));
      return true;
    } else {
      return false;
    }
  }

  public SupplyCache getSupplyCache() {
    return supplyCache;
  }

  public AirFleetCache getAirFleetCache() {
    return airFleetCache;
  }

  public PieceCache getPieceCache() {
    return pieceCache;
  }

  public HexRef getHexRef(Point p) {
    return mainGrid.getHexPos(p);
  }
  
  public String getLocationName(HexRef hr) {
    return mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow()));
  }

  public Filter getFilter() {
    return filter;
  }

  private void initializePropertyReaderSource() {
    propertyReaderSource.addReader(T_STEPS, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getSteps(gp));
      }
    });

    propertyReaderSource.addReader(T_CURRENT_STEP, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getCurrentStep(gp));
      }
    });

    propertyReaderSource.addReader(T_SUPPLY_LEVEL, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getSupplyLevel(gp));
      }
    });

    propertyReaderSource.addReader(T_SUPPLY_ISOLATED, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getSupplyIsolated(gp));
      }
    });

    propertyReaderSource.addReader(T_STRENGTH_ID, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return RsrTrait.getStrengthID(gp);
      }
    });

    propertyReaderSource.addReader(T_MOVEMENT_LEFT, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getMovementLeft(gp));
      }
    });

    propertyReaderSource.addReader(T_START_HEX, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return RsrTrait.getStartHex(gp);
      }
    });

    propertyReaderSource.addReader(T_MOVE_INDEX, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getMoveIndex(gp));
      }
    });

    propertyReaderSource.addReader(T_HL_LOSS, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getHlLoss(gp));
      }
    });

    propertyReaderSource.addReader(T_HL_ACTION, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getHlAction(gp));
      }
    });

    propertyReaderSource.addReader(T_HL_MOVEMENT, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getHlMovement(gp));
      }
    });

    propertyReaderSource.addReader(T_ATTACKER, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getAttacker(gp));
      }
    });

    propertyReaderSource.addReader(T_DEFENDER, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getDefender(gp));
      }
    });

    propertyReaderSource.addReader(T_COMBAT_HEX, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return RsrTrait.getCombatHex(gp);
      }
    });

    propertyReaderSource.addReader(T_LOST, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getLost(gp));
      }
    });

    propertyReaderSource.addReader(T_RETREATING, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getRetreating(gp));
      }
    });

    propertyReaderSource.addReader(T_ADVANCING, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getAdvancing(gp));
      }
    });

    propertyReaderSource.addReader(T_ATTACKED, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getAttacked(gp));
      }
    });

    propertyReaderSource.addReader(T_PLACED, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getPlaced(gp));
      }
    });

    propertyReaderSource.addReader(T_MOVED, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getMoved(gp));
      }
    });

    propertyReaderSource.addReader(T_REORGANIZED, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getReorganized(gp));
      }
    });

    propertyReaderSource.addReader(T_FIRED, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getFired(gp));
      }
    });

    propertyReaderSource.addReader(T_REPOSITIONING, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getRepositioning(gp));
      }
    });

    propertyReaderSource.addReader(T_OVERSTACK_RETREATING, new PropertyReader() {
      public String getProperty(GamePiece gp) {
        return String.valueOf(RsrTrait.getOverstackRetreating(gp));
      }
    });
  }

  public boolean isWinter() {
    return isWinter(getTurnNumber());
  }

  public boolean isWinter(int turnNumber) {
    return ((turnNumber >= 8) && (turnNumber <= 11)) ||
            ((turnNumber >= 22) && (turnNumber <= 25)) ||
            ((turnNumber >= 36) && (turnNumber <= 39));
  }

  public boolean isFirstWinter() {
    return isFirstWinter(getTurnNumber());
  }

  public boolean isFirstWinter(int turnNumber) {
    return (turnNumber >= 8) && (turnNumber <= 11);
  }

  public boolean isMud() {
    return isMud(getTurnNumber());
  }
  public boolean isMud(int turnNumber) {
    return (turnNumber == 7) || (turnNumber == 12) ||
            (turnNumber == 21) || (turnNumber == 26) ||
            (turnNumber == 35) || (turnNumber == 40);
  }

  public boolean isProductionTurn() {
    return isProductionTurn(getTurnNumber());
  }

  public boolean isProductionTurn(int turnNumber) {
    return (RsrGlobal.getScenario() != 1) && ((turnNumber == 5) || (turnNumber == 7) ||
            (turnNumber == 9) || (turnNumber == 11) ||
            (turnNumber == 12) || (turnNumber == 14) ||
            (turnNumber == 19) || (turnNumber == 21) ||
            (turnNumber == 23) || (turnNumber == 25) ||
            (turnNumber == 26) || (turnNumber == 28) ||
            (turnNumber == 33) || (turnNumber == 35) ||
            (turnNumber == 37) || (turnNumber == 39) ||
            (turnNumber == 40));
  }

  public int getProductionNumber() {
    return getProductionNumber(getTurnNumber());
  }

  public int getProductionNumber(int turnNumber) {
    switch (turnNumber) {
      case 5 : return 1;
      case 7 : return 3;
      case 9 : return 5;
      case 11 : return 8;
      case 12 : return 2;
      case 14 : return 12;
      case 19 : return 10;
      case 21 : return 4;
      case 23 : return 18;
      case 25 : return 6;
      case 26 : return 20;
      case 28 : return 7;
      case 33 : return 24;
      case 35 : return 14;
      case 37 : return 22;
      case 39 : return 16;
      case 40 : return 20;
      default : return 0;
    }
  }

  public boolean isImpassable(String et) {
    if (et.equals(EDGE_IMPASSABLE)) {
      return true;
    } else
    if ((isSouthwestMapUsed == false) && ((et.equals(EDGE_IMPASSABLESW)) || (et.equals(EDGE_IMPASSABLESWLAKESEA)) || (et.equals(EDGE_IMPASSABLESWMINORRIVER)))) {
      return true;
    } else
    if ((isNorthMapUsed == false) && ((et.equals(EDGE_IMPASSABLEN)) || (et.equals(EDGE_IMPASSABLENLAKESEA)))) {
      return true;
    } else
    if ((isSubMapUsed == true) && ((et.equals(EDGE_IMPASSABLETP)) || (et.equals(EDGE_IMPASSABLETPLAKESEA)) || (et.equals(EDGE_IMPASSABLETPMAJORRIVER)) || (et.equals(EDGE_IMPASSABLETPMINORRIVER)))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isLakeSea(String et) {
    if (et.equals(EDGE_LAKESEA)) {
      return true;
    } else
    if ((isSouthwestMapUsed == true) && (et.equals(EDGE_IMPASSABLESWLAKESEA))) {
      return true;
    } else
    if ((isNorthMapUsed == true) && (et.equals(EDGE_IMPASSABLENLAKESEA))) {
      return true;
    } else
    if ((isSubMapUsed == false) && (et.equals(EDGE_IMPASSABLETPLAKESEA))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isMajorCity(String tt) {
    return (tt.equals(HEX_MAJORCITY)) || ((tt.equals(HEX_SEVASTOPOL)) && (isCityDestroyed(TAG_CITY_SEVASTOPOL) == false));
  }

  public boolean isMinorCity(String tt) {
    return (tt.equals(HEX_MINORCITY)) || ((tt.equals(HEX_SEVASTOPOL)) && (isCityDestroyed(TAG_CITY_SEVASTOPOL) == true));
  }

  public boolean isMinorRiver(String et) {
    if (et.equals(EDGE_MINORRIVER)) {
      return true;
    } else
    if ((isSouthwestMapUsed == true) && (et.equals(EDGE_IMPASSABLESWMINORRIVER))) {
      return true;
    } else
    if ((isSubMapUsed == false) && (et.equals(EDGE_IMPASSABLETPMINORRIVER))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isMajorRiver(String et) {
    if (et.equals(EDGE_MAJORRIVER)) {
      return true;
    } else
    if ((isSubMapUsed == false) && (et.equals(EDGE_IMPASSABLETPMAJORRIVER))) {
      return true;
    } else {
      return false;
    }
  }

  public final HexRef getCityHexRef(String city) {
    for (HexRef hr : mainTerrain.findHexesWithTag(TAG_CITY, city)) {
      return hr;
    }
    return null;
  }

  public final boolean isHexAxisControlled(HexRef hr) {
    return filter.filterPieces(pieceCache.getPiecesIn(hr), "MarkerType=='Control'&&Control==2").size() == 1;
  }

  public final boolean isHexSovietControlled(HexRef hr) {
    return filter.filterPieces(pieceCache.getPiecesIn(hr), "MarkerType=='Control'&&Control==1").size() == 1;
  }

  public final boolean isHexDestroyed(HexRef hr) {
    return filter.filterPieces(pieceCache.getPiecesIn(hr), "MarkerType=='Destruction'&&Destruction==2").size() == 1;
  }

  public final boolean isHexNotDestroyed(HexRef hr) {
    return filter.filterPieces(pieceCache.getPiecesIn(hr), "MarkerType=='Destruction'&&Destruction==1").size() == 1;
  }

  public final boolean isCityAxisControlled(String city) {
    return isHexAxisControlled(getCityHexRef(city));
  }

  public final boolean isCitySovietControlled(String city) {
    return isHexSovietControlled(getCityHexRef(city));
  }

  public final boolean isCityDestroyed(String city) {
    return isHexDestroyed(getCityHexRef(city));
  }

  public final boolean isCityNotDestroyed(String city) {
    return isHexNotDestroyed(getCityHexRef(city));
  }

  public final HashSet<HexRef> getPossibleSupplySources(String side, String capital) {
    HashSet<HexRef> result = new HashSet<HexRef>();
    HashSet<HexRef> northHexes = mainTerrain.findHexesWithTag(TAG_MAPPART, TAG_MAPPART_NORTH);
    HashSet<HexRef> southwestHexes = mainTerrain.findHexesWithTag(TAG_MAPPART, TAG_MAPPART_SOUTHWEST);
    HashSet<HexRef> tpHexes = mainTerrain.findHexesWithTag(TAG_MAPPART, TAG_MAPPART_TP);
    HashSet<HexRef> tpnswHexes = mainTerrain.findHexesWithTag(TAG_MAPPART, TAG_MAPPART_TPNSW);
    HashSet<HexRef> tpnormalHexes = mainTerrain.findHexesWithTag(TAG_MAPPART, TAG_MAPPART_TPNORMAL);
    if (side.equals(SIDE_AXIS)) {
      if (isSubMapUsed) {
        for (HexRef hr : mainTerrain.findHexesWithTag(TAG_SUPPLY, TAG_SUPPLY_AXIS)) {
          if ((tpHexes.contains(hr)) || (tpnswHexes.contains(hr)) || (tpnormalHexes.contains(hr))) {
            result.add(hr);
          }
        }
      } else {
        for (HexRef hr : mainTerrain.findHexesWithTag(TAG_SUPPLY, TAG_SUPPLY_AXIS)) {
          if ((isNorthMapUsed == false) && (northHexes.contains(hr))) {
            continue;
          }
          if ((isSouthwestMapUsed == false) && (southwestHexes.contains(hr))) {
            continue;
          }
          if ((isSouthwestMapUsed == true) && (tpnswHexes.contains(hr))) {
            continue;
          }
          if (tpHexes.contains(hr)) {
            continue;
          }
          result.add(hr);
        }
        if ((isNorthMapUsed) && (isCityAxisControlled(TAG_CITY_MURMANSK))) {
          for (HexRef hr : mainTerrain.findHexesWithTag(TAG_CITY, TAG_CITY_MURMANSK)) {
            result.add(hr);
            break;
          }
        }
        if (capital != null) {
          if (isCityAxisControlled(capital)) {
            for (HexRef hr : mainTerrain.findHexesWithTag(TAG_CITY, capital)) {
              result.add(hr);
              break;
            }
          } else
          if (isCitySovietControlled(capital)) {
            result.clear();
          }
        }
      }
    } else {
      if (isSubMapUsed) {
        for (HexRef hr : mainTerrain.findHexesWithTag(TAG_SUPPLY, TAG_SUPPLY_SOVIET)) {
          if ((tpHexes.contains(hr)) || (tpnswHexes.contains(hr)) || (tpnormalHexes.contains(hr))) {
            result.add(hr);
          }
        }
      } else {
        for (HexRef hr : mainTerrain.findHexesWithTag(TAG_SUPPLY, TAG_SUPPLY_SOVIET)) {
          if ((isNorthMapUsed == false) && (northHexes.contains(hr))) {
            continue;
          }
          if ((isSouthwestMapUsed == false) && (southwestHexes.contains(hr))) {
            continue;
          }
          if ((isSouthwestMapUsed == true) && (tpnswHexes.contains(hr))) {
            continue;
          }
          if (tpHexes.contains(hr)) {
            continue;
          }
          result.add(hr);
        }
        if ((isCityNotDestroyed(TAG_CITY_MOSCOW)) && (isCitySovietControlled(TAG_CITY_MOSCOW))) {
          for (HexRef hr : mainTerrain.findHexesWithTag(TAG_CITY, TAG_CITY_MOSCOW)) {
            result.add(hr);
            break;
          }
        }
        if ((isSouthwestMapUsed == true) && (isCityNotDestroyed(TAG_CITY_BAKU)) && (isCitySovietControlled(TAG_CITY_BAKU))) {
          for (HexRef hr : mainTerrain.findHexesWithTag(TAG_CITY, TAG_CITY_BAKU)) {
            result.add(hr);
            break;
          }
        }
      }
    }
    return result;
  }

  public String getCombatResult(ArrayList<GamePiece> attackers, ArrayList<GamePiece> defenders, CombatHexList attackerHexes, CombatHexList defenderHexes) {

    int totalStrengthA = 0;
    String attackerSide = RsrGlobal.getAttackerSide();

    for (GamePiece gp : attackers) {
      totalStrengthA += getAttackStrength(gp, false);
    }

    int totalStrengthD = 0;
    boolean foundDepot = false;
    boolean foundHeadquarters = false;
    String type;
    for (GamePiece gp : defenders) {
      type = RsrTrait.getType(gp);
      if (type.equals("Depot")) {
        foundDepot = true;
      } else if (type.equals("Headquarters")) {
        foundHeadquarters = true;
      } else {
        totalStrengthD += getDefendStrength(gp, false);
      }
    }
    if (foundDepot) {
      totalStrengthD++;
    }
    if (foundHeadquarters) {
      totalStrengthD++;
    }

    HashSet<HexRef> aH = attackerHexes.getHexes();
    HashSet<HexRef> dH = defenderHexes.getHexes();

    int totalEdges = 0;
    int minorRiverEdges = 0;
    int majorRiverEdges = 0;
    boolean majorCity = false;
    int class3 = 0;
    int class2 = 0;
    String et, tt;
    for (HexRef ahr : aH) {
      for (HexRef dhr : dH) {
        et = mainTerrain.getEdgeTerrainName(ahr, dhr);
        if ((isMinorRiver(et)) || (isLakeSea(et))) {
          minorRiverEdges++;
        } else
        if (isMajorRiver(et)) {
          majorRiverEdges++;
        }
        totalEdges++;
        tt = mainTerrain.getHexTerrainName(dhr);
        if (isMajorCity(tt)) {
          majorCity = true;
          class3++;
        } else if ((tt.equals(HEX_SWAMP))) {
          if (isWinter()) { //swamp as wood in winter
            class2++;
          } else {
            class3++;
          }
        } else if ((tt.equals(HEX_WOODS)) || (tt.equals(HEX_MOUNTAIN))) {
          class2++;
        }
      }
    }
    //ignore minor rivers on turn 1 overruns
    if ((minorRiverEdges > 0) && (getTurnNumber() == 1) && (RsrGlobal.getCombatMode() == 1) && (attackerSide.equals(SIDE_AXIS))) {
      minorRiverEdges = 0;
    }
    //try to apply minor river bonus first
    if (minorRiverEdges == totalEdges) {
      if (totalStrengthD == 1) {
        totalStrengthD += 1;
      } else {
        totalStrengthD += 2;
      }
    } else
    if (minorRiverEdges + majorRiverEdges == totalEdges) {
      totalStrengthD *= 2;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Total attack strength is " + totalStrengthA + ", total defense strength is " + totalStrengthD + ". Simple ratio is ");
    //calculate ratio
    double ratio = (double)totalStrengthA / (double)totalStrengthD;
    int column;
    if (ratio < 0.5d) { //1:3
      column = 0;
      sb.append("1:3");
    } else if (ratio < 1.0d) { //1:2
      column = 1;
      sb.append("1:2");
    } else if (ratio < 1.5d) { //1:1
      column = 2;
      sb.append("1:1");
    } else if (ratio < 2.0d) { //3:2
      column = 3;
      sb.append("3:2");
    } else if (ratio < 3.0d) { //2:1
      column = 4;
      sb.append("2:1");
    } else if (ratio < 4.0d) { //3:1
      column = 5;
      sb.append("3:1");
    } else if (ratio < 5.0d) { //4:1
      column = 6;
      sb.append("4:1");
    } else if (ratio < 6.0d) { //5:1
      column = 7;
      sb.append("5:1");
    } else if (ratio < 7.0d) { //6:1
      column = 8;
      sb.append("6:1");
    } else if (ratio < 8.0d) { //7:1
      column = 9;
      sb.append("7:1");
    } else if (ratio < 9.0d) { //8:1
      column = 10;
      sb.append("8:1");
    } else { //9:1
      column = 11;
      sb.append("9:1");
    }
    //apply terrain column modifier
    if (class3 > 0) {
      column -= 2;
      sb.append(", terrain shift -2");
    } else
    if (class2 > 0) {
      column -= 1;
      sb.append(", terrain shift -1");
    }
    //apply air fleet modifier
    if ((isMud() == false) && (isWinter() == false)) {
      if (airFleetCache.getAirFleetSupportZone().containsAll(dH)) {
        if (attackerSide.equals(SIDE_AXIS)) {
          column += 1;
          sb.append(", air fleet shift +1");
        } else {
          column -= 1;
          sb.append(", air fleet shift -1");
        }
      }
    }
    if (column < 0) {
      sb.append(". Attack is impossible due to low odds. Final result is NE.");
      commandBaseLog(sb.toString());
      return "NE";
    } else {
      //ensure column is not greater than 9
      column = Math.min(9, column);
      //
      int combatMode = RsrGlobal.getCombatMode();
      int modifier = 0;
      //overrun combat modifer
      if (combatMode == 1) {
        modifier += 1;
      }
      //winter combat modifier
      if (isFirstWinter()) {
        modifier += 3;
      } else if (isWinter()) {
        modifier += 1;
      }
      //leader combat modifier
      if (attackerSide.equals(SIDE_SOVIET)) {
        modifier += pieceCache.getLeaderFirepowerValue(dH);
      }
      //siege artillery combat modifier
      if ((attackerSide.equals(SIDE_AXIS)) && (majorCity == true) && (combatMode == 0)) {
        modifier += pieceCache.getSiegeArtilleryFirepowerValue(dH);
      }
      //ensure roll is between 0 and 9
      int die = ran.nextInt(6);
      int moddie = Math.max(0, Math.min(9, modifier + die));
      String result = COMBAT_TABLE[column][moddie];
      sb.append((modifier > 0 ? ". Die roll modifier is +" + modifier : "") + ". Die roll is " + (die+1) + ". Final result is " + result + ".");
      commandBaseLog(sb.toString());
      return result;
    }
  }

  public static final String[][] COMBAT_TABLE = new String[][]{
    new String[]{"AE","AE","A2","A2","A1","NE","AE","AE","2/1","1/1"},
    new String[]{"A2","A2","A1","A1","NE","D1","1/1","2/1","1/-","1/1"},
    new String[]{"A2","A1","A1","NE","D1","D1","1/1","1/-","1/1","1/1"},
    new String[]{"A1","A1","NE","NE","D1","D1","1/1","1/-","1/1","1/1"},
    new String[]{"A1","A1","NE","D1","D1","D2","1/1","1/1","1/1","1/2"},
    new String[]{"A1","NE","D1","D1","D2","D2","1/1","1/1","1/2","1/2"},
    new String[]{"NE","D1","D1","D2","D2","D3","1/1","1/2","1/2","1/3"},
    new String[]{"D1","D1","D2","D2","D3","D3","1/2","1/2","1/3","DE"},
    new String[]{"D1","D2","D2","D3","D3","DE","1/2","1/3","DE","DE"},
    new String[]{"D2","D3","D3","DE","DE","DE","1/3","DE","DE","DE"}
  };

  public ArrayList<GamePiece> getLossCandidates(ArrayList<GamePiece> pieces) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    if (pieces.size() > 0) {
      //try to select units that can contest spaces (units except 3)
      String unitType;
      for (GamePiece gp : pieces) {
        if (RsrTrait.getType(gp).equals("Unit")) {
          unitType = RsrTrait.getUnitType(gp);
          if ((RsrTrait.getCurrentStep(gp) > 1) || ((unitType.equals("SovietGuardsCavalryCorps") == false) && (unitType.equals("AxisGermanInfantryBattlegroup") == false) && (unitType.equals("AxisGermanMountainBattlegroup") == false))) {
            result.add(gp);
          }
        }
      }
      //if there are no candidates, try to select units that sometimes can contest spaces (other units)
      if (result.size() == 0) {
        for (GamePiece gp : pieces) {
          if (RsrTrait.getType(gp).equals("Unit")) {
            result.add(gp);
          }
        }
        //if there are no candidates, try to select units that can't contest spaces (not assets)
        if (result.size() == 0) {
          for (GamePiece gp : pieces) {
            if (RsrTrait.getType(gp).equals("Asset") == false) {
              result.add(gp);
            }
          }
          //if there are no candidates, try to select units that can't move (assets)
          if (result.size() == 0) {
            for (GamePiece gp : pieces) {
              if (RsrTrait.getType(gp).equals("Asset")) {
                result.add(gp);
              }
            }
          }
        }
      }
    }
    //find minimum number of lost steps by selected units, and leave only such units
    if (result.size() > 1) {
      int min = 0;
      for (GamePiece gp : result) {
        min = Math.min(min, RsrTrait.getLost(gp));
      }
      Iterator<GamePiece> it = result.iterator();
      GamePiece gpi;
      while (it.hasNext()) {
        gpi = it.next();
        if (RsrTrait.getLost(gpi) > min) {
          it.remove();
        }
      }
    }
    return result;
  }

  public HashSet<HexRef> getAvailablePorts(String side, String tag) {
    HashSet<HexRef> result = mainTerrain.findHexesWithTag(TAG_PORT, tag);
    if ((Filter.isAny(tag, TAG_PORT_ARCTIC, TAG_PORT_BALTIC)) && (isWinter())) {
      result.clear();
    } else {
      Iterator<HexRef> it = result.iterator();
      if (side.equals(SIDE_AXIS)) {
        while (it.hasNext()) {
          if (isHexAxisControlled(it.next()) == false) {
            it.remove();
          }
        }
      } else {
        while (it.hasNext()) {
          if (isHexSovietControlled(it.next()) == false) {
            it.remove();
          }
        }
      }
    }
    return result;
  }

  public int allowSeaMovement(String side) {
    int result = 0;
    if (RsrGlobal.getScenario() != 1) {
      result += allowSeaMovement(side, TAG_PORT_ARCTIC);
      result += allowSeaMovement(side, TAG_PORT_BALTIC);
      result += allowSeaMovement(side, TAG_PORT_BLACKSEA);
    }
    return result;
  }

  private int allowSeaMovement(String side, String port) {
    HashSet<HexRef> ports = getAvailablePorts(side, port);
    if (ports.size() < 2) {
      return 0;
    } else {
      int count = 0;
      for (HexRef hr : ports) {
        for (GamePiece gp : pieceCache.getPiecesIn(hr)) {
          if (getSeaMovementWeight(gp) > 0.0d) {
            commandAllowMovement(gp, 0.0d, true);
            count++;
          }
        }
      }
      return count;
    }
  }

  public double getSeaMovementWeight(GamePiece gp) {
    String type = RsrTrait.getType(gp);
    if (type.equals("Unit")) {
      String unitType = RsrTrait.getUnitType(gp);
      if (Filter.isAny(unitType, "AxisGermanInfantryBattlegroup", "AxisGermanMountainBattlegroup", "SovietInfantryCorps", "SovietMarineCorps", "SovietAirborneCorps")) {
        return 0.5d;
      } else
      if (Filter.isAny(unitType, "AxisGermanInfantryCorps", "AxisGermanMountainCorps", "AxisFinnishInfantryCorps", "AxisRomanianInfantryCorps", "AxisRomanianMountainCorps", "SovietGuardsArmy", "SovietShockArmy", "SovietInfantryArmy")) {
        return 1.0d;
      }
    } else if (type.equals("Depot")) {
      return 1.0d;
    }
    return 0.0d;
  }

  public HashSet<HexRef> getSeaMovementHexes(GamePiece gp) {
    String side = RsrTrait.getSide(gp);
    HexRef fromPort = getHexRef(gp.getPosition());
    HashSet<HexRef> ports = getAvailablePorts(side, TAG_PORT_ARCTIC);
    if (ports.contains(fromPort) == false) {
      ports = getAvailablePorts(side, TAG_PORT_BALTIC);
      if (ports.contains(fromPort) == false) {
        ports = getAvailablePorts(side, TAG_PORT_BLACKSEA);
        if (ports.contains(fromPort) == false) {
          ports.clear();
        }
      }
    }
    ports.remove(fromPort);
    return ports;
  }

  public double getMovementAllowance(GamePiece gp) {
    double result = RsrTrait.getMovement(gp);
    if (isLockedInFinlandOrRomania(gp)) {
      result = 0.0d;
    }
    if (RsrTrait.isSupplyLevel(gp, SUPPLY_LACK, SUPPLY_OUT)) {
      result = Math.ceil(result * 0.5d);
    }
    return result;
  }

  public double getStrategicMovementAllowance(GamePiece gp) {
    String type = RsrTrait.getType(gp);
    if (Filter.isAny(type, "Airbase", "Partisans", "Asset", "AirFleet")) {
      return 0.0d;
    }
    if ((type.equals("Depot")) && (isMud() == false)) {
      return 0.0d;
    }
    if (RsrTrait.isSupplyLevel(gp, SUPPLY_EMERGENCY, SUPPLY_LACK, SUPPLY_OUT)) {
      return 0.0d;
    }
    if (isLockedInFinlandOrRomania(gp)) {
      return 0.0d;
    }
    return 25.0d;
  }

  public double getOverrunCost(GamePiece gp) {
    if (RsrTrait.getType(gp).equals("Unit") == false) {
      return Double.MAX_VALUE;
    }
    if (RsrTrait.isSupplyLevel(gp, SUPPLY_LACK, SUPPLY_OUT)) {
      return Double.MAX_VALUE;
    }
    double cost;
    if ((isWinter()) && (RsrTrait.getSide(gp).equals(SIDE_SOVIET))) {
      cost = 2.0d;
    } else {
      cost = 3.0d;
    }
    if (cost <= RsrTrait.getMovementLeft(gp)) {
      return cost;
    } else {
      return Double.MAX_VALUE;
    }
  }

  public boolean canContestOwnSpace(ArrayList<GamePiece> list) {
    for (GamePiece gp : list) {
      if ((Filter.isAny(RsrTrait.getType(gp), "AirFleet", "Partisans") == false) && (RsrTrait.getRetreating(gp) == 0) && (RsrTrait.getOverstackRetreating(gp) == 0)) {
        return true;
      }
    }
    return false;
  }

  public boolean canContestNeighbourSpace1(ArrayList<GamePiece> list) {
    String unitType;
    int battlegroups = 0;
    for (GamePiece gp : list) {
      if ((RsrTrait.getType(gp).equals("Unit")) && (RsrTrait.getRetreating(gp) == 0) && (RsrTrait.getOverstackRetreating(gp) == 0)) {
        unitType = RsrTrait.getUnitType(gp);
        if (unitType.equals("SovietGuardsCavalryCorps")) {
        //soviet guards cavalry corps can't contest neighbour space
        } else if (Filter.isAny(unitType, "AxisGermanInfantryBattlegroup", "AxisGermanMountainBattlegroup")) {
          if (RsrTrait.getCurrentStep(gp) == 1) {
            battlegroups++;
            if (battlegroups >= 2) {
              //2 weak infantry battlegroups can contest neighbour space
              return true;
            }
          } else {
            //1 strong infantry battlegroup can contest neighbour space
            return true;
          }
        } else {
          //any other unit can contest neighbour space
          return true;
        }
      }
    }
    return false;
  }

  public boolean canContestNeighbourSpace2(ArrayList<GamePiece> list, HexRef fromHex, HexRef toHex) {
    String et = mainTerrain.getEdgeTerrainName(fromHex, toHex);
    String lt = mainTerrain.getLineTerrainName(fromHex, toHex);
    if (isImpassable(et)) {
      return false;
    } else
    if (isLakeSea(et)) {
      if ((lt.equals(LINE_FROZENSURFACE)) && (isWinter())) {
        for (GamePiece gp : list) {
          if ((RsrTrait.getType(gp).equals("Unit")) && (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_NONMOTORIZED) && (RsrTrait.getUnitType(gp).equals("SovietGuardsCavalryCorps") == false)) {
            //soviet non-motorized, non-soviet guards cavalry corps can contest neighbour space across frozen surface in winter
            return true;
          }
        }
      }
      return false;
    }
    return true;
  }

  public int getAttackStrength(GamePiece gp, boolean test) {
    String type = RsrTrait.getType(gp);
    if (Filter.isAny(type, "Airbase", "AirFleet", "Depot", "SiegeArtillery", "SecurityDivision", "Headquarters", "Partisans", "Leader", "Asset")) {
      return 0;
    }
    String unitType = RsrTrait.getUnitType(gp);
    GamePiece strengthPiece;
    if ((unitType.startsWith("Soviet")) && (unitType.endsWith("Army"))) {
      String strengthID = RsrTrait.getStrengthID(gp);
      if (strengthID.equals("")) {
        if (test) {
          return -1;
        } else {
          commandFetchCounter(gp);
        }
      }
      strengthPiece = findStrength(RsrTrait.getCombatClass(gp), RsrTrait.getStrengthID(gp));
    } else {
      strengthPiece = gp;
    }
    double strength = RsrTrait.getCurrentStep(gp) == 2 ? RsrTrait.getAttack2(strengthPiece) : RsrTrait.getAttack1(strengthPiece);
    if (RsrTrait.isSupplyLevel(gp, SUPPLY_LACK, SUPPLY_OUT)) {
      return (int)Math.ceil(strength / 2.0d);
    } else {
      return (int)strength;
    }
  }

  public int getDefendStrength(GamePiece gp, boolean test) {
    if (RsrTrait.getAttacked(gp) > 0) {
      return 0;
    }
    String type = RsrTrait.getType(gp);
    if (Filter.isAny(type, "AirFleet", "Partisans")) {
      return 0;
    }
    if (Filter.isAny(type, "Airbase", "Depot", "SiegeArtillery", "SecurityDivision", "Headquarters", "Leader", "Asset")) {
      return 1;
    }
    String unitType = RsrTrait.getUnitType(gp);
    GamePiece strengthPiece;
    if ((unitType.startsWith("Soviet")) && (unitType.endsWith("Army"))) {
      String strengthID = RsrTrait.getStrengthID(gp);
      if (strengthID.equals("")) {
        if (test) {
          return -1;
        } else {
          commandFetchCounter(gp);
        }
      }
      strengthPiece = findStrength(RsrTrait.getCombatClass(gp), RsrTrait.getStrengthID(gp));
    } else {
      strengthPiece = gp;
    }
    double strength = RsrTrait.getCurrentStep(gp) == 2 ? RsrTrait.getDefend2(strengthPiece) : RsrTrait.getDefend1(strengthPiece);
    if (RsrTrait.isSupplyLevel(gp, SUPPLY_OUT)) {
      return (int)Math.ceil(strength / 2.0d);
    } else {
      return (int)strength;
    }
  }

  public boolean isStackSizeOK(HexRef hr) {
    ArrayList<GamePiece> pieces = pieceCache.getPiecesIn(hr);
    if ((pieces == null) || (pieces.size() == 0)) {
      return true;
    } else {
      if (filter.filterPieces(pieces, "Side=='" + SIDE_AXIS + "'").size() > 0) {
        return isStackSizeOK(hr, SIDE_AXIS, 0.0d);
      } else {
        return isStackSizeOK(hr, SIDE_SOVIET, 0.0d);
      }
    }
  }

  public boolean isStackSizeOK(HexRef hr, String side, double mySize) {
    if (side.equals(SIDE_AXIS)) {
      return pieceCache.getStackInfo(hr).stackSize + mySize <= 14.0d;
    } else {
      return pieceCache.getStackInfo(hr).stackSize + mySize <= 2.0d;
    }
  }

  public boolean isStackSizeOK(HexRef hr, ArrayList<GamePiece> pieces) {
    if (pieces.size() == 0) {
      return true;
    } else {
      double mySize = 0.0d;
      for (GamePiece gp : pieces) {
        mySize += RsrTrait.getStackPoints(gp);
      }
      return isStackSizeOK(hr, RsrTrait.getSide(pieces.get(0)), mySize);
    }
  }

  public boolean isStackFriendly(HexRef hr) {
    ArrayList<GamePiece> pieces = pieceCache.getPiecesIn(hr);
    if ((pieces == null) || (pieces.size() == 0)) {
      return true;
    } else {
      StackInfo si = pieceCache.getStackInfo(hr);
      if (RsrTrait.getSide(pieces.get(0)).equals(SIDE_AXIS)) {
        return si.nationalities.contains(NATIONALITY_GERMAN) ? si.nationalities.size() <= 2 : si.nationalities.size() <= 1;
      } else {
        return si.guardsCavalryCorps <= 1;
      }
    }
  }

  public boolean isStackFriendly(HexRef hr, ArrayList<GamePiece> pieces) {
    for (GamePiece gp : pieces) {
      if (isStackFriendly(hr, RsrTrait.getSide(gp), RsrTrait.getNationality(gp), RsrTrait.getUnitType(gp)) == false) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isStackFriendly(HexRef hr, String side, String nationality, String unitType) {
    if ((side.equals(SIDE_AXIS)) && (nationality.equals(NATIONALITY_GERMAN) == false)) {
      for (String n : pieceCache.getStackInfo(hr).nationalities) {
        if ((n.equals(NATIONALITY_GERMAN) == false) && (n.equals(nationality) == false)) {
          return false;
        }
      }
      return true;
    } else if ((side.equals(SIDE_SOVIET)) && (unitType.equals("SovietGuardsCavalryCorps"))) {
      return pieceCache.getStackInfo(hr).guardsCavalryCorps == 0;
    } else {
      return true;
    }
  }

  public void scenarioChanged(int scenario) {
    isNorthMapUsed = scenario == 6;
    isSouthwestMapUsed =(scenario == 3) || (scenario == 4) || (scenario == 6);
    isSubMapUsed = scenario == 1;
  }

  public void showSupplyShading(int mode) {
    if (mode == lastSupplyDisplayed) {
      shaderSupply.setShadedHexes(null);
      lastSupplyDisplayed = -1;
    } else {
      switch (mode) {
        case 1 : shaderSupply.setShadedHexes(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesDepotSupply, "Normal", null).keySet()); break;
        case 2 : shaderSupply.setShadedHexes(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesAxisHQSupply, "Bucharest;Romanian;Normal", null).keySet()); break;
        case 3 : shaderSupply.setShadedHexes(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesAxisHQSupply, "Helsinki;Finnish;Normal", null).keySet()); break;
        default : shaderSupply.setShadedHexes(TerrainMapShaderRule.getHexes(mainMap, mainGrid, mainTerrain, rulesSovietSupply, "Normal", null).keySet()); break;
      }
      lastSupplyDisplayed = mode;
    }
  }
  
  public void commandSwitchBreakdownMethod() {
    int breakdownMode = RsrGlobal.getBreakdownMode();
    breakdownMode++;
    if (breakdownMode == 3) {
      breakdownMode = 0;
    }
    commandBaseSetProperty(dataPiece, G_BREAKDOWN_MODE, breakdownMode);
    switch (breakdownMode) {
      case 1  : commandBaseLog("Desired Soviet army breakdown set to infantry and mechanized corps."); break;
      case 2  : commandBaseLog("Desired Soviet army breakdown set to 2 mechanized corps."); break;
      default : commandBaseLog("Desired Soviet army breakdown set to 2 infantry corps."); break;
    }
  }
  
  public void commandBreakdown(ArrayList<GamePiece> pieces) {
    if (pieces.size() != 1) {
      commandBaseLog("Select a single unit for breakdown.");
      return;
    }
    GamePiece gp = pieces.get(0);
    if (isLockedInFinlandOrRomania(gp)) {
      commandBaseLog("No breakdown allowed in Finland or Romania on turn 1.");
      return;
    }
    if (RsrTrait.getReorganized(gp) > 0) {
      commandBaseLog("No breakdown allowed.");
      return;
    }
    String unitType = RsrTrait.getUnitType(gp);
    int currentStep = RsrTrait.getCurrentStep(gp);
    ArrayList<GamePiece> bb = null;
    int req = Integer.MAX_VALUE;
    if (unitType.equals("AxisGermanTankCorps")) {
      req = 2;
      bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanTankBattlegroup", req);
    } else if (unitType.equals("AxisGermanInfantryCorps")) {
      req = 2;
      bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanInfantryBattlegroup", req);
    } else if (unitType.equals("AxisGermanMountainCorps")) {
      req = 2;
      bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanMountainBattlegroup", req);
    } else if ((unitType.equals("AxisGermanTankBattlegroup")) && (currentStep == 2)) {
      req = 1;
      bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanTankBattlegroup", req);
    } else if ((unitType.equals("AxisGermanInfantryBattlegroup")) && (currentStep == 2)) {
      req = 1;
      bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanInfantryBattlegroup", req);
    } else if ((unitType.equals("AxisGermanMountainBattlegroup")) && (currentStep == 2)) {
      req = 1;
      bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanMountainBattlegroup", req);
    } else if (unitType.equals("SovietInfantryArmy")) {
      if (isProductionTurn()) {
        req = 2;
        switch (RsrGlobal.getBreakdownMode()) {
          case 1  :
            bb = findInOffboardZone("Force Pool Box", "Unit", "SovietInfantryCorps','SovietMarineCorps", 1);
            bb.addAll(findInOffboardZone("Force Pool Box", "Unit", "SovietMechanizedCorps", 1));
            break;
          case 2  : bb = findInOffboardZone("Force Pool Box", "Unit", "SovietMechanizedCorps", req); break;
          default : bb = findInOffboardZone("Force Pool Box", "Unit", "SovietInfantryCorps','SovietMarineCorps", req); break;
        }
      }
    }
    if (bb == null) {
      commandBaseLog("No breakdown allowed.");
      return;
    }
    if (bb.size() < req) {
      commandBaseLog("No breakdown if not enough replacement units.");
      return;
    }
    if (RsrTrait.getSupplyLevel(gp) != SUPPLY_NORMAL) {
      commandBaseLog("No breakdown if not in supply.");
      return;
    }
    HexRef hr = getHexRef(gp.getPosition());
    HashSet<HexRef> inZOC = isMud() ? new HashSet<HexRef>() : pieceCache.getHexesInZOCEnemy(RsrTrait.getSide(gp));
    if (inZOC.contains(hr)) {
      commandBaseLog("No breakdown if in contested space (unless it is a mud turn).");
      return;
    }
    if (Filter.isAny(unitType, "AxisGermanTankCorps", "AxisGermanInfantryCorps")) {
      commandSendToPiece(gp, bb.get(0), false);
      commandSendToPiece(gp, bb.get(1), false);
      if (currentStep == 1) {
        commandBaseSetProperty(bb.get(1), T_CURRENT_STEP, 1);
      }
      commandSendToOffboardZone(gp, "German Breakdown & Buildup Box");
      commandMarkAsReorganized(bb.get(0));
      commandMarkAsReorganized(bb.get(1));
    } else if (unitType.equals("AxisGermanMountainCorps")) {
      commandSendToPiece(gp, bb.get(0), false);
      if (currentStep == 1) {
        commandBaseSetProperty(bb.get(0), T_CURRENT_STEP, 1);
      }
      commandSendToPiece(gp, bb.get(1), false);
      commandBaseSetProperty(bb.get(1), T_CURRENT_STEP, 1);
      commandSendToOffboardZone(gp, "German Breakdown & Buildup Box");
      commandMarkAsReorganized(bb.get(0));
      commandMarkAsReorganized(bb.get(1));
    } else if (Filter.isAny(unitType, "AxisGermanTankBattlegroup", "AxisGermanInfantryBattlegroup", "AxisGermanMountainBattlegroup")) {
      commandBaseSetProperty(gp, T_CURRENT_STEP, 1);
      commandSendToPiece(gp, bb.get(0), false);
      commandBaseSetProperty(bb.get(0), T_CURRENT_STEP, 1);
      commandMarkAsReorganized(gp);
      commandMarkAsReorganized(bb.get(0));
    } else if (Filter.isAny(unitType, "SovietInfantryArmy")) {
      commandSendToPiece(gp, bb.get(0), false);
      commandSendToPiece(gp, bb.get(1), false);
      commandSendToOffboardZone(gp, "Force Pool Box");
      commandMarkAsReorganized(bb.get(0));
      commandMarkAsReorganized(bb.get(1));
    }
  }

  public void commandBuildup(ArrayList<GamePiece> pieces) {
    if (pieces.size() != 2) {
      commandBaseLog("Select two units for buildup.");
      return;
    }
    GamePiece gp1 = pieces.get(0);
    GamePiece gp2 = pieces.get(1);
    if (RsrTrait.getNominalSteps(gp1) < RsrTrait.getNominalSteps(gp2)) {
      GamePiece gpr = gp1;
      gp1 = gp2;
      gp2 = gpr;
    }
    if ((isLockedInFinlandOrRomania(gp1)) || (isLockedInFinlandOrRomania(gp2))) {
      commandBaseLog("No buildup allowed in Finland or Romania on turn 1.");
      return;
    }
    if ((RsrTrait.getReorganized(gp1) > 0) && (RsrTrait.getReorganized(gp2) > 0)) { //&& to allow buildup if one unit was part of earlier breakdown
      commandBaseLog("No buildup allowed.");
      return;
    }
    String unitType1 = RsrTrait.getUnitType(gp1);
    int currentStep1 = RsrTrait.getCurrentStep(gp1);
    String unitType2 = RsrTrait.getUnitType(gp2);
    int currentStep2 = RsrTrait.getCurrentStep(gp2);
    boolean allowed = false;
    boolean ignoreZOC = false;
    ArrayList<GamePiece> bb = null;
    int size = 0;
    if (unitType1.equals("AxisGermanTankCorps")) {
      allowed = (currentStep1 == 1) && (unitType2.equals("AxisGermanTankBattlegroup")) && (currentStep2 == 1);
    } else if (unitType1.equals("AxisGermanInfantryCorps")) {
      allowed = (currentStep1 == 1) && (unitType2.equals("AxisGermanInfantryBattlegroup")) && (currentStep2 == 1);
    } else if (unitType1.equals("AxisGermanMountainCorps")) {
      allowed = (currentStep1 == 1) && (unitType2.equals("AxisGermanMountainBattlegroup")) && (currentStep2 == 1);
    } else if (unitType1.equals("AxisGermanTankBattlegroup")) {
      size = 3;
      allowed = (unitType2.equals("AxisGermanTankBattlegroup")) && (((currentStep1 == 1) && (currentStep2 == 1)) || ((bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanTankCorps", 1)).size() == 1));
    } else if (unitType1.equals("AxisGermanInfantryBattlegroup")) {
      size = 3;
      allowed = (unitType2.equals("AxisGermanInfantryBattlegroup")) && (((currentStep1 == 1) && (currentStep2 == 1)) || ((bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanInfantryCorps", 1)).size() == 1));
    } else if (unitType1.equals("AxisGermanMountainBattlegroup")) {
      size = 2;
      allowed = (unitType2.equals("AxisGermanMountainBattlegroup")) && (((currentStep1 == 1) && (currentStep2 == 1)) || ((currentStep1 + currentStep2 < 4) && ((bb = findInOffboardZone("German Breakdown & Buildup Box", "Unit", "AxisGermanMountainCorps", 1)).size() == 1)));
    } else if (Filter.isAny(unitType1, "SovietInfantryCorps", "SovietMechanizedCorps", "SovietMarineCorps")) {
      allowed = (isProductionTurn()) && (Filter.isAny(unitType2, "SovietInfantryCorps", "SovietMechanizedCorps", "SovietMarineCorps")) && ((bb = findInOffboardZone("Force Pool Box", "Unit", "SovietInfantryArmy", 1)).size() == 1);
      ignoreZOC = true;
    }
    if (allowed == false) {
      commandBaseLog("No buildup allowed.");
      return;
    }
    HexRef hr1 = getHexRef(gp1.getPosition());
    HexRef hr2 = getHexRef(gp2.getPosition());
    if (hr1.equals(hr2) == false) {
      commandBaseLog("No buildup if not in the same space.");
      return;
    }
    if ((RsrTrait.getSupplyLevel(gp1) != SUPPLY_NORMAL) || (RsrTrait.getSupplyLevel(gp2) != SUPPLY_NORMAL)) {
      commandBaseLog("No buildup if not in supply.");
      return;
    }
    HashSet<HexRef> inZOC = isMud() ? new HashSet<HexRef>() : pieceCache.getHexesInZOCEnemy(RsrTrait.getSide(gp1));
    if ((ignoreZOC == false) && (inZOC.contains(hr1))) {
      commandBaseLog("No buildup if in contested space (unless it is a mud turn).");
      return;
    }
    if (Filter.isAny(unitType1, "AxisGermanTankCorps", "AxisGermanInfantryCorps", "AxisGermanMountainCorps")) {
      commandBaseSetProperty(gp1, T_CURRENT_STEP, 2);
      commandSendToOffboardZone(gp2, "German Breakdown & Buildup Box");
      commandMarkAsReorganized(gp1);
    } else if (Filter.isAny(unitType1, "AxisGermanTankBattlegroup", "AxisGermanInfantryBattlegroup", "AxisGermanMountainBattlegroup")) {
      if ((currentStep1 == 1) && (currentStep2 == 1)) {
        commandBaseSetProperty(gp1, T_CURRENT_STEP, 2);
        commandSendToOffboardZone(gp2, "German Breakdown & Buildup Box");
        commandMarkAsReorganized(gp1);
      } else {
        commandSendToPiece(gp1, bb.get(0), false);
        if (currentStep1 + currentStep2 == size) {
          commandBaseSetProperty(bb.get(0), T_CURRENT_STEP, 1);
        }
        commandSendToOffboardZone(gp1, "German Breakdown & Buildup Box");
        commandSendToOffboardZone(gp2, "German Breakdown & Buildup Box");
        commandMarkAsReorganized(bb.get(0));
      }
    } else if (Filter.isAny(unitType1, "SovietInfantryCorps", "SovietMechanizedCorps", "SovietMarineCorps")) {
      commandSendToPiece(gp1, bb.get(0), false);
      commandSendToOffboardZone(gp1, "Force Pool Box");
      commandSendToOffboardZone(gp2, "Force Pool Box");
      commandMarkAsReorganized(bb.get(0));
    }
  }

  public void commandIncorporate(ArrayList<GamePiece> pieces) {
    if (pieces.size() != 2) {
      commandBaseLog("Select two units for incorporation.");
      return;
    }
    GamePiece gp1 = pieces.get(0);
    GamePiece gp2 = pieces.get(1);
    if (RsrTrait.getNominalSteps(gp1) < RsrTrait.getNominalSteps(gp2)) {
      GamePiece gpr = gp1;
      gp1 = gp2;
      gp2 = gpr;
    }
    if ((RsrTrait.getReorganized(gp1) > 0) || (RsrTrait.getReorganized(gp2) > 0)) {
      commandBaseLog("No incorporation allowed.");
      return;
    }
    String unitType1 = RsrTrait.getUnitType(gp1);
    int currentStep1 = RsrTrait.getCurrentStep(gp1);
    String unitType2 = RsrTrait.getUnitType(gp2);
    boolean allowed = false;
    if (unitType1.equals("SovietTankArmy")) {
      allowed = (currentStep1 == 1) && (unitType2.equals("SovietMechanizedCorps"));
    } else if (Filter.isAny(unitType1, "SovietGuardsArmy", "SovietShockArmy", "SovietInfantryArmy")) {
      allowed = (currentStep1 == 1) && (Filter.isAny(unitType2, "SovietMechanizedCorps", "SovietInfantryCorps", "SovietMarineCorps"));
    }
    if (allowed == false) {
      commandBaseLog("No incorporation allowed.");
      return;
    }
    HexRef hr1 = getHexRef(gp1.getPosition());
    HexRef hr2 = getHexRef(gp2.getPosition());
    if (mainGrid.getHexesInRange(hr1, 1).contains(hr2) == false) {
      commandBaseLog("No incorporation if not in adjacent spaces.");
      return;
    }
    if ((RsrTrait.getSupplyLevel(gp1) != SUPPLY_NORMAL) || (RsrTrait.getSupplyLevel(gp2) != SUPPLY_NORMAL)) {
      commandBaseLog("No incorporation if not in supply.");
      return;
    }
    commandBaseSetProperty(gp1, T_CURRENT_STEP, 2);
    commandSendToOffboardZone(gp2, "Force Pool Box");
    commandMarkAsReorganized(gp1);
  }

  public String calculateVP(boolean log) {
    int vpc = 0;
    int vpu = 0;
    int vpa = 0;
    int scenario = RsrGlobal.getScenario();
    supplyCache.clear();
    HashSet<HexRef> axisLOC = supplyCache.getAxisLine();
    for (GamePiece gp : filter.filterPieces(mainMap, "MarkerType=='Control'&&Control==2&&VP>0")) {
      HexRef hr = getHexRef(gp.getPosition());
      if (axisLOC.contains(hr)) {
        vpc += RsrTrait.getVP(gp);
      }
    }
    String t, ut;
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='" + SIDE_AXIS + "'")) {
      if (RsrTrait.getSupplyLevel(gp) == 0) {
        t = RsrTrait.getType(gp);
        ut = RsrTrait.getUnitType(gp);
        if (t.equals("Unit")) {
          if (ut.equals("AxisGermanTankCorps")) {
            vpu += RsrTrait.getCurrentStep(gp) == 2 ? 8 : 6;
          } else if (ut.equals("AxisGermanInfantryCorps")) {
            vpu += RsrTrait.getCurrentStep(gp) == 2 ? 4 : 3;
          } else if (ut.equals("AxisGermanMountainCorps")) {
            vpu += RsrTrait.getCurrentStep(gp) == 2 ? 3 : 2;
          } else if (ut.equals("AxisGermanTankBattlegroup")) {
            vpu += RsrTrait.getCurrentStep(gp) == 2 ? 4 : 2;
          } else if (ut.equals("AxisGermanInfantryBattlegroup")) {
            vpu += RsrTrait.getCurrentStep(gp);
          } else if (ut.equals("AxisGermanMountainBattlegroup")) {
            vpu += RsrTrait.getCurrentStep(gp);
          } else if (ut.equals("AxisGermanTankDivision")) {
            vpu += RsrTrait.getCurrentStep(gp) == 2 ? 4 : 2;
          } else if (ut.equals("AxisItalianArmy")) {
            vpu += RsrTrait.getCurrentStep(gp);
          } else if (ut.equals("AxisHungarianArmy")) {
            vpu += RsrTrait.getCurrentStep(gp);
          } else if (ut.equals("AxisFinnishInfantryCorps")) {
            vpu += RsrTrait.getCurrentStep(gp);
          } else if (ut.equals("AxisRomanianInfantryCorps")) {
            vpu += RsrTrait.getCurrentStep(gp);
          } else if (ut.equals("AxisRomanianMountainCorps")) {
            vpu += RsrTrait.getCurrentStep(gp);
          }
        } else if (t.equals("Depot")) {
          vpu += 2;
        } else if (t.equals("SecurityDivision")) {
          vpu += 1;
        } else if (t.equals("Headquarters")) {
          vpu += 1;
        } else if (t.equals("Airbase")) {
          vpu += 4;
        }
      }
    }
    if (scenario == 3) {
      for (GamePiece gp : filter.filterPieces(Map.getMapById("Offboard"), "CurrentZone=='Eliminated Economic Assets'")) {
        t = RsrTrait.getType(gp);
        ut = RsrTrait.getAssetType(gp);
        if (t.equals("Asset")) {
          if (ut.equals("Oil")) {
            vpa += 10;
          } else if (ut.equals("Oil Pipeline")) {
            vpa += 8;
          } else {
            vpa += 5;
          }
        }
      }
    }
    int vpt = vpc + vpu + vpa;
    String level;
    if (scenario == 1) {
      if (vpt <= 50) {
        level = "Soviet Win";
      } else if (vpt <= 57) {
        level = "Nobody Wins";
      } else {
        level = "German Win";
      }
    } else if (scenario == 2) {
      if (vpt <= 289) {
        level = "Really Big Soviet Win";
      } else if (vpt <= 309) {
        level = "Big Soviet Win";
      } else if (vpt <= 324) {
        level = "Little Soviet Win";
      } else if (vpt <= 335) {
        level = "Little German Win";
      } else if (vpt <= 350) {
        level = "Big German Win";
      } else {
        level = "Really Big German Win";
      }
    } else if (scenario == 3) {
      if (vpt <= 220) {
        level = "Really Big Soviet Win";
      } else if (vpt <= 240) {
        level = "Big Soviet Win";
      } else if (vpt <= 260) {
        level = "Little Soviet Win";
      } else if (vpt <= 280) {
        level = "Nobody Wins";
      } else if (vpt <= 300) {
        level = "Little German Win";
      } else if (vpt <= 320) {
        level = "Big German Win";
      } else {
        level = "Really Big German Win";
      }
    } else {
      //scenarios 4, 5 and 6
      if (vpt <= 450) {
        level = "Really Big Soviet Win";
      } else if (vpt <= 480) {
        level = "Big Soviet Win";
      } else if (vpt <= 510) {
        level = "Little Soviet Win";
      } else if (vpt <= 530) {
        level = "Nobody Wins";
      } else if (vpt <= 560) {
        level = "Little German Win";
      } else if (vpt <= 590) {
        level = "Big German Win";
      } else {
        level = "Really Big German Win";
      }
    }
    if (log) {
      Command cmd = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "Total VP: " + vpt + " (control: " + vpc + ", units: " + vpu + ", assets: " + vpa + ") - " + level);
      cmd.execute();
    }
    return level;
  }

  public Command getRestoreCommand() {
    return null;
  }
  
  public void discoverOverstackHexes() {
    CombatHexList chl = new CombatHexList("");
    HashSet<HexRef> hexes = chl.getHexes();
    for (HexRef hr : pieceCache.getOccupiedHexes()) {
      if ((isStackSizeOK(hr) == false) || (isStackFriendly(hr) == false)) {
        chl.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
        hexes.add(hr);
      }
    }
    commandBaseSetProperty(dataPiece, G_OVERSTACK_HEXES, chl.toString());
    if (hexes.size() > 0) {
      shaderOverstack.setShadedHexes(hexes);
    } else {
      shaderOverstack.setShadedHexes(null);
    }       
  }  
  
}
