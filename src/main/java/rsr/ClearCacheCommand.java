package rsr;

import VASSAL.command.Command;

/**
 *
 * @author derwido
 * @since 2009-07-01
 */
public class ClearCacheCommand extends Command {

  public ClearCacheCommand() {
  }

  @Override
  protected void executeCommand() {
    RsrAdvance.getInstance().clearAll();
  }

  @Override
  protected Command myUndoCommand() {
    return this;
  }

}
