package pl.poznan.put.cs.bioserver.comparison;

import org.biojava.bio.structure.Structure;

/**
 * An abstraction of all global comparison measures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class GlobalComparison {
    /**
     * Compare two structures.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return Distance between the structures according to some measure.
     * @throws IncomparableStructuresException
     *             If the two structure could not be compared.
     */
    public abstract double compare(Structure s1, Structure s2)
            throws IncomparableStructuresException;

    /**
     * Compare each structures with each other.
     * 
     * @param structures
     *            An array of structures to be compared.
     * @return A distance matrix.
     * @throws IncomparableStructuresException
     *             If any two structures were not comparable.
     */
    public double[][] compare(Structure[] structures)
            throws IncomparableStructuresException {
        double[][] result = new double[structures.length][];
        for (int i = 0; i < structures.length; ++i) {
            result[i] = new double[structures.length];
        }
        for (int i = 0; i < structures.length; ++i) {
            for (int j = i + 1; j < structures.length; ++j) {
                double value = compare(structures[i], structures[j]);
                result[i][j] = value;
                result[j][i] = value;
            }
        }
        return result;
    }
}
