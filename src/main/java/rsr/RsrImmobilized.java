package rsr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.Box;
import javax.swing.KeyStroke;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.Properties;
import VASSAL.i18n.TranslatablePiece;
import VASSAL.tools.SequenceEncoder;

/**
 *
 * @author morvael
 * @since 2009-05-08
 */
public class RsrImmobilized extends Decorator implements TranslatablePiece, RsrConstants {

  private static final int[] DRAW_VP_X = new int[]{49, 20};
  private static final int[] DRAW_VP_Y = new int[]{27, 12};
  
  // trait id
  public static final String ID = "rsr_immob;";

  // dynamic counter information (volatile)
  private GamePiece gp;
  private RsrKeyCommand[] commands;

  //common to all traits
  private static RsrAdvance core;

  private static RsrAdvance getCore() {
    if (core == null) {
      core = RsrAdvance.getInstance();
    }
    return core;
  }

  public static void invalidate() {
    core = null;
  }
  
  public RsrImmobilized() {
    this(RsrImmobilized.ID, null);
  }

  public RsrImmobilized(String type, GamePiece inner) {
    setInner(inner);
    mySetType(type);
  }

  public void mySetType(String type) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    st.nextToken();
  }

  public String getName() {
    return piece.getName();
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

  @Override
  public Object getLocalizedProperty(Object key) {
    if (Properties.NON_MOVABLE.equals(key)) {
      return RsrGlobal.getDebug() == 0;
    } else {
      return super.getLocalizedProperty(key);
    }
  }

  @Override
  public Object getProperty(Object key) {
    if (Properties.NON_MOVABLE.equals(key)) {
      return RsrGlobal.getDebug() == 0;
    } else {
      return super.getProperty(key);
    }
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
    int vp = RsrTrait.getVP(this);
    if (vp > 0) {
      int mod = (((int)(zoom*63))/2);
      int px = x - mod;
      int py = y - mod;
      if ((zoom == 1.0d) || (zoom == 0.5d)) {
        int zoomindex = zoom == 1.0d ? 0 : 1;        
        RsrTrait.drawNumber(g, px + DRAW_VP_X[zoomindex], py + DRAW_VP_Y[zoomindex], 1.0d, obs, vp);
      }      
    }
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public String myGetType() {
    final StringBuilder buffer = new StringBuilder(ID);
    return buffer.toString();
  }

  public String myGetState() {
    return "";
  }

  public void mySetState(String s) {
  }

  private GamePiece getGamePiece() {
    if (gp == null) {
      gp = Decorator.getOutermost(this);
    }
    return gp;
  }
  
  public String getDescription() {
    return "Rsr Immobilized";
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("RsrImmobilized.htm");
  }

  @Override
  public PieceEditor getEditor() {
    return new Ed(this);
  }

  private static class Ed implements PieceEditor {
    private Box controls;

    public Ed(RsrImmobilized p) {
      controls = Box.createVerticalBox();
    }

    public String getState() {
      return "";
    }

    public String getType() {
      return ID;
    }

    public Component getControls() {
      return controls;
    }
  }
}

