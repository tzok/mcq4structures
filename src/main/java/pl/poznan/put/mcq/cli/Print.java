package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.NumberFormatUtils;

import java.util.stream.Collectors;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Print {
  private static final Options OPTIONS = new Options().addOption(Helper.OPTION_SELECTION_TARGET);

  private Print() {
    super();
  }

  public static void main(final String[] args) throws ParseException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("mcq-print", Print.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Print.OPTIONS, args);
    final StructureSelection target = Helper.selectModel(commandLine);

    final String angleDescription =
        MoleculeType.RNA.mainAngleTypes().stream()
            .map(angleType -> String.format("%s\t", angleType.exportName()))
            .collect(Collectors.joining());
    System.out.println("Chain\tResNum\tiCode\tName\t" + angleDescription);

    for (final PdbCompactFragment fragment : target.getCompactFragments()) {
      for (final PdbResidue residue : fragment.residues()) {
        final String insertionCode = residue.insertionCode();

        System.out.print(residue.chainIdentifier());
        System.out.print('\t');
        System.out.print(residue.residueNumber());
        System.out.print('\t');
        System.out.print(" ".equals(insertionCode) ? "-" : insertionCode);
        System.out.print('\t');
        System.out.print(residue.modifiedResidueName());
        System.out.print('\t');

        for (final MasterTorsionAngleType angleType : MoleculeType.RNA.mainAngleTypes()) {
          final Angle value = fragment.torsionAngles(residue.identifier()).value(angleType);
          System.out.print(
              value.isValid()
                  ? NumberFormatUtils.threeDecimalDigits().format(value.degrees())
                  : "-");
          System.out.print('\t');
        }

        System.out.println();
      }
    }
  }
}
