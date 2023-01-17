package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.pdb.analysis.*;
import pl.poznan.put.structure.CanonicalStructureExtractor;
import pl.poznan.put.structure.StructureManager;
import pl.poznan.put.structure.formats.DotBracket;
import pl.poznan.put.structure.formats.DotBracketFromPdb;
import pl.poznan.put.structure.formats.ImmutableDefaultConverter;
import pl.poznan.put.structure.formats.ImmutableDefaultDotBracketFromPdb;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Print {
  private static final Options OPTIONS =
      new Options()
          .addOption(
              Option.builder()
                  .option("i")
                  .longOpt("input")
                  .hasArg()
                  .desc("path to input PDB or PDBx/mmCIF file")
                  .required()
                  .build())
          .addOption(
              Option.builder()
                  .option("o")
                  .longOpt("output")
                  .hasArg()
                  .desc("path to output CSV file")
                  .required()
                  .build());
  private static final String[] CSV_HEADER =
      Stream.concat(
              Stream.of("chain", "number", "icode", "name", "structure"),
              MoleculeType.RNA.allAngleTypes().stream().map(DisplayableExportable::exportName))
          .toArray(String[]::new);

  private Print() {
    super();
  }

  public static void main(final String[] args) throws ParseException, IOException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("mcq-print", Print.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(Print.OPTIONS, args);
    final File pdbFile = new File(commandLine.getOptionValue("input"));
    final File csvFile = new File(commandLine.getOptionValue("output"));
    List<? extends PdbModel> models = StructureManager.loadStructure(pdbFile);

    if (models.isEmpty()) {
      System.err.println("Failed to load any model from file: " + pdbFile);
    }

    final PdbModel model = models.get(0);
    final CSVFormat format = CSVFormat.Builder.create().setHeader(Print.CSV_HEADER).build();

    try (final FileWriter writer = new FileWriter(csvFile);
        final CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
      final DotBracketFromPdb dotBracket = Print.toDotBracket(model);

      for (final PdbChain chain : model.chains()) {
        ImmutablePdbCompactFragment fragment = ImmutablePdbCompactFragment.of(chain.residues());

        for (final PdbResidue residue : fragment.residues()) {
          final ResidueTorsionAngles residueTorsionAngles =
              fragment.torsionAngles(residue.identifier());

          csvPrinter.print(residue.chainIdentifier());
          csvPrinter.print(residue.residueNumber());
          csvPrinter.print(residue.insertionCode());
          csvPrinter.print(residue.modifiedResidueName());
          csvPrinter.print(dotBracket.symbol(residue.identifier()).structure());
          for (final MasterTorsionAngleType angleType : MoleculeType.RNA.allAngleTypes()) {
            csvPrinter.print(residueTorsionAngles.value(angleType).degrees());
          }
          csvPrinter.println();
        }
      }
    }
  }

  private static DotBracketFromPdb toDotBracket(final ResidueCollection model) {
    final DotBracket dotBracket =
        ImmutableDefaultConverter.of().convert(CanonicalStructureExtractor.bpSeq(model));
    return ImmutableDefaultDotBracketFromPdb.of(
        dotBracket.sequence(),
        dotBracket.structure(),
        DefaultPdbModel.of(model.filteredAtoms(MoleculeType.RNA)));
  }
}
