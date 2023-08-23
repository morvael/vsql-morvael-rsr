package rsr;

import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.command.Command;
import java.awt.Point;
import VASSAL.counters.GamePiece;
import filter.Filter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.KeyStroke;
import terrain.HexRef;
import terrain.TerrainMapShaderCost;

/**
 *
 * @author morvael
 * @since 2009-05-08
 */
public final class RsrAdvance extends RsrAdvanceBase {

  private static RsrAdvance instance;

  public static RsrAdvance getInstance() {
    if (instance == null) {
      new RsrAdvance();
    }
    return instance;
  }
  //
  private RsrKeyCommand[] commandsArray = new RsrKeyCommand[0];
  private ArrayList<RsrKeyCommand> commands = new ArrayList<RsrKeyCommand>();

  public RsrAdvance() {
    super();
    if (instance == null) {
      instance = this;
      ArrayList<GameComponent> toRemove = new ArrayList<GameComponent>();
      Iterator<GameComponent> it = GameModule.getGameModule().getGameState().getGameComponents().iterator();
      GameComponent gc;
      while (it.hasNext()) {
        gc = it.next();
        if (gc instanceof RsrAdvance) {
          toRemove.add(gc);
        }
      }
      for (GameComponent fgc : toRemove) {
        GameModule.getGameModule().getGameState().removeGameComponent(fgc);
      }
      GameModule.getGameModule().getGameState().addGameComponent(this);
      initialize("");
    }
  }

  public RsrButtonCommand getDesiredInstance() {
    return RsrAdvance.getInstance();
  }

  protected void executeSetPhaseParts() {
    commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PART, 1);
    String phaseName = getPhaseName();
    if (phaseName.equals(PHASE_SUPPLY)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 1);
    } else if (phaseName.equals(PHASE_AXIS_REINFORCEMENT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 1);
    } else if (phaseName.equals(PHASE_AXIS_MOVEMENT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 4);
    } else if (phaseName.equals(PHASE_AXIS_COMBAT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 1);
    } else if (phaseName.equals(PHASE_SOVIET_COMBAT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 1);
    } else if (phaseName.equals(PHASE_SOVIET_REINFORCEMENT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 1);
    } else if (phaseName.equals(PHASE_SOVIET_MOVEMENT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 5);
    } else if (phaseName.equals(PHASE_GERMAN_TANK_MOVEMENT)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 3);
    } else if (phaseName.equals(PHASE_ADMINISTRATIVE)) {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 6);
    } else {
      commandBaseSetProperty(dataPiece, PROPERTY_PHASE_PARTS, 1);
    }
  }

  protected void executePartPhase(int part) {
    //0 - enter
    //1..x - parts
    //x+1 - leave

    supplyCache.clear();
    commandDisallowCombatActionAll();
    commandDisallowActionAll();
    commandDisallowMovementAll();

    String phaseName = getPhaseName();
    if (phaseName.equals(PHASE_SUPPLY)) {
      switch (part) {
        case 1: executeSupply1(); break;
        case 2: executeSupply2(); break;
      }
    } else if (phaseName.equals(PHASE_AXIS_REINFORCEMENT)) {
      switch (part) {
        case 1: executeAxisReinforcement1(); break;
        case 2: executeAxisReinforcement2(); break;
      }
    } else if (phaseName.equals(PHASE_AXIS_MOVEMENT)) {
      switch (part) {
        case 1: executeAxisMovement1(); break;
        case 2: executeAxisMovement2(); break;
        case 3: executeAxisMovement3(); break;
        case 4: executeAxisMovement4(); break;
        case 5: executeAxisMovement5(); break;
      }
    } else if (phaseName.equals(PHASE_AXIS_COMBAT)) {
      switch (part) {
        case 1: executeAxisCombat1(); break;
        case 2: executeAxisCombat2(); break;
      }
    } else if (phaseName.equals(PHASE_SOVIET_COMBAT)) {
      switch (part) {
        case 1: executeSovietCombat1(); break;
        case 2: executeSovietCombat2(); break;
      }
    } else if (phaseName.equals(PHASE_SOVIET_REINFORCEMENT)) {
      switch (part) {
        case 1: executeSovietReinforcement1(); break;
        case 2: executeSovietReinforcement2(); break;
      }
    } else if (phaseName.equals(PHASE_SOVIET_MOVEMENT)) {
      switch (part) {
        case 1: executeSovietMovement1(); break;
        case 2: executeSovietMovement2(); break;
        case 3: executeSovietMovement3(); break;
        case 4: executeSovietMovement4(); break;
        case 5: executeSovietMovement5(); break;
        case 6: executeSovietMovement6(); break;
      }
    } else if (phaseName.equals(PHASE_GERMAN_TANK_MOVEMENT)) {
      switch (part) {
        case 1: executeGermanTankMovement1(); break;
        case 2: executeGermanTankMovement2(); break;
        case 3: executeGermanTankMovement3(); break;
      }
    } else if (phaseName.equals(PHASE_ADMINISTRATIVE)) {
      switch (part) {
        case 1: executeAdministrative1(); break;
        case 2: executeAdministrative2(); break;
        case 3: executeAdministrative3(); break;
        case 4: executeAdministrative4(); break;
        case 5: executeAdministrative5(); break;
        case 6: executeAdministrative6(); break;
        case 7: executeAdministrative7(); break;
      }
    }
  }

  /**
   * Check supply of airbases. (AUTO)
   * Remove out of supply airbases from the map. (AUTO)
   * If not mud turn, place air fleets on airbases. (AUTO)
   * If there are air fleets on airbases, ask Axis player to move them to desired location in range. (MANUAL)
   * Ask Axis player to voluntarily remove supply depots and/or convert supply depots into emergency supply sources. (MANUAL)
   */
  private void executeSupply1() {
    commandSetPhaseOwner(SIDE_AXIS);
    commandBaseLog("Checking supply of airbases.");
    HashMap<HexRef, TerrainMapShaderCost> inSupply = supplyCache.getSupply(NATIONALITY_GERMAN, false, false);
    HexRef hr;
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Airbase'")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      if ((inSupply.containsKey(hr) == false) || (inSupply.get(hr).getFlag().equals(MODE_NORMAL) == false)) {
        commandSendToReinforcementZone(gp, 4, "Axis Permanently Eliminated Units Box");
      }
    }
    //
    int count;
    if (isMud() == false) {
      count = 0;
      for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Airbase'")) {
        GamePiece fgp = getSinglePiece(offboardMap, "Type=='AirFleet'&&CurrentZone=='Airfleet pool'&&FleetNumber==" + RsrTrait.getFleetNumber(gp));
        if (fgp != null) {
          commandSendToPiece(gp, fgp, false);
          commandAllowMovement(fgp, 0.0d, true);
          count++;
        }
      }
      if (count > 0) {
        commandBaseLog("Axis Player: Move air fleets to desired location in range.");
      }
    }
    count = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Depot'")) {
      commandAllowAction(gp);
      count++;
    }
    if (count > 0) {
      commandBaseLog("Axis Player: Voluntarily remove supply depots and/or convert supply depots into emergency supply sources.");
    }
  }

  /**
   * Check supply of all units (except airbases). (AUTO)
   * Check attrition of all isolated units. (AUTO)
   * Remove supply depots converted into emergency supply sources. (AUTO)
   * Move Soviet Combat Strength Markers received as reinforcements into the proper draw cups. (AUTO)
   */
  private void executeSupply2() {
    //Check supply of all units (except airbases).
    commandBaseLog("Checking supply of all units (except airbases).");
    //clear and recalculate AirFleet support zone for this turn (it will not change during the turn)
    airFleetCache.clear();
    HashSet<HexRef> inAirRange = airFleetCache.getAirFleetSupportZone();
    //
    HashSet<HexRef> inFactory = new HashSet<HexRef>();
    for (GamePiece gp : filter.filterPieces(mainMap, "AssetType=='Factory'")) {
      inFactory.add(mainGrid.getHexPos(gp.getPosition()));
    }
    HexRef hr;
    HashMap<HexRef, TerrainMapShaderCost> infoN, infoA;
    TerrainMapShaderCost costN, costA;
    String side, nationality, type;
    int supplyLevel, isolated;
    ArrayList<GamePiece> isolatedUnits = new ArrayList<GamePiece>();
    for (GamePiece gp : filter.filterPieces(mainMap, "Type!='Airbase'&&Type!='AirFleet'&&Type!='Partisans'&&Type!='Asset'&&Type!='Depot'&&Type!='Marker'")) {
      nationality = RsrTrait.getNationality(gp);
      infoN = supplyCache.getSupply(nationality, false, false);
      infoA = supplyCache.getSupply(nationality, true, false);
      if ((infoN == null) || (infoA == null)) {
        continue;
      }
      supplyLevel = -1;
      isolated = 0;
      side = RsrTrait.getSide(gp);
      type = RsrTrait.getType(gp);
      hr = mainGrid.getHexPos(gp.getPosition());
      costN = infoN.get(hr);
      if (costN != null) {
        if ((costN.getFlag() == null) || (MODE_NORMAL.equals(costN.getFlag()))) {
          supplyLevel = 0;
        } else if (MODE_EMERGENCY.equals(costN.getFlag())) {
          supplyLevel = 1;
        } else if (MODE_SEA.equals(costN.getFlag())) {
          if ((side.equals(SIDE_SOVIET)) && (Filter.isAny(type, "Headquarters", "Leader") == false)) {
            supplyLevel = 0;
          } else if ((nationality.equals(NATIONALITY_GERMAN)) && (Filter.isAny(type, "Airbase") == false)) {
            supplyLevel = 0;
          }
        }
      }
      if (supplyLevel == -1) {
        if ((side.equals(SIDE_SOVIET)) && (inFactory.contains(hr))) {
          supplyLevel = 2;
        } else if ((nationality.equals(NATIONALITY_GERMAN)) && (inAirRange.contains(hr))) {
          supplyLevel = 2;
        } else {
          supplyLevel = 3;
          costA = infoA.get(hr);
          if ((costA == null) && (getTurnNumber() > 1) && (Filter.isAny(type, "Headquarters", "Leader") == false)) {
            isolated = 1;
            isolatedUnits.add(gp);
          }
        }
      }
      if ((supplyLevel == 3) && (Filter.isAny(type, "Headquarters", "Leader"))) {
        commandEliminate(gp, false, false);
      } else {
        commandBaseSetProperty(gp, T_SUPPLY_LEVEL, supplyLevel, T_SUPPLY_ISOLATED, isolated);
      }
    }
    //Check attrition of all isolated units.
    if (isolatedUnits.size() > 0) {
      commandBaseLog("Checking attrition of isolated units.");
      int d6;
      String tt;
      int stepsLost = 0;
      for (GamePiece gp : isolatedUnits) {
        d6 = ran.nextInt(6) + 1;
        hr = mainGrid.getHexPos(gp.getPosition());
        tt = mainTerrain.getHexTerrainName(hr);
        if (isMinorCity(tt)) {
          d6++;
        } else if (isMajorCity(tt)) {
          d6 += 2;
        }
        if (isWinter()) {
          side = RsrTrait.getSide(gp);
          if (side.equals(SIDE_SOVIET)) {
            if (d6 <= 3) {
              stepsLost = 1;
            }
          } else {
            nationality = RsrTrait.getNationality(gp);
            if (nationality.equals(NATIONALITY_FINNISH)) {
              if (d6 <= 2) {
                stepsLost = 1;
              }
            } else {
              if (d6 == 1) {
                stepsLost = 2;
              } else if (d6 <= 4) {
                stepsLost = 1;
              }
            }
          }
        } else if (isMud()) {
          side = RsrTrait.getSide(gp);
          if (side.equals(SIDE_SOVIET)) {
            if (d6 <= 2) {
              stepsLost = 1;
            }
          } else {
            if (d6 <= 3) {
              stepsLost = 1;
            }
          }
        } else {
          if (d6 == 1) {
            stepsLost = 1;
          }
        }
        for (int i = 1; i <= stepsLost; i++) {
          commandStepLoss(gp, false, false);
        }
      }
    }
    //Remove supply depots converted into emergency supply sources.
    ArrayList<GamePiece> emergencyDepots = filter.filterPieces(mainMap, "Type=='Depot'&&SupplyMode==2");
    if (emergencyDepots.size() > 0) {
      commandBaseLog("Removing " + emergencyDepots.size() + " supply depot(s) converted into emergency supply source(s).");
      for (GamePiece gp : emergencyDepots) {
        commandSendToReinforcementZone(gp, 4, "Axis Permanently Eliminated Units Box");
      }
    }
    //
    ArrayList<GamePiece> receivedCombatStrengthMarkers = filter.filterPieces(turnMap, "Type=='Strength'&&LocationName=='S" + getTurnName() + "'");
    if (receivedCombatStrengthMarkers.size() > 0) {
      commandBaseLog("Moving " + receivedCombatStrengthMarkers.size() + " Soviet Combat Strength Marker(s) received as reinforcement(s) into the proper draw cups.");
      for (GamePiece gp : receivedCombatStrengthMarkers) {
        commandSendToOffboardZone(gp, RsrTrait.getStrengthClass(gp) + "-strength pool");
      }
    }
  }

  /**
   * Ask Axis player to put received reinforcements on map. (MANUAL)
   */
  private void executeAxisReinforcement1() {
    ArrayList<GamePiece> reinforcements = filter.filterPieces(turnMap, "LocationName=='A" + getTurnName() + "'");
    if (reinforcements.size() > 0) {
      int count = 0;
      for (GamePiece gp : reinforcements) {
        if (getReinforcementHexes(gp).isEmpty()) {
          commandSendToReinforcementZone(gp, 1, "Axis Permanently Eliminated Units Box");
        } else {
          commandAllowMovement(gp, 0.0d, true);
          commandAllowAction(gp);
          count++;
        }
      }
      if (count > 0) {
        commandBaseLog("Axis Player: Put reinforcements on map.");
      } else {
        autoAdvance = true;
      }
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Clear reinforcement status from new units. (AUTO)
   */
  private void executeAxisReinforcement2() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_PLACED + ">0")) {
      commandBaseSetProperty(gp, T_PLACED, 0);
    }
  }

  /**
   * Sea movement. (MANUAL)
   */
  private void executeAxisMovement1() {
    if (allowSeaMovement(SIDE_AXIS) > 0) {
      commandBaseLog("Axis Player: Move units (sea movement).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Set Axis strategic supply cache. (AUTO)
   * Set strategic movement for all Axis units. (AUTO)
   * Ask Axis player to strategically move units. (MANUAL)
   */
  private void executeAxisMovement2() {
    HexRef hr;
    CombatHexList chln = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Depot'&&SupplyMode==1&&" + T_PLACED + "==0")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chln.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }
    CombatHexList chle = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Depot'&&SupplyMode==2&&" + T_PLACED + "==0")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chle.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }    
    CombatHexList chlf = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Headquarters'&&Nationality=='" + NATIONALITY_FINNISH + "'")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chlf.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }      
    CombatHexList chlr = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Headquarters'&&Nationality=='" + NATIONALITY_ROMANIAN + "'")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chlr.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }      
    commandBaseSetProperty(dataPiece, G_AXIS_NORMAL_STRAT_HEXES, chln.toString(), G_AXIS_EMERGENCY_STRAT_HEXES, chle.toString(), G_FINNISH_STRAT_HEXES, chlf.toString(), G_ROMANIAN_STRAT_HEXES, chlr.toString());    
    int count = 0;
    HashSet<HexRef> blockedByEnemy = pieceCache.getHexesInRangeAnyEnemy(SIDE_AXIS, 4);
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='Axis'&&" + T_MOVED + "==0")) {
      if ((getStrategicMovementAllowance(gp) > 0) && (blockedByEnemy.contains(mainGrid.getHexPos(gp.getPosition())) == false)) {
        commandAllowMovement(gp, 0.0d, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Axis Player: Move units (strategic movement).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset Axis strategic supply cache. (AUTO)
   * Set movement for all movable Axis units (except airbases). (AUTO)
   * Ask Axis player to move units (except airbases). (MANUAL)
   */
  private void executeAxisMovement3() {
    commandBaseSetProperty(dataPiece, G_AXIS_NORMAL_STRAT_HEXES, "", G_AXIS_EMERGENCY_STRAT_HEXES, "", G_FINNISH_STRAT_HEXES, "", G_ROMANIAN_STRAT_HEXES, "");
    int count = 0;
    double mp = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='Axis'&&Movement>0&&Type!='Airbase'&&" + T_MOVED + "==0")) {
      mp = getMovementAllowance(gp);
      if (mp > 0) {
        commandAllowMovement(gp, mp, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Axis Player: Move units (except airbases).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Set movement for all movable airbases. (AUTO)
   * Ask Axis player to move airbases. (MANUAL)
   */
  private void executeAxisMovement4() {
    int count = 0;
    double mp = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Airbase'&&" + T_MOVED + "==0")) {
      mp = getMovementAllowance(gp);
      if (mp > 0) {
        commandAllowMovement(gp, mp, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Axis Player: Move airbases.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset status of moved units. (AUTO)
   * Fix stacks. (AUTO)
   */
  private void executeAxisMovement5() {
    commandMarkAsUnmovedAll();
    commandFixAllStacks();
  }

  /**
   * Allow attack for all Axis units that can. (AUTO)
   * Ask Axis player to attack. (MANUAL)
   */
  private void executeAxisCombat1() {
    int count = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='Axis'&&Type=='Unit'")) {
      if (getAttackHexes(gp).size() > 0) {
        commandAllowMovement(gp, 0.0d, true);
        count++;
      }
    }
    if (count > 0) {
      for (GamePiece gp : filter.filterPieces(mainMap, "Type=='SiegeArtillery'")) {
        commandAllowAction(gp);
      }
      commandBaseLog("Axis Player: Attack with your units.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset status of attacked defenders and/or fired siege artillery. (AUTO)
   */
  private void executeAxisCombat2() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_ATTACKED + ">0||" + T_FIRED + ">0")) {
      commandBaseSetProperty(gp, T_ATTACKED, 0, T_FIRED, 0);
    }
  }

  /**
   * Allow attack for all Soviet units that can. (AUTO)
   * Ask Soviet player to attack. (MANUAL)
   */
  private void executeSovietCombat1() {
    commandSetPhaseOwner(SIDE_SOVIET);
    int count = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='Soviet'&&Type=='Unit'")) {
      if (getAttackHexes(gp).size() > 0) {
        commandAllowMovement(gp, 0.0d, true);
        count++;
      }
    }
    if (count > 0) {
      commandBaseLog("Soviet Player: Attack with your units.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset status of attacked defenders. (AUTO)
   */
  private void executeSovietCombat2() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_ATTACKED + ">0")) {
      commandBaseSetProperty(gp, T_ATTACKED, 0);
    }
  }

  /**
   * Move received Guards and Shock armies to Awaiting Promotion Box. (AUTO)
   * Move received Airborne corps to Paradrop Waiting Box. (AUTO)
   * Ask Soviet player to put received reinforcements on map. (MANUAL)
   */
  private void executeSovietReinforcement1() {
    ArrayList<GamePiece> reinforcements = filter.filterPieces(turnMap, "LocationName=='S" + getTurnName() + "'");
    if (reinforcements.size() > 0) {
      int count = 0;
      String type, unitType;
      for (GamePiece gp : reinforcements) {
        type = RsrTrait.getType(gp);
        if (type.equals("Unit")) {
          unitType = RsrTrait.getUnitType(gp);
          if ((unitType.equals("SovietGuardsArmy")) || (unitType.equals("SovietShockArmy"))) {
            if (RsrGlobal.getScenario() == 1) {
              if (getReinforcementHexes(gp).isEmpty()) {
                commandSendToReinforcementZone(gp, 1, "Soviet Permanently Eliminated Units Box");
              } else {
                commandAllowMovement(gp, 0.0d, true);
                commandAllowAction(gp);
                count++;
              }
            } else {
              commandSendToOffboardZone(gp, "Awaiting Promotion Box");
            }
          } else
          if (unitType.equals("SovietAirborneCorps")) {
            commandSendToOffboardZone(gp, "Paradrop Waiting Box");
          } else {
            if (getReinforcementHexes(gp).isEmpty()) {
              commandSendToReinforcementZone(gp, 1, "Soviet Permanently Eliminated Units Box");
            } else {
              commandAllowMovement(gp, 0.0d, true);
              commandAllowAction(gp);
              count++;
            }
          }
        } else
        if (type.equals("Headquarters")) {
          if (getReinforcementHexes(gp).isEmpty()) {
            commandSendToReinforcementZone(gp, 1, "Soviet Permanently Eliminated Units Box");
          } else {
            commandAllowMovement(gp, 0.0d, true);
            commandAllowAction(gp);
            count++;
          }
        } else
        if (type.equals("Leader")) {
          if (getReinforcementHexes(gp).isEmpty()) {
            commandSendToReinforcementZone(gp, 1, "Soviet Permanently Eliminated Units Box");
          } else {
            commandAllowMovement(gp, 0.0d, true);
            commandAllowAction(gp);
            count++;
          }
        }
      }
      if (count > 0) {
        commandBaseLog("Soviet Player: Put reinforcements on map.");
      } else {
        autoAdvance = true;
      }
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Clear reinforcement status from new units. (AUTO)
   */
  private void executeSovietReinforcement2() {
    for (GamePiece gp : filter.filterPieces(mainMap, T_PLACED + ">0")) {
      commandBaseSetProperty(gp, T_PLACED, 0);
    }
  }

  /**
   * Airborne assault. (MANUAL)
   */
  private void executeSovietMovement1() {
    int count = 0;
    for (GamePiece gp : findInOffboardZone("Paradrop Waiting Box", "Unit", "SovietAirborneCorps", 1)) {
      commandAllowMovement(gp, 0.0d, true);
      count++;
    }
    if (count > 0) {
      commandBaseLog("Soviet Player: Put airborne corps on map.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Sea movement. (MANUAL)
   */
  private void executeSovietMovement2() {
    if (allowSeaMovement(SIDE_SOVIET) > 0) {
      commandBaseLog("Soviet Player: Move units (sea movement).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Set Soviet strategic supply cache. (AUTO)
   * Set strategic movement for all Soviet units. (AUTO)
   * Ask Soviet player to strategically move units. (MANUAL)
   */
  private void executeSovietMovement3() {
    HexRef hr;
    CombatHexList chl = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Headquarters'&&Nationality=='Soviet'")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chl.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }
    commandBaseSetProperty(dataPiece, G_SOVIET_STRAT_HEXES, chl.toString());
    int count = 0;
    HashSet<HexRef> blockedByEnemy = pieceCache.getHexesInRangeAnyEnemy(SIDE_SOVIET, 4);
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='Soviet'&&" + T_MOVED + "==0")) {
      if ((getStrategicMovementAllowance(gp) > 0) && (blockedByEnemy.contains(mainGrid.getHexPos(gp.getPosition())) == false)) {
        commandAllowMovement(gp, 0.0d, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Soviet Player: Move units (strategic movement).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset Soviet strategic supply cache. (AUTO)
   * Set movement for all movable Soviet units (except leaders). (AUTO)
   * Ask Soviet player to move units (except leaders). (MANUAL)
   */
  private void executeSovietMovement4() {
    commandBaseSetProperty(dataPiece, G_SOVIET_STRAT_HEXES, "");
    int count = 0;
    double mp = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Side=='Soviet'&&Movement>0&&Type!='Leader'&&" + T_MOVED + "==0")) {
      mp = getMovementAllowance(gp);
      if (mp > 0) {
        commandAllowMovement(gp, mp, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Soviet Player: Move units (except leaders).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Set movement for all movable Soviet leaders. (AUTO)
   * Ask Soviet player to move leaders. (MANUAL)
   */
  private void executeSovietMovement5() {
    int count = 0;
    double mp = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Leader'&&" + T_MOVED + "==0")) {
      mp = getMovementAllowance(gp);
      if (mp > 0) {
        commandAllowMovement(gp, mp, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Soviet Player: Move leaders.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset status of moved units. (AUTO)
   * Fix stacks. (AUTO)
   */
  private void executeSovietMovement6() {
    commandMarkAsUnmovedAll();
    commandFixAllStacks();
  }

  /**
   * Set movement for all movable German tank units. (AUTO)
   * Ask Axis player to move German tank units. (MANUAL)
   */
  private void executeGermanTankMovement1() {
    commandSetPhaseOwner(SIDE_AXIS);
    int count = 0;
    double mp = 0;
    for (GamePiece gp : filter.filterPieces(mainMap, "UnitType=='AxisGermanTankCorps','AxisGermanTankBattlegroup','AxisGermanTankDivision'")) {
      mp = getMovementAllowance(gp);
      if (mp > 0) {
        commandAllowMovement(gp, mp, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Axis Player: Move German tank units.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Set Axis strategic supply cache. (AUTO)
   * Set strategic movement for all German tank units. (AUTO)
   * Ask Axis player to strategically move German tank units. (MANUAL)
   */
  private void executeGermanTankMovement2() {
    HexRef hr;
    CombatHexList chln = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Depot'&&SupplyMode==1&&" + T_PLACED + "==0")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chln.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }
    CombatHexList chle = new CombatHexList("");
    for (GamePiece gp : filter.filterPieces(mainMap, "Type=='Depot'&&SupplyMode==2&&" + T_PLACED + "==0")) {
      hr = mainGrid.getHexPos(gp.getPosition());
      chle.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
    }    
    commandBaseSetProperty(dataPiece, G_AXIS_NORMAL_STRAT_HEXES, chln.toString(), G_AXIS_EMERGENCY_STRAT_HEXES, chle.toString());
    int count = 0;
    HashSet<HexRef> blockedByEnemy = pieceCache.getHexesInRangeAnyEnemy(SIDE_AXIS, 4);
    for (GamePiece gp : filter.filterPieces(mainMap, "UnitType=='AxisGermanTankCorps','AxisGermanTankBattlegroup','AxisGermanTankDivision'&&" + T_MOVED + "==0")) {
      if ((getStrategicMovementAllowance(gp) > 0) && (blockedByEnemy.contains(mainGrid.getHexPos(gp.getPosition())) == false)) {
        commandAllowMovement(gp, 0.0d, true);
        count++;
      }
    }
    if (count > 0) {
      commandResetMovementIndex();
      commandBaseLog("Axis Player: Move German tank units (strategic movement).");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Reset Axis strategic supply cache. (AUTO)
   * Reset status of moved units. (AUTO)
   * Fix stacks. (AUTO)
   * Remove Air Fleets from map. (AUTO)
   * Remove Airbase on given turn. (MANUAL)
   */
  private void executeGermanTankMovement3() {
    commandBaseSetProperty(dataPiece, G_AXIS_NORMAL_STRAT_HEXES, "", G_AXIS_EMERGENCY_STRAT_HEXES, "");
    //
    commandMarkAsUnmovedAll();
    commandFixAllStacks();
    //
    ArrayList<GamePiece> airfleets = filter.filterPieces(mainMap, "Type=='AirFleet'");
    if (airfleets.size() > 0) {
      commandBaseLog("Removing air fleets from the map.");
      for (GamePiece gp : airfleets) {
        commandSendToOffboardZone(gp, "Airfleet pool");
      }
    }
    //
    int turnNumber = getTurnNumber();
    if ((turnNumber == 11) || (turnNumber == 31)) {
      ArrayList<GamePiece> airbases = filter.filterPieces(mainMap, "Type=='Airbase'");
      airbases.addAll(filter.filterPieces(turnMap, "Type=='Airbase'"));
      int desiredCount = turnNumber == 11 ? 2 : 1;
      if (airbases.size() > desiredCount) {
        commandBaseLog("Axis Player: Remove any one airbase.");
        for (GamePiece gp : airbases) {
          commandAllowCombatAction(gp);
        }
      } else {
        autoAdvance = true;
      }
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Remove the Italian 8th Army from the game on turn 34. (AUTO)
   * Ask Axis player to perform breakdowns and buildups. (MANUAL)
   */
  private void executeAdministrative1() {
    if (getTurnNumber() == 34) {
      for (GamePiece gp : filter.filterPieces(mainMap, "UnitType=='AxisItalianArmy'")) {
        commandEliminate(gp, false, false);
      }
    }
    commandBaseLog("Axis Player: Perform breakdowns and buildups.");
  }

  /**
   * Ask Soviet player to promote infantry armies into Guards and Shock armies. (MANUAL)
   */
  private void executeAdministrative2() {
    commandSetPhaseOwner(SIDE_SOVIET);
    //
    commandMarkAsUnreorganizedAll();
    //
    ArrayList<GamePiece> convertible = filter.filterPieces(mainMap, "UnitType=='SovietInfantryArmy'&&" + T_SUPPLY_LEVEL + "==0");
    HashSet<HexRef> inZOC = isMud() ? new HashSet<HexRef>() : pieceCache.getHexesInZOCEnemy(SIDE_SOVIET);
    Iterator<GamePiece> it = convertible.iterator();
    while (it.hasNext()) {
      if (inZOC.contains(getHexRef(it.next().getPosition()))) {
        it.remove();
      }
    }
    if (convertible.size() > 0) {
      ArrayList<GamePiece> converts = findInOffboardZone("Awaiting Promotion Box", "Unit", null, Integer.MAX_VALUE);
      if (converts.size() > 0) {
        for (GamePiece gp : convertible) {
          commandAllowAction(gp);
        }
        commandBaseLog("Soviet Player: Promote infantry armies into Guards and Shock armies.");
        return;
      }
    }
    autoAdvance = true;
  }

  /**
   * If this is a production turn...
   * ...ask Soviet player to perform breakdowns and buildups. (MANUAL)
   * Ask Soviet player to perform incorporations. (MANUAL)
   */
  private void executeAdministrative3() {
    if (isProductionTurn()) {
      commandBaseLog("Soviet Player: Perform breakdowns, buildups and incorporations.");
    } else {
      commandBaseLog("Soviet Player: Perform incorporations.");
    }
  }

  /**
   * If this is a production turn...
   * ...check to see if strategic port economic assets have a railroad line of communication. (AUTO)
   * ...check to see if a Leader is "produced". (AUTO)
   * ......and ask Soviet player to put "produced" leader on map. (MANUAL)
   */
  private void executeAdministrative4() {
    //
    commandMarkAsUnreorganizedAll();
    //
    if (isProductionTurn()) {
      ArrayList<GamePiece> ports = filter.filterPieces(mainMap, "AssetType=='Strategic Port'");
      if (ports.size() > 0) {
        commandBaseLog("Checking railroad line of communication of strategic port assets.");
        HashSet<HexRef> withLOC = supplyCache.getSovietLine();
        for (GamePiece gp : ports) {
          if (withLOC.contains(getHexRef(gp.getPosition())) == false) {
            commandEliminate(gp, false, false);
          }
        }
      }
      int eliminatedAssets = findInOffboardZone("Eliminated Economic Assets", "Asset", null, Integer.MAX_VALUE).size();
      if (eliminatedAssets < getProductionNumber()) {
        ArrayList<GamePiece> leadersOnMap = filter.filterPieces(mainMap, "Type=='Leader'");
        ArrayList<GamePiece> leadersInPool = findInOffboardZone("Leader pool", "Leader", null, Integer.MAX_VALUE);
        if ((leadersOnMap.size() < 10) && (leadersInPool.size() > 0)) {
          GamePiece gp = leadersInPool.get(ran.nextInt(leadersInPool.size()));
          commandSendToReinforcementZone(gp, 0, "Leader pool");
          commandAllowMovement(gp, 0.0d, true);
          commandBaseLog("Soviet Player: Put leader on map.");
          return;
        }
      }
    }
    autoAdvance = true;
  }

  /**
   * If this is a production turn...
   * ...ask Soviet player to evacuate factory economic assets. (MANUAL)
   */
  private void executeAdministrative5() {
    if (isProductionTurn()) {
      HashSet<HexRef> withLOC = supplyCache.getSovietLine();
      HashSet<HexRef> inZOC = pieceCache.getHexesInZOCEnemy(SIDE_SOVIET);
      int count = 0;
      HexRef hr;
      for (GamePiece gp : filter.filterPieces(mainMap, "AssetType=='Factory'")) {
        hr = getHexRef(gp.getPosition());
        if ((withLOC.contains(hr)) && (inZOC.contains(hr) == false)) {
          commandAllowAction(gp);
          count++;
        }
      }
      if (count > 0) {
        commandBaseLog("Soviet Player: Evacuate factory economic assets.");
        return;
      }
    }
    autoAdvance = true;
  }

  /**
   * Ask Soviet player to put received Partisans units on map. (MANUAL)
   */
  private void executeAdministrative6() {
    //
    commandBaseSetProperty(dataPiece, G_EVACUATED, 0);
    //
    int count = 0;
    for (GamePiece gp : filter.filterPieces(turnMap, "LocationName=='S" + getTurnName() + "'&&Type=='Partisans'")) {
      commandAllowMovement(gp, 0.0d, true);
      count++;
    }
    if (count > 0) {
      commandBaseLog("Soviet Player: Put partisans on map.");
    } else {
      autoAdvance = true;
    }
  }

  /**
   * Draw a combat strength marker for each Soviet army that i) does not already have a combat strength marker, and ii) is next to an Axis unit. (AUTO)
   * Remove a combat strength marker from each in supply or lack of supply Soviet army that is not next to an Axis unit. (AUTO)
   */
  private void executeAdministrative7() {
    HashSet<HexRef> inZOC = pieceCache.getHexesInZOCEnemy(SIDE_SOVIET);
    ArrayList<GamePiece> list1 = filter.filterPieces(mainMap, "UnitType=='SovietInfantryArmy','SovietGuardsArmy','SovietShockArmy','SovietTankArmy'&&" + T_STRENGTH_ID + "==''");
    ArrayList<GamePiece> list2 = filter.filterPieces(mainMap, "UnitType=='SovietInfantryArmy','SovietGuardsArmy','SovietShockArmy','SovietTankArmy'&&" + T_STRENGTH_ID + "!=''");
    for (GamePiece gp : list1) {
      if (inZOC.contains(getHexRef(gp.getPosition()))) {
        commandFetchCounter(gp);
      }
    }
    for (GamePiece gp : list2) {
      if ((inZOC.contains(getHexRef(gp.getPosition())) == false) && (RsrTrait.isSupplyLevel(gp, SUPPLY_NORMAL, SUPPLY_LACK))) {
        commandReleaseCounter(gp);
      }
    }
  }

  /**
   * Leaves on list only those pieces that can move, then if they are on the
   * main map, selects the intersection of their individual movement zones.
   * If the zone is not empty, displays it on the map, otherwise clears the
   * list of pieces to prevent any movement. In debug mode, every piece can
   * be moved anywhere (no check). Used by RsrPieceMover.
   * @param map Map on which the movement is occuring.
   * @param pieces List of pieces that want to be moved together.
   * @see rsr.RsrPieceMover
   */
  public void selectPiecesToMove(Map map, ArrayList<GamePiece> pieces) {
    if (RsrGlobal.getDebug() == 1) {
      return;
    }
    Iterator<GamePiece> it = pieces.iterator();
    while (it.hasNext()) {
      if (isPieceMoveable(map, it.next()) == false) {
        it.remove();
      }
    }
    if (pieces.size() == 0) {
      return;
    }
    Set<HexRef> targets = RsrAllowedMovement.getCommonAllowedHexes(pieces, allowedMovement);
    if (targets.size() > 0) {
      //block movement of too many pieces via sea movement
      String phaseName = getPhaseName();
      int phasePart = RsrGlobal.getPhasePart() - 1;
      if (((phaseName.equals(PHASE_AXIS_MOVEMENT)) && (phasePart == 1)) || ((phaseName.equals(PHASE_SOVIET_MOVEMENT)) && (phasePart == 2))) {
        double totalWeight = 0.0d;
        for (GamePiece gp : pieces) {
          totalWeight += getSeaMovementWeight(gp);
        }
        if (totalWeight <= 1.0d) {
          shader.setShadedHexes(targets);
        } else {
          pieces.clear();
        }
      } else {
        shader.setShadedHexes(targets);
      }
    } else {
      pieces.clear();
    }
  }

  public void clearMovementHighlight() {
    shader.setShadedHexes(null);
  }
  
  /**
   * Checks if a piece can be moved, and calculates it's movement zone if it
   * has not been done yet. The cache is cleared after each logged command.
   * Used by selectPiecesToMove. Pieces that want to be moved at all must first
   * be marked by commandAllowMovement.
   * @param map Map on which the movement is occuring.
   * @param gp Piece that wants to be moved.
   * @return true if the gp can be moved, false otherwise.
   */
  private boolean isPieceMoveable(Map map, GamePiece gp) {
    if (canCommand(gp) == false) {
      return false;
    }
    int combatStage = RsrGlobal.getCombatStage();
    int combatMode = RsrGlobal.getCombatMode();
    //normal move is allowed for blue units when there is no combat, or normal combat has not yet been resolved
    boolean anyMoveBlocked = ((combatStage == 5) && (RsrGlobal.getUrbanLossRequired() > 0)) || ((combatStage == 4) && (RsrGlobal.getMotorizedLossRequired() > 0)) || ((combatStage == 4) && (RsrGlobal.getContestedLossRequired() > 0));
    boolean repositionMoveRequired = RsrGlobal.getRepositioningRequired() > 0;
    boolean normalMoveAllowed = (repositionMoveRequired == false) && (RsrTrait.getHlMovement(gp) > 0) && ((combatStage == 0) || ((combatStage == 1) && (combatMode == 0)));
    boolean repositionMoveAllowed = RsrTrait.getRepositioning(gp) > 0;
    boolean retreatMoveAllowed = (repositionMoveRequired == false) && (combatStage == 4) && ((RsrTrait.getRetreating(gp) > 0) || (RsrTrait.getOverstackRetreating(gp) > 0));
    boolean advanceMoveAllowed = (repositionMoveRequired == false) && (combatStage == 6) && (RsrTrait.getAdvancing(gp) > 0);
    if ((anyMoveBlocked) || ((normalMoveAllowed == false) && (repositionMoveAllowed == false) && (retreatMoveAllowed == false) && (advanceMoveAllowed == false))) {
      return false;
    } else if (allowedMovement.containsKey(gp)) {
      return true;
    } else {
      if (repositionMoveAllowed) {
        if (allowMovement(gp, getPartisanRepositionHexes(gp))) {
          return true;
        }
      } else
      if (retreatMoveAllowed) {
        if (allowMovement(gp, getRetreatHexes(gp))) {
          return true;
        }
      } else
      if (advanceMoveAllowed) {
        if (allowMovement(gp, getAdvanceHexes(gp))) {
          return true;
        }
      } else {
        String phaseName = getPhaseName();
        int phasePart = RsrGlobal.getPhasePart() - 1;
        if (phaseName.equals(PHASE_SUPPLY)) {
          if (phasePart == 1) {
            GamePiece bgp = getSinglePiece(mainMap, "Type=='Airbase'&&FleetNumber==" + RsrTrait.getFleetNumber(gp));
            if ((bgp != null) && (allowMovement(gp, getStartHex(gp), RsrTrait.getRange(bgp)))) {
              return true;
            }
          }
        } else if ((phaseName.equals(PHASE_AXIS_REINFORCEMENT)) || (phaseName.equals(PHASE_SOVIET_REINFORCEMENT)) || ((phaseName.equals(PHASE_ADMINISTRATIVE)) && (phasePart == 4))) {
          if (allowMovement(gp, getReinforcementHexes(gp))) {
            return true;
          }
        } else if ((phaseName.equals(PHASE_SOVIET_MOVEMENT)) && (phasePart == 1)) {
          if (allowMovement(gp, getAirborneAssaultHexes())) {
            return true;
          }
        } else if ((phaseName.equals(PHASE_ADMINISTRATIVE)) && (phasePart == 6)) {
          if (allowMovement(gp, getPartisanEntryHexes())) {
            return true;
          }
        } else if (phaseName.equals(PHASE_AXIS_MOVEMENT)) {
          if (phasePart == 1) {
            if (allowMovement(gp, getSeaMovementHexes(gp))) {
              return true;
            }
          } else if (phasePart == 2) {
            if (allowMovement(gp, rulesStrategic, false)) {
              return true;
            }
          } else if ((phasePart == 3) || (phasePart == 4)) {
            if (allowMovement(gp, rulesMovement, true)) {
              return true;
            }
          }
        } else if (phaseName.equals(PHASE_SOVIET_MOVEMENT)) {
          if (phasePart == 2) {
            if (allowMovement(gp, getSeaMovementHexes(gp))) {
              return true;
            }
          } else if (phasePart == 3) {
            if (allowMovement(gp, rulesStrategic, false)) {
              return true;
            }
          } else if ((phasePart == 4) || (phasePart == 5)) {
            if (allowMovement(gp, rulesMovement, true)) {
              return true;
            }
          }
        } else if (phaseName.equals(PHASE_GERMAN_TANK_MOVEMENT)) {
          if (phasePart == 2) {
            if (allowMovement(gp, rulesStrategic, false)) {
              return true;
            }
          } else if (phasePart == 1) {
            if (allowMovement(gp, rulesMovement, true)) {
              return true;
            }
          }
        } else if ((phaseName.equals(PHASE_AXIS_COMBAT)) || (phaseName.equals(PHASE_SOVIET_COMBAT))) {
          if (allowMovement(gp, getAttackHexes(gp))) {
            return true;
          }
        }
      }
      //commandDisallowMovement(gp);
      return false;
    }
  }

  /**
   * Checks if all pieces can be dropped together at given point on the main
   * map. If yes, then adds commands that are required for movement completion.
   * In debug mode, every piece can be dropped anywhere (no check). Used by
   * RsrPieceMover.
   * @param map Map on which the movement is occuring.
   * @param p Drop point.
   * @param pieces List of pieces that want to be dropped together.
   * @return true if pieces can be dropped, false otherwise.
   */
  public boolean isValidTarget(Map map, Point p, ArrayList<GamePiece> pieces) {
    if (RsrGlobal.getDebug() == 1) {
      return true;
    }
    if (map != mainMap) {
      return false;
    }
    HexRef hr = mainGrid.getHexPos(p);    
    RsrAllowedMovement am;
    for (GamePiece gp : pieces) {
      am = allowedMovement.get(gp);
      if ((am == null) || (am.isAllowed(hr) == false)) {
        return false;
      }
    }
    return testTargetForStack(hr, pieces, false);
  }

  public void movementDone(Map map, Point p, ArrayList<GamePiece> pieces) {
    if (RsrGlobal.getDebug() == 1) {
      return;
    }
    if (map != mainMap) {
      return;
    }
    HexRef hr = mainGrid.getHexPos(p);
    int lossVal = 0;
    String phaseName = getPhaseName();
    int phasePart = RsrGlobal.getPhasePart() - 1;
    int combatStage = RsrGlobal.getCombatStage();
    int combatMode = RsrGlobal.getCombatStage();
    for (GamePiece gp : pieces) {
      allowedMovement.get(gp).movementDone(hr);
      if (moveMode == MOVE_ONCE) {
        if (RsrTrait.getRepositioning(gp) > 0) {
          commandBaseSetProperty(gp, T_REPOSITIONING, 0);
          commandBaseSetProperty(dataPiece, G_REPOSITIONING_REQUIRED, RsrGlobal.getRepositioningRequired()-1);
        } else
        if (combatStage == 4) {
          lossVal = Math.max(lossVal, allowedMovement.get(gp).getCost(hr).getValue());
          if (RsrTrait.getRetreating(gp) > 0) {
            commandBaseSetProperty(gp, T_RETREATING, -1);
          } else {
            commandBaseSetProperty(gp, T_OVERSTACK_RETREATING, -1);
          }
        } else if (combatStage == 6) {
          commandBaseSetProperty(gp, T_ADVANCING, 0);
          if (combatMode == 0) {
            commandDisallowMovement(gp);
          }
        } else if ((phaseName.equals(PHASE_AXIS_REINFORCEMENT)) || (phaseName.equals(PHASE_SOVIET_REINFORCEMENT))) {
          commandDisallowMovement(gp);
          commandDisallowAction(gp);
        } else {
          commandDisallowMovement(gp);
        }
      }
    }
    //apply motorized loss & contested loss
    if (lossVal > 0) {
      int motLossVal = lossVal % 10;
      if (motLossVal > 0) {
        int availableStepsMot = 0;
        commandBaseLog("Due to retreat(s) across major river(s), the stack must lose " + motLossVal + " motorized step(s).");
        for (GamePiece gp : pieces) {
          if (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED) {
            availableStepsMot += RsrTrait.getCurrentNominalSteps(gp);
          }
        }
        if (availableStepsMot <= motLossVal) {
          GamePiece gpi;
          Iterator<GamePiece> it = pieces.iterator();
          while (it.hasNext()) {
            gpi = it.next();
            if (RsrTrait.getMovementTypeAsInt(gpi) == MOVEMENT_MOTORIZED) {
              commandEliminate(gpi, true, false);
              it.remove();
            }
          }
          lossVal -= motLossVal;
        } else {
          commandBaseSetProperty(dataPiece, MOTORIZED_LOSS_REQUIRED, motLossVal);
          for (GamePiece gp : pieces) {
            if (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED) {
              commandAllowCombatAction(gp);
            }
          }
        }
      }
      int conLossVal = lossVal / 10;
      if (conLossVal > 0) {
        int availableStepsCon = 0;
        commandBaseLog("Due to retreat through enemy contested, unoccupied space(s) the stack must lose " + conLossVal + " step(s).");
        for (GamePiece gp : pieces) {
          availableStepsCon += RsrTrait.getCurrentNominalSteps(gp);
        }
        if (availableStepsCon <= conLossVal) {
          Iterator<GamePiece> it = pieces.iterator();
          while (it.hasNext()) {
            commandEliminate(it.next(), true, false);
            it.remove();
          }
          lossVal -= conLossVal * 10;
        } else {
          commandBaseSetProperty(dataPiece, CONTESTED_LOSS_REQUIRED, conLossVal);
          for (GamePiece gp : pieces) {
            commandAllowCombatAction(gp);
          }
        }
      }
    }
    //adjust move index for normal moves
    if (moveMode == MOVE_UNTIL_NEXT) {
      commandSetMovementIndex(pieces);
    }
    //adjust control of spaces traversed, except when moving retreating units
    if (combatStage != 4) {
      commandAdjustControlAndPartisans(mainMap, pieces, p);
    }
    //auto-advance combat if there are no more losses to take and no more retreating units,
    //except when there are secondary retreating units which didn't retreat yet and they are overstacked
    if ((combatStage == 4) && (lossVal == 0) && (filter.filterPieces(mainMap, T_RETREATING + ">0").size() == 0)) {
      boolean resolve = true;
      HashSet<HexRef> ignore = new HashSet<HexRef>();
      HexRef hex;
      for (GamePiece gp : filter.filterPieces(mainMap, T_OVERSTACK_RETREATING + ">0")) {
        hex = getHexRef(gp.getPosition());
        //never check the same hex more than once
        if (ignore.contains(hex) == false) {
          ignore.add(hex);
          if ((isStackSizeOK(hex, RsrTrait.getSide(gp), 0.0d) == false) || (isStackFriendly(hex) == false)) {
            //stack is not OK yet, the combat will not advance automatically
            resolve = false;
          } else {
            //since this stack is now ok, disable secondary retreat from it
            for (GamePiece gp2 : pieceCache.getPiecesIn(hex)) {
              commandBaseSetProperty(gp2, T_OVERSTACK_RETREATING, 0);
            }
          }
        }
      }
      if (resolve) {
        commandResolveCombat(true);
      }
    }
    //auto-advance combat if there are no more losses to take and no more advancing units
    if ((combatStage == 6) && (lossVal == 0) && (filter.filterPieces(mainMap, T_ADVANCING + ">0").size() == 0)) {
      commandResolveCombat(true);
    }
    //perform paradrop elimination test and auto-advance game
    if ((phaseName.equals(PHASE_SOVIET_MOVEMENT)) && (phasePart == 1)) {
      commandParadrop(hr, pieces);
    }
    //auto-advance game on sea movement
    if (((phaseName.equals(PHASE_AXIS_MOVEMENT)) && (phasePart == 1)) || ((phaseName.equals(PHASE_SOVIET_MOVEMENT)) && (phasePart == 2))) {
      for (GamePiece gp : pieces) {
        commandMarkAsMoved(gp);
      }
      commandAdvance(true);
    }    
    //warning about possible overstacks
    updateOverstackHexes(hr);
  }

  /**
   * 
   * @param hr
   * @param pieces
   * @param test
   * @return
   */
  public boolean testTargetForStack(HexRef hr, ArrayList<GamePiece> pieces, boolean test) {
    GamePiece fgp = pieces.get(0);
    RsrAllowedMovement am = allowedMovement.get(fgp);
    TerrainMapShaderCost cost = am.getCost(hr);
    if (cost != null) {
      String flag = cost.getFlag();
      if (flag != null) {
        if (flag.equals(MODE_OVERRUN)) {
          String firstHex = RsrTrait.getStartHex(fgp);
          String firstMovementType = RsrTrait.getMovementType(fgp);
          for (int i=1; i<pieces.size(); i++) {
            if (firstHex.equals(RsrTrait.getStartHex(pieces.get(i))) == false) {
              return false;
            }
            if (firstMovementType.equals(RsrTrait.getMovementType(pieces.get(i))) == false) {
              return false;
            }
          }
          if (test == false) {
            commandAddToCombat(pieces, hr, true);
            return false;
          } else {
            return true;
          }
        } else if (flag.equals(MODE_ATTACK)) {
          if (test == false) {
            commandAddToCombat(pieces, hr, false);
            return false;
          } else {
            return true;
          }
        }
      }
    }
    String phaseName = getPhaseName();
    if (phaseName.equals(PHASE_SUPPLY)) {
      //in this phase only AirFleets are moved, no checks needed
      return true;
    } else {
      //in all other phases axis minor may not mix
      //if (isStackFriendly(hr, pieces)) {
        if ((phaseName.equals(PHASE_AXIS_REINFORCEMENT)) || (phaseName.equals(PHASE_SOVIET_REINFORCEMENT))) {
          //reinforcements must be marked as fresh units to ignore them for supply recalculation
          if (test == false) {
            //supply will have to be recalculated after a unit is placed
            supplyCache.clear();
            for (GamePiece gp : pieces) {
              commandBaseSetProperty(gp, T_PLACED, 1);
            }
          }
          return true;
        } else {
          //during retreats & and all movement overstacks are allowed
          int phasePart = RsrGlobal.getPhasePart() - 1;
          return ((phaseName.equals(PHASE_AXIS_MOVEMENT)) && (phasePart >= 1) && (phasePart <= 4)) || ((phaseName.equals(PHASE_SOVIET_MOVEMENT)) && (phasePart >= 1) && (phasePart <= 5)) || ((phaseName.equals(PHASE_GERMAN_TANK_MOVEMENT)) && (phasePart >= 1) && (phasePart <= 2)) || (RsrGlobal.getCombatStage() == 4) || ((isStackSizeOK(hr, pieces)) && (isStackFriendly(hr, pieces)));
        }
      //} else {
      //  return false;
      //}
    }
  }

  public RsrKeyCommand[] getCommands(RsrImmobilized trait, GamePiece gp) {
    commands.clear();
    if (RsrGlobal.getDebug() == 1) {
      commands.add(new RsrKeyCommand("Delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), gp, trait));
      if (Filter.isAny(RsrTrait.getMarkerType(gp), "Destruction", "Control")) {
        commands.add(new RsrKeyCommand("Toggle", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), gp, trait));
      }
      if ((RsrTrait.getType(gp).equals("Strength")) && (gp.getMap() == mainMap)) {
        commands.add(new RsrKeyCommand("Fetch to army here", KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), gp, trait));
        commands.add(new RsrKeyCommand("Fetch to army here (reduced)", KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), gp, trait));
      }
    }
    return commands.toArray(commandsArray);
  }

  public RsrKeyCommand[] getCommands(RsrTrait trait, GamePiece gp) {
    commands.clear();
    if (RsrGlobal.getDebug() == 1) {
      commands.add(new RsrKeyCommand("Delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), gp, trait));
      if ((RsrTrait.getCurrentStep(gp) == 2) && (gp.getMap() == mainMap)) {
        commands.add(new RsrKeyCommand("Reduce strength", KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), gp, trait));
      }
    }
    if (canCommand(gp) == false) {
      return commands.toArray(commandsArray);
    }
    String phaseName = getPhaseName();
    int phasePart = RsrGlobal.getPhasePart() - 1;
    if (phaseName.equals(PHASE_SUPPLY)) {
      if ((phasePart == 1) && (RsrTrait.getHlAction(gp) > 0)) {
        commands.add(new RsrKeyCommand("Remove Depot", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), gp, trait));
        commands.add(new RsrKeyCommand("Convert Depot", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), gp, trait));
      }
    } else if ((phaseName.equals(PHASE_AXIS_REINFORCEMENT)) || (phaseName.equals(PHASE_SOVIET_REINFORCEMENT))) {
      if ((phasePart == 1) && (RsrTrait.getHlAction(gp) > 0)) {
        commands.add(new RsrKeyCommand("Delay", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), gp, trait));
      }
    } else if (phaseName.equals(PHASE_GERMAN_TANK_MOVEMENT)) {
      if ((phasePart == 3) && (RsrTrait.getHlLoss(gp) > 0)) {
        commands.add(new RsrKeyCommand("Eliminate", KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), gp, trait));
      }
    } else if (phaseName.equals(PHASE_ADMINISTRATIVE)) {
      if (RsrTrait.getHlAction(gp) > 0) {
        switch (phasePart) {
          case 2 :
            commands.add(new RsrKeyCommand("Promote", KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), gp, trait));
            break;
          case 5 :
            commands.add(new RsrKeyCommand("Evacuate", KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), gp, trait));
            break;
        }
      }
    } else if (phaseName.equals(PHASE_AXIS_COMBAT)) {
      if ((RsrTrait.getType(gp).equals("SiegeArtillery")) && (RsrTrait.getHlAction(gp) > 0)) {
        if (RsrTrait.getFired(gp) > 0) {
          commands.add(new RsrKeyCommand("Enable support", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), gp, trait));
        } else {
          commands.add(new RsrKeyCommand("Disable support", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), gp, trait));
        }
      }
    }
    int combatStage = RsrGlobal.getCombatStage();
    if (((combatStage == 2) || (combatStage == 3) || (combatStage == 4)) && (RsrTrait.getHlLoss(gp) > 0)) {
      commands.add(new RsrKeyCommand("Step loss", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), gp, trait));
    }
    return commands.toArray(commandsArray);
  }

  public Command getCommand(RsrImmobilized trait, GamePiece gp, RsrKeyCommand kc) {
    String name = kc.getName();
    if (name.equals("Delete")) {
      commandBaseDelete(gp);
    } else if (name.equals("Toggle")) {
      String property = RsrTrait.getMarkerType(gp);
      int v = RsrCounter.getIntProperty(gp, property, 1, false);
      if (v == 1) {
        v = 2;
      } else {
        v = 1;
      }
      commandBaseSetProperty(gp, property, String.valueOf(v));
    } else if (name.startsWith("Fetch to army here")) {
      HexRef hr = getHexRef(gp.getPosition());
      String unitType;
      for (GamePiece fgp : pieceCache.getPiecesIn(hr)) {
        unitType = RsrTrait.getUnitType(fgp);
        if ((unitType.startsWith("Soviet")) && (unitType.endsWith("Army")) && (RsrTrait.getStrengthClass(gp).equals(RsrTrait.getCombatClass(fgp)))) {
          commandSendToOffboardZone(gp, RsrTrait.getCombatClass(fgp) + "-used strength pool");
          if (name.endsWith(" (reduced)")) {
            commandBaseSetProperty(fgp, T_CURRENT_STEP, 1);
          }
          commandUpdateStrength(fgp, gp);
          break;
        }
      }
    }
    return c;
  }

  public Command getCommand(RsrTrait trait, GamePiece gp, RsrKeyCommand kc) {
    if (canCommand(gp) == false) {
      return c;
    }
    String name = kc.getName();
    if (name.equals("Delete")) {
      commandBaseDelete(gp);
    } else if (name.equals("Reduce strength")) {
      commandBaseSetProperty(gp, T_CURRENT_STEP, 1);
      commandUpdateStrength(gp, findStrength(RsrTrait.getCombatClass(gp), RsrTrait.getStrengthID(gp)));
    } else if (name.equals("Step loss")) {
      int combatStage = RsrGlobal.getCombatStage();
      if ((combatStage == 2) || (combatStage == 3)) {
        commandCombatStepLoss(gp);
        commandStepLoss(gp, true, false);
        if (commandRefreshCombatLossList(false)) {
          commandResolveCombat(true);
        }
      } else if (combatStage == 4) {
        if (RsrGlobal.getMotorizedLossRequired() > 0) {
          if (RsrTrait.getMovementTypeAsInt(gp) == MOVEMENT_MOTORIZED) {
            commandStepLoss(gp, true, false);
            commandBaseSetProperty(dataPiece, MOTORIZED_LOSS_REQUIRED, RsrGlobal.getMotorizedLossRequired() - 1);
          }
        } else if (RsrGlobal.getContestedLossRequired() > 0) {
          commandStepLoss(gp, true, false);
          commandBaseSetProperty(dataPiece, CONTESTED_LOSS_REQUIRED, RsrGlobal.getContestedLossRequired() - 1);
        }
        if ((RsrGlobal.getMotorizedLossRequired() == 0) && (RsrGlobal.getContestedLossRequired() == 0)) {
          commandResolveCombat(true);
        }
      } else {

      }
    } else if (name.equals("Eliminate")) { //airbase elimination
      commandSendToOffboardZone(gp, "Axis Permanently Eliminated Units Box");
      commandAdvance(true);
    } else if (name.equals("Promote")) {
      commandPromote(gp);
    } else if (name.equals("Evacuate")) {
      commandEvacuate(gp);
    } else if (name.equals("Enable support")) {
      commandBaseSetProperty(gp, T_FIRED, 0);
    } else if (name.equals("Disable support")) {
      commandBaseSetProperty(gp, T_FIRED, 1);
    } else if (name.equals("Remove Depot")) {
      commandLogMy("Voluntarily removed supply depot in " + mainGrid.locationName(gp.getPosition()) + ".");
      commandSendToReinforcementZone(gp, 4, "Axis Permanently Eliminated Units Box");
    } else if (name.equals("Convert Depot")) {
      if (RsrTrait.getSupplyMode(gp) == 1) {
        commandBaseSetProperty(gp, PROPERTY_SUPPLY_MODE, 2);
        commandLogMy("Converted supply depot in " + mainGrid.locationName(gp.getPosition()) + " into emergency supply source.");
      } else {
        commandBaseSetProperty(gp, PROPERTY_SUPPLY_MODE, 1);
        commandLogMy("Converted emergency supply source in " + mainGrid.locationName(gp.getPosition()) + " back into supply depot.");
      }
    } else if (name.equals("Delay")) {
      commandLogMy("Voluntarily delayed entry of " + gp.getName() + ".");
      commandSendToReinforcementZone(gp, 1, RsrTrait.getSide(gp).equals(SIDE_AXIS) ? "Axis Permanently Eliminated Units Box" : "Soviet Permanently Eliminated Units Box");
    }
    return c;
  }

  public void setup(boolean gameStarting) {
    if (instance == this) {
      instance = null;
      RsrGlobal.invalidate();
      RsrImmobilized.invalidate();
      RsrTrait.invalidate();
    }
  }
  
  public void updateOverstackHexes(HexRef hr) {
    boolean addThis = (isStackSizeOK(hr) == false) || (isStackFriendly(hr) == false); 
    if (addThis) {
      commandBaseLog("Warning! Stack rules violation in " + getLocationName(hr) + ".");
    }    
    CombatHexList chl = new CombatHexList(RsrGlobal.getOverstackHexes());
    boolean update = addThis;
    HashSet<HexRef> hexes = chl.getHexes();
    Iterator<HexRef> it = hexes.iterator();
    HexRef hr2;
    while (it.hasNext()) {
      hr2 = it.next();
      if ((isStackSizeOK(hr2)) && (isStackFriendly(hr2))) {
        it.remove();
        chl.remove(mainGrid.locationName(mainGrid.getHexCenter(hr2.getColumn(), hr2.getRow())));
        commandBaseLog("Stack rules violation ended in " + getLocationName(hr2) + ".");
        update = true;
      }
    }
    if (addThis) {
      chl.add(mainGrid.locationName(mainGrid.getHexCenter(hr.getColumn(), hr.getRow())));
      hexes.add(hr);
    }
    if (update) {
      commandBaseSetProperty(dataPiece, G_OVERSTACK_HEXES, chl.toString());
    }
    if (hexes.size() > 0) {
      shaderOverstack.setShadedHexes(hexes);
    } else {
      shaderOverstack.setShadedHexes(null);
    }
  }
  
}
