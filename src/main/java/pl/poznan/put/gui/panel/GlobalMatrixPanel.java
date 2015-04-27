package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.util.Collections;
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

import pl.poznan.put.comparison.ParallelGlobalComparison.ComparisonListener;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

public class GlobalMatrixPanel extends JPanel implements ComparisonListener {
    private final JTextPane labelInfoMatrix = new JTextPane();
    private final JTable tableMatrix = new JTable();
    private final JProgressBar progressBar = new JProgressBar();

    private List<PdbModel> structures = Collections.emptyList();

    public GlobalMatrixPanel() {
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

    public void setStructures(List<PdbModel> structures) {
        this.structures = structures;
        updateHeader();
    }

    @Override
    public void stateChanged(long all, long completed) {
        progressBar.setMaximum((int) all);
        progressBar.setValue((int) completed);
    }

    public void updateHeader() {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (PdbModel s : structures) {
            assert s != null;
            builder.append("<span style=\"color: " + (i % 2 == 0 ? "blue" : "green") + "\">");
            builder.append(StructureManager.getName(s));
            builder.append("</span>, ");
            i++;
        }

        builder.delete(builder.length() - 2, builder.length());
        labelInfoMatrix.setText("<html>Structures selected for global distance measure: " + builder.toString() + "</html>");
    }
}
