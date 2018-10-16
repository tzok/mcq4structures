package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.comparison.local.SelectedAngle;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.comparison.mapping.ComparisonMapper;
import pl.poznan.put.comparison.mapping.RangeDifferenceMapper;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.svg.SecondaryStructureVisualizer;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.ExecHelper;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

@SuppressWarnings({"CallToSystemExit", "UseOfSystemOutOrSystemErr"})
public final class Local {
  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_TARGET)
          .addOption(Helper.OPTION_MODELS)
          .addOption(Helper.OPTION_SELECTION_TARGET)
          .addOption(Helper.OPTION_SELECTION_MODEL)
          .addOption(Helper.OPTION_ANGLES);

  public static void main(final String[] args)
      throws ParseException, IncomparableStructuresException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("local", Local.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Local.OPTIONS, args);
    final StructureSelection target = Helper.selectTarget(commandLine);
    final List<StructureSelection> models = Helper.selectModels(commandLine);

    // check for gaps
    Stream.concat(Stream.of(target), models.stream())
        .parallel()
        .filter(selection -> selection.getCompactFragments().size() != 1)
        .peek(
            selection ->
                System.err.println("The following structure contains gaps: " + selection.getName()))
        .findAny()
        .ifPresent(selection -> System.exit(1));

    // check for size
    final int expectedSize = target.getResidues().size();
    models
        .parallelStream()
        .filter(selection -> selection.getResidues().size() != expectedSize)
        .peek(
            selection ->
                System.err.printf(
                    "The following structure has different size (%d) than the target (%d)%n",
                    selection.getResidues().size(), expectedSize))
        .findAny()
        .ifPresent(selection -> System.exit(1));

    final PdbCompactFragment targetFragment = target.getCompactFragments().get(0);
    final List<PdbCompactFragment> modelFragments =
        models
            .stream()
            .map(selection -> selection.getCompactFragments().get(0))
            .collect(Collectors.toList());

    // rename
    targetFragment.setName(target.getName());
    IntStream.range(0, models.size())
        .forEach(i -> modelFragments.get(i).setName(models.get(i).getName()));

    final List<MasterTorsionAngleType> angleTypes = Helper.parseAngles(commandLine);
    angleTypes.add(RNATorsionAngleType.getAverageOverMainAngles());

    final LocalComparator mcq = new MCQ(angleTypes);
    final ModelsComparisonResult comparisonResult =
        mcq.compareModels(targetFragment, modelFragments);
    final File directory = Local.exportResults(comparisonResult);
    System.out.println("Results are available in: " + directory);
  }

  private static File exportResults(final ModelsComparisonResult comparisonResult) {
    try {
      final File directory = ExecHelper.createRandomDirectory();
      Local.exportTable(directory, comparisonResult);
      comparisonResult
          .getFragmentMatches()
          .parallelStream()
          .forEach(fragmentMatch -> Local.exportModelResults(directory, fragmentMatch));
      return directory;
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to export results", e);
    }
  }

  private static void exportModelResults(
      final File parentDirectory, final FragmentMatch fragmentMatch) {
    try {
      final String name = fragmentMatch.getModelFragment().getName();
      final File directory = new File(parentDirectory, name);
      FileUtils.forceMkdir(directory);

      Local.exportSecondaryStructureImage(
          fragmentMatch, directory, "delta.svg", AngleDeltaMapper.getInstance());
      Local.exportSecondaryStructureImage(
          fragmentMatch, directory, "range.svg", RangeDifferenceMapper.getInstance());
      Local.exportDifferences(fragmentMatch, directory);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to export results", e);
    }
  }

  private static void exportDifferences(final Exportable fragmentMatch, final File directory)
      throws IOException {
    final File file = new File(directory, "differences.csv");
    try (final OutputStream stream = new FileOutputStream(file)) {
      fragmentMatch.export(stream);
    }
  }

  private static void exportSecondaryStructureImage(
      final FragmentMatch fragmentMatch,
      final File directory,
      final String filename,
      final ComparisonMapper deltaMapper)
      throws IOException {
    final SVGDocument svg = SecondaryStructureVisualizer.visualize(fragmentMatch, deltaMapper);
    final File file = new File(directory, filename);

    try (final OutputStream stream = new FileOutputStream(file)) {
      stream.write(SVGHelper.export(svg, Format.SVG));
    }
  }

  private static void exportTable(
      final File directory, final ModelsComparisonResult comparisonResult) throws IOException {
    final File file = new File(directory, "table.csv");
    try (final OutputStream stream = new FileOutputStream(file)) {
      final SelectedAngle selectedAngle =
          comparisonResult.selectAngle(RNATorsionAngleType.getAverageOverMainAngles());
      selectedAngle.export(stream);
    }
  }

  private Local() {
    super();
  }
}
