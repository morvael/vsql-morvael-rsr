package rsr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.LaunchButton;
import VASSAL.tools.RecursionLimitException;
import VASSAL.tools.RecursionLimiter;
import java.awt.Component;
import javax.swing.KeyStroke;

/**
 * TODO: remove this class, replace with automation.CodeButton
 *
 * @author derwido
 * @since 2009-05-08
 */
public class RsrButton extends AbstractConfigurable implements RecursionLimiter.Loopable {

  public static final String DESCRIPTION = "Rsr Button"; //$NON-NLS-1$
  public static final String BUTTON_TEXT = "text"; //$NON-NLS-1$
  public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
  public static final String NAME = "name"; //$NON-NLS-1$
  public static final String HOTKEY = "hotkey"; //$NON-NLS-1$
  public static final String ICON = "icon"; //$NON-NLS-1$
  public static final String BUTTON_CLASS = "button_class"; //$NON-NLS-1$
  public static final String BUTTON_PARAM = "button_param"; //$NON-NLS-1$
  protected LaunchButton launch;
  protected String buttonClass = "";
  protected String buttonParam = "";
  protected RsrButtonCommand cmd;

  public RsrButton() {
    ActionListener action = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        execute(e);
      }
    };

    launch = new LaunchButton(DESCRIPTION, TOOLTIP, BUTTON_TEXT, HOTKEY, ICON, action);
    setAttribute(NAME, DESCRIPTION);
    setAttribute(TOOLTIP, DESCRIPTION);
    launch.setAttribute(BUTTON_TEXT, DESCRIPTION);
  }

  public static String getConfigureTypeName() {
    return DESCRIPTION;
  }

  public String[] getAttributeNames() {
    return new String[]{
      NAME,
      BUTTON_TEXT,
      TOOLTIP,
      ICON,
      HOTKEY,
      BUTTON_CLASS,
      BUTTON_PARAM
    };
  }

  public String[] getAttributeDescriptions() {
    return new String[]{
      DESCRIPTION,
      Resources.getString(Resources.BUTTON_TEXT),
      Resources.getString(Resources.TOOLTIP_TEXT),
      Resources.getString(Resources.BUTTON_ICON),
      Resources.getString(Resources.HOTKEY_LABEL),
      "Button Class", //$NON-NLS-1$
      "Button Param" //$NON-NLS-1$
    };
  }

  public static class IconConfig implements ConfigurerFactory {

    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, null);
    }
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{
      String.class,
      String.class,
      String.class,
      IconConfig.class,
      KeyStroke.class,
      String.class,
      String.class
    };
  }

  public void addTo(Buildable parent) {
    GameModule.getGameModule().getToolBar().add(getComponent());
  }

  /**
   * The component to be added to the control window toolbar
   */
  protected Component getComponent() {
    return launch;
  }

  public void setAttribute(String key, Object o) {
    if (NAME.equals(key)) {
      setConfigureName((String) o);
    } else if (BUTTON_CLASS.equals(key)) {
      buttonClass = (String) o;
    } else if (BUTTON_PARAM.equals(key)) {
      buttonParam = (String) o;
    } else {
      launch.setAttribute(key, o);
    }
  }

  public String getAttributeValueString(String key) {
    if (NAME.equals(key)) {
      return getConfigureName();
    } else if (BUTTON_CLASS.equals(key)) {
      return buttonClass;
    } else if (BUTTON_PARAM.equals(key)) {
      return buttonParam;
    } else {
      return launch.getAttributeValueString(key);
    }
  }

  @Override
  public VisibilityCondition getAttributeVisibility(String name) {
    return null;
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[0];
  }

  public void removeFrom(Buildable b) {
    GameModule.getGameModule().getToolBar().remove(getComponent());
    GameModule.getGameModule().getToolBar().revalidate();
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("RsrButton.htm"); //$NON-NLS-1$
  }

  protected void execute(ActionEvent e) {
    try {
      RecursionLimiter.startExecution(this);
      if (buttonClass.length() > 0) {
        try {
          if (cmd == null) {
            Class<?> c = Class.forName(buttonClass);
            cmd = (RsrButtonCommand) c.newInstance();
          }
          cmd.getDesiredInstance().execute(buttonParam, (e.getModifiers() & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK);
        } catch (Exception ex) {
          ErrorDialog.bug(ex);
        }
      }
    } catch (RecursionLimitException ex) {
      RecursionLimiter.infiniteLoop(ex);
    } finally {
      RecursionLimiter.endExecution();
    }
  }

  public String getComponentTypeName() {
    return getConfigureTypeName();
  }

  public String getComponentName() {
    return getConfigureName();
  }
}
