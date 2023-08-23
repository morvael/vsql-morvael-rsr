package rsr;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceEditor;
import VASSAL.i18n.TranslatablePiece;
import VASSAL.tools.SequenceEncoder;
import java.awt.event.InputEvent;

/**
 *
 * @author morvael
 * @since 2009-05-12
 */
public final class RsrGlobal extends Decorator implements TranslatablePiece, RsrConstants {



  // static
  public static final String ID = "RsrGlobal;";
  // configuration
  private String description = "";
  // counter information
  private int debug = 0;
  private int scenario = 6;
  private int phaseParts = 0;
  private int phasePart = 0;
  private int lastMovedIndex = 0;
  private int combatStage = 0;
  private int combatMode = 0;
  private String attackerHexes = "";
  private String defenderHexes = "";
  private int attackerLoss = 0;
  private int attackerRetreat = 0;
  private int defenderLoss = 0;
  private int defenderRetreat = 0;
  private int attackerLost = 0;
  private int defenderLost = 0;
  private int urbanLossRequired = 0;
  private int motorizedLossRequired = 0;
  private int contestedLossRequired = 0;
  private int evacuated = 0;
  private int repositioningRequired = 0;
  private String phaseOwner = "";
  private String attackerSide = "";
  private String defenderSide = "";
  private int gameStatus = 0;
  private String axisNormalStratHexes = "";
  private String axisEmergencyStratHexes = "";
  private String finnishStratHexes = "";
  private String romanianStratHexes = "";
  private String sovietStratHexes = "";  
  private int breakdownMode = 0;
  private int newRules2010 = 0;
  private String overstackHexes = "";
  // dynamic
  private GamePiece gp;
  private RsrKeyCommand[] commands0;
  private RsrKeyCommand[] commands1;

  private static RsrGlobal instance;
  private static RsrAdvance core;

  public RsrGlobal() {
    this(ID, null);
  }

  public RsrGlobal(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
    instance = this;
  }

  @Override
  public Object getProperty(Object key) {
    if (PROPERTY_DEBUG.equals(key)) {
      return debug;
    } else if (PROPERTY_SCENARIO.equals(key)) {
      return scenario;
    } else if (PROPERTY_PHASE_PARTS.equals(key)) {
      return phaseParts;
    } else if (PROPERTY_PHASE_PART.equals(key)) {
      return phasePart;
    } else if (PROPERTY_LAST_MOVED_INDEX.equals(key)) {
      return lastMovedIndex;
    } else if (COMBAT_STAGE.equals(key)) {
      return combatStage;
    } else if (COMBAT_MODE.equals(key)) {
      return combatMode;
    } else if (ATTACKER_HEXES.equals(key)) {
      return attackerHexes;
    } else if (DEFENDER_HEXES.equals(key)) {
      return defenderHexes;
    } else if (ATTACKER_LOSS.equals(key)) {
      return attackerLoss;
    } else if (ATTACKER_RETREAT.equals(key)) {
      return attackerRetreat;
    } else if (DEFENDER_LOSS.equals(key)) {
      return defenderLoss;
    } else if (DEFENDER_RETREAT.equals(key)) {
      return defenderRetreat;
    } else if (ATTACKER_LOST.equals(key)) {
      return attackerLost;
    } else if (DEFENDER_LOST.equals(key)) {
      return defenderLost;
    } else if (URBAN_LOSS_REQUIRED.equals(key)) {
      return urbanLossRequired;
    } else if (MOTORIZED_LOSS_REQUIRED.equals(key)) {
      return motorizedLossRequired;
    } else if (CONTESTED_LOSS_REQUIRED.equals(key)) {
      return contestedLossRequired;
    } else if (G_EVACUATED.equals(key)) {
      return evacuated;
    } else if (G_REPOSITIONING_REQUIRED.equals(key)) {
      return repositioningRequired;
    } else if (G_PHASE_OWNER.equals(key)) {
      return phaseOwner;
    } else if (G_ATTACKER_SIDE.equals(key)) {
      return attackerSide;
    } else if (G_DEFENDER_SIDE.equals(key)) {
      return defenderSide;
    } else if (G_GAME_STATUS.equals(key)) {
      return gameStatus;
    } else if (G_AXIS_NORMAL_STRAT_HEXES.equals(key)) {
      return axisNormalStratHexes;
    } else if (G_AXIS_EMERGENCY_STRAT_HEXES.equals(key)) {
      return axisEmergencyStratHexes;
    } else if (G_FINNISH_STRAT_HEXES.equals(key)) {
      return finnishStratHexes;
    } else if (G_ROMANIAN_STRAT_HEXES.equals(key)) {
      return romanianStratHexes;
    } else if (G_SOVIET_STRAT_HEXES.equals(key)) {
      return sovietStratHexes;
    } else if (G_BREAKDOWN_MODE.equals(key)) {
      return breakdownMode;
    } else if (G_NEW_RULES_2010.equals(key)) {
      return newRules2010;
    } else if (G_OVERSTACK_HEXES.equals(key)) {
      return overstackHexes;
    } else {
      return super.getProperty(key);
    }
  }

  @Override
  public Object getLocalizedProperty(Object key) {
    if (PROPERTY_DEBUG.equals(key)) {
      return debug;
    } else if (PROPERTY_SCENARIO.equals(key)) {
      return scenario;
    } else if (PROPERTY_PHASE_PARTS.equals(key)) {
      return phaseParts;
    } else if (PROPERTY_PHASE_PART.equals(key)) {
      return phasePart;
    } else if (PROPERTY_LAST_MOVED_INDEX.equals(key)) {
      return lastMovedIndex;
    } else if (COMBAT_STAGE.equals(key)) {
      return combatStage;
    } else if (COMBAT_MODE.equals(key)) {
      return combatMode;
    } else if (ATTACKER_HEXES.equals(key)) {
      return attackerHexes;
    } else if (DEFENDER_HEXES.equals(key)) {
      return defenderHexes;
    } else if (ATTACKER_LOSS.equals(key)) {
      return attackerLoss;
    } else if (ATTACKER_RETREAT.equals(key)) {
      return attackerRetreat;
    } else if (DEFENDER_LOSS.equals(key)) {
      return defenderLoss;
    } else if (DEFENDER_RETREAT.equals(key)) {
      return defenderRetreat;
    } else if (ATTACKER_LOST.equals(key)) {
      return attackerLost;
    } else if (DEFENDER_LOST.equals(key)) {
      return defenderLost;
    } else if (URBAN_LOSS_REQUIRED.equals(key)) {
      return urbanLossRequired;
    } else if (MOTORIZED_LOSS_REQUIRED.equals(key)) {
      return motorizedLossRequired;
    } else if (CONTESTED_LOSS_REQUIRED.equals(key)) {
      return contestedLossRequired;
    } else if (G_EVACUATED.equals(key)) {
      return evacuated;
    } else if (G_REPOSITIONING_REQUIRED.equals(key)) {
      return repositioningRequired;
    } else if (G_PHASE_OWNER.equals(key)) {
      return phaseOwner;
    } else if (G_ATTACKER_SIDE.equals(key)) {
      return attackerSide;
    } else if (G_DEFENDER_SIDE.equals(key)) {
      return defenderSide;
    } else if (G_GAME_STATUS.equals(key)) {
      return gameStatus;
    } else if (G_AXIS_NORMAL_STRAT_HEXES.equals(key)) {
      return axisNormalStratHexes;
    } else if (G_AXIS_EMERGENCY_STRAT_HEXES.equals(key)) {
      return axisEmergencyStratHexes;
    } else if (G_FINNISH_STRAT_HEXES.equals(key)) {
      return finnishStratHexes;
    } else if (G_ROMANIAN_STRAT_HEXES.equals(key)) {
      return romanianStratHexes;
    } else if (G_SOVIET_STRAT_HEXES.equals(key)) {
      return sovietStratHexes;
    } else if (G_BREAKDOWN_MODE.equals(key)) {
      return breakdownMode;
    } else if (G_NEW_RULES_2010.equals(key)) {
      return newRules2010;
    } else if (G_OVERSTACK_HEXES.equals(key)) {
      return overstackHexes;
    } else {
      return super.getLocalizedProperty(key);
    }
  }

  @Override
  public void setProperty(Object key, Object val) {
    if (PROPERTY_DEBUG.equals(key)) {
      try {
        debug = Integer.parseInt(val.toString());
      } catch (Exception e) {
        debug = 0;
      }
    } else if (PROPERTY_SCENARIO.equals(key)) {
      try {
        scenario = Integer.parseInt(val.toString());
      } catch (Exception e) {
        scenario = 6;
      }
      getCore().scenarioChanged(scenario);
    } else if (PROPERTY_PHASE_PARTS.equals(key)) {
      try {
        phaseParts = Integer.parseInt(val.toString());
      } catch (Exception e) {
        phaseParts = 0;
      }
    } else if (PROPERTY_PHASE_PART.equals(key)) {
      try {
        phasePart = Integer.parseInt(val.toString());
      } catch (Exception e) {
        phasePart = 0;
      }
    } else if (PROPERTY_LAST_MOVED_INDEX.equals(key)) {
      try {
        lastMovedIndex = Integer.parseInt(val.toString());
      } catch (Exception e) {
        lastMovedIndex = 0;
      }
    } else if (COMBAT_STAGE.equals(key)) {
      try {
        combatStage = Integer.parseInt(val.toString());
      } catch (Exception e) {
        combatStage = 0;
      }
    } else if (COMBAT_MODE.equals(key)) {
      try {
        combatMode = Integer.parseInt(val.toString());
      } catch (Exception e) {
        combatMode = 0;
      }
    } else if (ATTACKER_HEXES.equals(key)) {
      try {
        attackerHexes = val.toString();
      } catch (Exception e) {
        attackerHexes = "";
      }
    } else if (DEFENDER_HEXES.equals(key)) {
      try {
        defenderHexes = val.toString();
      } catch (Exception e) {
        defenderHexes = "";
      }
    } else if (ATTACKER_LOSS.equals(key)) {
      try {
        attackerLoss = Integer.parseInt(val.toString());
      } catch (Exception e) {
        attackerLoss = 0;
      }
    } else if (ATTACKER_RETREAT.equals(key)) {
      try {
        attackerRetreat = Integer.parseInt(val.toString());
      } catch (Exception e) {
        attackerRetreat = 0;
      }
    } else if (DEFENDER_LOSS.equals(key)) {
      try {
        defenderLoss = Integer.parseInt(val.toString());
      } catch (Exception e) {
        defenderLoss = 0;
      }
    } else if (DEFENDER_RETREAT.equals(key)) {
      try {
        defenderRetreat = Integer.parseInt(val.toString());
      } catch (Exception e) {
        defenderRetreat = 0;
      }
    } else if (ATTACKER_LOST.equals(key)) {
      try {
        attackerLost = Integer.parseInt(val.toString());
      } catch (Exception e) {
        attackerLost = 0;
      }
    } else if (DEFENDER_LOST.equals(key)) {
      try {
        defenderLost = Integer.parseInt(val.toString());
      } catch (Exception e) {
        defenderLost = 0;
      }
    } else if (URBAN_LOSS_REQUIRED.equals(key)) {
      try {
        urbanLossRequired = Integer.parseInt(val.toString());
      } catch (Exception e) {
        urbanLossRequired = 0;
      }
    } else if (MOTORIZED_LOSS_REQUIRED.equals(key)) {
      try {
        motorizedLossRequired = Integer.parseInt(val.toString());
      } catch (Exception e) {
        motorizedLossRequired = 0;
      }
    } else if (CONTESTED_LOSS_REQUIRED.equals(key)) {
      try {
        contestedLossRequired = Integer.parseInt(val.toString());
      } catch (Exception e) {
        contestedLossRequired = 0;
      }
    } else if (G_EVACUATED.equals(key)) {
      try {
        evacuated = Integer.parseInt(val.toString());
      } catch (Exception e) {
        evacuated = 0;
      }
    } else if (G_REPOSITIONING_REQUIRED.equals(key)) {
      try {
        repositioningRequired = Integer.parseInt(val.toString());
      } catch (Exception e) {
        repositioningRequired = 0;
      }
    } else if (G_PHASE_OWNER.equals(key)) {
      try {
        phaseOwner = val.toString();
      } catch (Exception e) {
        phaseOwner = "";
      }
    } else if (G_ATTACKER_SIDE.equals(key)) {
      try {
        attackerSide = val.toString();
      } catch (Exception e) {
        attackerSide = "";
      }
    } else if (G_DEFENDER_SIDE.equals(key)) {
      try {
        defenderSide = val.toString();
      } catch (Exception e) {
        defenderSide = "";
      }
    } else if (G_GAME_STATUS.equals(key)) {
      try {
        gameStatus = Integer.parseInt(val.toString());
      } catch (Exception e) {
        gameStatus = 0;
      }
    } else if (G_AXIS_NORMAL_STRAT_HEXES.equals(key)) {
      try {
        axisNormalStratHexes = val.toString();
      } catch (Exception e) {
        axisNormalStratHexes = "";
      }
    } else if (G_AXIS_EMERGENCY_STRAT_HEXES.equals(key)) {
      try {
        axisEmergencyStratHexes = val.toString();
      } catch (Exception e) {
        axisEmergencyStratHexes = "";
      }
    } else if (G_FINNISH_STRAT_HEXES.equals(key)) {
      try {
        finnishStratHexes = val.toString();
      } catch (Exception e) {
        finnishStratHexes = "";
      }
    } else if (G_ROMANIAN_STRAT_HEXES.equals(key)) {
      try {
        romanianStratHexes = val.toString();
      } catch (Exception e) {
        romanianStratHexes = "";
      }
    } else if (G_SOVIET_STRAT_HEXES.equals(key)) {
      try {
        sovietStratHexes = val.toString();
      } catch (Exception e) {
        sovietStratHexes = "";
      }
    } else if (G_BREAKDOWN_MODE.equals(key)) {
      try {
        breakdownMode = Integer.parseInt(val.toString());
      } catch (Exception e) {
        breakdownMode = 0;
      }
    } else if (G_NEW_RULES_2010.equals(key)) {
      try {
        newRules2010 = Integer.parseInt(val.toString());
      } catch (Exception e) {
        newRules2010 = 0;
      }
    } else if (G_OVERSTACK_HEXES.equals(key)) {
      try {
        overstackHexes = val.toString();
      } catch (Exception e) {
        overstackHexes = "";
      }
    } else {
      super.setProperty(key, val);
    }
  }

  private GamePiece getGamePiece() {
    if (gp == null) {
      gp = Decorator.getOutermost(this);
    }
    return gp;
  }

  public String getDescription() {
    String d = "Rsr Global";
    if (description.length() > 0) {
      d += " - " + description;
    }
    return d;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(description);
    return ID + se.getValue();
  }

  public void mySetType(String type) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    st.nextToken(); // Discard ID
    description = st.nextToken("");
  }

  public String myGetState() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(debug);
    se.append(scenario);
    se.append(phaseParts);
    se.append(phasePart);
    se.append(lastMovedIndex);
    se.append(combatStage);
    se.append(combatMode);
    se.append(attackerHexes);
    se.append(defenderHexes);
    se.append(attackerLoss);
    se.append(attackerRetreat);
    se.append(defenderLoss);
    se.append(defenderRetreat);
    se.append(attackerLost);
    se.append(defenderLost);
    se.append(urbanLossRequired);
    se.append(motorizedLossRequired);
    se.append(contestedLossRequired);
    se.append(evacuated);
    se.append(repositioningRequired);
    se.append(phaseOwner);
    se.append(attackerSide);
    se.append(defenderSide);
    se.append(gameStatus);
    se.append(axisNormalStratHexes);
    se.append(axisEmergencyStratHexes);
    se.append(finnishStratHexes);
    se.append(romanianStratHexes);
    se.append(sovietStratHexes);
    se.append(newRules2010);
    se.append(overstackHexes);
    return se.getValue();
  }

  public void mySetState(String newState) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(newState, ';');
    debug = st.nextInt(0);
    scenario = st.nextInt(6);
    phaseParts = st.nextInt(0);
    phasePart = st.nextInt(0);
    lastMovedIndex = st.nextInt(0);
    combatStage = st.nextInt(0);
    combatMode = st.nextInt(0);
    attackerHexes = st.nextToken("");
    defenderHexes = st.nextToken("");
    attackerLoss = st.nextInt(0);
    attackerRetreat = st.nextInt(0);
    defenderLoss = st.nextInt(0);
    defenderRetreat = st.nextInt(0);
    attackerLost = st.nextInt(0);
    defenderLost = st.nextInt(0);
    urbanLossRequired = st.nextInt(0);
    motorizedLossRequired = st.nextInt(0);
    contestedLossRequired = st.nextInt(0);
    evacuated = st.nextInt(0);
    repositioningRequired = st.nextInt(0);
    phaseOwner = st.nextToken("");
    attackerSide = st.nextToken("");
    defenderSide = st.nextToken("");
    gameStatus = st.nextInt(0);
    axisNormalStratHexes = st.nextToken("");
    axisEmergencyStratHexes = st.nextToken("");
    finnishStratHexes = st.nextToken("");
    romanianStratHexes = st.nextToken("");
    sovietStratHexes = st.nextToken("");
    newRules2010 = st.nextInt(0);
    overstackHexes = st.nextToken("");
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public String getName() {
    return piece.getName();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
  }

  @Override
  protected RsrKeyCommand[] myGetKeyCommands() {
    if ((commands0 == null) || (commands1 == null)) {
      getGamePiece();
      commands0 = new RsrKeyCommand[1];
      commands0[0] = new RsrKeyCommand("Switch to Debug mode", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1 = new RsrKeyCommand[7];
      commands1[0] = new RsrKeyCommand("Switch to Normal mode", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1[1] = new RsrKeyCommand("Use scenario 1", KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1[2] = new RsrKeyCommand("Use scenario 2", KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1[3] = new RsrKeyCommand("Use scenario 3", KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1[4] = new RsrKeyCommand("Use scenario 4", KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1[5] = new RsrKeyCommand("Use scenario 5", KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK), gp, this);
      commands1[6] = new RsrKeyCommand("Use scenario 6", KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK), gp, this);
    }
    return debug == 0 ? commands0 : commands1;
  }

  public Command myKeyEvent(KeyStroke stroke) {
    RsrKeyCommand[] commands = myGetKeyCommands();
    int match = -1;
    for (int i = 0; i < commands.length; i++) {
      if (commands[i].getKeyStroke().equals(stroke)) {
        match = i;
        break;
      }
    }
    if ((match == -1) || ((debug == 1) && (match == scenario))) {
      return null;
    } else {
      ChangeTracker tracker = new ChangeTracker(gp);
      if (debug == 0) {
        debug = 1;
      } else {
        switch (match) {
          case 0 : debug = 0; break;
          case 1 : scenario = 1; break;
          case 2 : scenario = 2; break;
          case 3 : scenario = 3; break;
          case 4 : scenario = 4; break;
          case 5 : scenario = 5; break;
          case 6 : scenario = 6; break;
        }
      }
      getCore().scenarioChanged(scenario);
      Command c = tracker.getChangeCommand();
      Command cmd = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "Debug " + debug + ", scenario " + scenario);
      cmd.execute();
      c.append(cmd);
      return c;
    }
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("RsrGlobal.htm");
  }

  @Override
  public PieceEditor getEditor() {
    return new TraitEditor(this);
  }

  protected static class TraitEditor implements PieceEditor {

    protected JPanel panel;
    protected BooleanConfigurer newRules2010Configurer;
    protected StringConfigurer descriptionConfigurer;

    protected TraitEditor(RsrGlobal trait) {
      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(new JLabel("By morvael", JLabel.CENTER));
      panel.add(new JSeparator());
      panel.add(new JLabel(" "));
      
      newRules2010Configurer = new BooleanConfigurer(null, "New rules 2010:  ", trait.newRules2010 == 1);
      panel.add(newRules2010Configurer.getControls());

      descriptionConfigurer = new StringConfigurer(null, "Description:  ", trait.description);
      panel.add(descriptionConfigurer.getControls());

    }

    public Component getControls() {
      return panel;
    }

    public String getState() {
      return "0;6;0;0;0;0;0;;;0;0;0;0;0;0;0;0;0;0;0;;;;0;;;;;;" + (newRules2010Configurer.booleanValue() ? 1 : 0) + ";";
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(descriptionConfigurer.getValueString());
      return RsrGlobal.ID + se.getValue();
    }
  }

  private static final RsrAdvance getCore() {
    if (core == null) {
      core = RsrAdvance.getInstance();
    }
    return core;
  }

  public static void invalidate() {
    core = null;
    instance = null;
  }

  public static final int getDebug() {
    return instance.debug;
  }

  public static final int getScenario() {
    return instance.scenario;
  }

  public static final int getPhaseParts() {
    return instance.phaseParts;
  }

  public static final int getPhasePart() {
    return instance.phasePart;
  }

  public static final int getLastMovedIndex() {
    return instance.lastMovedIndex;
  }

  public static final int getCombatStage() {
    return instance.combatStage;
  }

  public static final int getCombatMode() {
    return instance.combatMode;
  }

  public static final String getAttackerHexes() {
    return instance.attackerHexes;
  }

  public static final String getDefenderHexes() {
    return instance.defenderHexes;
  }

  public static final int getAttackerLoss() {
    return instance.attackerLoss;
  }

  public static final int getAttackerRetreat() {
    return instance.attackerRetreat;
  }

  public static final int getDefenderLoss() {
    return instance.defenderLoss;
  }

  public static final int getDefenderRetreat() {
    return instance.defenderRetreat;
  }

  public static final int getAttackerLost() {
    return instance.attackerLost;
  }

  public static final int getDefenderLost() {
    return instance.defenderLost;
  }

  public static final int getUrbanLossRequired() {
    return instance.urbanLossRequired;
  }

  public static final int getMotorizedLossRequired() {
    return instance.motorizedLossRequired;
  }

  public static final int getContestedLossRequired() {
    return instance.contestedLossRequired;
  }

  public static final int getEvacuated() {
    return instance.evacuated;
  }

  public static final int getRepositioningRequired() {
    return instance.repositioningRequired;
  }

  public static final String getPhaseOwner() {
    return instance.phaseOwner;
  }

  public static final String getAttackerSide() {
    return instance.attackerSide;
  }

  public static final String getDefenderSide() {
    return instance.defenderSide;
  }

  public static final int getGameStatus() {
    return instance.gameStatus;
  }

  public static final String getAxisNormalStratHexes() {
    return instance.axisNormalStratHexes;
  }

  public static final String getAxisEmergencyStratHexes() {
    return instance.axisEmergencyStratHexes;
  }

  public static final String getFinnishStratHexes() {
    return instance.finnishStratHexes;
  }

  public static final String getRomanianStratHexes() {
    return instance.romanianStratHexes;
  }

  public static final String getSovietStratHexes() {
    return instance.sovietStratHexes;
  }
  
  public static final int getBreakdownMode() {
    return instance.breakdownMode;
  }
  
  public static final boolean isNewRules2010() {
    return instance.newRules2010 == 1;
  }

  public static final String getOverstackHexes() {
    return instance.overstackHexes;
  }
    
}
