package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.component.ChartComponent;
import pl.poznan.put.gui.component.SecondaryStructureComponent;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.visualisation.SecondaryStructureVisualizer;

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
                component.setBackground(Colors.DISTINCT_COLORS[column]);
            }
            return component;
        }
    };

    private final JTextPane labelInfoMatrix = new JTextPane();
    private final JTable tableMatrix = new JTable();
    private final JScrollPane scrollPane = new JScrollPane(tableMatrix);
    private final JTabbedPane tabbedPane = new JTabbedPane();

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

        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

        tabbedPane.add("Results", scrollPane);

        add(panelInfo, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setStructuresAndChains(Pair<PdbModel, PdbModel> structures,
            Pair<List<PdbChain>, List<PdbChain>> chains) {
        this.structures = structures;
        this.chains = chains;
        removeAllButFirstTab();
        tableMatrix.setModel(new DefaultTableModel());
        updateHeader(false);
    }

    private void removeAllButFirstTab() {
        while (tabbedPane.getComponentCount() > 1) {
            tabbedPane.remove(1);
        }
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
        try {
            StructureSelection selectionL = SelectionFactory.create(StructureManager.getName(structures.getLeft()), chains.getLeft());
            StructureSelection selectionR = SelectionFactory.create(StructureManager.getName(structures.getRight()), chains.getRight());
            MCQ mcq = new MCQ(selectedAngles);
            MCQLocalResult result = (MCQLocalResult) mcq.comparePair(selectionL, selectionR);
            SelectionMatch selectionMatch = result.getSelectionMatch();
            removeAllButFirstTab();

            List<SVGDocument> visualizations = new ArrayList<>();

            for (FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
                SVGDocument svgDocument = fragmentMatch.visualize(1024, 576);
                String title = fragmentMatch.toString();
                ChartComponent component = new ChartComponent(svgDocument);
                tabbedPane.add(title, component);
                visualizations.add(svgDocument);

                if (fragmentMatch.getTargetFragment().getMoleculeType() == MoleculeType.RNA) {
                    svgDocument = SecondaryStructureVisualizer.visualize(fragmentMatch);
                    title += " (secondary structure)";
                    tabbedPane.add(title, new SecondaryStructureComponent(svgDocument));
                    visualizations.add(svgDocument);
                }
            }

            tableMatrix.setModel(result.asDisplayableTableModel());
            updateHeader(true);

            return new ProcessingResult(result, visualizations);
        } catch (IncomparableStructuresException e) {
            String message = "Failed to compare structures";
            LocalMatrixPanel.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }

        return ProcessingResult.emptyInstance();
    }
}
