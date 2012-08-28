package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StrucAligParameters;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;

import pl.poznan.put.cs.bioserver.gui.PdbManager;
import pl.poznan.put.cs.bioserver.helper.Helper;

public class StructureAligner {
    private static Logger LOGGER = Logger.getLogger(StructureAligner.class);

    public static Chain[] align(Chain c1, Chain c2) throws StructureException {
        StructurePairAligner aligner = new StructurePairAligner();
        if (Helper.isNucleicAcid(c1)) {
            StrucAligParameters parameters = new StrucAligParameters();
            parameters.setUsedAtomNames(new String[] { " C1'", " C2 ", " C2'",
                    " C3'", " C4 ", " C4'", " C5 ", " C5'", " C6 ", " N1 ",
                    " N3 ", " O2'", " O3'", " O4'", " O5'", " OP1", " OP2",
                    " P  " });
            aligner.setParams(parameters);
        }

        StructureImpl s1 = new StructureImpl(c1);
        StructureImpl s2 = new StructureImpl(c2);
        aligner.align(s1, s2);
        AlternativeAlignment alignment = aligner.getAlignments()[0];
        Structure structure = alignment.getAlignedStructure(s1, s2);

        Chain[] chains = new Chain[4];
        chains[0] = structure.getModel(0).get(0);
        chains[1] = structure.getModel(1).get(0);
        chains[2] = (Chain) chains[0].clone();
        chains[3] = (Chain) chains[1].clone();

        String[][] residues = new String[][] { alignment.getPDBresnum1(),
                alignment.getPDBresnum2() };
        chains[2].setAtomGroups(StructureAligner.filterGroups(chains[0],
                residues[0]));
        chains[3].setAtomGroups(StructureAligner.filterGroups(chains[1],
                residues[1]));

        if (StructureAligner.LOGGER.isTraceEnabled()) {
            String[] numerals = new String[] { "1st", "2nd" };
            for (int i = 0; i < 2; i++) {
                StringBuilder builder = new StringBuilder();
                for (String residueNumber : residues[i]) {
                    builder.append(residueNumber);
                    builder.append('\t');
                }
                StructureAligner.LOGGER.trace("Residues aligned from "
                        + numerals[i] + " structure: " + builder.toString());
            }
        }

        PdbManager.putAlignmentInfo(new Chain[] { c1, c2 }, residues);
        return chains;
    }

    public static Structure[] align(Structure s1, Structure s2)
            throws StructureException {
        StructureAligner.LOGGER.info("Aligning the following structures: "
                + s1.getPDBCode() + " and " + s2.getPDBCode());
        Set<String> c1 = new TreeSet<>();
        Set<String> c2 = new TreeSet<>();
        for (Chain c : s1.getChains())
            c1.add(c.getChainID());
        for (Chain c : s2.getChains())
            c2.add(c.getChainID());
        c1.retainAll(c2);
        if (StructureAligner.LOGGER.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            for (String chainName : c1) {
                builder.append(chainName);
                builder.append(' ');
            }
            StructureAligner.LOGGER
                    .debug("The following chain names are common for both "
                            + "structures: " + builder.toString());
        }

        Chain[][] chains = new Chain[c1.size()][];
        int i = 0;
        for (String id : c1) {
            chains[i++] = StructureAligner.align(s1.getChainByPDB(id),
                    s2.getChainByPDB(id));
            StructureAligner.LOGGER.trace("Aligned chain: " + id);
        }

        Structure[] structures = new Structure[] { s1.clone(), s2.clone(),
                s1.clone(), s2.clone() };
        for (i = 0; i < 4; i++) {
            Vector<Chain> vector = new Vector<>();
            for (Chain[] chain : chains)
                vector.add(chain[i]);
            structures[i].setChains(vector);
        }
        return structures;
    }

    private static List<Group> filterGroups(Chain c1, String[] indices) {
        Set<Integer> set = new HashSet<>();
        for (String s : indices)
            set.add(Integer.valueOf(s.split(":")[0]));

        List<Group> list = new Vector<>();
        for (Group g : c1.getAtomGroups())
            if (set.contains(g.getResidueNumber().getSeqNum()))
                list.add(g);
        return list;
    }
}
