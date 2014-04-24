package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import pl.poznan.put.clustering.ClustererKMedoids;
import pl.poznan.put.clustering.ClustererKMedoids.Result;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;

public class Clusterer {
    public static void main(String[] args) {
        List<Double> values;

        values = new ArrayList<>();
        try {
            LineIterator iterator = IOUtils.lineIterator(System.in, "utf-8");
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                values.add(Double.valueOf(line));
            }
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // values =
        // Arrays.asList(new Double[] { 197.1, 290.1, 183.5, 232.6, 238.6,
        // 213.2, 179.9, 228.1, 244.1, 205.3 });

        double[][] matrix = new double[values.size()][];
        for (int i = 0; i < values.size(); i++) {
            matrix[i] = new double[values.size()];
        }

        for (int i = 0; i < values.size(); i++) {
            for (int j = i + 1; j < values.size(); j++) {
                matrix[i][j] =
                        matrix[j][i] =
                                DihedralAngles.subtractDihedral(
                                        Math.toRadians(values.get(i)),
                                        Math.toRadians(values.get(j)));
            }
        }

        DecimalFormat format = new DecimalFormat("0.00");
        for (int i = 0; i < values.size(); i++) {
            for (int j = 0; j < values.size(); j++) {
                System.out.print(format.format(matrix[i][j]));
                System.out.print('\t');
            }
            System.out.println();
        }
        System.out.println();

        ClustererKMedoids clusterer = new ClustererKMedoids();
        Result kMedoids = clusterer.kMedoids(matrix, ClustererKMedoids.PAM, 4);
        Map<Integer, Set<Integer>> clusters =
                ClustererKMedoids.getClusterAssignments(kMedoids.getMedoids(),
                        matrix);

        for (Entry<Integer, Set<Integer>> entry : clusters.entrySet()) {
            StringBuilder builder = new StringBuilder();
            for (int i : new TreeSet<>(entry.getValue())) {
                builder.append(i);
                builder.append(',');
            }
            builder.deleteCharAt(builder.length() - 1);
            System.out.println(builder.toString());
        }
        System.out.println();

        // for (Entry<Integer, Set<Integer>> entry : clusters.entrySet()) {
        // StringBuilder builder = new StringBuilder();
        // for (int i : entry.getValue()) {
        // builder.append(values.get(i));
        // builder.append(',');
        // }
        // builder.deleteCharAt(builder.length() - 1);
        // System.out.println(builder.toString());
        // }
    }
}
