package pl.poznan.put.cs.bioserver.alignment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Element;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.ce.CeParameters;
import org.biojava.bio.structure.align.model.AFPChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

/**
 * A class that allows to computer structural alignment.
 * 
 * @author tzok
 */
public final class AlignerStructure {
    private static Map<AlignmentInput, AlignmentOutput> cache = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AlignerStructure.class);

    /**
     * Align structurally two structures.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @param description
     * @return An object with all information about computed alignment.
     * @throws StructureException
     *             If there were problems during alignment computation.
     */
    public static AlignmentOutput align(Structure left, Structure right,
            String description) throws StructureException {
        /*
         * Check if alignment was made before
         */
        AlignmentInput input = new AlignmentInput(left, right);
        if (AlignerStructure.cache.containsKey(input)) {
            AlignerStructure.LOGGER.info("Reusing alignment data from cache");
            return AlignerStructure.cache.get(input);
        }

        Set<Atom> changedAtoms = new HashSet<>();
        List<Atom> listLeft = AlignerStructure.changePToCA(left, changedAtoms);
        List<Atom> listRight = AlignerStructure
                .changePToCA(right, changedAtoms);

        StructureAlignment alignment = new CeMain();
        CeParameters parameters = new CeParameters();
        while (true) {
            AFPChain align = alignment.align(
                    listLeft.toArray(new Atom[listLeft.size()]),
                    listRight.toArray(new Atom[listRight.size()]), parameters);
            if (align.getBlockRotationMatrix().length == 0) {
                int winSize = parameters.getWinSize();
                winSize--;
                if (winSize <= 0) {
                    throw new StructureException(
                            "Could not find structure alignment");
                }
                parameters.setWinSize(winSize);
                continue;
            }

            for (Atom atom : changedAtoms) {
                atom.setName("P");
                atom.setFullName(" P  ");
            }
            AlignmentOutput result = new AlignmentOutput(align, left, right,
                    listLeft, listRight, description);
            AlignerStructure.cache.put(input, result);
            return result;
        }
    }

    private static List<Atom> changePToCA(Structure structure,
            Set<Atom> changedAtoms) {
        List<Atom> list = Helper.getAtomArray(structure,
                Arrays.asList(new String[] { "P", "CA" }));
        assert list.size() != 0 : "There are no P or CA atoms in: "
                + StructureManager.getName(structure);

        for (Atom atom : list) {
            assert atom != null;
            if (atom.getElement().equals(Element.P)) {
                atom.setName("CA");
                atom.setFullName(" CA ");
                changedAtoms.add(atom);
            }
        }
        return list;
    }

    private AlignerStructure() {
    }
}
