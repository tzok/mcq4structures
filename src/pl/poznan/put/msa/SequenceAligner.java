package pl.poznan.put.msa;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceScorerType;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.Profile;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.compound.RNACompoundSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Sequence;

/**
 * A class which allows to compute a global or local sequence alignment.
 * 
 * @author tzok
 * 
 */
public final class SequenceAligner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceAligner.class);

    // FIXME: rewrite all of this!
    public static SequenceAlignment align(List<CompactFragment> fragments,
            boolean isGlobal) {
        if (fragments.size() == 0) {
            return null;
        }

        PairwiseSequenceScorerType type = isGlobal ? PairwiseSequenceScorerType.GLOBAL
                : PairwiseSequenceScorerType.LOCAL;

        if (fragments.get(0).getMoleculeType() == MoleculeType.RNA) {
            List<RNASequence> list = new ArrayList<>();
            Map<RNASequence, CompactFragment> map = new HashMap<>();

            for (CompactFragment fragment : fragments) {
                String string = Sequence.fromCompactFragment(fragment).toString();
                string = string.replace('T', 'U'); // FIXME
                RNASequence rnaSequence = new RNASequence(string);
                list.add(rnaSequence);
                map.put(rnaSequence, fragment);
            }

            SubstitutionMatrix<NucleotideCompound> matrix = getRNASubstitutionMatrix();
            Profile<RNASequence, NucleotideCompound> profile = Alignments.getMultipleSequenceAlignment(
                    list, matrix, type);

            /*
             * get name of every structure and chain
             */
            Map<CompactFragment, String> mapChainToName = new HashMap<>();
            for (CompactFragment chain : fragments) {
                mapChainToName.put(chain, chain.getParentName());
            }

            /*
             * prepare a title (names separated with comma)
             */
            StringBuilder builder = new StringBuilder();
            for (String name : mapChainToName.values()) {
                builder.append(name);
                builder.append(", ");
            }

            builder.delete(builder.length() - 2, builder.length());
            String title = builder.toString();

            /*
             * convert every sequence into an array of characters
             */
            List<AlignedSequence<RNASequence, NucleotideCompound>> list2 = profile.getAlignedSequences();
            char[][] sequences = new char[list2.size()][];
            for (int i = 0; i < list2.size(); i++) {
                sequences[i] = list2.get(i).toString().toCharArray();
                assert sequences[i].length == sequences[0].length;
            }

            /*
             * format alignment to clustalw
             */
            builder = new StringBuilder();
            for (int i = 0; i < sequences[0].length; i += 60) {
                char[][] copy = new char[list2.size()][];
                for (int j = 0; j < list2.size(); j++) {
                    copy[j] = Arrays.copyOfRange(sequences[j], i,
                            Math.min(i + 60, sequences[j].length));

                    AlignedSequence<RNASequence, NucleotideCompound> alignedSequence = list2.get(j);
                    RNASequence sequence = alignedSequence.getOriginalSequence();
                    CompactFragment chain = map.get(sequence);
                    String name = mapChainToName.get(chain);
                    name = name.substring(0, Math.min(name.length(), 11));
                    builder.append(String.format("%-12s", name));
                    builder.append(copy[j]);
                    builder.append('\n');
                }
                builder.append("            ");
                for (int k = 0; k < copy[0].length; k++) {
                    boolean flag = true;
                    for (int j = 0; j < list2.size(); j++) {
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
            return new SequenceAlignment(isGlobal, alignment, title);
        }

        if (fragments.get(0).getMoleculeType() == MoleculeType.PROTEIN) {
            List<ProteinSequence> list = new ArrayList<>();
            Map<ProteinSequence, CompactFragment> map = new HashMap<>();

            for (CompactFragment fragment : fragments) {
                String string = Sequence.fromCompactFragment(fragment).toString();
                ProteinSequence proteinSequence = new ProteinSequence(string);
                list.add(proteinSequence);
                map.put(proteinSequence, fragment);
            }

            SubstitutionMatrix<AminoAcidCompound> matrix = getProteinSubstitutionMatrix();
            Profile<ProteinSequence, AminoAcidCompound> profile = Alignments.getMultipleSequenceAlignment(
                    list, matrix, type);

            /*
             * get name of every structure and chain
             */
            Map<CompactFragment, String> mapChainToName = new HashMap<>();
            for (CompactFragment chain : fragments) {
                mapChainToName.put(chain, chain.getParentName());
            }

            /*
             * prepare a title (names separated with comma)
             */
            StringBuilder builder = new StringBuilder();
            for (String name : mapChainToName.values()) {
                builder.append(name);
                builder.append(", ");
            }

            builder.delete(builder.length() - 2, builder.length());
            String title = builder.toString();

            /*
             * convert every sequence into an array of characters
             */
            List<AlignedSequence<ProteinSequence, AminoAcidCompound>> list2 = profile.getAlignedSequences();
            char[][] sequences = new char[list2.size()][];
            for (int i = 0; i < list2.size(); i++) {
                sequences[i] = list2.get(i).toString().toCharArray();
                assert sequences[i].length == sequences[0].length;
            }

            /*
             * format alignment to clustalw
             */
            builder = new StringBuilder();
            for (int i = 0; i < sequences[0].length; i += 60) {
                char[][] copy = new char[list2.size()][];
                for (int j = 0; j < list2.size(); j++) {
                    copy[j] = Arrays.copyOfRange(sequences[j], i,
                            Math.min(i + 60, sequences[j].length));

                    AlignedSequence<ProteinSequence, AminoAcidCompound> alignedSequence = list2.get(j);
                    ProteinSequence sequence = alignedSequence.getOriginalSequence();
                    CompactFragment chain = map.get(sequence);
                    String name = mapChainToName.get(chain);
                    name = name.substring(0, Math.min(name.length(), 11));
                    builder.append(String.format("%-12s", name));
                    builder.append(copy[j]);
                    builder.append('\n');
                }
                builder.append("            ");
                for (int k = 0; k < copy[0].length; k++) {
                    boolean flag = true;
                    for (int j = 0; j < list2.size(); j++) {
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
            return new SequenceAlignment(isGlobal, alignment, title);
        }

        return null;
    }

    private static SubstitutionMatrix<NucleotideCompound> getRNASubstitutionMatrix() {
        try (InputStreamReader reader = new InputStreamReader(
                SequenceAligner.class.getResourceAsStream("/pl/poznan/put"
                        + "/alignment/NUC44.txt"), "UTF-8")) {
            return new SimpleSubstitutionMatrix<>(
                    RNACompoundSet.getRNACompoundSet(), reader, "NUC44");
        } catch (IOException e) {
            SequenceAligner.LOGGER.error(
                    "Failed to load substitution matrix for RNA", e);
        }

        // warning, the default will not work with MSA for RNAs!
        return SubstitutionMatrixHelper.getNuc4_4();
    }

    private static SubstitutionMatrix<AminoAcidCompound> getProteinSubstitutionMatrix() {
        return SubstitutionMatrixHelper.getBlosum62();
    }

    private SequenceAligner() {
    }
}
