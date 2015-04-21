package pl.poznan.put.gui;

public class MainHandler {
    private static final MainHandler INSTANCE = new MainHandler();

    public static MainHandler getInstance() {
        return MainHandler.INSTANCE;
    }

    private MainHandler() {
        // empty, private constructor
    }
}
