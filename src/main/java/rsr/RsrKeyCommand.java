package rsr;

import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.i18n.TranslatablePiece;
//import VASSAL.tools.NamedKeyStroke;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author derwido
 * @since 2009-06-02
 */
public class RsrKeyCommand extends KeyCommand {

  private static final long serialVersionUID = 1L;

  public RsrKeyCommand(String name, KeyStroke key, GamePiece target) {
    super(name, key, target);
  }

  //public RsrKeyCommand(String name, NamedKeyStroke key, GamePiece target) {
  //  super(name, key, target);
  //}

  public RsrKeyCommand(String name, KeyStroke key, GamePiece target, TranslatablePiece i18nPiece) {
    super(name, key, target, i18nPiece);
  }

  //public RsrKeyCommand(String name, NamedKeyStroke key, GamePiece target, TranslatablePiece i18nPiece) {
  //  super(name, key, target, i18nPiece);
  //}

  //public RsrKeyCommand(String name, NamedKeyStroke key, GamePiece target, boolean enabled) {
  //  super(name, key, target, enabled);
  //}

  public RsrKeyCommand(String name, KeyStroke key, GamePiece target, boolean enabled) {
    super(name, key, target, enabled);
  }

  public RsrKeyCommand(RsrKeyCommand command) {
    super(command);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (getKeyStroke() != null) {
      Decorator.getOutermost(getTarget()).keyEvent(getKeyStroke());
      RsrAdvance.getInstance().sendAndLog();
    }
  }

}
