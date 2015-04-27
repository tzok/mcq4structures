package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.tuple.Pair;

import pl.poznan.put.comparison.ParallelGlobalComparison.ComparisonListener;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

public class LocalMatrixPanel extends JPanel implements ComparisonListener {
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
        progressBar.setStringPainted(true);

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
        updateHeader();
    }

    @Override
    public void stateChanged(long all, long completed) {
        progressBar.setMaximum((int) all);
        progressBar.setValue((int) completed);
    }

    public void updateHeader() {
        PdbModel left = structures.getLeft();
        PdbModel right = structures.getRight();

        StringBuilder builder = new StringBuilder();
        builder.append("<span style=\"color: blue\">");
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
        labelInfoMatrix.setText("<html>Structures selected for local distance measure: " + builder.toString() + "</html>");
    }
}
