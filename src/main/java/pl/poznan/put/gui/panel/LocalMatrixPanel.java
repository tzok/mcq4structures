package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.MCQLocalResult;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.gui.ProcessingResult;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;

public class LocalMatrixPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMatrixPanel.class);

    private final TableCellRenderer colorsRenderer = new DefaultTableCellRenderer() {
        private final TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 0) {
                component.setBackground(Color.WHITE);
                component.setForeground(Color.BLACK);
            } else {
                component.setBackground(Colors.COLORS[column - 1]);
            }
            return component;
        }
    };

    private final JTextPane labelInfoMatrix = new JTextPane();
    private final JTable tableMatrix = new JTable();
    private final JProgressBar progressBar = new JProgressBar();

    private Pair<PdbModel, PdbModel> structures;
    private Pair<List<PdbChain>, List<PdbChain>> chains;

    public LocalMatrixPanel() {
        super(new BorderLayout());

        labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoMatrix.setContentType("text/html");
        labelInfoMatrix.setEditable(false);
        labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
        labelInfoMatrix.setOpaque(false);

        tableMatrix.setDefaultRenderer(Object.class, colorsRenderer);

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

    public void setStructuresAndChains(Pair<PdbModel, PdbModel> structures,
            Pair<List<PdbChain>, List<PdbChain>> chains) {
        this.structures = structures;
        this.chains = chains;
        updateHeader(false);
    }

    public void updateHeader(boolean readyResults) {
        PdbModel left = structures.getLeft();
        PdbModel right = structures.getRight();

        StringBuilder builder = new StringBuilder();
        builder.append("<html>Structures selected for local distance measure: <span style=\"color: blue\">");
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
            builder.append("<br>Local distance vector(s):");
        }

        builder.append("</html>");
        labelInfoMatrix.setText(builder.toString());
    }

    public ProcessingResult compareAndDisplayTable(
            List<MasterTorsionAngleType> selectedAngles) {
        progressBar.setValue(0);

        try {
            StructureSelection selectionL = SelectionFactory.create(StructureManager.getName(structures.getLeft()), chains.getLeft());
            StructureSelection selectionR = SelectionFactory.create(StructureManager.getName(structures.getRight()), chains.getRight());
            MCQ mcq = new MCQ(selectedAngles);
            MCQLocalResult result = (MCQLocalResult) mcq.comparePair(selectionL, selectionR);

            tableMatrix.setModel(result.asDisplayableTableModel());
            updateHeader(true);

            return new ProcessingResult(result);
        } catch (IncomparableStructuresException e) {
            String message = "Failed to compare structures";
            LocalMatrixPanel.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            progressBar.setValue(1);
        }

        return ProcessingResult.emptyInstance();
    }
}
