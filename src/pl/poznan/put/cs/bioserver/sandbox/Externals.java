package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.gui.TableModelLocal;
import pl.poznan.put.cs.bioserver.helper.StructureManager;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

public class Externals {
    public static void main(String[] args) throws ParserConfigurationException,
            IOException, StructureException {
        List<File> pdbs = Externals.list(new File("/home/tzok/pdb/puzzles/"));
        Structure[] structures = new Structure[pdbs.size()];
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures[i] = StructureManager.loadStructure(pdbs.get(i))[0];
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] angles = new String[] { "AVERAGE" };
        Map<String, List<AngleDifference>> localResult = TorsionLocalComparison
                .compare(structures[0], structures[1], false);
        TableModelLocal model = new TableModelLocal(localResult, angles, "");

        double[][] differences = new double[angles.length][];
        int counter = 0;
        for (String angle : angles) {
            List<AngleDifference> list = localResult.get(angle);
            double[] values = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                values[i] = list.get(i).getDifference();
            }
            differences[counter] = values;
            counter++;
        }
        Matplotlib.local(new File("/tmp/local.png"), model.getRowsNames(),
                differences, angles);

        // double[][] result = new MCQ().compare(structures, null);
        // String[] labels = new String[pdbs.size()];
        // for (int i = 0; i < structures.length; i++) {
        // labels[i] = StructureManager.getName(structures[i]);
        // }
        //
        // Matplotlib.hierarchicalClustering(new File("/tmp/clust.png"), result,
        // labels, Method.COMPLETE);
        //
        // Result clustering = Clusterer.clusterPAM(result, 3);
        // Map<Integer, Set<Integer>> assignment =
        // Clusterer.getClusterAssignment(
        // clustering.medoids, result);
        // double[][] mds = MDS.multidimensionalScaling(result, 2);
        //
        // int size = assignment.keySet().size();
        // double[][] x = new double[size][];
        // double[][] y = new double[size][];
        // double[] mx = new double[size];
        // double[] my = new double[size];
        // String[] clusterNames = new String[size];
        // Iterator<Integer> iterator = assignment.keySet().iterator();
        // for (int i = 0; i < size; i++) {
        // Integer index = iterator.next();
        // mx[i] = mds[index][0];
        // my[i] = mds[index][1];
        //
        // Set<Integer> objects = assignment.get(index);
        // x[i] = new double[objects.size()];
        // y[i] = new double[objects.size()];
        // Iterator<Integer> iteratorObjects = objects.iterator();
        //
        // StringBuilder builder = new StringBuilder();
        // for (int j = 0; j < objects.size(); j++) {
        // int k = iteratorObjects.next();
        // x[i][j] = mds[k][0];
        // y[i][j] = mds[k][1];
        // builder.append(labels[k]);
        // if (j != objects.size() - 1) {
        // builder.append(", ");
        // }
        // }
        // clusterNames[i] = builder.toString();
        // }
        //
        // Matplotlib.partitionalClustering(new File("/tmp/medoids.png"), x, y,
        // mx, my, clusterNames);
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
