package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StrucAligParameters;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.model.AFPChain;

import pl.poznan.put.cs.bioserver.helper.Helper;

/**
 * A class that allows to computer structural alignment.
 * 
 * @author tzok
 */
public final class StructureAligner {
    private static final Logger LOGGER = Logger
            .getLogger(StructureAligner.class);
    private static Map<AlignmentInput, AlignmentOutput> cache = new HashMap<>();

    /**
     * Align structurally two chains.
     * 
     * @param c1
     *            First chain.
     * @param c2
     *            Second chain
     * @return An object with all information about computed alignment.
     * @throws StructureException
     *             If there were problems during alignment computation.
     */
    public static AlignmentOutput align(Chain c1, Chain c2)
            throws StructureException {
        StructureImpl s1 = new StructureImpl(c1);
        StructureImpl s2 = new StructureImpl(c2);
        return StructureAligner.align(s1, s2);
    }

    /**
     * Align structurally two structures.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return An object with all information about computed alignment.
     * @throws StructureException
     *             If there were problems during alignment computation.
     */
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

        Atom[][] atoms = new Atom[2][];
        List<Atom> list = Helper.getAtomArray(s1, new String[] { "P", "CA" });
        for (Atom a : list) {
            if (a.getName().equals("P")) {
                a.setName("CA");
                a.setFullName(" CA ");
            }
        }
        atoms[0] = list.toArray(new Atom[list.size()]);

        list = Helper.getAtomArray(s2, new String[] { "P", "CA" });
        for (Atom a : list) {
            if (a.getName().equals("P")) {
                a.setName("CA");
                a.setFullName(" CA ");
            }
        }
        atoms[1] = list.toArray(new Atom[list.size()]);

        StructureAlignment alignment = new CeMain();
        AFPChain align = alignment.align(atoms[0], atoms[1]);
        AlignmentOutput result = new AlignmentOutput(align, s1, s2, atoms);
        StructureAligner.cache.put(input, result);
        return result;
    }

    private StructureAligner() {
    }
}
