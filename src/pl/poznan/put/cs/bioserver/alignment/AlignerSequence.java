package pl.poznan.put.cs.bioserver.alignment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.ProfileProfileAlignerType;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.SubstitutionMatrixHelper;
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
        ProfileProfileAlignerType type = isGlobal ? ProfileProfileAlignerType.GLOBAL
                : ProfileProfileAlignerType.LOCAL;

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

    private AlignerSequence() {
    }
}
