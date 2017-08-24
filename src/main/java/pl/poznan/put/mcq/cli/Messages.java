package pl.poznan.put.mcq.cli;

import java.util.ResourceBundle;

public final class Messages {
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("mcq-cli-messages");

    public static ResourceBundle getBundle() {
        return Messages.BUNDLE;
    }

    private Messages() {
        super();
    }
}
