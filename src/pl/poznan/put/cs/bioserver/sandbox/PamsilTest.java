package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;

import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.clustering.Clusterer;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.helper.PdbManager;

public class PamsilTest {
    public static void main(String[] args) {
        String[] paths = new String[] {
                "/home/tzok/pdb/puzzles/Challenge1/models/1_bujnicki_1.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_bujnicki_2.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_bujnicki_3.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_bujnicki_4.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_bujnicki_5.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_chen_1.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_das_1.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_das_2.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_das_3.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_das_4.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_das_5.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_dokholyan_1.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_major_1.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/models/1_santalucia_1.pdb",
                "/home/tzok/pdb/puzzles/Challenge1/targets/1_solution_0.pdb" };
        Structure[] structures = new Structure[paths.length];
        for (int i = 0; i < paths.length; i++) {
            structures[i] = PdbManager.loadStructure(new File(paths[i]));
        }

        MCQ mcq = new MCQ();
        double[][] matrix = mcq.compare(structures, null);
        Clusterer.clusterPAMSIL(matrix);
    }
}
