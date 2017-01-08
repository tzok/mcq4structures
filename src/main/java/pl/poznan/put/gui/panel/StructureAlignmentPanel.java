package pl.poznan.put.gui.panel;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.gui.jmol.JmolPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.CifPdbIncompatibilityException;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StructureAlignmentPanel extends JPanel {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(StructureAlignmentPanel.class);

    // @formatter:off
    private static final String JMOL_SCRIPT =
            "frame 0.0\n" +
                    "cartoon only\n" +
                    "select model=1.1\n" +
                    "color green\n" +
                    "select model=1.2\n" +
                    "color red";
    // @formatter:on

    private final JTextPane labelHeader = new JTextPane();
    private final JLabel labelStatus = new JLabel("", SwingConstants.CENTER);
    private final JmolPanel panelJmolLeft = new JmolPanel();
    private final JmolPanel panelJmolRight = new JmolPanel();

    private Pair<PdbModel, PdbModel> structures;
    private Pair<List<PdbChain>, List<PdbChain>> chains;

    public StructureAlignmentPanel() {
        super(new BorderLayout());

        labelHeader.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelHeader.setContentType("text/html");
        labelHeader.setEditable(false);
        labelHeader.setFont(UIManager.getFont("Label.font"));
        labelHeader.setOpaque(false);

        panelJmolLeft.executeCmd("background lightgrey; save state state_init");
        panelJmolRight.executeCmd("background darkgray; save state state_init");

        JPanel panelInfo = new JPanel(new GridLayout(1, 3));
        panelInfo.add(new JLabel("Whole structures (Jmol view)",
                                 SwingConstants.CENTER));
        panelInfo.add(labelStatus);
        panelInfo.add(new JLabel("Aligned fragments (Jmol view)",
                                 SwingConstants.CENTER));

        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.add(labelHeader, BorderLayout.NORTH);
        panelMain.add(panelInfo, BorderLayout.CENTER);

        JPanel panelJmols = new JPanel(new GridLayout(1, 2));
        panelJmols.add(panelJmolLeft);
        panelJmols.add(panelJmolRight);

        add(panelMain, BorderLayout.NORTH);
        add(panelJmols, BorderLayout.CENTER);
    }

    public void setStructuresAndChains(Pair<PdbModel, PdbModel> structures,
                                       Pair<List<PdbChain>, List<PdbChain>>
                                               chains) {
        this.structures = structures;
        this.chains = chains;

        panelJmolLeft.executeCmd("restore state state_init");
        panelJmolRight.executeCmd("restore state state_init");
        labelStatus.setText("Ready");
        updateHeader(false);
    }

    public void updateHeader(boolean readyResults) {
        PdbModel left = structures.getLeft();
        PdbModel right = structures.getRight();

        StringBuilder builder = new StringBuilder();
        builder.append(
                "<html>Structures selected for 3D structure alignment: <span "
                + "style=\"color: blue\">");
        builder.append(StructureManager.getName(left));
        builder.append('.');

        for (PdbChain chain : chains.getLeft()) {
            builder.append(chain.getIdentifier());
        }

        builder.append("</span>, <span style=\"color: green\">");
        builder.append(StructureManager.getName(right));
        builder.append('.');

        for (PdbChain chain : chains.getRight()) {
            builder.append(chain.getIdentifier());
        }

        builder.append("</span>");

        if (readyResults) {
            builder.append("<br>3D structure alignment results:");
        }

        builder.append("</html>");
        labelHeader.setText(builder.toString());
    }

    public ProcessingResult alignAndDisplayStructures() {
        labelStatus.setText("Computing...");

        List<MasterTorsionAngleType> torsionAngleTypes = new ArrayList<>();
        torsionAngleTypes
                .addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
        torsionAngleTypes
                .addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));

        String nameLeft = StructureManager.getName(structures.getLeft());
        String nameRight = StructureManager.getName(structures.getRight());

        StructureSelection left =
                SelectionFactory.create(nameLeft, chains.getLeft());
        StructureSelection right =
                SelectionFactory.create(nameRight, chains.getRight());

        MCQMatcher matcher = new MCQMatcher(torsionAngleTypes);
        SelectionMatch selectionMatch = matcher.matchSelections(left, right);

        if (selectionMatch.getFragmentCount() == 0) {
            JOptionPane.showMessageDialog(this,
                                          "The selected structures have no "
                                          + "matching fragments in common",
                                          "Warning",
                                          JOptionPane.WARNING_MESSAGE);
            return ProcessingResult.emptyInstance();
        }

        try {
            panelJmolLeft.openStringInline(selectionMatch.toPDB(false));
            panelJmolLeft.executeCmd(StructureAlignmentPanel.JMOL_SCRIPT);
            panelJmolRight.openStringInline(selectionMatch.toPDB(true));
            panelJmolRight.executeCmd(StructureAlignmentPanel.JMOL_SCRIPT);
            updateHeader(true);
            return new ProcessingResult(selectionMatch);
        } catch (StructureException e) {
            String message = "Failed to align structures: " + nameLeft + " and "
                             + nameRight;
            StructureAlignmentPanel.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        } catch (CifPdbIncompatibilityException e) {
            String message = "Failed to align structures: " + nameLeft + " and "
                             + nameRight;
            StructureAlignmentPanel.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error",
                                          JOptionPane.ERROR_MESSAGE);
        } finally {
            labelStatus.setText("Computation finished");
        }

        return ProcessingResult.emptyInstance();
    }
}
