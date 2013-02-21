package pl.poznan.put.cs.bioserver.alignment;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava3.alignment.NeedlemanWunsch;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SmithWaterman;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.AbstractPairwiseSequenceAligner;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

/**
 * A class which allows to compute a global or local sequence alignment.
 * 
 * @author tzok
 * 
 */
public final class AlignerSequence {
    private static Logger logger = LoggerFactory
            .getLogger(AlignerSequence.class);

    @SuppressWarnings("unchecked")
    public static OutputAlignSeq align(Chain c1, Chain c2, boolean isGlobal) {
        /*
         * Parse sequences
         */
        Sequence<? extends Compound> query = AlignerSequence.getSequence(c1);
        Sequence<? extends Compound> target = AlignerSequence.getSequence(c2);
        if (query.getLength() == 0 || target.getLength() == 0) {
            AlignerSequence.logger.warn("At least one chain has 0 residues");
            return null;
        }
        AlignerSequence.logger.trace("Sequences to be aligned:\n" + query
                + "\n" + target);
        /*
         * Prepare substitution matrices for the alignment
         */
        SubstitutionMatrix<? extends Compound> matrix;
        if (Helper.isNucleicAcid(c1)) {
            matrix = SubstitutionMatrixHelper.getNuc4_4();
        } else {
            matrix = SubstitutionMatrixHelper.getBlosum62();
        }
        /*
         * Align the sequences
         */
        AbstractPairwiseSequenceAligner<Sequence<Compound>, Compound> aligner;
        if (isGlobal) {
            aligner = new NeedlemanWunsch<>();
        } else {
            aligner = new SmithWaterman<>();
        }
        aligner.setQuery((Sequence<Compound>) query);
        aligner.setTarget((Sequence<Compound>) target);
        aligner.setGapPenalty(new SimpleGapPenalty());
        aligner.setSubstitutionMatrix((SubstitutionMatrix<Compound>) matrix);

        Structure[] structures = new Structure[] { c1.getParent(),
                c2.getParent() };
        String[] names = StructureManager.getNames(structures);
        names[0] += "." + c1.getChainID();
        names[1] += "." + c2.getChainID();
        return new OutputAlignSeq(aligner, names);
    }

    private static Sequence<? extends Compound> getSequence(Chain chain) {
        /*
         * Try to get sequence from parsed SEQRES entries
         */
        Sequence<?> sequence = chain.getBJSequence();
        if (sequence.getLength() != 0) {
            return sequence;
        }
        /*
         * Iterate over the structure and prepare a sequence string in FASTA
         * format
         */
        AlignerSequence.logger.debug("Failed to parse SEQRES from PDB file. "
                + "Will attempt to get sequence manually");
        StringBuilder builder = new StringBuilder();
        List<Group> list = new ArrayList<>();
        for (Group g : chain.getAtomGroups()) {
            String type = g.getType();
            if (type.equals("nucleotide") || type.equals("amino")
                    || g.hasAminoAtoms() || g.hasAtom("P")) {
                String fasta = g.getChemComp().getOne_letter_code();
                if (fasta.equals("?")) {
                    fasta = g.getPDBName();
                    fasta = fasta.substring(fasta.length() - 1, fasta.length());
                }
                builder.append(fasta);
                list.add(g);
            }
        }
        String seqString = builder.toString();
        AlignerSequence.logger.trace("Parsed sequence: " + seqString);
        /*
         * Create a Sequence object in correct type
         */
        if (Helper.isNucleicAcid(chain)) {
            sequence = new RNASequence(seqString);
        } else {
            sequence = new ProteinSequence(seqString);
        }
        return sequence;
    }

    private AlignerSequence() {
    }
}
