package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.ResidueCollection;
import pl.poznan.put.rna.NucleotideTorsionAngle;
import pl.poznan.put.structure.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@Slf4j
final class Helper {
  static final Option OPTION_TARGET =
      Option.builder("t")
          .longOpt("target")
          .numberOfArgs(1)
          .desc("Path to PDB file of the native 3D RNA target")
          .required()
          .type(File.class)
          .build();
  static final Option OPTION_MCQ_THRESHOLD =
      Option.builder("v")
          .longOpt("mcq-threshold-value")
          .numberOfArgs(1)
          .desc("Value of MCQ threshold in degrees")
          .required()
          .build();
  static final Option OPTION_SELECTION_TARGET =
      Option.builder("T")
          .longOpt("selection-target")
          .numberOfArgs(1)
          .desc("Selection query for native 3D RNA target")
          .type(String.class)
          .build();
  static final Option OPTION_SELECTION_MODEL =
      Option.builder("M")
          .longOpt("selection-model")
          .numberOfArgs(1)
          .desc("Selection query for 3D RNA model")
          .type(String.class)
          .build();
  static final Option OPTION_ANGLES =
      Option.builder("a")
          .longOpt("angles")
          .numberOfArgs(1)
          .desc(
              String.format(
                  "Torsion angle types (separated by comma without space), select from: %s. Default"
                      + " is: %s",
                  Helper.arrayToString(NucleotideTorsionAngle.values()),
                  Helper.arrayToString(
                      MoleculeType.RNA.mainAngleTypes().toArray(new MasterTorsionAngleType[0]))))
          .type(String.class)
          .build();
  static final Option OPTION_NAMES =
      Option.builder("n")
          .longOpt("names")
          .numberOfArgs(1)
          .desc("Model names to be saved in output files (separated by comma without space)")
          .build();
  static final Option OPTION_DIRECTORY =
      Option.builder("d")
          .longOpt("directory")
          .numberOfArgs(1)
          .desc("Output directory")
          .required()
          .type(File.class)
          .build();
  static final Option OPTION_RELAXED =
      Option.builder("r")
          .longOpt("relaxed-mode")
          .numberOfArgs(1)
          .desc(
              "Relaxed mode. 0 = (default) does not compare if any violation is found. 1 = only"
                  + " compare models without violations. 2 = compare everything regardless of any"
                  + " violations")
          .build();
  static final Option OPTION_MULTI_MODEL =
      Option.builder("m")
          .longOpt("multi-model")
          .numberOfArgs(1)
          .desc(
              String.format(
                  "In case of multi-model file, process: %s. Default is: %s",
                  Helper.arrayToString(MultiModelMode.values()), MultiModelMode.FIRST))
          .build();
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("mcq-cli-messages");
  private static final Options HELP_OPTIONS = PatternOptionBuilder.parsePattern("h");

  private Helper() {
    super();
  }

  static StructureSelection selectModel(final CommandLine commandLine) throws ParseException {
    final File modelFile = new File(commandLine.getArgs()[0]);
    final PdbModel model = Helper.loadStructure(modelFile);
    final String selectionQuery =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_MODEL.getOpt());
    final String name = Helper.modelName(modelFile, model);
    return Helper.select(model, name, selectionQuery, Helper.parseRelaxedMode(commandLine));
  }

  static List<StructureSelection> selectModels(final CommandLine commandLine)
      throws ParseException {
    final String[] paths = commandLine.getArgs();
    final String selectionQuery =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_MODEL.getOpt());

    final Map<String, String> pathToName = Helper.loadPathToNameMap(commandLine);

    return Arrays.stream(paths)
        .map(
            path -> {
              final PdbModel model = Helper.loadStructure(new File(path));
              final String name =
                  pathToName.getOrDefault(path, Helper.modelName(new File(path), model));
              return Helper.select(
                  model, name, selectionQuery, Helper.parseRelaxedMode(commandLine));
            })
        .collect(Collectors.toList());
  }

  static StructureSelection selectTarget(final CommandLine commandLine) throws ParseException {
    final File targetFile = (File) commandLine.getParsedOptionValue(Helper.OPTION_TARGET.getOpt());
    final PdbModel target = Helper.loadStructure(targetFile);
    final String selectionQuery =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_TARGET.getOpt());
    final String name = Helper.modelName(targetFile, target);
    return Helper.select(target, name, selectionQuery, Helper.parseRelaxedMode(commandLine));
  }

  public static List<StructureSelection> loadMultiModelFile(final CommandLine commandLine)
      throws ParseException {
    final File modelFile = new File(commandLine.getArgs()[0]);
    final MultiModelMode multiModelMode =
        MultiModelMode.valueOf(
            commandLine.getOptionValue(
                Helper.OPTION_MULTI_MODEL.getOpt(), MultiModelMode.FIRST.name()));
    final String query =
        (String) commandLine.getParsedOptionValue(Helper.OPTION_SELECTION_TARGET.getOpt());

    return Helper.loadStructures(modelFile, multiModelMode).stream()
        .map(
            model ->
                Helper.select(
                    model,
                    String.format(
                        Locale.US,
                        "%s (model: %d)",
                        Helper.modelName(modelFile, model),
                        model.modelNumber()),
                    query,
                    Helper.parseRelaxedMode(commandLine)))
        .collect(Collectors.toList());
  }

  /**
   * Parses angles' names to create a list of their singleton instances.
   *
   * @param commandLine The command-line.
   * @return The list of torsion angle types.
   */
  static List<MasterTorsionAngleType> parseAngles(final CommandLine commandLine) {
    return commandLine.hasOption(Helper.OPTION_ANGLES.getOpt())
        ? Arrays.stream(commandLine.getOptionValues(Helper.OPTION_ANGLES.getOpt()))
            .flatMap(s -> Arrays.stream(s.split("\\s*,\\s*")))
            .map(NucleotideTorsionAngle::valueOf)
            .collect(Collectors.toList())
        : MoleculeType.RNA.mainAngleTypes();
  }

  static boolean isHelpRequested(final String[] args) throws ParseException {
    final CommandLineParser parser = new DefaultParser();
    final CommandLine helpCommandLine = parser.parse(Helper.HELP_OPTIONS, args, true);
    return helpCommandLine.hasOption('h') || (args.length == 0);
  }

  static void printHelp(final String commandName, final Options options) {
    final String footer = Helper.getMessage("selection.query.syntax");
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp(78, commandName, "", options, footer, true);
  }

  static File getOutputDirectory(final CommandLine commandLine) throws ParseException {
    return (File) commandLine.getParsedOptionValue(Helper.OPTION_DIRECTORY.getOpt());
  }

  private static String arrayToString(final Object[] values) {
    if (values.length == 0) {
      return "";
    }

    return IntStream.range(1, values.length)
        .mapToObj(i -> "," + values[i])
        .collect(Collectors.joining("", values[0].toString(), ""));
  }

  /**
   * Load first PDB or PDBx/mmCIF model in a given file.
   *
   * @param file An object representing path to file.
   * @return An object with parsed data about 3D coordinates.
   */
  private static PdbModel loadStructure(final File file) {
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

  private static List<? extends PdbModel> loadStructures(
      final File file, final MultiModelMode multiModelMode) {
    try {
      final List<? extends PdbModel> models = StructureManager.loadStructure(file);

      switch (multiModelMode) {
        case FIRST:
          return models.subList(0, 1);
        case ALL:
        default:
          return models;
      }
    } catch (final IOException e) {
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
   * @param relaxedMode If relaxed mode is FULL, then each chain is treated as a single compact
   *     fragment (gaps are not taken into account).
   * @return A {@link StructureSelection} made on the given structure.
   */
  private static StructureSelection select(
      final PdbModel structure,
      final String name,
      final String query,
      final Local.RelaxedMode relaxedMode) {
    if (relaxedMode == Local.RelaxedMode.FULL) {
      return new StructureSelection(
          name,
          structure.chains().stream()
              .map(chain -> Helper.residueCollectionToCompactFragment(chain, name))
              .collect(Collectors.toList()));
    }

    if ("*".equals(query)) {
      return new StructureSelection(
          name,
          Collections.singletonList(Helper.residueCollectionToCompactFragment(structure, name)));
    }

    if (StringUtils.isBlank(query)) {
      return SelectionFactory.create(name, structure);
    }

    return SelectionFactory.create(name, structure, SelectionQuery.parse(query));
  }

  private static PdbCompactFragment residueCollectionToCompactFragment(
      final ResidueCollection residueCollection, final String name) {
    return ImmutablePdbCompactFragment.of(residueCollection.residues())
        .withName(
            String.format(
                "%s %s:%d",
                name, residueCollection.residues().get(0), residueCollection.residues().size()));
  }

  private static Map<String, String> loadPathToNameMap(final CommandLine commandLine) {
    final String names = commandLine.getOptionValue(Helper.OPTION_NAMES.getOpt(), "");

    if (StringUtils.isBlank(names)) {
      return Collections.emptyMap();
    }

    final String[] paths = commandLine.getArgs();
    final String[] split = StringUtils.split(names, ',');

    if (paths.length != split.length) {
      Helper.log.warn(
          "Number of model names ({}) is different than number of models ({})",
          split.length,
          paths.length);
      return Collections.emptyMap();
    }

    return IntStream.range(0, paths.length)
        .boxed()
        .collect(Collectors.toMap(i -> paths[i], i -> split[i]));
  }

  private static String modelName(final File modelFile, final PdbModel model) {
    final String idCode = model.idCode();
    return StringUtils.isNotBlank(idCode) ? idCode : modelFile.getName().replace(".pdb", "");
  }

  private static String getMessage(final String s) {
    return Helper.BUNDLE.getString(s);
  }

  private static String formatMessage(final String s, final Object... objects) {
    return MessageFormat.format(Helper.getMessage(s), objects);
  }

  public static Local.RelaxedMode parseRelaxedMode(final CommandLine commandLine) {
    if (commandLine.hasOption(Helper.OPTION_RELAXED.getOpt())) {
      final String optionValue = commandLine.getOptionValue(Helper.OPTION_RELAXED.getOpt());
      if ("0".equals(optionValue)) {
        return Local.RelaxedMode.NONE;
      } else if ("1".equals(optionValue)) {
        return Local.RelaxedMode.MEDIUM;
      } else if ("2".equals(optionValue)) {
        return Local.RelaxedMode.FULL;
      }
    }
    return Local.RelaxedMode.NONE;
  }
}
