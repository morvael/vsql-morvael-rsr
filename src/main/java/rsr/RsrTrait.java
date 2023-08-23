package rsr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.IntConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceEditor;
import VASSAL.i18n.TranslatablePiece;
import VASSAL.tools.SequenceEncoder;
import VASSAL.tools.imageop.ScaledImagePainter;
import java.util.HashMap;

/**
 *
 * @author morvael
 * @since 2009-05-11
 */
public final class RsrTrait extends Decorator implements TranslatablePiece, RsrConstants {

  // trait id
  public static final String ID = "RsrTrait;";
  // configurable trait information
  private String description = "";
  // configurable counter information (persistent)
  private int steps = 1;
  private int currentStep = 1;
  // run-time counter information (persistent)
  private int supplyLevel = 0;
  private int supplyIsolated = 0;
  private String strengthID = "";
  private double movementLeft = 0.0d;
  private String startHex = "";
  private int moveIndex = 0;
  private int hlLoss = 0;
  private int hlAction = 0;
  private int hlMovement = 0;
  private int attacker = 0;
  private int defender = 0;
  private String combatHex = "";
  private int lost = 0;
  private int retreating = 0;
  private int advancing = 0;
  private int attacked = 0;
  private int placed = 0;
  private int moved = 0;
  private boolean displayStrength = false;
  private int displayedAttack = 0;
  private int displayedDefense = 0;
  private int reorganized = 0;
  private int fired = 0;
  private int repositioning = 0;
  private int overstackRetreating = 0;
  // dynamic counter information (volatile)
  private GamePiece gp;
  private RsrKeyCommand[] commands;

  //common to all traits
  private static RsrAdvance core;
  private static ScaledImagePainter imagePainter[];

  static {
    imagePainter = new ScaledImagePainter[20];
    imagePainter[0] = new ScaledImagePainter();
    imagePainter[0].setImageName("hl_loss.png");
    imagePainter[1] = new ScaledImagePainter();
    imagePainter[1].setImageName("hl_action.png");
    imagePainter[2] = new ScaledImagePainter();
    imagePainter[2].setImageName("hl_movement.png");
    imagePainter[3] = new ScaledImagePainter();
    imagePainter[3].setImageName("hl_engaged.png");
    imagePainter[4] = new ScaledImagePainter();
    imagePainter[4].setImageName("supply_1.png");
    imagePainter[5] = new ScaledImagePainter();
    imagePainter[5].setImageName("supply_2.png");
    imagePainter[6] = new ScaledImagePainter();
    imagePainter[6].setImageName("supply_3.png");
    imagePainter[7] = new ScaledImagePainter();
    imagePainter[7].setImageName("hl_retreat.png");
    imagePainter[8] = new ScaledImagePainter();
    imagePainter[8].setImageName("hl_advance.png");
    imagePainter[9] = new ScaledImagePainter();
    imagePainter[9].setImageName("hl_passive.png");
    imagePainter[10] = new ScaledImagePainter();
    imagePainter[10].setImageName("v0.png");
    imagePainter[11] = new ScaledImagePainter();
    imagePainter[11].setImageName("v1.png");
    imagePainter[12] = new ScaledImagePainter();
    imagePainter[12].setImageName("v2.png");
    imagePainter[13] = new ScaledImagePainter();
    imagePainter[13].setImageName("v3.png");
    imagePainter[14] = new ScaledImagePainter();
    imagePainter[14].setImageName("v4.png");
    imagePainter[15] = new ScaledImagePainter();
    imagePainter[15].setImageName("v5.png");
    imagePainter[16] = new ScaledImagePainter();
    imagePainter[16].setImageName("v6.png");
    imagePainter[17] = new ScaledImagePainter();
    imagePainter[17].setImageName("v7.png");
    imagePainter[18] = new ScaledImagePainter();
    imagePainter[18].setImageName("v8.png");
    imagePainter[19] = new ScaledImagePainter();
    imagePainter[19].setImageName("v9.png");
  }

  private static RsrAdvance getCore() {
    if (core == null) {
      core = RsrAdvance.getInstance();
    }
    return core;
  }

  public static void invalidate() {
    core = null;
  }

  public RsrTrait() {
    this(ID, null);
  }

  public RsrTrait(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  @Override
  public Object getProperty(Object key) {
    if (T_STEPS.equals(key)) {
      return steps;
    } else if (T_CURRENT_STEP.equals(key)) {
      return String.valueOf(currentStep);
    } else if (T_SUPPLY_LEVEL.equals(key)) {
      return supplyLevel;
    } else if (T_SUPPLY_ISOLATED.equals(key)) {
      return supplyIsolated;
    } else if (T_STRENGTH_ID.equals(key)) {
      return strengthID;
    } else if (T_MOVEMENT_LEFT.equals(key)) {
      return movementLeft;
    } else if (T_START_HEX.equals(key)) {
      return startHex;
    } else if (T_MOVE_INDEX.equals(key)) {
      return moveIndex;
    } else if (T_HL_LOSS.equals(key)) {
      return hlLoss;
    } else if (T_HL_ACTION.equals(key)) {
      return hlAction;
    } else if (T_HL_MOVEMENT.equals(key)) {
      return hlMovement;
    } else if (T_ATTACKER.equals(key)) {
      return attacker;
    } else if (T_DEFENDER.equals(key)) {
      return defender;
    } else if (T_COMBAT_HEX.equals(key)) {
      return combatHex;
    } else if (T_LOST.equals(key)) {
      return lost;
    } else if (T_RETREATING.equals(key)) {
      return retreating;
    } else if (T_ADVANCING.equals(key)) {
      return advancing;
    } else if (T_ATTACKED.equals(key)) {
      return attacked;
    } else if (T_PLACED.equals(key)) {
      return placed;
    } else if (T_MOVED.equals(key)) {
      return moved;
    } else if (T_DISPLAY_STRENGTH.equals(key)) {
      return displayStrength;
    } else if (T_DISPLAYED_ATTACK.equals(key)) {
      return displayedAttack;
    } else if (T_DISPLAYED_DEFENCE.equals(key)) {
      return displayedDefense;
    } else if (T_REORGANIZED.equals(key)) {
      return reorganized;
    } else if (T_FIRED.equals(key)) {
      return fired;
    } else if (T_REPOSITIONING.equals(key)) {
      return repositioning;
    } else if (T_OVERSTACK_RETREATING.equals(key)) {
      return overstackRetreating;
    } else {
      return super.getProperty(key);
    }
  }

  @Override
  public Object getLocalizedProperty(Object key) {
    if (T_STEPS.equals(key)) {
      return steps;
    } else if (T_CURRENT_STEP.equals(key)) {
      return String.valueOf(currentStep);
    } else if (T_SUPPLY_LEVEL.equals(key)) {
      return supplyLevel;
    } else if (T_SUPPLY_ISOLATED.equals(key)) {
      return supplyIsolated;
    } else if (T_STRENGTH_ID.equals(key)) {
      return strengthID;
    } else if (T_MOVEMENT_LEFT.equals(key)) {
      return movementLeft;
    } else if (T_START_HEX.equals(key)) {
      return startHex;
    } else if (T_MOVE_INDEX.equals(key)) {
      return moveIndex;
    } else if (T_HL_LOSS.equals(key)) {
      return String.valueOf(hlLoss);
    } else if (T_HL_ACTION.equals(key)) {
      return String.valueOf(hlAction);
    } else if (T_HL_MOVEMENT.equals(key)) {
      return String.valueOf(hlMovement);
    } else if (T_ATTACKER.equals(key)) {
      return attacker;
    } else if (T_DEFENDER.equals(key)) {
      return defender;
    } else if (T_COMBAT_HEX.equals(key)) {
      return combatHex;
    } else if (T_LOST.equals(key)) {
      return lost;
    } else if (T_RETREATING.equals(key)) {
      return retreating;
    } else if (T_ADVANCING.equals(key)) {
      return advancing;
    } else if (T_ATTACKED.equals(key)) {
      return attacked;
    } else if (T_PLACED.equals(key)) {
      return placed;
    } else if (T_MOVED.equals(key)) {
      return moved;
    } else if (T_DISPLAY_STRENGTH.equals(key)) {
      return displayStrength;
    } else if (T_DISPLAYED_ATTACK.equals(key)) {
      return displayedAttack;
    } else if (T_DISPLAYED_DEFENCE.equals(key)) {
      return displayedDefense;
    } else if (T_REORGANIZED.equals(key)) {
      return reorganized;
    } else if (T_FIRED.equals(key)) {
      return fired;
    } else if (T_REPOSITIONING.equals(key)) {
      return repositioning;
    } else if (T_OVERSTACK_RETREATING.equals(key)) {
      return overstackRetreating;
    } else {
      return super.getLocalizedProperty(key);
    }
  }

  @Override
  public void setProperty(Object key, Object val) {
    if (T_CURRENT_STEP.equals(key)) {
      try {
        currentStep = Integer.parseInt(val.toString());
      } catch (Exception e) {
        currentStep = 0;
      }
      updateStrength();
    } else if (T_SUPPLY_LEVEL.equals(key)) {
      try {
        supplyLevel = Integer.parseInt(val.toString());
      } catch (Exception e) {
        supplyLevel = 0;
      }
      updateStrength();
    } else if (T_SUPPLY_ISOLATED.equals(key)) {
      try {
        supplyIsolated = Integer.parseInt(val.toString());
      } catch (Exception e) {
        supplyIsolated = 0;
      }
    } else if (T_STRENGTH_ID.equals(key)) {
      try {
        strengthID = val.toString();
      } catch (Exception e) {
        strengthID = "";
      }
      updateStrength();
    } else if (T_MOVEMENT_LEFT.equals(key)) {
      try {
        movementLeft = Double.parseDouble(val.toString());
      } catch (Exception e) {
        movementLeft = 0.0d;
      }
    } else if (T_START_HEX.equals(key)) {
      try {
        startHex = val.toString();
      } catch (Exception e) {
        startHex = "";
      }
    } else if (T_MOVE_INDEX.equals(key)) {
      try {
        moveIndex = Integer.parseInt(val.toString());
      } catch (Exception e) {
        moveIndex = 0;
      }
    } else if (T_HL_LOSS.equals(key)) {
      try {
        hlLoss = Integer.parseInt(val.toString());
      } catch (Exception e) {
        hlLoss = 0;
      }
    } else if (T_HL_ACTION.equals(key)) {
      try {
        hlAction = Integer.parseInt(val.toString());
      } catch (Exception e) {
        hlAction = 0;
      }
    } else if (T_HL_MOVEMENT.equals(key)) {
      try {
        hlMovement = Integer.parseInt(val.toString());
      } catch (Exception e) {
        hlMovement = 0;
      }
    } else if (T_ATTACKER.equals(key)) {
      try {
        attacker = Integer.parseInt(val.toString());
      } catch (Exception e) {
        attacker = 0;
      }
    } else if (T_DEFENDER.equals(key)) {
      try {
        defender = Integer.parseInt(val.toString());
      } catch (Exception e) {
        defender = 0;
      }
    } else if (T_COMBAT_HEX.equals(key)) {
      try {
        combatHex = val.toString();
      } catch (Exception e) {
        combatHex = "";
      }
    } else if (T_LOST.equals(key)) {
      try {
        lost = Integer.parseInt(val.toString());
      } catch (Exception e) {
        lost = 0;
      }
    } else if (T_RETREATING.equals(key)) {
      try {
        retreating = Integer.parseInt(val.toString());
      } catch (Exception e) {
        retreating = 0;
      }
    } else if (T_ADVANCING.equals(key)) {
      try {
        advancing = Integer.parseInt(val.toString());
      } catch (Exception e) {
        advancing = 0;
      }
    } else if (T_ATTACKED.equals(key)) {
      try {
        attacked = Integer.parseInt(val.toString());
      } catch (Exception e) {
        attacked = 0;
      }
    } else if (T_PLACED.equals(key)) {
      try {
        placed = Integer.parseInt(val.toString());
      } catch (Exception e) {
        placed = 0;
      }
    } else if (T_MOVED.equals(key)) {
      try {
        moved = Integer.parseInt(val.toString());
      } catch (Exception e) {
        moved = 0;
      }
    } else if (T_DISPLAY_STRENGTH.equals(key)) {
      try {
        displayStrength = Boolean.parseBoolean(val.toString());
      } catch (Exception e) {
        displayStrength = false;
      }
    } else if (T_DISPLAYED_ATTACK.equals(key)) {
      try {
        displayedAttack = Integer.parseInt(val.toString());
      } catch (Exception e) {
        displayedAttack = 0;
      }
    } else if (T_DISPLAYED_DEFENCE.equals(key)) {
      try {
        displayedDefense = Integer.parseInt(val.toString());
      } catch (Exception e) {
        displayedDefense = 0;
      }
    } else if (T_REORGANIZED.equals(key)) {
      try {
        reorganized = Integer.parseInt(val.toString());
      } catch (Exception e) {
        reorganized = 0;
      }
    } else if (T_FIRED.equals(key)) {
      try {
        fired = Integer.parseInt(val.toString());
      } catch (Exception e) {
        fired = 0;
      }
    } else if (T_REPOSITIONING.equals(key)) {
      try {
        repositioning = Integer.parseInt(val.toString());
      } catch (Exception e) {
        repositioning = 0;
      }
    } else if (T_OVERSTACK_RETREATING.equals(key)) {
      try {
        overstackRetreating = Integer.parseInt(val.toString());
      } catch (Exception e) {
        overstackRetreating = 0;
      }
    } else {
      super.setProperty(key, val);
    }
  }

  private void updateStrength() {
    getCore();
    getGamePiece();
    int nominalAttack = 0;
    int nominalDefense = 0;
    if (getCombatClass(gp).length() == 0) {
      if (getCurrentStep(gp) == 2) {
        nominalAttack = getAttack2(gp);
        nominalDefense = getDefend2(gp);
      } else {
        nominalAttack = getAttack1(gp);
        nominalDefense = getDefend1(gp);
      }
    }
    displayStrength = (nominalAttack > 0) || (nominalDefense > 0) || (strengthID.length() > 0);
    if (displayStrength) {
      displayedAttack = core.getAttackStrength(gp, true);
      displayedDefense = core.getDefendStrength(gp, true);
      displayStrength = (displayedAttack != nominalAttack) || (displayedDefense != nominalDefense);
    }
  }

  private GamePiece getGamePiece() {
    if (gp == null) {
      gp = Decorator.getOutermost(this);
    }
    return gp;
  }

  public String getDescription() {
    String d = "Rsr Trait";
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
    se.append(steps);
    se.append(currentStep);
    se.append(supplyLevel);
    se.append(supplyIsolated);
    se.append(strengthID == null ? "" : strengthID);
    se.append(movementLeft);
    se.append(startHex == null ? "" : startHex);
    se.append(moveIndex);
    se.append(hlLoss);
    se.append(hlAction);
    se.append(hlMovement);
    se.append(attacker);
    se.append(defender);
    se.append(combatHex);
    se.append(lost);
    se.append(retreating);
    se.append(advancing);
    se.append(attacked);
    se.append(placed);
    se.append(moved);
    se.append(displayStrength);
    se.append(displayedAttack);
    se.append(displayedDefense);
    se.append(reorganized);
    se.append(fired);
    se.append(repositioning);
    se.append(overstackRetreating);
    return se.getValue();
  }

  public void mySetState(String newState) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(newState, ';');
    steps = st.nextInt(0);
    currentStep = st.nextInt(0);
    supplyLevel = st.nextInt(0);
    supplyIsolated = st.nextInt(0);
    strengthID = st.nextToken("");
    movementLeft = st.nextDouble(0.0d);
    startHex = st.nextToken("");
    moveIndex = st.nextInt(0);
    hlLoss = st.nextInt(0);
    hlAction = st.nextInt(0);
    hlMovement = st.nextInt(0);
    attacker = st.nextInt(0);
    defender = st.nextInt(0);
    combatHex = st.nextToken("");
    lost = st.nextInt(0);
    retreating = st.nextInt(0);
    advancing = st.nextInt(0);
    attacked = st.nextInt(0);
    placed = st.nextInt(0);
    moved = st.nextInt(0);
    displayStrength = st.nextBoolean(false);
    displayedAttack = st.nextInt(0);
    displayedDefense = st.nextInt(0);
    reorganized = st.nextInt(0);
    fired = st.nextInt(0);
    repositioning = st.nextInt(0);
    overstackRetreating = st.nextInt(0);
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

  private static final int[] DRAW_SUPPLY_X = new int[]{53, 26};
  private static final int[] DRAW_SUPPLY_Y = new int[]{4, 0};

  private static final int[] DRAW_STRENGTH_X_1 = new int[]{4, 0};
  private static final int[] DRAW_STRENGTH_X_2 = new int[]{2, 1};
  private static final int[] DRAW_STRENGTH_Y = new int[]{50, 23};

  private static final int[] DRAW_MOVEMENT_X = new int[]{53, 26};
  private static final int[] DRAW_MOVEMENT_Y = new int[]{50, 23};
  
  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
    int mod = (((int)(zoom*63))/2);
    int px = x - mod;
    int py = y - mod;
    if ((zoom == 1.0d) || (zoom == 0.5d)) {
      int zoomindex = zoom == 1.0d ? 0 : 1;
      if (supplyLevel > 0) {
        imagePainter[3 + supplyLevel].draw(g, px + DRAW_SUPPLY_X[zoomindex], py + DRAW_SUPPLY_Y[zoomindex], 1.0d, obs);
      }
      if (displayStrength) {
        int w = drawNumber(g, px + DRAW_STRENGTH_X_1[zoomindex], py + DRAW_STRENGTH_Y[zoomindex], 1.0d, obs, displayedAttack);
        drawNumber(g, px + DRAW_STRENGTH_X_1[zoomindex] + w + DRAW_STRENGTH_X_2[zoomindex], py + DRAW_STRENGTH_Y[zoomindex], 1.0d, obs, displayedDefense);
      }
      if (movementLeft >= 1.0d) {
        drawNumber(g, px + DRAW_MOVEMENT_X[zoomindex], py + DRAW_MOVEMENT_Y[zoomindex], 1.0d, obs, (int)movementLeft);
      }
    }
    if ((retreating > 0) || (repositioning > 0) || (overstackRetreating > 0)) {
      imagePainter[7].draw(g, px, py, zoom, obs);
    } else
    if (advancing > 0) {
      imagePainter[8].draw(g, px, py, zoom, obs);
    } else
    if ((attacked > 0) || (placed > 0) || (moved > 0) || (reorganized > 0) || (fired > 0)) {
      imagePainter[9].draw(g, px, py, zoom, obs);
    } else
    if ((attacker > 0) || (defender > 0)) {
      imagePainter[3].draw(g, px, py, zoom, obs);
    }
    if (hlAction > 0) {
      imagePainter[1].draw(g, px, py, zoom, obs);
    }
    if (hlMovement > 0) {
      imagePainter[2].draw(g, px, py, zoom, obs);
    }
    if (hlLoss > 0) {
      imagePainter[0].draw(g, px, py, zoom, obs);
    }
  }

  public static int drawNumber(Graphics g, int px, int py, double zoom, Component obs, int v) {
    int d1 = v / 10;
    int d2 = v % 10;
    if (d1 > 0) {
      imagePainter[10 + d1].draw(g, px, py, zoom, obs);
      imagePainter[10 + d2].draw(g, px + 6, py, zoom, obs);
      return 12;
    } else {
      imagePainter[10 + d2].draw(g, px, py, zoom, obs);
      return 6;
    }
  }

  @Override
  protected RsrKeyCommand[] myGetKeyCommands() {
    commands = getCore().getCommands(this, getGamePiece());
    return commands;
  }

  // No hot-keys
  public Command myKeyEvent(KeyStroke stroke) {
    myGetKeyCommands();
    int match = -1;
    for (int i = 0; i < commands.length; i++) {
      if (commands[i].getKeyStroke().equals(stroke)) {
        match = i;
        break;
      }
    }
    if (match == -1) {
      return null;
    } else {
      return getCore().getCommand(this, gp, commands[match]);
    }
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("RsrTrait.htm");
  }

  @Override
  public PieceEditor getEditor() {
    return new TraitEditor(this);
  }

  protected static class TraitEditor implements PieceEditor {

    protected JPanel panel;
    protected IntConfigurer stepsConfigurer;
    protected IntConfigurer currentStepConfigurer;
    protected StringConfigurer descriptionConfigurer;

    protected TraitEditor(RsrTrait trait) {
      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(new JLabel("By morvael", JLabel.CENTER));
      panel.add(new JSeparator());
      panel.add(new JLabel(" "));

      descriptionConfigurer = new StringConfigurer(null, "Description:  ", trait.description);
      panel.add(descriptionConfigurer.getControls());

      stepsConfigurer = new IntConfigurer(null, "Steps:  ", trait.steps);
      panel.add(stepsConfigurer.getControls());

      currentStepConfigurer = new IntConfigurer(null, "Current step:  ", trait.currentStep);
      panel.add(currentStepConfigurer.getControls());
    }

    public Component getControls() {
      return panel;
    }

    public String getState() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(stepsConfigurer.getValueString())
          .append(currentStepConfigurer.getValueString())
          .append(0)
          .append(0)
          .append("")
          .append(0)
          .append("")
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append("")
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append(false)
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append(0)
          .append(0);
      return se.getValue();
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(descriptionConfigurer.getValueString());
      return ID + se.getValue();
    }

  }

  public static void commandClearAttributes(GamePiece gp) {
    //T_STRENGTH_ID is set in commandReleaseCounter
    //T_DISPLAY_STRENGTH is set due to T_CURRENT_STEP and T_SUPPLY_LEVEL
    //T_DISPLAYED_ATTACK is set due to T_CURRENT_STEP and T_SUPPLY_LEVEL
    //T_DISPLAYED_DEFENCE is set due to T_CURRENT_STEP and T_SUPPLY_LEVEL
    getCore().commandBaseSetProperty(gp,
      //external elements (Markers)
      PROPERTY_SUPPLY_MODE, 1,
      //RsrTrait elements
      T_CURRENT_STEP, getSteps(gp),
      T_HL_LOSS, 0,
      T_HL_ACTION, 0,
      T_HL_MOVEMENT, 0,
      T_SUPPLY_LEVEL, 0,
      T_SUPPLY_ISOLATED, 0,
      T_MOVEMENT_LEFT, 0.0d,
      T_START_HEX, "",
      T_MOVE_INDEX, 0,
      T_ATTACKER, 0,
      T_DEFENDER, 0,
      T_COMBAT_HEX, "",
      T_LOST, 0,
      T_RETREATING, 0,
      T_ADVANCING, 0,
      T_ATTACKED, 0,
      T_PLACED, 0,
      T_MOVED, 0,
      T_REORGANIZED, 0,
      T_FIRED, 0,
      T_REPOSITIONING, 0,
      T_OVERSTACK_RETREATING, 0);
  }

  public static void commandClearCombatAttributes(GamePiece gp, boolean attacked) {
    getCore().commandBaseSetProperty(gp,
      T_HL_LOSS, 0,
      T_ATTACKER, 0,
      T_DEFENDER, 0,
      T_COMBAT_HEX, "",
      T_LOST, 0,
      T_RETREATING, 0,
      T_ADVANCING, 0,
      T_ATTACKED, attacked ? 1 : 0);
  }

  public static void commandCopyAttributes(GamePiece from, GamePiece to) {
    double dif = getMovement(to) - getMovement(from);
    double mov = Math.max(0, getMovementLeft(from) + dif);
    getCore().commandBaseSetProperty(to,
      //RsrTrait elements
      T_HL_LOSS, getHlLoss(from),
      T_HL_ACTION, getHlAction(from),
      T_HL_MOVEMENT, (mov > 0 ? getHlMovement(from) : 0),
      T_SUPPLY_LEVEL, getSupplyLevel(from),
      T_SUPPLY_ISOLATED, getSupplyIsolated(from),
      T_MOVEMENT_LEFT, mov,
      T_START_HEX, getStartHex(from),
      T_MOVE_INDEX, getMoveIndex(from),
      T_ATTACKER, getAttacker(from),
      T_DEFENDER, getDefender(from),
      T_COMBAT_HEX, getCombatHex(from),
      T_LOST, getLost(from),
      T_RETREATING, getRetreating(from),
      T_ADVANCING, getAdvancing(from),
      T_ATTACKED, getAttacked(from),
      T_PLACED, getPlaced(from),
      T_MOVED, getMoved(from),
      T_REORGANIZED, getReorganized(from),
      T_FIRED, getFired(from),
      T_REPOSITIONING, getRepositioning(from),
      T_OVERSTACK_RETREATING, getOverstackRetreating(from));
  }


  public static String getLocationName(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "LocationName", "", false);
	}

  public static String getSide(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "Side", "", true);
	}

	public static String getNationality(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "Nationality", "", true);
	}

	public static String getType(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "Type", "", true);
	}

	public static String getAssetType(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "AssetType", "", true);
	}

	public static String getMarkerType(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "MarkerType", "", true);
	}

	public static int getControl(GamePiece gp) {
		return RsrCounter.getIntProperty(gp, "Control", 1, false);
	}

	public static int getDestruction(GamePiece gp) {
		return RsrCounter.getIntProperty(gp, "Destruction", 1, false);
	}

  public static int getRange(GamePiece gp) {
		return RsrCounter.getIntProperty(gp, "Range", 0, true);
	}

  public static int getFirepower(GamePiece gp) {
		return RsrCounter.getIntProperty(gp, "Firepower", 0, true);
	}

  public static int getFleetNumber(GamePiece gp) {
    return RsrCounter.getIntProperty(gp, "FleetNumber", 0, true);
  }

	public static String getUnitType(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "UnitType", "", true);
	}

	public static String getCombatClass(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "CombatClass", "", true);
	}

	public static String getStrengthClass(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "StrengthClass", "", true);
	}

	public static String getStrengthType(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, "StrengthType", "", true);
	}

  public static double getStackPoints(GamePiece gp) {
    double result = RsrCounter.getDoubleProperty(gp, "StackPoints", 0.0d, true);
    if (result == -1.0d) {
      return 0.0d;
    } else {
      return result;
    }
  }

  public static int getSteps(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.steps : 0;
	}

	public static int getCurrentStep(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.currentStep : 0;
	}

  public static int getSupplyLevel(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.supplyLevel : 0;
	}

	public static int getSupplyIsolated(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.supplyIsolated : 0;
	}

	public static boolean isSupplyLevel(GamePiece gp, int ... types) {
		int t = getSupplyLevel(gp);
		for (int i=0; i<types.length; i++) {
			if (t == types[i]) {
				return true;
			}
		}
		return false;
	}

	public static double getMovement(GamePiece gp) {
		return RsrCounter.getDoubleProperty(gp, MOVEMENT, 0.0d, true);
	}

	public static double getMovementLeft(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.movementLeft : 0;
	}

	public static String getMovementType(GamePiece gp) {
		return RsrCounter.getStringProperty(gp, PROPERTY_MOVEMENT_TYPE, MOVEMENT_NONMOTORIZED_STRING, true);
	}

	public static int getMovementTypeAsInt(GamePiece gp) {
		String s = getMovementType(gp);
		if (s.equals(MOVEMENT_MOTORIZED_STRING)) {
			return MOVEMENT_MOTORIZED;
		} else
		if (s.equals(MOVEMENT_RAIL_STRING)) {
			return MOVEMENT_RAIL;
		} else {
			return MOVEMENT_NONMOTORIZED;
		}
	}

  public static int getVP(GamePiece gp) {
		return RsrCounter.getIntProperty(gp, VP, 0, true);
  }

  public static int getAttack1(GamePiece gp) {
    return RsrCounter.getIntProperty(gp, "Attack_1", 0, true);
  }

  public static int getAttack2(GamePiece gp) {
    return RsrCounter.getIntProperty(gp, "Attack_2", 0, true);
  }

  public static int getDefend1(GamePiece gp) {
    return RsrCounter.getIntProperty(gp, "Defend_1", 0, true);
  }

  public static int getDefend2(GamePiece gp) {
    return RsrCounter.getIntProperty(gp, "Defend_2", 0, true);
  }

	public static int getSupplyMode(GamePiece gp) {
		return RsrCounter.getIntProperty(gp, PROPERTY_SUPPLY_MODE, 0, false);
	}

	public static int getHlLoss(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.hlLoss : 0;
	}

	public static int getHlAction(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.hlAction : 0;
	}

	public static int getHlMovement(GamePiece gp) {
    RsrTrait t = getTrait(gp);
		return t != null ? t.hlMovement : 0;
	}

  public static String getStrengthID(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.strengthID : "";
  }

  public static String getStartHex(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.startHex : "";
  }

  public static int getMoveIndex(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.moveIndex : 0;
  }

  public static int getAttacker(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.attacker : 0;
  }

  public static int getDefender(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.defender : 0;
  }

  public static String getCombatHex(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.combatHex : "";
  }

  public static int getLost(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.lost : 0;
  }

  public static int getRetreating(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.retreating : 0;
  }

  public static int getAdvancing(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.advancing : 0;
  }

  public static int getAttacked(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.attacked : 0;
  }

  public static int getPlaced(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.placed : 0;
  }

  public static int getMoved(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.moved : 0;
  }

  public static int getReorganized(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.reorganized : 0;
  }

  public static int getFired(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.fired : 0;
  }

  public static int getRepositioning(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.repositioning : 0;
  }

  public static int getOverstackRetreating(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    return t != null ? t.overstackRetreating : 0;
  }

  public static int getCurrentNominalSteps(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    if (t != null) {
      int result = t.currentStep;
      String unitType = getUnitType(gp);
      if ((unitType.equals("AxisGermanTankCorps")) || (unitType.equals("AxisGermanInfantryCorps"))) {
        result += 2;
      } else if (unitType.equals("AxisGermanMountainCorps")) {
        result += 1;
      }
      return result;
    } else {
      return 1;
    }
  }

  public static int getNominalSteps(GamePiece gp) {
    RsrTrait t = getTrait(gp);
    if (t != null) {
      int result = t.steps;
      String unitType = getUnitType(gp);
      if ((unitType.equals("AxisGermanTankCorps")) || (unitType.equals("AxisGermanInfantryCorps"))) {
        result += 2;
      } else if (unitType.equals("AxisGermanMountainCorps")) {
        result += 1;
      }
      return result;
    } else {
      return 1;
    }
  }

  private static HashMap<GamePiece, RsrTrait> map = new HashMap<GamePiece, RsrTrait>();

  private static RsrTrait getTrait(GamePiece gp) {
    RsrTrait result = map.get(gp);
    if (result == null) {
      result = (RsrTrait)Decorator.getDecorator(gp, RsrTrait.class);
      map.put(gp, result);
    }
    return result;
  }


}
