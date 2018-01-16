package pl.poznan.put.matching;

import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which allow to parse a selection query of the following form CHAIN:NUMBER-ICODE:COUNT e.g.
 * A:1:5.
 */
public final class SelectionQuery {
  // @formatter:off
  /*
   * Match residue number (possibly negative) with following optional
   * insertion code
   *
   * Regular expression:
   * (-?\d+)(.*)
   *
   * Examples:
   * -1
   * 1
   * 100
   * 100A
   * -1x
   *
   * Groups:
   *   1: residue number
   *   2: insertion code
   */
  // @formatter:on
  private static final Pattern PATTERN = Pattern.compile("(-?\\d+)(.*)");

  private final String originalQuery;
  private final PdbResidueIdentifier identifier;
  private final int count;

  public static SelectionQuery parse(final String query) {
    final String[] split = query.split(":");

    if (split.length != 3) {
      throw new InvalidQueryException(
          "Selection Query must consist of three parts separated by"
              + " colons. For example: A:1:5 means 5 residues starting "
              + "from residue 1 from chain A.");
    }

    final Matcher matcher = SelectionQuery.PATTERN.matcher(split[1]);
    if (!matcher.matches() || (matcher.groupCount() != 2)) {
      throw new InvalidQueryException(
          String.format(
              "Failed to parse second part as residue number "
                  + "followed by optional insertion code: %s",
              split[1]));
    }

    try {
      final String chainIdentifier = split[0];
      final int residueNumber = Integer.parseInt(matcher.group(1));
      final String insertionCode = matcher.group(2).isEmpty() ? " " : matcher.group(2);

      final PdbResidueIdentifier identifier =
          new PdbResidueIdentifier(chainIdentifier, residueNumber, insertionCode);

      final int count = Integer.parseInt(split[2]);

      return new SelectionQuery(query, identifier, count);
    } catch (final NumberFormatException e) {
      throw new InvalidQueryException("Failed to parse given input as " + "integer number", e);
    }
  }

  private SelectionQuery(
      final String originalQuery, final PdbResidueIdentifier identifier, final int count) {
    super();
    this.originalQuery = originalQuery;
    this.identifier = identifier;
    this.count = count;
  }

  public PdbCompactFragment apply(final PdbModel model) {
    final List<PdbResidue> selectedResidues = new ArrayList<>(count);

    for (final PdbChain chain : model.getChains()) {
      final String queryChainIdentifier = identifier.getChainIdentifier();
      final String chainIdentifier = chain.getIdentifier();

      if (queryChainIdentifier.equals(chainIdentifier)) {
        boolean found = false;

        for (final PdbResidue residue : chain.getResidues()) {
          if (identifier.equals(residue.getResidueIdentifier())) {
            found = true;
          }
          if (found) {
            selectedResidues.add(residue);
          }
          if (selectedResidues.size() == count) {
            break;
          }
        }

        break;
      }
    }

    if (selectedResidues.size() != count) {
      throw new IllegalArgumentException("Failed to create selection: " + originalQuery);
    }

    return new PdbCompactFragment(originalQuery, selectedResidues);
  }
}
