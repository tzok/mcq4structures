package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.matching.AngleDeltaIteratorFactory;
import pl.poznan.put.matching.TypedDeltaIteratorFactory;
import pl.poznan.put.matching.stats.MultiMatchStatistics;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.svg.SVGHelper;

public class LocalMultiMatrixPanel extends JPanel {
    private class PdbCompactFragmentWrapper {
        private final PdbCompactFragment fragment;

        public PdbCompactFragmentWrapper(PdbCompactFragment fragment) {
            super();
            this.fragment = fragment;
        }

        public PdbCompactFragment getFragment() {
            return fragment;
        }

        @Override
        public String toString() {
            return fragment.getName();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMultiMatrixPanel.class);

    private final JTextPane labelInfoMatrix = new JTextPane();
    private final JTable tableMatrix = new JTable();
    private final JTable histogramMatrix = new JTable();
    private final JTable percentileMatrix = new JTable();
    private final SVGComponent visualization = new SVGComponent(SVGHelper.emptyDocument(), "colorbar");

    private List<PdbCompactFragment> fragments = Collections.emptyList();

    public LocalMultiMatrixPanel() {
        super(new BorderLayout());

        labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoMatrix.setContentType("text/html");
        labelInfoMatrix.setEditable(false);
        labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
        labelInfoMatrix.setOpaque(false);

        histogramMatrix.setAutoCreateRowSorter(true);
        percentileMatrix.setAutoCreateRowSorter(true);

        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Results", new JScrollPane(tableMatrix));
        tabbedPane.add("Histograms", new JScrollPane(histogramMatrix));
        tabbedPane.add("Percentiles", new JScrollPane(percentileMatrix));
        tabbedPane.add("Visualization", new JScrollPane(visualization));

        add(panelInfo, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setFragments(List<PdbCompactFragment> fragments) {
        this.fragments = fragments;
        DefaultTableModel emptyDataModel = new DefaultTableModel();
        tableMatrix.setModel(emptyDataModel);
        histogramMatrix.setModel(emptyDataModel);
        percentileMatrix.setModel(emptyDataModel);
        visualization.setSVGDocument(SVGHelper.emptyDocument());
        updateHeader(false);
    }

    public void updateHeader(boolean readyResults) {
        StringBuilder builder = new StringBuilder("<html>Structures selected for local distance measure: ");
        int i = 0;

        for (PdbCompactFragment c : fragments) {
            builder.append("<span style=\"color: " + (i % 2 == 0 ? "blue" : "green") + "\">");
            builder.append(c.getName());
            builder.append("</span>, ");
            i++;
        }

        builder.delete(builder.length() - 2, builder.length());

        if (readyResults) {
            builder.append("<br>Local distance vector(s):");
        }

        builder.append("</html>");
        labelInfoMatrix.setText(builder.toString());
    }

    public ProcessingResult compareAndDisplayTable() {
        try {
            PdbCompactFragment reference = selectReferenceStructure();
            if (reference == null) {
                return ProcessingResult.emptyInstance();
            }

            MasterTorsionAngleType selectedAngleType = selectReferenceTorsionAngleType(reference);
            if (selectedAngleType == null) {
                return ProcessingResult.emptyInstance();
            }

            MCQ mcq = new MCQ(Collections.singletonList(selectedAngleType));
            ModelsComparisonResult result = mcq.compareModels(reference, fragments);
            ModelsComparisonResult.SelectedAngle selectedAngle = result.selectAngle(selectedAngleType);
            AngleDeltaIteratorFactory iteratorFactory = new TypedDeltaIteratorFactory(selectedAngleType);
            MultiMatchStatistics statistics = MultiMatchStatistics.calculate(iteratorFactory, selectedAngle);
            SVGDocument document = selectedAngle.visualize();

            tableMatrix.setModel(selectedAngle.asDisplayableTableModel());
            histogramMatrix.setModel(statistics.histogramsAsTableModel(true));
            percentileMatrix.setModel(statistics.percentilesAsTableModel(true));
            visualization.setSVGDocument(document);
            updateHeader(true);

            return new ProcessingResult(selectedAngle);
        } catch (IncomparableStructuresException e) {
            String message = "Failed to compare structures";
            LocalMultiMatrixPanel.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }

        return ProcessingResult.emptyInstance();
    }

    private MasterTorsionAngleType selectReferenceTorsionAngleType(
            PdbCompactFragment reference) {
        MoleculeType moleculeType = reference.getMoleculeType();
        MasterTorsionAngleType[] mainAngles;
        AverageTorsionAngleType averageTorsionAngleType;

        switch (moleculeType) {
        case PROTEIN:
            mainAngles = ProteinTorsionAngleType.mainAngles();
            averageTorsionAngleType = ProteinTorsionAngleType.getAverageOverMainAngles();
            break;
        case RNA:
            mainAngles = RNATorsionAngleType.mainAngles();
            averageTorsionAngleType = RNATorsionAngleType.getAverageOverMainAngles();
            break;
        case UNKNOWN:
        default:
            throw new IllegalArgumentException("Unknown molecule type: " + moleculeType);
        }

        MasterTorsionAngleType[] anglesToSelectFrom = Arrays.copyOf(mainAngles, mainAngles.length + 1);
        anglesToSelectFrom[mainAngles.length] = averageTorsionAngleType;
        return (MasterTorsionAngleType) JOptionPane.showInputDialog(this, "Select torsion angle", "Torsion angle", JOptionPane.INFORMATION_MESSAGE, null, anglesToSelectFrom, averageTorsionAngleType);
    }

    private PdbCompactFragment selectReferenceStructure() {
        PdbCompactFragmentWrapper[] fragmentArray = new PdbCompactFragmentWrapper[fragments.size()];
        for (int i = 0; i < fragments.size(); i++) {
            fragmentArray[i] = new PdbCompactFragmentWrapper(fragments.get(i));
        }
        PdbCompactFragmentWrapper wrapper = (PdbCompactFragmentWrapper) JOptionPane.showInputDialog(this, "Select your reference structure", "Reference structure", JOptionPane.INFORMATION_MESSAGE, null, fragmentArray, fragmentArray[0]);
        return wrapper.getFragment();
    }
}
