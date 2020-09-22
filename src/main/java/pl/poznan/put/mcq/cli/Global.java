package pl.poznan.put.mcq.cli;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.immutables.value.Value;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.clustering.partitional.ClusterAssignment;
import pl.poznan.put.clustering.partitional.KMedoids;
import pl.poznan.put.clustering.partitional.PAM;
import pl.poznan.put.clustering.partitional.PartitionalClustering;
import pl.poznan.put.clustering.partitional.PrototypeBasedClusterer;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.ParallelGlobalComparator;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
@Value.Immutable
public abstract class Global {
  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_SELECTION_MODEL)
          .addOption(Helper.OPTION_ANGLES)
          .addOption(Helper.OPTION_NAMES)
          .addOption(Helper.OPTION_DIRECTORY);

  public static void main(final String[] args) throws ParseException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("mcq-global", Global.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Global.OPTIONS, args);

    final List<StructureSelection> models = Helper.selectModels(commandLine);
    final List<MasterTorsionAngleType> angleTypes = Helper.parseAngles(commandLine);
    final File outputDirectory = Helper.getOutputDirectory(commandLine);
    final Global global = ImmutableGlobal.of(models, angleTypes, outputDirectory.toPath());
    global.compare();
  }

  @Value.Parameter(order = 1)
  public abstract List<StructureSelection> models();

  @Value.Parameter(order = 2)
  public abstract List<MasterTorsionAngleType> angleTypes();

  @Value.Parameter(order = 3)
  public abstract Path outputDirectory();

  @Value.Check
  protected void validate() {
    // models list not empty
    Validate.notEmpty(models());

    // output directory existing
    Validate.isTrue(
        outputDirectory().toFile().exists() || outputDirectory().toFile().mkdirs(),
        "Failed to create output directory");
    Validate.isTrue(outputDirectory().toFile().isDirectory(), "The output path is not a directory");
  }

  private void compare() {
    final long size = models().size();
    final long initialMax = (size * (size - 1L)) / 2L;
    final ProgressBar progressBar = new ProgressBar("Comparing...", initialMax);
    final Listener listener = new Listener(progressBar, outputDirectory().toFile());
    final ParallelGlobalComparator comparator =
        new ParallelGlobalComparator(
            ImmutableMCQ.of(MoleculeType.RNA).withAngleTypes(angleTypes()), models(), listener);
    comparator.start();
  }

  private static final class Listener implements ParallelGlobalComparator.ProgressListener {
    private final ProgressBar progressBar;
    private final File outputDirectory;

    private Listener(final ProgressBar progressBar, final File outputDirectory) {
      super();
      this.progressBar = progressBar;
      this.outputDirectory = outputDirectory;
    }

    @Override
    public void setProgress(final int progress) {
      progressBar.stepTo(progress);
    }

    @Override
    public void complete(final GlobalMatrix matrix) {
      try {
        progressBar.close();

        exportResults(matrix);

        final double[][] rawMatrix = matrix.getDistanceMatrix().matrix();

        if (rawMatrix.length > 2) {
          final PrototypeBasedClusterer clusterer = new KMedoids();

          for (int k = 2; k <= Math.min(12, rawMatrix.length); k++) {
            final ScoredClusteringResult clustering =
                clusterer.findPrototypes(rawMatrix, PAM.getInstance(), k);
            final PartitionalClustering partitionalClustering =
                new PartitionalClustering(matrix.getDistanceMatrix(), clustering);
            exportDrawing(partitionalClustering);
            exportClustering(partitionalClustering);
          }
        }

        System.out.println("Results available in: " + outputDirectory);
      } catch (final IOException e) {
        System.err.println("Failed to store results");
        e.printStackTrace(System.err);
      }
    }

    private void exportResults(final Exportable matrix) throws IOException {
      final File matrixFile = new File(outputDirectory, "matrix.csv");
      try (final OutputStream stream = new FileOutputStream(matrixFile)) {
        matrix.export(stream);
      }
    }

    private void exportDrawing(final PartitionalClustering clustering) throws IOException {
      final int k = clustering.getClusterCount();
      final double silhouette = clustering.getSilhouette();
      final File drawingFile =
          new File(
              outputDirectory,
              String.format(Locale.US, "clustering-%02d-%06d.svg", k, (int) (silhouette * 1000.0)));
      try (final OutputStream stream = new FileOutputStream(drawingFile)) {
        final SVGDocument document = clustering.visualize();
        final byte[] bytes = SVGHelper.export(document, Format.SVG);
        stream.write(bytes);
      }
    }

    private void exportClustering(final PartitionalClustering clustering) throws IOException {
      final ClusterAssignment assignment = clustering.getAssignment();
      final DistanceMatrix distanceMatrix = clustering.getDistanceMatrix();
      final List<String> names = distanceMatrix.names();

      final String description =
          assignment.getPrototypesIndices().stream()
              .mapToInt(prototype -> prototype)
              .mapToObj(
                  prototype ->
                      assignment.getAssignedTo(prototype).stream()
                          .mapToInt(assigned -> assigned)
                          .mapToObj(assigned -> names.get(assigned) + ' ')
                          .collect(Collectors.joining("", names.get(prototype) + ": ", "\n")))
              .collect(Collectors.joining());

      final int k = clustering.getClusterCount();
      final double silhouette = clustering.getSilhouette();
      final File clusteringFile =
          new File(
              outputDirectory,
              String.format(Locale.US, "clustering-%02d-%06d.txt", k, (int) (silhouette * 1000.0)));
      FileUtils.write(clusteringFile, description, StandardCharsets.UTF_8);

      try (final OutputStream stream =
          new FileOutputStream(
              new File(
                  outputDirectory,
                  String.format("clustering-%02d-%06d.csv", k, (int) (silhouette * 1000.0))))) {
        clustering.export(stream);
      }
    }
  }
}
