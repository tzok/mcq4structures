package pl.poznan.put.mcq.cli;

import com.github.slugify.Slugify;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.DefaultPdbModel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.pdb.analysis.ResidueCollection;
import pl.poznan.put.pdb.analysis.ResidueTorsionAngles;
import pl.poznan.put.structure.CanonicalStructureExtractor;
import pl.poznan.put.structure.formats.DotBracket;
import pl.poznan.put.structure.formats.DotBracketFromPdb;
import pl.poznan.put.structure.formats.ImmutableDefaultConverter;
import pl.poznan.put.structure.formats.ImmutableDefaultDotBracketFromPdb;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Print {
  private static final Options OPTIONS =
      new Options()
          .addOption(Helper.OPTION_SELECTION_TARGET)
          .addOption(Helper.OPTION_MULTI_MODEL)
          .addOption(Helper.OPTION_DIRECTORY);
  private static final String[] CSV_HEADER =
      Stream.concat(
              Stream.of("Chain", "ResNum", "iCode", "Name", "Structure"),
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
    final List<StructureSelection> models = Helper.loadMultiModelFile(commandLine);
    final File outputDirectory = Helper.getOutputDirectory(commandLine);

    FileUtils.forceMkdir(outputDirectory);

    for (final StructureSelection model : models) {
      final File csvFile = new File(outputDirectory, Print.csvFileName(model));

      final CSVFormat format = CSVFormat.Builder.create().setHeader(Print.CSV_HEADER).build();
      try (final FileWriter writer = new FileWriter(csvFile);
           final CSVPrinter csvPrinter =
              new CSVPrinter(writer, format)) {
        final DotBracketFromPdb dotBracket = Print.toDotBracket(model);

        for (final PdbCompactFragment fragment : model.getCompactFragments()) {
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
  }

  private static String csvFileName(final StructureSelection selection) {
    final Slugify slugify = Slugify.builder().build();
    return slugify.slugify(selection.getName()).toLowerCase(Locale.ROOT) + ".csv";
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
