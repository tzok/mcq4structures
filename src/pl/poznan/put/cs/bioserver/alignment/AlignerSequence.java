package pl.poznan.put.cs.bioserver.alignment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceScorerType;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.Profile;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.compound.RNACompoundSet;
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
    public static Profile<Sequence<Compound>, Compound> align(Chain[] chains,
            boolean isGlobal) {
        List<Sequence<Compound>> sequences = new ArrayList<>();
        for (Chain c : chains) {
            sequences.add((Sequence<Compound>) AlignerSequence.getSequence(c));
        }

        SubstitutionMatrix<? extends Compound> matrix = AlignerSequence
                .getSubstitutionMatrix(chains[0]);
        PairwiseSequenceScorerType type = isGlobal ? PairwiseSequenceScorerType.GLOBAL
                : PairwiseSequenceScorerType.LOCAL;

        return Alignments.getMultipleSequenceAlignment(sequences, matrix, type);
    }

    private static SubstitutionMatrix<? extends Compound> getProteinSubsitutionMatrix() {
        return SubstitutionMatrixHelper.getBlosum62();
    }

    private static SubstitutionMatrix<NucleotideCompound> getRNASubstitutionMatrix() {
        try (InputStreamReader reader = new InputStreamReader(
                AlignerSequence.class.getResourceAsStream("/pl/poznan/put/cs/"
                        + "bioserver/alignment/NUC44.txt"))) {
            return new SimpleSubstitutionMatrix<>(
                    RNACompoundSet.getRNACompoundSet(), reader, "NUC44");
        } catch (IOException e) {
            AlignerSequence.logger.error(
                    "Failed to load substitution matrix for RNA", e);
        }

        // warning, the default will not work with MSA for RNAs!
        return SubstitutionMatrixHelper.getNuc4_4();
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

    private static SubstitutionMatrix<? extends Compound> getSubstitutionMatrix(
            Chain chain) {
        if (Helper.isNucleicAcid(chain)) {
            return AlignerSequence.getRNASubstitutionMatrix();
        }
        return AlignerSequence.getProteinSubsitutionMatrix();
    }

    public static String toClustalFormat(
            Profile<Sequence<Compound>, Compound> profile, Chain[] chains) {
        final String[] names = new String[chains.length];
        for (int i = 0; i < chains.length; i++) {
            Chain chain = chains[i];
            names[i] = StructureManager.getName(chain.getParent()) + "."
                    + chain.getChainID();
        }

        List<AlignedSequence<Sequence<Compound>, Compound>> list = profile
                .getAlignedSequences();
        char[][] sequences = new char[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            sequences[i] = list.get(i).toString().toCharArray();
            assert sequences[i].length == sequences[0].length;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sequences[0].length; i += 60) {
            char[][] copy = new char[list.size()][];
            for (int j = 0; j < list.size(); j++) {
                copy[j] = Arrays.copyOfRange(sequences[j], i,
                        Math.min(i + 60, sequences[j].length));
                String name = names[j].substring(0,
                        Math.min(names[j].length(), 11));
                builder.append(String.format("%-12s", name));
                builder.append(copy[j]);
                builder.append('\n');
            }
            builder.append("            ");
            for (int k = 0; k < copy[0].length; k++) {
                boolean flag = true;
                for (int j = 0; j < list.size(); j++) {
                    if (copy[j][k] != copy[0][k]) {
                        flag = false;
                        break;
                    }
                }
                builder.append(flag ? '*' : ' ');
            }
            builder.append("\n\n");
        }
        return builder.toString();
    }

    private AlignerSequence() {
    }
}
