package pl.poznan.put.cs.bioserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.junit.Test;

import pl.poznan.put.beans.AlignmentSequence;
import pl.poznan.put.beans.ClusteringHierarchical;
import pl.poznan.put.beans.ClusteringPartitional;
import pl.poznan.put.beans.ComparisonGlobal;
import pl.poznan.put.beans.ComparisonLocal;
import pl.poznan.put.beans.ComparisonLocalMulti;
import pl.poznan.put.clustering.ClustererHierarchical.Linkage;
import pl.poznan.put.clustering.ClustererKMedoids;
import pl.poznan.put.comparison.ParallelGlobalComparison;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.external.XSLT;
import pl.poznan.put.helper.XMLSerializable;
import pl.poznan.put.torsion.AngleAverageAll;
import pl.poznan.put.torsion.AnglePseudophasePucker;
import pl.poznan.put.torsion.AngleType;
import pl.poznan.put.utility.InvalidInputException;
import pl.poznan.put.utility.StructureManager;

public class TestXmlSerializable {
    private static final File TMPDIR = new File(
            System.getProperty("java.io.tmpdir"));

    @SuppressWarnings("static-method")
    @Test
    public void testJaxbContext() throws Exception {
        JAXBContext.newInstance(AlignmentSequence.class);
        JAXBContext.newInstance(ClusteringHierarchical.class);
        JAXBContext.newInstance(ClusteringPartitional.class);
        JAXBContext.newInstance(ComparisonGlobal.class);
        JAXBContext.newInstance(ComparisonLocal.class);
        JAXBContext.newInstance(ComparisonLocalMulti.class);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testAlignmentSequence() throws FileNotFoundException,
            IOException, TransformerException, JAXBException,
            ParserConfigurationException {
        List<Chain> chains = new ArrayList<>();
        chains.add(StructureManager.loadStructure("1EHZ").get(0).getChain(0));
        chains.add(StructureManager.loadStructure("1EVV").get(0).getChain(0));
        boolean isGlobal = true;

        XMLSerializable xmlSerializable =
                AlignmentSequence.newInstance(chains, isGlobal);
        TestXmlSerializable.storeXml(xmlSerializable);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testClusteringHierarchical() throws FileNotFoundException,
            IOException, TransformerException, JAXBException,
            ParserConfigurationException {
        List<Structure> structures = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (String pdbid : new String[] { "1EHZ", "1EVV" }) {
            List<Structure> list = StructureManager.loadStructure(pdbid);
            structures.addAll(list);
        }

        for (Structure s : structures) {
            labels.add(StructureManager.getName(s));
        }

        ParallelGlobalComparison method = new MCQ();
        double[][] distanceMatrix = method.match(structures, null);
        ComparisonGlobal comparison =
                ComparisonGlobal.newInstance(distanceMatrix, labels, method);

        XMLSerializable xmlSerializable =
                ClusteringHierarchical.newInstance(comparison, Linkage.Average);
        TestXmlSerializable.storeXml(xmlSerializable);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testClusteringPartitional() throws InvalidInputException,
            FileNotFoundException, IOException, TransformerException,
            JAXBException, ParserConfigurationException {
        List<Structure> structures = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (String pdbid : new String[] { "1EHZ", "1EVV", "1I9V", "1ZO3" }) {
            List<Structure> list = StructureManager.loadStructure(pdbid);
            structures.addAll(list);
        }

        for (Structure s : structures) {
            labels.add(StructureManager.getName(s));
        }

        ParallelGlobalComparison method = new MCQ();
        double[][] distanceMatrix = method.match(structures, null);
        ComparisonGlobal comparison =
                ComparisonGlobal.newInstance(distanceMatrix, labels, method);

        XMLSerializable xmlSerializable =
                ClusteringPartitional.newInstance(comparison,
                        ClustererKMedoids.PAM, 2);
        TestXmlSerializable.storeXml(xmlSerializable);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testComparisonGlobal() throws FileNotFoundException,
            IOException, TransformerException, JAXBException,
            ParserConfigurationException {
        List<Structure> structures = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (String pdbid : new String[] { "1EHZ", "1EVV" }) {
            List<Structure> list = StructureManager.loadStructure(pdbid);
            structures.addAll(list);
        }

        for (Structure s : structures) {
            labels.add(StructureManager.getName(s));
        }

        ParallelGlobalComparison method = new MCQ();
        double[][] distanceMatrix = method.match(structures, null);

        XMLSerializable xmlSerializable =
                ComparisonGlobal.newInstance(distanceMatrix, labels, method);
        TestXmlSerializable.storeXml(xmlSerializable);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testComparisonLocal() throws StructureException,
            FileNotFoundException, IOException, TransformerException,
            JAXBException, ParserConfigurationException {
        Chain c1 = StructureManager.loadStructure("1EHZ").get(0).getChain(0);
        Chain c2 = StructureManager.loadStructure("1EVV").get(0).getChain(0);

        List<AngleType> usedAngles = new ArrayList<>(MCQ.USED_ANGLES);
        usedAngles.add(AngleAverageAll.getInstance());
        usedAngles.add(AnglePseudophasePucker.getInstance());

        XMLSerializable xmlSerializable =
                ComparisonLocal.newInstance(c1, c2, usedAngles);
        TestXmlSerializable.storeXml(xmlSerializable);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testComparisonLocalMulti() throws StructureException,
            InvalidInputException, FileNotFoundException, IOException,
            TransformerException, JAXBException, ParserConfigurationException {
        List<Chain> chains = new ArrayList<>();
        chains.add(StructureManager.loadStructure("1EHZ").get(0).getChain(0));
        chains.add(StructureManager.loadStructure("1EVV").get(0).getChain(0));
        chains.add(StructureManager.loadStructure("1I9V").get(0).getChain(0));

        XMLSerializable xmlSerializable =
                ComparisonLocalMulti.newInstance(chains, chains.get(0),
                        AngleAverageAll.getInstance());
        TestXmlSerializable.storeXml(xmlSerializable);
    }

    public static void storeXml(XMLSerializable xmlSerializable)
            throws IOException, TransformerException, JAXBException,
            ParserConfigurationException, FileNotFoundException {
        String className = xmlSerializable.getClass().getSimpleName();
        File xml = new File(TestXmlSerializable.TMPDIR, className + ".xml");
        try (OutputStream stream = new FileOutputStream(xml)) {
            XSLT.printDocument(xmlSerializable.toXML(), stream);
        }
    }
}
