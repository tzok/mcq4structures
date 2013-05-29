package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.beans.ClusteringHierarchical;
import pl.poznan.put.cs.bioserver.beans.ClusteringPartitional;
import pl.poznan.put.cs.bioserver.beans.ComparisonGlobal;
import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.XMLSerializable;
import pl.poznan.put.cs.bioserver.clustering.Clusterer;
import pl.poznan.put.cs.bioserver.clustering.Clusterer.Result;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.external.Matplotlib.Method;
import pl.poznan.put.cs.bioserver.helper.StructureManager;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

public class Externals {
    public static void main(String[] args) throws ParserConfigurationException,
            IOException, StructureException, JAXBException {
        List<File> pdbs = Externals.list(new File("/home/tzok/pdb/puzzles/"));
        Structure[] structures = new Structure[pdbs.size()];
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures[i] = StructureManager.loadStructure(pdbs.get(i))[0];
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, List<AngleDifference>> results = TorsionLocalComparison
                .compare(structures[0], structures[1], false);
        String[] angleNames = new String[MCQ.USED_ANGLES.length];
        for (int i = 0; i < MCQ.USED_ANGLES.length; i++) {
            angleNames[i] = MCQ.USED_ANGLES[i].getAngleName();
        }
        XMLSerializable xmlResults = ComparisonLocal.newInstance(results,
                new Structure[] { structures[0], structures[1] }, angleNames);
        // XSLT.printDocument(xmlResults.toXML(), System.out);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("angles", "[ 'ALPHA', 'BETA', 'GAMMA', 'DELTA', "
                + "'EPSILON', 'ZETA', 'CHI', 'TAU0', 'TAU1', 'TAU2', 'TAU3', "
                + "'TAU4', 'P', 'AVERAGE' ]");
        parameters.put("angles", "[ 'AVERAGE' ]");
        Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                + "put/cs/bioserver/external/MatplotlibLocal.xsl"), new File(
                "/tmp/local.py"), new File("/tmp/local.pdf"), xmlResults,
                parameters);

        double[][] matrix = new MCQ().compare(structures, null);
        String[] labels = new String[pdbs.size()];
        for (int i = 0; i < structures.length; i++) {
            labels[i] = StructureManager.getName(structures[i]);
        }
        ComparisonGlobal global = ComparisonGlobal.newInstance(matrix, labels,
                "MCQ");

        xmlResults = ClusteringHierarchical
                .newInstance(global, Method.COMPLETE);
        // XSLT.printDocument(xmlResults.toXML(), System.out);

        Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                + "put/cs/bioserver/external/MatplotlibHierarchical.xsl"),
                new File("/tmp/hierarchical.py"), new File(
                        "/tmp/hierarchical.pdf"), xmlResults);

        Result clustering = Clusterer.clusterPAM(matrix, 3);
        xmlResults = ClusteringPartitional.newInstance(global, clustering);
        // XSLT.printDocument(xmlResults.toXML(), System.out);

        Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                + "put/cs/bioserver/external/MatplotlibPartitional.xsl"),
                new File("/tmp/partitional.py"), new File(
                        "/tmp/partitional.pdf"), xmlResults);
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
