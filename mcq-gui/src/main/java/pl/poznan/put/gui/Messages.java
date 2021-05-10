package pl.poznan.put.gui;

import java.util.ResourceBundle;

/** A delegate class for messages {@link ResourceBundle}. */
public final class Messages {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("mcq4structures-messages");

  private Messages() {
    super();
  }

  public static String getString(final String s) {
    return Messages.BUNDLE.getString(s);
  }
}
