package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.enums.ValueType;
import pl.poznan.put.comparison.LCS;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.LCSGlobalResult;
import pl.poznan.put.matching.StructureSelection;

@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class Lcs {
  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_TARGET)
          .addOption(Helper.OPTION_SELECTION_TARGET)
          .addOption(Helper.OPTION_SELECTION_MODEL)
          .addOption(Helper.OPTION_MCQ_THRESHOLD);

  /**
   * The application entrypoint which allows to compute LCS-TA from the command line.
   *
   * @param args Arguments from command line. Required are "-t" for target, "-m" for model and "-v"
   *     for threshold value.
   * @throws ParseException If the arguments given in command line could not be parsed.
   * @throws IncomparableStructuresException If the comparison procedure fails.
   */
  public static void main(final String[] args)
      throws ParseException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("mcq-lcs", Lcs.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Lcs.OPTIONS, args);
    final StructureSelection s1 = Helper.selectTarget(commandLine);
    final StructureSelection s2 = Helper.selectModel(commandLine);
    final double threshold =
        Double.parseDouble(commandLine.getOptionValue(Helper.OPTION_MCQ_THRESHOLD.getOpt()));

    final LCS lcs = new LCS(new Angle(threshold, ValueType.DEGREES));
    final LCSGlobalResult result = (LCSGlobalResult) lcs.compareGlobally(s1, s2);

    System.out.println(result.cliOutput(s2, s1));
  }

  private Lcs() {
    super();
  }
}
