package rsr;

import VASSAL.command.Command;

/**
 *
 * @author morvael
 * @since 2011-08-25
 */
public class OverstackCommand extends Command {

  protected boolean onUndo = false;
  
  public OverstackCommand() {
    this(false);
  }

  protected OverstackCommand(boolean onUndo) {
    this.onUndo = onUndo;
  }

  @Override
  protected void executeCommand() {
    if (onUndo) {
      RsrAdvance.getInstance().discoverOverstackHexes();
    }
  }

  @Override
  protected Command myUndoCommand() {
    return new OverstackCommand(true);
  }

}
