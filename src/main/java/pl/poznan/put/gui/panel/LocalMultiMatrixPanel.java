package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.comparison.ModelsComparisonResult.SelectedAngle;
import pl.poznan.put.gui.ProcessingResult;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.type.AverageTorsionAngleType;
import pl.poznan.put.torsion.type.MasterTorsionAngleType;

public class LocalMultiMatrixPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMultiMatrixPanel.class);

    private final JTextPane labelInfoMatrix = new JTextPane();
    private final JTable tableMatrix = new JTable();
    private final JProgressBar progressBar = new JProgressBar();

    private List<PdbCompactFragment> fragments = Collections.emptyList();

    public LocalMultiMatrixPanel() {
        super(new BorderLayout());

        labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoMatrix.setContentType("text/html");
        labelInfoMatrix.setEditable(false);
        labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
        labelInfoMatrix.setOpaque(false);

        progressBar.setStringPainted(true);
        progressBar.setMaximum(1);

        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

        JPanel panelProgressBar = new JPanel();
        panelProgressBar.setLayout(new BoxLayout(panelProgressBar, BoxLayout.X_AXIS));
        panelProgressBar.add(new JLabel("Progress in computing:"));
        panelProgressBar.add(progressBar);

        add(panelInfo, BorderLayout.NORTH);
        add(new JScrollPane(tableMatrix), BorderLayout.CENTER);
        add(panelProgressBar, BorderLayout.SOUTH);
    }

    public void setFragments(List<PdbCompactFragment> fragments) {
        this.fragments = fragments;
        updateHeader(false);
    }

    public void updateHeader(boolean readyResults) {
        StringBuilder builder = new StringBuilder("<html>Structures selected for local distance measure: ");
        int i = 0;

        for (PdbCompactFragment c : fragments) {
            builder.append("<span style=\"color: " + (i % 2 == 0 ? "blue" : "green") + "\">");
            builder.append(c.toString());
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
        progressBar.setValue(0);

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
            SelectedAngle selectedAngle = result.selectAngle(selectedAngleType);

            tableMatrix.setModel(selectedAngle.asDisplayableTableModel());
            updateHeader(true);

            return new ProcessingResult(selectedAngle);
        } catch (IncomparableStructuresException e) {
            String message = "Failed to compare structures";
            LocalMultiMatrixPanel.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            progressBar.setValue(1);
        }

        return ProcessingResult.emptyInstance();
    }

    private MasterTorsionAngleType selectReferenceTorsionAngleType(
            PdbCompactFragment reference) {
        MoleculeType moleculeType = reference.getMoleculeType();
        MasterTorsionAngleType[] mainAngles;

        switch (moleculeType) {
        case PROTEIN:
            mainAngles = ProteinTorsionAngleType.mainAngles();
            break;
        case RNA:
            mainAngles = RNATorsionAngleType.mainAngles();
            break;
        case UNKNOWN:
        default:
            mainAngles = new MasterTorsionAngleType[0];
            break;
        }

        AverageTorsionAngleType averageTorsionAngleType = AverageTorsionAngleType.instanceForMainAngles(moleculeType);
        MasterTorsionAngleType[] anglesToSelectFrom = Arrays.copyOf(mainAngles, mainAngles.length + 1);
        anglesToSelectFrom[mainAngles.length] = averageTorsionAngleType;
        return (MasterTorsionAngleType) JOptionPane.showInputDialog(this, "Select torsion angle", "Torsion angle", JOptionPane.INFORMATION_MESSAGE, null, anglesToSelectFrom, averageTorsionAngleType);
    }

    private PdbCompactFragment selectReferenceStructure() {
        PdbCompactFragment[] fragmentArray = fragments.toArray(new PdbCompactFragment[fragments.size()]);
        return (PdbCompactFragment) JOptionPane.showInputDialog(this, "Select your reference structure", "Reference structure", JOptionPane.INFORMATION_MESSAGE, null, fragmentArray, fragmentArray[0]);
    }
}
