package pl.poznan.put.cs.bioserver.comparison;

import org.biojava.bio.structure.Structure;

/**
 * An abstraction for any local comparison measure.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class LocalComparison {
    public abstract Object compare(Structure s1, Structure s2)
            throws IncomparableStructuresException;
}
