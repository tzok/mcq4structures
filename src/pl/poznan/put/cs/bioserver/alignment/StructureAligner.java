package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StrucAligParameters;
import org.biojava.bio.structure.align.StructurePairAligner;

import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

public class StructureAligner {
    private static Logger LOGGER = Logger.getLogger(StructureAligner.class);
    private static Map<AlignmentInput, AlignmentOutput> cache = new HashMap<>();

    public static AlignmentOutput align(Chain c1, Chain c2)
            throws StructureException {
        StructureImpl s1 = new StructureImpl(c1);
        StructureImpl s2 = new StructureImpl(c2);
        return align(s1, s2);
    }

    public static AlignmentOutput align(Structure s1, Structure s2)
            throws StructureException {
        /*
         * Check if alignment was made before
         */
        AlignmentInput input = new AlignmentInput(s1, s2);
        if (StructureAligner.cache.containsKey(input)) {
            StructureAligner.LOGGER.info("Reusing alignment data from cache");
            return StructureAligner.cache.get(input);
        }

        /*
         * Align the structures
         */
        Atom[][] atoms = Helper.getCommonAtomArray(s1, s2,
                NucleotideDihedral.USED_ATOMS);

        StructurePairAligner aligner = new StructurePairAligner();
        aligner.align(atoms[0], atoms[1], new StrucAligParameters());
        AlignmentOutput result = new AlignmentOutput(aligner, s1, s2, atoms);
        StructureAligner.cache.put(input, result);
        return result;
    }
}
