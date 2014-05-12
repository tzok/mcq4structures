package pl.poznan.put.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.comparison.MCQ;

class BenchmarkReference {
    static List<File> list(File directory) {
        List<File> list = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                list.addAll(BenchmarkReference.list(file));
            } else {
                if (file.getName().endsWith(".pdb")) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public static void main(String[] args) {
        List<File> pdbs =
                BenchmarkReference.list(new File("/home/tzok/pdb/puzzles/"));
        List<Structure> structures = new ArrayList<>();
        PDBFileReader reader = new PDBFileReader();
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures.add(reader.getStructure(pdbs.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        double[][] result = new MCQ().compare(structures, null);
        for (double[] row : result) {
            System.out.println(Arrays.toString(row));
        }
    }

}
