package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.external.Matplotlib.Method;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

public class Externals {
    public static void main(String[] args) throws ParserConfigurationException,
            IOException {
        List<File> pdbs = Externals.list(new File("/home/tzok/pdb/puzzles/"));
        Structure[] structures = new Structure[pdbs.size()];
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures[i] = StructureManager.loadStructure(pdbs.get(i))[0];
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        double[][] result = new MCQ().compare(structures, null);
        String[] labels = new String[pdbs.size()];
        int i = 0;
        for (Structure structure : structures) {
            labels[i++] = StructureManager.getName(structure);
        }

        Matplotlib.hierarchicalClustering(new File("/tmp/clust.pdf"), result,
                labels, Method.COMPLETE);
    }

    public static List<File> list(File directory) {
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
}
