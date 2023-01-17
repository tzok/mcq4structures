package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import pl.poznan.put.pdb.ImmutablePdbResidueIdentifier;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;

/**
 * Class which allow to parse a selection query of the following form CHAIN:NUMBER-ICODE:COUNT e.g.
 * A:1:5. Additionally, several selections can be supplied using plus operator like A:1:5+A:10:2
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
  private final List<Pair<PdbResidueIdentifier, Integer>> identifierCountPairs;

  private SelectionQuery(
      final String originalQuery,
      final List<Pair<PdbResidueIdentifier, Integer>> identifierCountPairs) {
    super();
    this.originalQuery = originalQuery;
    this.identifierCountPairs = new ArrayList<>(identifierCountPairs);
  }

  public static SelectionQuery parse(final String query) {
    final List<Pair<PdbResidueIdentifier, Integer>> pairs = new ArrayList<>();

    for (final String subquery : query.split("\\+")) {
      final String[] split = subquery.split(":");
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
        final Optional<String> insertionCode =
            matcher.group(2).isEmpty() ? Optional.empty() : Optional.of(matcher.group(2));

        final PdbResidueIdentifier identifier =
            ImmutablePdbResidueIdentifier.of(chainIdentifier, residueNumber, insertionCode);
        final int count = Integer.parseInt(split[2]);

        pairs.add(Pair.of(identifier, count));
      } catch (final NumberFormatException e) {
        throw new InvalidQueryException("Failed to parse given input as integer number", e);
      }
    }

    return new SelectionQuery(query, pairs);
  }

  public List<PdbCompactFragment> apply(final PdbModel model) {
    final List<PdbCompactFragment> fragments = new ArrayList<>();

    for (final Pair<PdbResidueIdentifier, Integer> pair : identifierCountPairs) {
      final PdbResidueIdentifier identifier = pair.getKey();
      final int count = pair.getValue();
      final List<PdbResidue> selectedResidues = new ArrayList<>(count);
      boolean found = false;

      for (final PdbChain chain : model.chains()) {
        final String queryChainIdentifier = identifier.chainIdentifier();
        final String chainIdentifier = chain.identifier();

        if (queryChainIdentifier.equals(chainIdentifier)) {
          for (final PdbResidue residue : chain.residues()) {
            if (identifier.equals(residue.identifier())) {
              found = true;
            }
            if (found) {
              selectedResidues.add(residue);
            }
            if (selectedResidues.size() == count) {
              break;
            }
          }
        }
      }

      if (selectedResidues.size() != count) {
        throw new InvalidSelectionException(
            String.format(
                "Failed to create selection starting at %s with %d residues. Was able to select"
                    + " only %d residues",
                identifier, count, selectedResidues.size()));
      }

      fragments.add(ImmutablePdbCompactFragment.of(selectedResidues).withName(originalQuery));
    }

    return fragments;
  }
}
