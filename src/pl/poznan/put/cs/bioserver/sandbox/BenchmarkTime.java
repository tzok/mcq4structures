package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.comparison.MCQ;

public class BenchmarkTime {
    public static void main(String[] args) {
        List<File> pdbs = list(new File("/home/tzok/pdb/puzzles/"));
        pdbs.addAll(list(new File("/home/tzok/pdb/puzzles/")));
        Structure[] structures = new Structure[pdbs.size()];
        PDBFileReader reader = new PDBFileReader();
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures[i] = reader.getStructure(pdbs.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            new MCQ().compare(structures, null);
            System.out.println("Time: " + (System.currentTimeMillis() - start)
                    + " ms");
        }
    }

    public static List<File> list(File directory) {
        List<File> list = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                list.addAll(list(file));
            } else {
                if (file.getName().endsWith(".pdb")) {
                    list.add(file);
                }
            }
        }
        return list;
    }

}
