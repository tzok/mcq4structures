package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.LCSGlobalResult;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.ExecHelper;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.AngleDeltaMapper;
import pl.poznan.put.visualisation.SecondaryStructureVisualizer;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "MethodWithTooExceptionsDeclared"})
public final class LCS {
  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_TARGET)
          .addOption(Helper.OPTION_MODEL)
              .addOption(Helper.MCQ_VALUE)
          .addOption(Helper.OPTION_SELECTION_TARGET)
          .addOption(Helper.OPTION_SELECTION_MODEL);

  public static void main(final String[] args)
          throws ParseException, IOException, PdbParsingException, IncomparableStructuresException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("LCS", LCS.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(LCS.OPTIONS, args);
    final StructureSelection s1 = Helper.selectTarget(commandLine);
    final StructureSelection s2 = Helper.selectModel(commandLine);
    final double mcqValue = Double.valueOf(commandLine.getOptionValue(Helper.MCQ_VALUE.getOpt()));

    final List<RNATorsionAngleType> angleTypes = Helper.parseAngles(commandLine);
    final pl.poznan.put.comparison.LCS lcs = new pl.poznan.put.comparison.LCS(mcqValue);
    final LCSGlobalResult result = (LCSGlobalResult) lcs.compareGlobally(s1, s2);

    final File directory = ExecHelper.createRandomDirectory();
    final File textFile = new File(directory, "results.txt");

    String textResult = result.cliOutput(s1,s2);
    FileUtils.write(textFile, textResult, Charset.defaultCharset());



    System.out.println("Results are available in: " + directory);
  }

  private LCS() {
    super();
  }
}
