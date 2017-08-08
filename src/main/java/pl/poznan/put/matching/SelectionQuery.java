package pl.poznan.put.matching;

import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.ResidueCollection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectionQuery {
    /* @formatter:off
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
     *   2: insertion code or null
     *
     * @formatter:on
     */
    private static final Pattern PATTERN = Pattern.compile("(-?\\d+)(.*)");

    private final PdbResidueIdentifier identifier;
    private final int count;

    public static SelectionQuery parse(final String query) {
        final String[] split = query.split(":");

        if (split.length != 3) {
            throw new InvalidQueryException(
                    "Selection Query must consist of three parts separated by" +
                    " colons. For example: A:1:5 means 5 residues starting " +
                    "from residue 1 from chain A.");
        }

        final Matcher matcher = SelectionQuery.PATTERN.matcher(split[1]);
        if (!matcher.matches() || (matcher.groupCount() != 2)) {
            throw new InvalidQueryException(String.format(
                    "Failed to parse second part as residue number " +
                    "followed by optional insertion code: %s",
                    split[1]));
        }

        try {
            final String chainIdentifier = split[0];
            final int residueNumber = Integer.parseInt(matcher.group(1));
            final String insertionCode = matcher.group(2);
            final PdbResidueIdentifier identifier =
                    new PdbResidueIdentifier(chainIdentifier, residueNumber,
                                             insertionCode);

            final int count = Integer.parseInt(split[2]);

            return new SelectionQuery(identifier, count);
        } catch (final NumberFormatException e) {
            throw new InvalidQueryException("Failed to parse given input as " +
                                            "integer number", e);
        }
    }

    public SelectionQuery(final PdbResidueIdentifier identifier,
                          final int count) {
        this.identifier = identifier;
        this.count = count;
    }

    public ResidueCollection apply(final PdbModel model) {
        // FIXME TODO
        return null;
    }
}
