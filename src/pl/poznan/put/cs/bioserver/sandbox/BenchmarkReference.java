package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

public class BenchmarkReference {
    public static void main(String[] args) {
        List<File> pdbs = new ArrayList<>();
        // pdbs.addAll(list(new File("/home/tzok/pdb/puzzles/")));
        pdbs.addAll(list(new File(
                "/home/tzok/pdb/puzzles/Challenge3/1/targets/3_solution_1.pdb")));
        pdbs.addAll(list(new File(
                "/home/tzok/pdb/puzzles/Challenge3/1/models/3_bujnicki_1.pdb")));
        Structure[] structures = new Structure[pdbs.size()];
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures[i] = StructureManager.loadStructure(pdbs.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(Arrays.deepToString(new MCQ().compare(structures,
                null)));
    }

    public static List<File> list(File directory) {
        List<File> list = new ArrayList<>();
        if (!directory.isDirectory()) {
            list.add(directory);
            return list;
        }
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
