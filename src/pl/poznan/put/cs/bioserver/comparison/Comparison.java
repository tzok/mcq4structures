
package pl.poznan.put.cs.bioserver.comparison;

import org.biojava.bio.structure.Structure;

/**
 * An abstract class to represent every class that does structure comparison.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class Comparison {
    /**
     * An example validity check which globally verifies that chains and groups
     * counts are the same along whole structures.
     * 
     * @param structures An array of structures to be checked
     * @throws IncomparableStructuresException If two structures cannot be
     *             compared.
     */
    protected void checkValidity(Structure[] structures)
            throws IncomparableStructuresException {
        // FIXME
        return;
        // MoleculeInfo[] molecules = new MoleculeInfo[structures.length];
        // int i = 0;
        // for (Structure s : structures)
        // molecules[i++] = new MoleculeInfo(s);
        //
        // String name1 = molecules[0].name;
        // int size1 = molecules[0].size;
        // for (i = 1; i < molecules.length; ++i) {
        // String name2 = molecules[i].name;
        //
        // int size2 = molecules[i].size;
        // if (size1 != size2)
        // throw new IncomparableStructuresException(String.format(
        // "Chain count mismatch.\n"
        // + "%s [chains: %d] vs %s [chains: %d]", name1,
        // size1, name2, size2));
        //
        // boolean flag = true;
        // for (int k = 0; k < 2; ++k)
        // for (int j = 0; j < size1; ++j) {
        // int groups1 = molecules[0].groups[k][j];
        // int groups2 = molecules[i].groups[k][j];
        // if (groups1 != groups2)
        // throw new IncomparableStructuresException(
        // String.format(errorMessages[k], name1, j,
        // groups1, name2, j, groups2));
        // // check if both amino acid and nucleotide count is > 0
        // flag &= groups1 == 0;
        // }
        //
        // if (flag)
        // throw new IncomparableStructuresException(
        // "Both structures contain zero amino "
        // + "acid and nucleotides groups");
        // }
    }
}
