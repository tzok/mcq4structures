package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.compound.RNACompoundSet;
import org.biojava3.core.sequence.io.FastaReaderHelper;
import org.biojava3.core.sequence.template.Sequence;

public class MSA {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) throws IOException, Exception {
        List<ProteinSequence> sequences = new ArrayList<>();
        for (String id : new String[] { "Q21691", "Q21495", "O48771" }) {
            URL uniprotFasta = new URL(String.format(
                    "http://www.uniprot.org/uniprot/%s.fasta", id));
            ProteinSequence seq = FastaReaderHelper.readFastaProteinSequence(
                    uniprotFasta.openStream()).get(id);
            sequences.add(seq);
        }

        System.out.println(Alignments.getMultipleSequenceAlignment(sequences));

        List<Sequence> list = new ArrayList<>();
        list.add(new DNASequence("ATTTGGGAATTCCC"));
        list.add(new DNASequence("ATTTAGCTAACGCC"));
        list.add(new DNASequence("ATGCATGCATGCGCGCGCGC"));
        System.out.println(Alignments.getMultipleSequenceAlignment(list));

        SubstitutionMatrix<NucleotideCompound> matrix;
        try (InputStreamReader reader = new InputStreamReader(
                MSA.class
                        .getResourceAsStream("/pl/poznan/put/cs/bioserver/alignment/NUC44.txt"))) {
            matrix = new SimpleSubstitutionMatrix<>(
                    RNACompoundSet.getRNACompoundSet(), reader, "NUC44");
        }

        list.clear();
        list.add(new RNASequence("GAACCUUGGA"));
        list.add(new RNASequence("AUUGGUAGAGAUGAUGAGAUCCCA"));
        list.add(new RNASequence("AUUUCGCGCUACGC"));
        System.out.println(Alignments
                .getMultipleSequenceAlignment(list, matrix));
    }
}
