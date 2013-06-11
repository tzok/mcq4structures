package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.beans.ClusteringHierarchical;
import pl.poznan.put.cs.bioserver.beans.ClusteringPartitional;
import pl.poznan.put.cs.bioserver.beans.ComparisonGlobal;
import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.ComparisonLocalMulti;
import pl.poznan.put.cs.bioserver.beans.XMLSerializable;
import pl.poznan.put.cs.bioserver.clustering.ClustererHierarchical.Linkage;
import pl.poznan.put.cs.bioserver.clustering.ClustererKMedoids;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.external.XSLT;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

public class Externals {
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

    public static void main(String[] args) throws ParserConfigurationException, IOException,
            StructureException, JAXBException, TransformerException, InvalidInputException {
        List<File> pdbs = Externals.list(new File("/home/tzok/pdb/puzzles/Challenge2/"));
        List<Structure> structures = new ArrayList<>();
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures.addAll(StructureManager.loadStructure(pdbs.get(i)));
            } catch (IOException | InvalidInputException e) {
                e.printStackTrace();
            }
        }

        List<Chain> list = new ArrayList<>();
        for (Structure s : structures) {
            list.add(s.getChain(0));
        }
        List<String> listNames = new ArrayList<>(MCQ.USED_ANGLES_NAMES);
        listNames.add("AVERAGE");
        XMLSerializable xmlResults;
        try {
            xmlResults = ComparisonLocalMulti.newInstance(list, list.get(0), listNames);
            try (OutputStream stream = new FileOutputStream("/tmp/multi.xml")) {
                XSLT.printDocument(xmlResults.toXML(), stream);
            }
            Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                    + "put/cs/bioserver/external/MatplotlibLocalMulti.xsl"), new File(
                    "/tmp/multi.py"), new File("/tmp/multi.pdf"), xmlResults, null);
        } catch (InvalidInputException e) {
            e.printStackTrace();
        }

        xmlResults = ComparisonLocal.newInstance(structures.get(0).getChain(0), structures.get(1)
                .getChain(0), MCQ.USED_ANGLES_NAMES);
        // XSLT.printDocument(xmlResults.toXML(), System.out);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("angles", "[ 'ALPHA', 'BETA', 'GAMMA', 'DELTA', "
                + "'EPSILON', 'ZETA', 'CHI', 'TAU0', 'TAU1', 'TAU2', 'TAU3', " + "'TAU4' ]");
        Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                + "put/cs/bioserver/external/MatplotlibLocal.xsl"), new File("/tmp/local.py"),
                new File("/tmp/local.pdf"), xmlResults, parameters);

        double[][] matrix = new MCQ().compare(structures, null);
        List<String> labels = new ArrayList<>();
        for (Structure s : structures) {
            labels.add(StructureManager.getName(s));
        }
        ComparisonGlobal global = ComparisonGlobal.newInstance(matrix, labels, "MCQ");

        xmlResults = ClusteringHierarchical.newInstance(global, Linkage.Complete);
        // XSLT.printDocument(xmlResults.toXML(), System.out);

        Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                + "put/cs/bioserver/external/MatplotlibHierarchical.xsl"), new File(
                "/tmp/hierarchical.py"), new File("/tmp/hierarchical.pdf"), xmlResults);

        xmlResults = ClusteringPartitional.newInstance(global, ClustererKMedoids.PAM, null);
        // XSLT.printDocument(xmlResults.toXML(), System.out);

        Matplotlib.runXsltAndPython(Externals.class.getResource("/pl/poznan/"
                + "put/cs/bioserver/external/MatplotlibPartitional.xsl"), new File(
                "/tmp/partitional.py"), new File("/tmp/partitional.pdf"), xmlResults);
    }
}
