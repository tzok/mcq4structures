package pl.poznan.put.alignment;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceScorerType;
import org.biojava.nbio.core.alignment.matrices.SimpleSubstitutionMatrix;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.AlignedSequence;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.RNASequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.compound.RNACompoundSet;
import org.biojava.nbio.core.sequence.template.AbstractCompound;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SequenceAligner {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SequenceAligner.class);

    private final List<PdbCompactFragment> fragments;
    private final boolean isGlobal;
    private final MoleculeType moleculeType;
    private final PairwiseSequenceScorerType type;
    private final SubstitutionMatrix<? extends AbstractCompound>
            substitutionMatrix;

    public SequenceAligner(List<PdbCompactFragment> fragments,
                           boolean isGlobal) {
        super();
        this.fragments = fragments;
        this.isGlobal = isGlobal;

        moleculeType = fragments.get(0).getMoleculeType();
        type = isGlobal ? PairwiseSequenceScorerType.GLOBAL
                        : PairwiseSequenceScorerType.LOCAL;
        substitutionMatrix = moleculeType == MoleculeType.RNA ? SequenceAligner
                .getRNASubstitutionMatrix() : SequenceAligner
                                     .getProteinSubstitutionMatrix();
    }

    private static SubstitutionMatrix<NucleotideCompound>
    getRNASubstitutionMatrix() {
        try (InputStreamReader reader = new InputStreamReader(
                SequenceAligner.class.getResourceAsStream(
                        "/pl/poznan/put" + "/alignment/NUC44.txt"), "UTF-8")) {
            return new SimpleSubstitutionMatrix<>(
                    RNACompoundSet.getRNACompoundSet(), reader, "NUC44");
        } catch (IOException e) {
            SequenceAligner.LOGGER
                    .error("Failed to load substitution matrix for RNA", e);
        }

        // warning, the default will not work with MSA for RNAs!
        return SubstitutionMatrixHelper.getNuc4_4();
    }

    private static SubstitutionMatrix<AminoAcidCompound> getProteinSubstitutionMatrix() {
        return SubstitutionMatrixHelper.getBlosum62();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SequenceAlignment align() throws CompoundNotFoundException {
        if (fragments.size() == 0) {
            return null;
        }

        List<AbstractSequence> sequences = new ArrayList<>();
        Map<AbstractSequence, PdbCompactFragment> mapSequenceName =
                new HashMap<>();

        for (PdbCompactFragment fragment : fragments) {
            String string = fragment.toSequence();
            AbstractSequence sequence;

            if (moleculeType == MoleculeType.RNA) {
                string = string.replace('T', 'U'); // FIXME
                sequence = new RNASequence(string);
            } else {
                sequence = new ProteinSequence(string);
            }

            sequences.add(sequence);
            mapSequenceName.put(sequence, fragment);
        }

        Profile profile = Alignments
                .getMultipleSequenceAlignment(sequences, substitutionMatrix,
                                              type);

        /*
         * Convert every sequence into an array of characters
         */
        List<? extends AlignedSequence> alignedSequences =
                profile.getAlignedSequences();
        char[][] sequencesAsChars = new char[alignedSequences.size()][];

        for (int i = 0; i < alignedSequences.size(); i++) {
            sequencesAsChars[i] =
                    alignedSequences.get(i).toString().toCharArray();
            assert sequencesAsChars[i].length == sequencesAsChars[0].length;
        }

        /*
         * Format alignment to clustalw
         */
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < sequencesAsChars[0].length; i += 60) {
            char[][] copy = new char[alignedSequences.size()][];

            for (int j = 0; j < alignedSequences.size(); j++) {
                copy[j] = Arrays.copyOfRange(sequencesAsChars[j], i,
                                             Math.min(i + 60,
                                                      sequencesAsChars[j]
                                                              .length));

                AlignedSequence alignedSequence = alignedSequences.get(j);
                AbstractSequence sequence = (AbstractSequence) alignedSequence
                        .getOriginalSequence();

                PdbCompactFragment fragment = mapSequenceName.get(sequence);
                String name = fragment.getName();
                name = name.substring(0, Math.min(name.length(), 11));

                builder.append(String.format("%-12s", name));
                builder.append(copy[j]);
                builder.append('\n');
            }

            builder.append("            ");

            for (int k = 0; k < copy[0].length; k++) {
                boolean flag = true;
                for (int j = 0; j < alignedSequences.size(); j++) {
                    if (copy[j][k] != copy[0][k]) {
                        flag = false;
                        break;
                    }
                }
                builder.append(flag ? '*' : ' ');
            }
            builder.append("\n\n");
        }

        String alignment = builder.toString();
        return new SequenceAlignment(isGlobal, alignment);
    }
}
