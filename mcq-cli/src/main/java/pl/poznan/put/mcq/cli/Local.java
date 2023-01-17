package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.immutables.value.Value;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.atom.AtomName;
import pl.poznan.put.atom.AtomType;
import pl.poznan.put.atom.Bond;
import pl.poznan.put.atom.BondLength;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.comparison.local.SelectedAngle;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.comparison.mapping.ComparisonMapper;
import pl.poznan.put.comparison.mapping.RangeDifferenceMapper;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.svg.SecondaryStructureVisualizer;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

@SuppressWarnings({"CallToSystemExit", "UseOfSystemOutOrSystemErr"})
@Value.Immutable
public abstract class Local {
  public enum RelaxedMode {
    NONE,
    MEDIUM,
    FULL
  }

  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_TARGET)
          .addOption(Helper.OPTION_SELECTION_TARGET)
          .addOption(Helper.OPTION_SELECTION_MODEL)
          .addOption(Helper.OPTION_ANGLES)
          .addOption(Helper.OPTION_NAMES)
          .addOption(Helper.OPTION_DIRECTORY)
          .addOption(Helper.OPTION_RELAXED);

  public static void main(final String[] args) throws ParseException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("mcq-local", Local.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Local.OPTIONS, args);

    final StructureSelection target = Helper.selectTarget(commandLine);
    final List<StructureSelection> models = Helper.selectModels(commandLine);
    final List<MasterTorsionAngleType> angleTypes = Helper.parseAngles(commandLine);
    final File outputDirectory = Helper.getOutputDirectory(commandLine);
    final RelaxedMode relaxedMode = Helper.parseRelaxedMode(commandLine);

    final Local local =
        ImmutableLocal.of(target, models, angleTypes, outputDirectory.toPath(), relaxedMode);
    local.compare();
  }

  private static ImmutablePdbCompactFragment renamedInstance(
      final StructureSelection selection, final int i) {
    final List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
    final PdbCompactFragment compactFragment = compactFragments.get(i);
    final String name =
        compactFragments.size() == 1
            ? selection.getName()
            : String.format("%s %s", selection.getName(), compactFragment.name());
    return ImmutablePdbCompactFragment.copyOf(compactFragment).withName(name);
  }

  private static void printGapsDetails(
      final StructureSelection selection, final StringBuilder builder) {

    final List<PdbResidue> residues = selection.getResidues();
    boolean foundGaps = false;

    for (int i = 1; i < residues.size(); i++) {
      final PdbResidue previous = residues.get(i - 1);
      final PdbResidue current = residues.get(i);

      // skip check if any of the residues has icode
      if (StringUtils.isNotBlank(previous.insertionCode().orElse(""))
          || StringUtils.isNotBlank(current.insertionCode().orElse(""))) {
        continue;
      }

      // skip check if residues are in different chains
      if (!previous.chainIdentifier().equals(current.chainIdentifier())) {
        continue;
      }

      // skip check if residues are not consecutive
      if ((previous.residueNumber() + 1) != current.residueNumber()) {
        continue;
      }

      if (!MoleculeType.RNA.areConnected(previous, current)) {
        final String reason;
        if (!previous.hasAtom(AtomName.O3p)) {
          reason = "first residue lacks O3' atom";
        } else if (!current.hasAtom(AtomName.P)) {
          reason = "second residue lacks P atom";
        } else {
          final BondLength length = Bond.length(AtomType.O, AtomType.P);
          reason =
              String.format(
                  Locale.US,
                  "O3'-P distance is %.2f but should be [%.2f; %.2f]",
                  previous.findAtom(AtomName.O3p).distanceTo(current.findAtom(AtomName.P)),
                  length.min(),
                  length.max());
        }

        if (!foundGaps) {
          builder.append(String.format("Found gaps: (%s)%n", selection.getName()));
          foundGaps = true;
        }
        builder.append(String.format("- Between %s and %s: %s%n", previous, current, reason));
      }
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

    try (final OutputStream stream = Files.newOutputStream(file.toPath())) {
      stream.write(SVGHelper.export(svg, Format.SVG));
    }
  }

  @Value.Parameter(order = 1)
  public abstract StructureSelection target();

  @Value.Parameter(order = 2)
  public abstract List<StructureSelection> models();

  @Value.Parameter(order = 3)
  public abstract List<MasterTorsionAngleType> angleTypes();

  @Value.Parameter(order = 4)
  public abstract Path outputDirectory();

  @Value.Parameter(order = 5)
  public abstract RelaxedMode relaxedMode();

  @Value.Lazy
  protected LocalComparator mcq() {
    return ImmutableMCQ.of(MoleculeType.RNA).withAngleTypes(angleTypes());
  }

  @Value.Check
  protected Local validate() {
    // models list not empty
    Validate.notEmpty(models());

    // target is not on the model list
    if (models().contains(target())) {
      return ImmutableLocal.copyOf(this)
          .withModels(
              models().stream()
                  .filter(model -> !model.equals(target()))
                  .collect(Collectors.toList()));
    }

    // output directory existing
    Validate.isTrue(
        outputDirectory().toFile().exists() || outputDirectory().toFile().mkdirs(),
        "Failed to create output directory");
    Validate.isTrue(outputDirectory().toFile().isDirectory(), "The output path is not a directory");

    // check validity of comparison
    final List<StructureSelection> invalidBondLengths = checkBondLengthViolations();
    if (!invalidBondLengths.isEmpty()) {
      if (relaxedMode() == RelaxedMode.NONE) {
        System.err.println("Found bond lengths violations, cannot continue");
      } else if (relaxedMode() == RelaxedMode.MEDIUM) {
        System.err.println("Found bond lengths violations, proceed to remove invalid models");
        return ImmutableLocal.copyOf(this)
            .withModels(
                models().stream()
                    .filter(model -> !invalidBondLengths.contains(model))
                    .collect(Collectors.toList()));
      } else {
        System.err.println(
            "Found bond lengths violations, but will continue full comparison anyway");
      }
    }

    final List<StructureSelection> invalidFragmentCount = checkFragmentCountViolations();
    if (!invalidFragmentCount.isEmpty()) {
      if (relaxedMode() == RelaxedMode.NONE) {
        System.err.println("Found invalid fragment count, cannot continue");
      } else if (relaxedMode() == RelaxedMode.MEDIUM) {
        System.err.println("Found invalid fragment count, proceed to remove invalid models");
        return ImmutableLocal.copyOf(this)
            .withModels(
                models().stream()
                    .filter(model -> !invalidFragmentCount.contains(model))
                    .collect(Collectors.toList()));
      } else {
        System.err.println(
            "Found invalid fragment count, but will continue full comparison anyway");
      }
    }

    final List<StructureSelection> invalidResidueCount = checkResidueCountViolations();
    if (!invalidResidueCount.isEmpty()) {
      if (relaxedMode() == RelaxedMode.NONE) {
        System.err.println("Found invalid residue count, cannot continue");
      } else if (relaxedMode() == RelaxedMode.MEDIUM) {
        System.err.println("Found invalid residue count, proceed to remove invalid models");
        return ImmutableLocal.copyOf(this)
            .withModels(
                models().stream()
                    .filter(model -> !invalidResidueCount.contains(model))
                    .collect(Collectors.toList()));
      } else {
        System.err.println("Found invalid residue count, but will continue full comparison anyway");
      }
    }

    return this;
  }

  private void exportDifferences(final FragmentMatch fragmentMatch, final File directory)
      throws IOException {
    final File file = new File(directory, "differences.csv");
    try (final OutputStream stream = Files.newOutputStream(file.toPath())) {
      fragmentMatch.export(stream, angleTypes());
    }
  }

  private void compare() {
    // compare each compact fragment
    final List<ModelsComparisonResult> comparisonResults =
        IntStream.range(0, target().getCompactFragments().size())
            .mapToObj(this::compareFragment)
            .peek(this::exportResults)
            .collect(Collectors.toList());

    // compare mcq for each model
    final List<Angle> mcqs =
        IntStream.range(0, models().size())
            .mapToObj(
                i ->
                    comparisonResults.stream()
                        .map(ModelsComparisonResult::fragmentMatches)
                        .map(fragmentMatches -> fragmentMatches.get(i))
                        .map(FragmentMatch::getResidueComparisons)
                        .flatMap(Collection::stream)
                        .map(ResidueComparison::validDeltas)
                        .flatMap(Collection::stream)
                        .filter(Angle::isValid)
                        .collect(Collectors.toList()))
            .map(ImmutableAngleSample::of)
            .map(AngleSample::meanDirection)
            .collect(Collectors.toList());

    // generate ranking
    final List<Pair<Double, StructureSelection>> ranking =
        IntStream.range(0, models().size())
            .mapToObj(i -> Pair.of(mcqs.get(i).degrees(), models().get(i)))
            .sorted(Comparator.comparingDouble(Pair::getLeft))
            .collect(Collectors.toList());

    for (final Pair<Double, StructureSelection> pair : ranking) {
      System.out.printf(Locale.US, "%s %.2f%n", pair.getValue().getName(), pair.getKey());
    }
  }

  private void printFragmentDetails(final Iterable<StructureSelection> invalidModels) {
    final StringBuilder builder = new StringBuilder();
    builder.append("Fragments in reference structure: (").append(target().getName()).append(")\n");
    target()
        .getCompactFragments()
        .forEach(fragment -> builder.append("- ").append(fragment).append('\n'));

    Local.printGapsDetails(target(), builder);
    builder.append('\n');

    for (final StructureSelection model : invalidModels) {
      builder.append("Fragments in model: (").append(model.getName()).append(")\n");
      model
          .getCompactFragments()
          .forEach(fragment -> builder.append("- ").append(fragment).append('\n'));

      Local.printGapsDetails(model, builder);
      builder.append('\n');
    }

    System.err.print(builder);
  }

  private ModelsComparisonResult compareFragment(final int i) {
    final PdbCompactFragment targetFragment = Local.renamedInstance(target(), i);
    final List<PdbCompactFragment> modelFragments =
        models().stream()
            .map(model -> Local.renamedInstance(model, i))
            .collect(Collectors.toList());
    return mcq().compareModels(targetFragment, modelFragments);
  }

  private void exportResults(final ModelsComparisonResult comparisonResult) {
    try {
      exportTable(comparisonResult);
      comparisonResult.fragmentMatches().forEach(this::exportModelResults);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to export results", e);
    }
  }

  private void exportTable(final ModelsComparisonResult comparisonResult) throws IOException {
    final File file = new File(outputDirectory().toFile(), "table.csv");
    try (final OutputStream stream = Files.newOutputStream(file.toPath())) {
      final SelectedAngle selectedAngle = comparisonResult.selectAverageOfAngles();
      selectedAngle.export(stream);
    }
  }

  private void exportModelResults(final FragmentMatch fragmentMatch) {
    try {
      final String name = fragmentMatch.getModelFragment().name();
      final File directory = new File(outputDirectory().toFile(), name);
      FileUtils.forceMkdir(directory);

      Local.exportSecondaryStructureImage(
          fragmentMatch, directory, "delta.svg", AngleDeltaMapper.getInstance());
      Local.exportSecondaryStructureImage(
          fragmentMatch, directory, "range.svg", RangeDifferenceMapper.getInstance());

      exportDifferences(fragmentMatch, directory);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to export results", e);
    }
  }

  private List<StructureSelection> checkResidueCountViolations() {
    // check for size
    final List<StructureSelection> invalidCompactFragments =
        models().stream()
            .filter(
                model ->
                    IntStream.range(0, target().getCompactFragments().size())
                        .anyMatch(
                            i ->
                                target().getCompactFragments().get(i).residues().size()
                                    != model.getCompactFragments().get(i).residues().size()))
            .collect(Collectors.toList());

    if (!invalidCompactFragments.isEmpty()) {
      invalidCompactFragments.forEach(
          model ->
              IntStream.range(0, target().getCompactFragments().size())
                  .filter(
                      i ->
                          target().getCompactFragments().get(i).residues().size()
                              != model.getCompactFragments().get(i).residues().size())
                  .forEach(
                      i ->
                          System.err.printf(
                              "Invalid size (%d) of fragment `%s`. Expected size (%d) taken from"
                                  + " fragment `%s`%n",
                              model.getCompactFragments().get(i).residues().size(),
                              model.getCompactFragments().get(i).name(),
                              target().getCompactFragments().get(i).residues().size(),
                              target().getCompactFragments().get(i).name())));
    }

    return invalidCompactFragments;
  }

  private List<StructureSelection> checkFragmentCountViolations() {
    // check for gaps
    final int fragmentCount = target().getCompactFragments().size();
    final List<StructureSelection> invalidModels =
        models().stream()
            .filter(selection -> selection.getCompactFragments().size() != fragmentCount)
            .collect(Collectors.toList());

    if (!invalidModels.isEmpty()) {
      printFragmentDetails(invalidModels);
    }

    return invalidModels;
  }

  private List<StructureSelection> checkBondLengthViolations() {
    final StringBuilder builder = new StringBuilder();
    final List<StructureSelection> invalidModels = new ArrayList<>();

    for (final StructureSelection model : models()) {
      final List<String> violations = model.findBondLengthViolations();

      if (!violations.isEmpty()) {
        invalidModels.add(model);

        builder.append("Found bond length violations in ").append(model.getName()).append(":\n");
        violations.forEach(violation -> builder.append("- ").append(violation).append('\n'));
        builder.append('\n');
      }
    }

    System.err.print(builder);
    return invalidModels;
  }
}
