package pl.poznan.put.gui.component;

import javax.swing.*;

/**
 * An extension of JRadioButtonMenuItem that doesn't close the menu when selected.
 *
 * @author Darryl
 */
public class StayOpenRadioButtonMenuItem extends JRadioButtonMenuItem {
  private static final long serialVersionUID = 1L;
  private MenuElement[] path;

  /** @see JRadioButtonMenuItem#JRadioButtonMenuItem(String, boolean) */
  public StayOpenRadioButtonMenuItem(final String text, final boolean selected) {
    super(text, selected);

    getModel()
        .addChangeListener(
            event -> {
              if (getModel().isArmed() && isShowing()) {
                path = MenuSelectionManager.defaultManager().getSelectedPath();
              }
            });
  }

  /**
   * Overridden to reopen the menu.
   *
   * @param i the time to "hold down" the button, in milliseconds
   */
  @Override
  public final void doClick(final int i) {
    super.doClick(i);
    MenuSelectionManager.defaultManager().setSelectedPath(path);
  }
}
