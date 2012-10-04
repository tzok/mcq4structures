package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.compound.RNACompoundSet;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

public class SequenceAligner<C extends Compound> {
    private static Logger logger = Logger.getLogger(SequenceAligner.class);
    private final Class<?> compound;
    private final HashMap<Chain, List<Group>> sequenceMap;

    public SequenceAligner(Class<?> clazz) {
        compound = clazz;
        sequenceMap = new HashMap<>();
    }

    /**
     * Align the sequences found in the given chain globally.
     * 
     * @param c1
     *            First chain.
     * @param c2
     *            Second chain.
     * @return A global alignment of the sequences.
     */
    public SequencePair<Sequence<C>, C> alignSequences(Chain c1, Chain c2) {
        return alignSequences(c1, c2, PairwiseSequenceAlignerType.GLOBAL);
    }

    /**
     * Align the sequences found in the given chain.
     * 
     * @param c1
     *            First chain.
     * @param c2
     *            Second chain.
     * @param type
     *            Type of alignment (global or local).
     * @return A global alignment of the sequences.
     */
    @SuppressWarnings("unchecked")
    public SequencePair<Sequence<C>, C> alignSequences(Chain c1, Chain c2,
            PairwiseSequenceAlignerType type) {
        /*
         * Parse sequences
         */
        Sequence<C> seq1 = getSequence(c1);
        Sequence<C> seq2 = getSequence(c2);
        SequenceAligner.logger.trace("Sequences to be aligned:\n" + seq1 + "\n"
                + seq2);
        /*
         * Prepare substitution matrices for the alignment
         */
        SubstitutionMatrix<C> matrix = null;
        if (compound.equals(NucleotideCompound.class)) {
            matrix = (SubstitutionMatrix<C>) SubstitutionMatrixHelper
                    .getNuc4_4();
        } else if (compound.equals(AminoAcidCompound.class)) {
            matrix = (SubstitutionMatrix<C>) SubstitutionMatrixHelper
                    .getBlosum62();
        } else {
            throw new IllegalArgumentException("Unknown sequence type");
        }
        /*
         * Align the sequences
         */
        SequencePair<Sequence<C>, C> alignment = Alignments
                .getPairwiseAlignment(seq1, seq2, type, new SimpleGapPenalty(),
                        matrix);
        SequenceAligner.logger.trace("Found alignment:\n" + alignment);
        return alignment;
    }

    public List<Group> getAtomGroups(Chain c) {
        return sequenceMap.get(c);
    }

    @SuppressWarnings("unchecked")
    private Sequence<C> getSequence(final Chain chain) {
        /*
         * Try to get sequence from parsed SEQRES entries
         */
        Sequence<?> sequence = chain.getBJSequence();
        if (sequence.getLength() != 0) {
            return (Sequence<C>) sequence;
        }
        /*
         * Iterate over the structure and prepare a sequence string in FASTA
         * format
         */
        SequenceAligner.logger.debug("Failed to parse SEQRES from PDB file. "
                + "Will attempt to get sequence manually");
        StringBuilder builder = new StringBuilder();
        List<Group> list = new Vector<>();
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
        SequenceAligner.logger.trace("Parsed sequence: " + seqString);
        sequenceMap.put(chain, list);
        /*
         * Create a Sequence object in correct type
         */
        if (compound.equals(NucleotideCompound.class)) {
            sequence = new RNASequence(seqString,
                    RNACompoundSet.getRNACompoundSet());
        } else if (compound.equals(AminoAcidCompound.class)) {
            sequence = new ProteinSequence(seqString,
                    AminoAcidCompoundSet.getAminoAcidCompoundSet());
        } else {
            throw new IllegalArgumentException("Unknown sequence type");
        }
        return (Sequence<C>) sequence;
    }
}
