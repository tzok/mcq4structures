package pl.poznan.put.mcq.cli;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.utility.NumberFormatUtils;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Print {
  private static final Options OPTIONS =
      new Options().addOption(Helper.OPTION_TARGET).addOption(Helper.OPTION_SELECTION_TARGET);

  public static void main(final String[] args) throws ParseException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("print", Print.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Print.OPTIONS, args);
    final StructureSelection target = Helper.selectTarget(commandLine);

    final String angleDescription =
        Arrays.stream(RNATorsionAngleType.mainAngles())
            .map(angleType -> String.format("%s\t", angleType.getExportName()))
            .collect(Collectors.joining());
    System.out.println("Chain\tResNum\tiCode\tName\t" + angleDescription);

    for (final PdbCompactFragment fragment : target.getCompactFragments()) {
      for (final PdbResidue residue : fragment.getResidues()) {
        final String insertionCode = residue.getInsertionCode();

        System.out.print(residue.getChainIdentifier());
        System.out.print('\t');
        System.out.print(residue.getResidueNumber());
        System.out.print('\t');
        System.out.print(" ".equals(insertionCode) ? "-" : insertionCode);
        System.out.print('\t');
        System.out.print(residue.getOriginalResidueName());
        System.out.print('\t');

        for (final MasterTorsionAngleType angleType : RNATorsionAngleType.mainAngles()) {
          final TorsionAngleValue torsionAngleValue =
              fragment.getTorsionAngleValue(residue, angleType);
          final Angle value = torsionAngleValue.getValue();
          System.out.print(
              value.isValid()
                  ? NumberFormatUtils.threeDecimalDigits().format(value.getDegrees())
                  : "-");
          System.out.print('\t');
        }

        System.out.println();
      }
    }
  }

  private Print() {
    super();
  }
}
