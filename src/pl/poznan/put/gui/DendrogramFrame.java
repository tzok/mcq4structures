package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.batik.swing.JSVGCanvas;
import org.biojava.bio.structure.Structure;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.clustering.hierarchical.HierarchicalClusterer;
import pl.poznan.put.clustering.hierarchical.HierarchicalClusteringResult;
import pl.poznan.put.clustering.hierarchical.Linkage;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.ParallelGlobalComparison;
import pl.poznan.put.structure.SelectionFactory;
import pl.poznan.put.structure.StructureSelection;
import pl.poznan.put.utility.StructureManager;

public class DendrogramFrame extends JFrame {
    public DendrogramFrame(HierarchicalClusteringResult clustering,
            String[] names) throws HeadlessException {
        super("Dendrogram");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        SVGDocument svgDocument = clustering.toSVG(names, false);
        JSVGCanvas canvas = new JSVGCanvas();
        getContentPane().add(canvas, BorderLayout.CENTER);
        canvas.setSVGDocument(svgDocument);

        Element rootElement = svgDocument.getDocumentElement();
        int width = (int) Math.ceil(Double.parseDouble(rootElement.getAttribute("width")));
        int height = (int) Math.ceil(Double.parseDouble(rootElement.getAttribute("height")));
        canvas.setPreferredSize(new Dimension(width, height));

        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = screenSize.width / 2 - width / 2;
        int y = screenSize.height / 2 - height / 2;
        setLocation(x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                List<StructureSelection> selections = new ArrayList<>();

                for (Structure s : StructureManager.loadStructure("1EHZ")) {
                    StructureSelection selection = SelectionFactory.create(
                            "1EHZ", s);
                    selections.add(selection);
                }
                for (Structure s : StructureManager.loadStructure("1EVV")) {
                    StructureSelection selection = SelectionFactory.create(
                            "1EVV", s);
                    selections.add(selection);
                }
                for (Structure s : StructureManager.loadStructure("6TNA")) {
                    StructureSelection selection = SelectionFactory.create(
                            "6TNA", s);
                    selections.add(selection);
                }

                MCQ mcq = new MCQ(MCQ.getAllAvailableTorsionAngles());
                GlobalComparisonResultMatrix resultMatrix = ParallelGlobalComparison.run(
                        mcq, selections, null);
                HierarchicalClusteringResult clustering = HierarchicalClusterer.cluster(
                        resultMatrix.getDistanceMatrix().getArray(),
                        Linkage.COMPLETE);

                String[] names = new String[] { "1EHZ", "1EVV", "6TNA" };

                DendrogramFrame frame = new DendrogramFrame(clustering, names);
                frame.setVisible(true);
            }
        });
    }
}
