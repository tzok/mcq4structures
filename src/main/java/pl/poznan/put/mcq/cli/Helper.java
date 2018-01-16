package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionQuery;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.structure.tertiary.StructureManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class Helper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("mcq-cli-messages");
    private static final Options HELP_OPTIONS =
            PatternOptionBuilder.parsePattern("h");

    public static final Option OPTION_TARGET =
            Option.builder("t").longOpt("target").numberOfArgs(1)
                  .desc("Path to PDB file of the native 3D RNA target")
                  .required().type(File.class).build();
    public static final Option OPTION_MODEL =
            Option.builder("m").longOpt("model").numberOfArgs(1)
                  .desc("Path to PDB file of the 3D RNA model").required()
                  .type(File.class).build();
    public static final Option OPTION_MODELS =
            Option.builder("m").longOpt("models")
                  .numberOfArgs(Option.UNLIMITED_VALUES)
                  .desc("Path to PDB files of the 3D RNA models (any number)")
                  .required().type(File[].class).build();
    public static final Option OPTION_SELECTION_TARGET =
            Option.builder("T").longOpt("selection-target")
                  .numberOfArgs(Option.UNLIMITED_VALUES)
                  .desc("Selection query for native 3D RNA target")
                  .type(String.class).build();
    public static final Option OPTION_SELECTION_MODEL =
            Option.builder("M").longOpt("selection-model")
                  .numberOfArgs(Option.UNLIMITED_VALUES)
                  .desc("Selection query for 3D RNA model").type(String.class)
                  .build();
    public static final Option OPTION_ANGLES =
            Option.builder("a").longOpt("angles")
                  .numberOfArgs(Option.UNLIMITED_VALUES)
                  .desc("Torsion angle types, any number values from: " +
                        Arrays.toString(RNATorsionAngleType.values()))
                  .type(String.class).build();

    public static PdbModel loadStructure(final File file)
            throws IOException, PdbParsingException {
        final List<? extends PdbModel> models =
                StructureManager.loadStructure(file);

        if (models.isEmpty()) {
            Helper.LOGGER
                    .error(Helper.getMessage("no.models.found.in.the.file"),
                           file);
        }

        if (models.size() > 1) {
            Helper.LOGGER.warn(Helper.getMessage("more.than.1.model.found"));
        }

        return models.get(0);
    }

    /**
     * Make selection on structure. An asterisk '*' means to treat residues
     * in file in the order of appearance as a single fragment. An empty
     * string means to divide automatically into compact fragments. Other
     * strings are parsed according to selection syntax.
     *
     * @param structure A PDB structure.
     * @param query     An asterisk, empty string or selection query.
     * @return A {@link StructureSelection} made on the given structure.
     */
    public static StructureSelection select(final PdbModel structure,
                                            final String query) {
        if ("*".equals(query)) {
            final PdbCompactFragment compactFragment =
                    new PdbCompactFragment("", structure.getResidues());
            return new StructureSelection("", Collections
                    .singleton(compactFragment));
        } else if (StringUtils.isBlank(query)) {
            return SelectionFactory.create("", structure);
        }

        return SelectionFactory
                .create("", structure, SelectionQuery.parse(query));
    }

    public static StructureSelection selectModel(final CommandLine commandLine)
            throws IOException, PdbParsingException, ParseException {
        final PdbModel model = Helper.loadStructure((File) commandLine
                .getParsedOptionValue(Helper.OPTION_MODEL.getOpt()));
        return Helper.select(model, (String) commandLine
                .getParsedOptionValue(Helper.OPTION_SELECTION_MODEL.getOpt()));
    }

    public static StructureSelection selectTarget(final CommandLine commandLine)
            throws IOException, PdbParsingException, ParseException {
        final PdbModel target = Helper.loadStructure((File) commandLine
                .getParsedOptionValue(Helper.OPTION_TARGET.getOpt()));
        return Helper.select(target, (String) commandLine
                .getParsedOptionValue(Helper.OPTION_SELECTION_TARGET.getOpt()));
    }

    public static List<RNATorsionAngleType> parseAngles(
            final CommandLine commandLine) {
        if (commandLine.hasOption(Helper.OPTION_ANGLES.getOpt())) {
            final List<RNATorsionAngleType> angles = new ArrayList<>();

            for (final String angleName : commandLine
                    .getOptionValues(Helper.OPTION_ANGLES.getOpt())) {
                angles.add(RNATorsionAngleType.valueOf(angleName));
            }

            return angles;
        }

        return Arrays.stream(RNATorsionAngleType.mainAngles())
                     .map(t -> (RNATorsionAngleType) t)
                     .collect(Collectors.toList());
    }

    public static boolean isHelpRequested(final String[] args)
            throws ParseException {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine helpCommandLine =
                parser.parse(Helper.HELP_OPTIONS, args, true);
        return helpCommandLine.hasOption('h') || (args.length == 0);
    }

    public static void printHelp(final String commandName,
                                 final Options options) {
        final String footer = Helper.getMessage("selection.query.syntax");
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(commandName, "", options, footer, true);
    }

    public static String getMessage(final String s) {
        return Helper.BUNDLE.getString(s);
    }

    private Helper() {
        super();
    }
}
