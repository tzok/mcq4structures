package pl.poznan.put.cs.bioserver.comparison;

import org.biojava.bio.structure.Structure;

/**
 * An abstraction for any local comparison measure.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class LocalComparison {
    /**
     * Compare two strucures using local measure. The result is an arbitrary
     * object, not limited to specific case.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return An object representing differences between structures on local
     *         level.
     * @throws IncomparableStructuresException
     *             If the two structures could not be compared.
     */
    public abstract Object compare(Structure s1, Structure s2)
            throws IncomparableStructuresException;
}
