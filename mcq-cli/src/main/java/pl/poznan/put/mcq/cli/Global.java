package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.clustering.partitional.ClusterAssignment;
import pl.poznan.put.clustering.partitional.KMedoids;
import pl.poznan.put.clustering.partitional.KScanner;
import pl.poznan.put.clustering.partitional.PAM;
import pl.poznan.put.clustering.partitional.PartitionalClustering;
import pl.poznan.put.clustering.partitional.PrototypeBasedClusterer;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.ParallelGlobalComparator;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.utility.ExecHelper;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "MethodWithTooExceptionsDeclared"})
public final class Global {
  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_MODELS)
          .addOption(Helper.OPTION_SELECTION_MODEL)
          .addOption(Helper.OPTION_ANGLES);

  public static void main(final String[] args) throws ParseException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("global", Global.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Global.OPTIONS, args);
    final List<StructureSelection> models = Helper.selectModels(commandLine);
    final List<MasterTorsionAngleType> angles = Helper.parseAngles(commandLine);
    final MCQ mcq = new MCQ(angles);

    final long size = models.size();
    final long initialMax = (size * (size - 1)) / 2;
    final ProgressBar progressBar = new ProgressBar("Comparing...", initialMax);
    final Listener listener = new Listener(progressBar);
    final ParallelGlobalComparator comparator = new ParallelGlobalComparator(mcq, models, listener);
    comparator.start();
  }

  private Global() {
    super();
  }

  private static final class Listener implements ParallelGlobalComparator.ProgressListener {
    private final ProgressBar progressBar;

    public Listener(final ProgressBar progressBar) {
      super();
      this.progressBar = progressBar;
    }

    @Override
    public void setProgress(final int progress) {
      progressBar.stepTo(progress);
    }

    @Override
    public void complete(final GlobalMatrix matrix) {
      progressBar.close();

      final PrototypeBasedClusterer clusterer = new KMedoids();
      final double[][] rawMatrix = matrix.getDistanceMatrix().getMatrix();
      final ScoredClusteringResult clustering =
          KScanner.parallelScan(clusterer, rawMatrix, PAM.getInstance());
      final PartitionalClustering partitionalClustering =
          new PartitionalClustering(matrix.getDistanceMatrix(), clustering);

      try {
        final File directory = ExecHelper.createRandomDirectory();
        Listener.exportResults(directory, matrix);
        Listener.exportDrawing(directory, partitionalClustering);
        Listener.exportClustering(directory, partitionalClustering);
        System.out.println("Results available in: " + directory);
      } catch (final IOException e) {
        System.err.println("Failed to store results");
        e.printStackTrace(System.err);
      }
    }

    private static void exportClustering(
        final File directory, final PartitionalClustering clustering) throws IOException {
      final ClusterAssignment assignment = clustering.getAssignment();
      final DistanceMatrix distanceMatrix = clustering.getDistanceMatrix();
      final List<String> names = distanceMatrix.getNames();
      final StringBuilder builder = new StringBuilder();

      for (final int prototype : assignment.getPrototypesIndices()) {
        builder.append(names.get(prototype)).append(": ");
        for (final int assigned : assignment.getAssignedTo(prototype)) {
          builder.append(names.get(assigned)).append(' ');
        }
        builder.append('\n');
      }

      final File clusteringFile = new File(directory, "clustering.txt");
      FileUtils.write(clusteringFile, builder.toString(), StandardCharsets.UTF_8);
    }

    private static void exportDrawing(final File directory, final Visualizable clustering)
        throws IOException {
      final File drawingFile = new File(directory, "clustering.svg");
      try (final OutputStream stream = new FileOutputStream(drawingFile)) {
        final SVGDocument document = clustering.visualize();
        final byte[] bytes = SVGHelper.export(document, Format.SVG);
        stream.write(bytes);
      }
    }

    private static void exportResults(final File directory, final Exportable matrix)
        throws IOException {
      final File matrixFile = new File(directory, "matrix.csv");
      try (final OutputStream stream = new FileOutputStream(matrixFile)) {
        matrix.export(stream);
      }
    }
  }
}
