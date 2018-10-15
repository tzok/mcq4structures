package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.lang3.StringUtils;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionQuery;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.structure.tertiary.StructureManager;

@Slf4j
public final class Helper {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("mcq-cli-messages");
  private static final Options HELP_OPTIONS = PatternOptionBuilder.parsePattern("h");

  public static final Option OPTION_TARGET =
      Option.builder("t")
          .longOpt("target")
          .numberOfArgs(1)
          .desc("Path to PDB file of the native 3D RNA target")
          .required()
          .type(File.class)
          .build();
  public static final Option OPTION_MODEL =
      Option.builder("m")
          .longOpt("model")
          .numberOfArgs(1)
          .desc("Path to PDB file of the 3D RNA model")
          .required()
          .type(File.class)
          .build();
  public static final Option OPTION_MODELS =
      Option.builder("m")
          .longOpt("models")
          .numberOfArgs(Option.UNLIMITED_VALUES)
          .desc("Path to PDB files of the 3D RNA models (any number)")
          .required()
          .type(File.class)
          .build();
  public static final Option OPTION_MCQ_THRESHOLD =
      Option.builder("v")
          .longOpt("mcq-value")
          .numberOfArgs(1)
          .desc("Value of MCQ threshold in degrees")
          .required()
          .build();
  public static final Option OPTION_SELECTION_TARGET =
      Option.builder("T")
          .longOpt("selection-target")
          .numberOfArgs(Option.UNLIMITED_VALUES)
          .desc("Selection query for native 3D RNA target")
          .type(String.class)
          .build();
  public static final Option OPTION_SELECTION_MODEL =
      Option.builder("M")
          .longOpt("selection-model")
          .numberOfArgs(Option.UNLIMITED_VALUES)
          .desc("Selection query for 3D RNA model")
          .type(String.class)
          .build();
  public static final Option OPTION_ANGLES =
      Option.builder("a")
          .longOpt("angles")
          .numberOfArgs(Option.UNLIMITED_VALUES)
          .desc(
              String.format(
                  "Torsion angle types, select from: %s. Default is: %s",
                  Arrays.toString(RNATorsionAngleType.values()),
                  Arrays.toString(RNATorsionAngleType.mainAngles())))
          .type(String[].class)
          .build();
  public static final Option OPTION_SIMPLE =
      Option.builder("s")
          .longOpt("simple")
          .hasArg(false)
          .desc("A simple mode (show just MCQ in radians)")
          .build();

  /**
   * Load first PDB or PDBx/mmCIF model in a given file.
   *
   * @param file An object representing path to file.
   * @return An object with parsed data about 3D coordinates.
   */
  public static PdbModel loadStructure(final File file) {
    try {
      final List<? extends PdbModel> models = StructureManager.loadStructure(file);

      if (models.isEmpty()) {
        Helper.log.error(Helper.getMessage("no.models.found.in.the.file"), file);
      }

      if (models.size() > 1) {
        Helper.log.warn(Helper.getMessage("more.than.1.model.found"));
      }

      return models.get(0);
    } catch (final IOException | PdbParsingException e) {
      throw new IllegalArgumentException(
          Helper.formatMessage("failed.to.load.structure.from.file.0", file), e);
    }
  }

  /**
   * Make selection on structure. An asterisk '*' means to treat residues in file in the order of
   * appearance as a single fragment. An empty string means to divide automatically into compact
   * fragments. Other strings are parsed according to selection syntax.
   *
   * @param structure A PDB structure.
   * @param name Name of the structure to be displayed in final results.
   * @param query An asterisk, empty string or selection query.
   * @return A {@link StructureSelection} made on the given structure.
   */
  private static StructureSelection select(
      final PdbModel structure, final String name, final String query) {
    if ("*".equals(query)) {
      final PdbCompactFragment compactFragment =
          new PdbCompactFragment(name, structure.getResidues());
      return new StructureSelection(name, Collections.singleton(compactFragment));
    } else if (StringUtils.isBlank(query)) {
      return SelectionFactory.create(name, structure);
    }

    return SelectionFactory.create(name, structure, SelectionQuery.parse(query));
  }

  public static StructureSelection selectModel(final CommandLine commandLine)
      throws ParseException {
    final File modelFile = (File) commandLine.getParsedOptionValue(Helper.OPTION_MODEL.getOpt());
    final PdbModel model = Helper.loadStructure(modelFile);
    final String selectionQuery =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_MODEL.getOpt());
    final String name = Helper.modelName(modelFile, model);
    return Helper.select(model, name, selectionQuery);
  }

  public static List<StructureSelection> selectModels(final CommandLine commandLine)
      throws ParseException {
    final String[] paths = commandLine.getOptionValues(Helper.OPTION_MODELS.getOpt());
    final String selectionQuery =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_MODEL.getOpt());

    return Arrays.stream(paths)
        .map(File::new)
        .map(
            file -> {
              final PdbModel model = Helper.loadStructure(file);
              final String name = Helper.modelName(file, model);
              return Helper.select(model, name, selectionQuery);
            })
        .collect(Collectors.toList());
  }

  public static StructureSelection selectTarget(final CommandLine commandLine)
      throws ParseException {
    final File targetFile = (File) commandLine.getParsedOptionValue(Helper.OPTION_TARGET.getOpt());
    final PdbModel target = Helper.loadStructure(targetFile);
    final String selectionQuery =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_TARGET.getOpt());
    final String name = Helper.modelName(targetFile, target);
    return Helper.select(target, name, selectionQuery);
  }

  private static String modelName(final File modelFile, final PdbModel model) {
    final String idCode = model.getIdCode();
    return StringUtils.isNotBlank(idCode) ? idCode : modelFile.getName();
  }

  public static List<RNATorsionAngleType> parseAngles(final CommandLine commandLine) {
    if (commandLine.hasOption(Helper.OPTION_ANGLES.getOpt())) {
      final List<RNATorsionAngleType> angles = new ArrayList<>();

      for (final String angleName : commandLine.getOptionValues(Helper.OPTION_ANGLES.getOpt())) {
        angles.add(RNATorsionAngleType.valueOf(angleName));
      }

      return angles;
    }

    return Arrays.stream(RNATorsionAngleType.mainAngles())
        .map(t -> (RNATorsionAngleType) t)
        .collect(Collectors.toList());
  }

  public static boolean isHelpRequested(final String[] args) throws ParseException {
    final CommandLineParser parser = new DefaultParser();
    final CommandLine helpCommandLine = parser.parse(Helper.HELP_OPTIONS, args, true);
    return helpCommandLine.hasOption('h') || (args.length == 0);
  }

  public static void printHelp(final String commandName, final Options options) {
    final String footer = Helper.getMessage("selection.query.syntax");
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp(commandName, "", options, footer, true);
  }

  private static String getMessage(final String s) {
    return Helper.BUNDLE.getString(s);
  }

  private static String formatMessage(final String s, final Object... objects) {
    return MessageFormat.format(Helper.getMessage(s), objects);
  }

  private Helper() {
    super();
  }
}
