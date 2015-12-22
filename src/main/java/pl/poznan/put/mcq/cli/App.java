package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.*;

import java.util.Arrays;

public class App {
    private enum Mode {
        SINGLE, MULTIPLE, TARGET_MODELS;
    }

    public static void main(String[] args) throws ParseException {
        App app = new App(args);
        app.run();
    }

    Options options = new Options();

    public App(String[] args) throws ParseException {
        options.addOption("m", "mode", true, "(required) mode of operation, one of: " + Arrays.toString(Mode.values()));

        DefaultParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);

        if (!commandLine.hasOption('m')) {
            printHelp();
            System.exit(1);
        }

        Mode mode = Mode.valueOf(commandLine.getOptionValue('m'));
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("mcq-cli", options);
    }

    private void run() {
    }
}
