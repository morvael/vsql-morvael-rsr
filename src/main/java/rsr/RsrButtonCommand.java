package rsr;

/**
 * TODO: remove this interface, replace with automation.CodeButtonCommand
 * @author derwido
 * @since 2009-05-08
 */
public interface RsrButtonCommand {

  RsrButtonCommand getDesiredInstance();
  void execute(String param, boolean alternate);
  
}
