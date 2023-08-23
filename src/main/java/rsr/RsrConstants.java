package rsr;

/**
 *
 * @author morvael
 * @since 2009-05-22
 */
public interface RsrConstants {

  /**
   * Units may be moved as many times as player wants, even again after other
   * units have been moved since.
   */
  public static final int MOVE_FREE = 0;
  /**
   * Units may be moved as many times as player wants, but no more if other
   * units have been moved since.
   */
  public static final int MOVE_UNTIL_NEXT = 1;
  /**
   * Units may be moved only once (first drop counts).
   */
  public static final int MOVE_ONCE = 2;

  public static final String PHASE_SUPPLY = "Supply";
  public static final String PHASE_AXIS_REINFORCEMENT = "Axis Reinforcement";
  public static final String PHASE_AXIS_MOVEMENT = "Axis Movement";
  public static final String PHASE_AXIS_COMBAT = "Axis Combat";
  public static final String PHASE_SOVIET_COMBAT = "Soviet Combat";
  public static final String PHASE_SOVIET_REINFORCEMENT = "Soviet Reinforcement";
  public static final String PHASE_SOVIET_MOVEMENT = "Soviet Movement";
  public static final String PHASE_GERMAN_TANK_MOVEMENT = "German Tank Movement";
  public static final String PHASE_ADMINISTRATIVE = "Administrative";

  public static final String MODE_NORMAL = "MODE_NORMAL";
  public static final String MODE_SEA = "MODE_SEA";
  public static final String MODE_EMERGENCY = "MODE_EMERGENCY";
  public static final String MODE_STRATEGIC = "MODE_STRATEGIC";
  public static final String MODE_MOVEMENT = "MODE_MOVEMENT";
  public static final String MODE_OVERRUN = "MODE_OVERRUN";
  public static final String MODE_ATTACK = "MODE_ATTACK";
  public static final String MODE_RETREAT = "MODE_RETREAT";
  public static final String MODE_ADVANCE = "MODE_ADVANCE";
  public static final String SIDE_AXIS = "Axis";
  public static final String SIDE_SOVIET = "Soviet";
  public static final String NATIONALITY_GERMAN = "German";
  public static final String NATIONALITY_ITALIAN = "Italian";
  public static final String NATIONALITY_HUNGARIAN = "Hungarian";
  public static final String NATIONALITY_FINNISH = "Finnish";
  public static final String NATIONALITY_ROMANIAN = "Romanian";
  public static final String NATIONALITY_SOVIET = "Soviet";
  public static final String HEX_WOODS = "Woods";
  public static final String HEX_SWAMP = "Swamp";
  public static final String HEX_MOUNTAIN = "Mountain";
  public static final String HEX_MINORCITY = "MinorCity";
  public static final String HEX_MAJORCITY = "MajorCity";
  public static final String HEX_WATER = "Water";
  public static final String HEX_SEVASTOPOL = "Sevastopol";
  public static final String EDGE_IMPASSABLE = "Impassable";
  public static final String EDGE_MINORRIVER = "MinorRiver";
  public static final String EDGE_MAJORRIVER = "MajorRiver";
  public static final String EDGE_LAKESEA = "LakeSea";
  public static final String EDGE_IMPASSABLEN = "Impassable N";
  public static final String EDGE_IMPASSABLENLAKESEA = "Impassable N LakeSea";
  public static final String EDGE_IMPASSABLESW = "Impassable SW";
  public static final String EDGE_IMPASSABLESWLAKESEA = "Impassable SW LakeSea";
  public static final String EDGE_IMPASSABLESWMINORRIVER = "Impassable SW MinorRiver";
  public static final String EDGE_IMPASSABLETP = "Impassable TP";
  public static final String EDGE_IMPASSABLETPLAKESEA = "Impassable TP LakeSea";
  public static final String EDGE_IMPASSABLETPMINORRIVER = "Impassable TP MinorRiver";
  public static final String EDGE_IMPASSABLETPMAJORRIVER = "Impassable TP MajorRiver";
  public static final String LINE_RAILROAD = "Railroad";
  public static final String LINE_CHONGARCAUSEWAY = "ChongarCauseway";
  public static final String LINE_FROZENSURFACE = "FrozenSurface";
  public static final String TAG_ARCTIC = "Arctic";
  public static final String TAG_ARCTIC_TRUE = "true";
  public static final String TAG_ARCTIC_FALSE = "false";
  public static final String TAG_CAPITAL = "Capital";
  public static final String TAG_CAPITAL_TRUE = "true";
  public static final String TAG_CAPITAL_FALSE = "false";
  public static final String TAG_COUNTRY = "Country";
  public static final String TAG_COUNTRY_FINLAND = "Finland";
  public static final String TAG_COUNTRY_GERMANY = "Germany";
  public static final String TAG_COUNTRY_ROMANIA = "Romania";
  public static final String TAG_COUNTRY_BULGARIA = "Bulgaria";
  public static final String TAG_COUNTRY_HUNGARY = "Hungary";
  public static final String TAG_COUNTRY_NORWAY = "Norway";
  public static final String TAG_COUNTRY_SOVIETUNION = "SovietUnion";
  public static final String TAG_PORT = "Port";
  public static final String TAG_PORT_NONE = "None";
  public static final String TAG_PORT_ARCTIC = "Arctic";
  public static final String TAG_PORT_BALTIC = "Baltic";
  public static final String TAG_PORT_BLACKSEA = "BlackSea";
  public static final String TAG_SUPPLY = "Supply";
  public static final String TAG_SUPPLY_NONE = "None";
  public static final String TAG_SUPPLY_AXIS = "Axis";
  public static final String TAG_SUPPLY_SOVIET = "Soviet";
  public static final String TAG_CITY = "City";
  public static final String TAG_CITY_NONE = "None";
  public static final String TAG_CITY_MOSCOW = "Moscow";
  public static final String TAG_CITY_BAKU = "Baku";
  public static final String TAG_CITY_HELSINKI = "Helsinki";
  public static final String TAG_CITY_BUCHAREST = "Bucharest";
  public static final String TAG_CITY_SEVASTOPOL = "Sevastopol";
  public static final String TAG_CITY_ODESSA = "Odessa";
  public static final String TAG_CITY_NOVOROSSIISK = "Novorossiisk";
  public static final String TAG_CITY_TUAPSE = "Tuapse";
  public static final String TAG_CITY_MURMANSK = "Murmansk";
  public static final String TAG_CITY_LENINGRAD = "Leningrad";
  public static final String TAG_CITY_STALINGRAD = "Stalingrad";
  public static final String TAG_MAPPART = "MapPart";
  public static final String TAG_MAPPART_NONE = "None";
  public static final String TAG_MAPPART_NORTH = "North";
  public static final String TAG_MAPPART_SOUTHWEST = "Southwest";
  public static final String TAG_MAPPART_TP = "TP";
  public static final String TAG_MAPPART_TPNSW = "TPnSW";
  public static final String TAG_MAPPART_TPNORMAL = "TPNormal";

  public static final String MOVEMENT = "Movement";
  public static final String VP = "VP";
  public static final String PROPERTY_SUPPLY_MODE = "SupplyMode"; //for Supply Depots
  public static final String PROPERTY_MOVEMENT_TYPE = "MovementType";

  public static final String T_STEPS = "Steps";
  public static final String T_CURRENT_STEP = "CurrentStep";
  public static final String T_SUPPLY_LEVEL = "SupplyLevel";
  public static final String T_SUPPLY_ISOLATED = "Isolated";
  public static final String T_STRENGTH_ID = "StrengthID";
  public static final String T_MOVEMENT_LEFT = "MovementLeft";
  public static final String T_START_HEX = "StartHex"; //hex on which piece starts certain phases
  public static final String T_MOVE_INDEX = "MoveIndex"; //move index in which piece was moved (used for 3.2.g restriction)
  public static final String T_HL_LOSS = "HlLoss"; //mandatory action allowed highlight
  public static final String T_HL_ACTION = "HlAction"; //action allowed highlight
  public static final String T_HL_MOVEMENT = "HlMovement"; //movement allowed highlight
  public static final String T_ATTACKER = "CombatAttacker"; //=1 unit attacking in current combat
  public static final String T_DEFENDER = "CombatDefender"; //=1 unit defending in current combat
  public static final String T_COMBAT_HEX = "CombatHex"; //hex name on which the unit was when combat started
  public static final String T_LOST = "CombatLost"; //number of steps this unit lost in combat (to ensure equal distribution)
  public static final String T_RETREATING = "CombatRetreating"; //>0 number of spaces unit must retreat, -1 - unit has just retreated (ignore as help to other units)
  public static final String T_ADVANCING = "CombatAdvancing"; //>0 number of spaces unit can advance
  public static final String T_ATTACKED = "CombatAttacked"; //unit already took part in combat (as defender) in this combat phase (not used in overruns)
  public static final String T_PLACED = "Placed"; //to ignore units that have been just placed as reinforcements (for supply)
  public static final String T_MOVED = "Moved"; //to mark units that have moved this turn and cannot more (for example strat move/normal move limit)
  public static final String T_DISPLAY_STRENGTH = "DisplayStrength"; //should attack and defense factors be displayed on counter
  public static final String T_DISPLAYED_ATTACK = "DisplayedAttack"; //current attack strength
  public static final String T_DISPLAYED_DEFENCE = "DisplayedDefence"; //current defence strength
  public static final String T_REORGANIZED = "Reorganized"; //to mark units that took part in breakdown or buildup or incorporation this turn
  public static final String T_FIRED = "CombatFired"; //siege artillery already took part in combat support in this combat phase (not used in overruns)
  public static final String T_REPOSITIONING = "Repositioning"; //partisans unit that has to be repositioned
  public static final String T_OVERSTACK_RETREATING = "CombatOverstackRetreating"; //number (1) of spaces unit must retreat if primary retreat caused overstack

	public static final int SUPPLY_NORMAL = 0;
	public static final int SUPPLY_EMERGENCY = 1;
	public static final int SUPPLY_LACK = 2;
	public static final int SUPPLY_OUT = 3;

  public static final int MOVEMENT_NONMOTORIZED = 0;
  public static final int MOVEMENT_MOTORIZED = 1;
  public static final int MOVEMENT_RAIL = 2;

  public static final String MOVEMENT_NONMOTORIZED_STRING = "NonMotorized";
  public static final String MOVEMENT_MOTORIZED_STRING = "Motorized";
  public static final String MOVEMENT_RAIL_STRING = "Rail";

  ///////////////////////////////////

  public static final String PROPERTY_DEBUG = "debug";
  public static final String PROPERTY_SCENARIO = "scenario_rules";
  public static final String PROPERTY_PHASE_PARTS = "phase_parts";
  public static final String PROPERTY_PHASE_PART = "phase_part";
  public static final String PROPERTY_LAST_MOVED_INDEX = "last_moved_index";
  public static final String COMBAT_STAGE = "combat_stage";
  public static final String COMBAT_MODE = "combat_mode"; //normal, overrun
  public static final String ATTACKER_HEXES = "attacker_hexes";
  public static final String DEFENDER_HEXES = "defender_hexes";
  public static final String ATTACKER_LOSS = "attacker_loss";
  public static final String ATTACKER_RETREAT = "attacker_retreat";
  public static final String DEFENDER_LOSS = "defender_loss";
  public static final String DEFENDER_RETREAT = "defender_retreat";
  public static final String ATTACKER_LOST = "attacker_lost";
  public static final String DEFENDER_LOST = "defender_lost";
  public static final String URBAN_LOSS_REQUIRED = "urban_loss_required";
  public static final String MOTORIZED_LOSS_REQUIRED = "motorized_loss_required";
  public static final String CONTESTED_LOSS_REQUIRED = "contested_loss_required";
  public static final String G_EVACUATED = "evacuated";
  public static final String G_REPOSITIONING_REQUIRED = "repositioning_required";
  public static final String G_PHASE_OWNER = "phase_owner";
  public static final String G_ATTACKER_SIDE = "attacker_side";
  public static final String G_DEFENDER_SIDE = "defender_side";
  public static final String G_GAME_STATUS = "game_status";
  public static final String G_AXIS_NORMAL_STRAT_HEXES = "axis_normal_strat_hexes";
  public static final String G_AXIS_EMERGENCY_STRAT_HEXES = "axis_emergency_strat_hexes";
  public static final String G_FINNISH_STRAT_HEXES = "finnish_strat_hexes";
  public static final String G_ROMANIAN_STRAT_HEXES = "romanian_strat_hexes";
  public static final String G_SOVIET_STRAT_HEXES = "soviet_strat_hexes";
  public static final String G_BREAKDOWN_MODE = "breakdown_mode";  
  public static final String G_NEW_RULES_2010 = "new_rules_2010";
  public static final String G_OVERSTACK_HEXES = "overstack_hexes";

}
