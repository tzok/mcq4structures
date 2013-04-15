package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;

import pl.poznan.put.cs.bioserver.jna.ClustalW;

public class ClustalWSandbox {
    public static void main(String[] args) throws IOException {
        File fileIn = new File("/tmp/input.fasta");
        File fileOut = File.createTempFile("clustalw", ".aln");
        String[] argv = new String[] { "clustalw", "-infile=" + fileIn,
                "-outfile=" + fileOut, "-align", "-seqnos=on" };
        ClustalW.INSTANCE.main(argv.length, argv);
    }
}
